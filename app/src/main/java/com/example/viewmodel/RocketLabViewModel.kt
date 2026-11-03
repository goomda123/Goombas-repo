package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BurritoType
import com.example.model.FlightStage
import com.example.model.FlightTelemetry
import com.example.model.OperatingMode
import com.example.model.PressureLevel
import com.example.model.PropulsionPhysics
import com.example.model.ThrustMetrics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

data class RocketLabUiState(
  val burritoCount: Int = 5,
  val burritoType: BurritoType = BurritoType.SEVEN_LAYER_NUCLEAR,
  val internalPressurePsi: Float = 340f,
  val exhaustVelocityMs: Double = 1850.0,
  val operatingMode: OperatingMode = OperatingMode.IDLE,
  val countdownSeconds: Int = 10,
  val isCountingDown: Boolean = false,
  val isIgnited: Boolean = false,
  val isEvacuationAlarmActive: Boolean = false,
  val showNasaLostControlBanner: Boolean = false,
  val showNasaLostControlModal: Boolean = false,
  val isVenting: Boolean = false,
  val statusMessage: String = "Laboratory online. Ready for fueling."
)

class RocketLabViewModel(application: Application) : AndroidViewModel(application) {

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  private val _uiState = MutableStateFlow(RocketLabUiState())
  val uiState: StateFlow<RocketLabUiState> = _uiState.asStateFlow()

  private val _flightTelemetry = MutableStateFlow(FlightTelemetry())
  val flightTelemetry: StateFlow<FlightTelemetry> = _flightTelemetry.asStateFlow()

  // Dynamic Thrust calculation updated automatically
  val thrustMetrics: StateFlow<ThrustMetrics> = combine(
    _uiState
  ) { states ->
    val s = states[0]
    PropulsionPhysics.calculateThrust(
      burritoCount = s.burritoCount,
      burritoType = s.burritoType,
      internalPressurePsi = s.internalPressurePsi,
      baseExhaustVelocityMs = s.exhaustVelocityMs,
      mode = s.operatingMode
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = PropulsionPhysics.calculateThrust(5, BurritoType.SEVEN_LAYER_NUCLEAR, 340f, 1850.0, OperatingMode.IDLE)
  )

  private var pressureSimulationJob: Job? = null
  private var countdownJob: Job? = null
  private var flightSimulationJob: Job? = null

  init {
    startPressureTicker()
  }

  private fun startPressureTicker() {
    pressureSimulationJob?.cancel()
    pressureSimulationJob = viewModelScope.launch {
      while (isActive) {
        delay(250)
        val currentState = _uiState.value
        if (currentState.isVenting) {
          // Rapid venting down to safe level
          val newPressure = max(PropulsionPhysics.MIN_PRESSURE_PSI, currentState.internalPressurePsi - 40f)
          val ventingDone = newPressure <= 150f
          _uiState.value = currentState.copy(
            internalPressurePsi = newPressure,
            isVenting = !ventingDone,
            isEvacuationAlarmActive = if (ventingDone) false else currentState.isEvacuationAlarmActive,
            showNasaLostControlModal = if (ventingDone) false else currentState.showNasaLostControlModal
          )
        } else {
          // Natural mode-based pressure evolution
          val delta = (currentState.operatingMode.pressureRatePerSec * 0.25f)
          var newPressure = currentState.internalPressurePsi + delta

          // Cap bounds
          newPressure = min(1350f, max(PropulsionPhysics.MIN_PRESSURE_PSI, newPressure))

          val level = PropulsionPhysics.evaluatePressureLevel(newPressure)
          val nasaLostControl = level == PressureLevel.MAXIMUM_NASA_LOST_CONTROL

          val autoAlarm = level == PressureLevel.MAXIMUM_NASA_LOST_CONTROL ||
            (level == PressureLevel.METHANE_OVERLOAD && currentState.operatingMode == OperatingMode.FULL_MONGO_LAUNCH)

          if (nasaLostControl && !currentState.showNasaLostControlBanner) {
            triggerHapticBurst(500)
          }

          _uiState.value = currentState.copy(
            internalPressurePsi = newPressure,
            showNasaLostControlBanner = nasaLostControl,
            showNasaLostControlModal = if (nasaLostControl && !currentState.showNasaLostControlBanner) true else currentState.showNasaLostControlModal,
            isEvacuationAlarmActive = if (autoAlarm && !currentState.isEvacuationAlarmActive) true else currentState.isEvacuationAlarmActive
          )
        }
      }
    }
  }

  fun setBurritoCount(count: Int) {
    val clamped = count.coerceIn(1, 25)
    _uiState.value = _uiState.value.copy(
      burritoCount = clamped,
      statusMessage = "Fuel payload updated: $clamped burritos staged."
    )
  }

  fun setBurritoType(type: BurritoType) {
    _uiState.value = _uiState.value.copy(
      burritoType = type,
      statusMessage = "Propellant switched to ${type.displayName} (${type.scovilleRating} SHU)."
    )
  }

  fun setInternalPressure(psi: Float) {
    val clamped = psi.coerceIn(PropulsionPhysics.MIN_PRESSURE_PSI, 1350f)
    val level = PropulsionPhysics.evaluatePressureLevel(clamped)
    val isMax = level == PressureLevel.MAXIMUM_NASA_LOST_CONTROL

    if (isMax) {
      triggerHapticBurst(600)
    }

    _uiState.value = _uiState.value.copy(
      internalPressurePsi = clamped,
      showNasaLostControlBanner = isMax,
      showNasaLostControlModal = if (isMax) true else _uiState.value.showNasaLostControlModal
    )
  }

  fun setExhaustVelocity(velocityMs: Double) {
    val clamped = velocityMs.coerceIn(400.0, 5000.0)
    _uiState.value = _uiState.value.copy(exhaustVelocityMs = clamped)
  }

  fun setOperatingMode(mode: OperatingMode) {
    triggerHapticTick()
    val newPressure = when (mode) {
      OperatingMode.FULL_MONGO_LAUNCH -> max(_uiState.value.internalPressurePsi, 800f)
      OperatingMode.TACO_BELL_AFTERBURNER -> max(_uiState.value.internalPressurePsi, 450f)
      OperatingMode.IDLE -> _uiState.value.internalPressurePsi
    }

    val isMax = newPressure >= PropulsionPhysics.MAX_PRESSURE_PSI

    _uiState.value = _uiState.value.copy(
      operatingMode = mode,
      internalPressurePsi = newPressure,
      showNasaLostControlBanner = isMax,
      showNasaLostControlModal = if (isMax) true else _uiState.value.showNasaLostControlModal,
      statusMessage = "Operating Mode: ${mode.title}. ${mode.description}"
    )
  }

  // --- COUNTDOWN & IGNITION ---

  fun startCountdown() {
    if (_uiState.value.isCountingDown) return
    countdownJob?.cancel()

    _uiState.value = _uiState.value.copy(
      countdownSeconds = 10,
      isCountingDown = true,
      isIgnited = false,
      statusMessage = "T-minus 10 seconds to burrito ignition! Clear the launch trench!"
    )

    countdownJob = viewModelScope.launch {
      for (sec in 10 downTo 1) {
        _uiState.value = _uiState.value.copy(
          countdownSeconds = sec,
          statusMessage = "T-$sec seconds! Mullet hairspray curing..."
        )
        triggerHapticTick()
        delay(1000)
      }

      // Ignition at T-0!
      triggerHapticBurst(800)
      _uiState.value = _uiState.value.copy(
        countdownSeconds = 0,
        isCountingDown = false,
        isIgnited = true,
        statusMessage = "🚀 WE HAVE IGNITION! JOE HAS CLEARED THE LAUNCHPAD!"
      )

      // Automatically launch the Flight Simulator!
      startFlightSimulation()
    }
  }

  fun abortCountdown() {
    countdownJob?.cancel()
    _uiState.value = _uiState.value.copy(
      isCountingDown = false,
      countdownSeconds = 10,
      statusMessage = "Ignition ABORTED by Range Safety. Venting lines."
    )
    triggerHapticTick()
  }

  // --- EMERGENCY EVACUATION & VENTING ---

  fun triggerEvacuationAlarm() {
    triggerHapticBurst(700)
    _uiState.value = _uiState.value.copy(
      isEvacuationAlarmActive = true,
      statusMessage = "🚨 EVACUATION ALARM ACTIVATED! ALL PERSONNEL RETREAT TO 15-MILE RADIUS!"
    )
  }

  fun dismissEvacuationAlarm() {
    _uiState.value = _uiState.value.copy(isEvacuationAlarmActive = false)
  }

  fun triggerEmergencyVenting() {
    triggerHapticBurst(300)
    _uiState.value = _uiState.value.copy(
      isVenting = true,
      statusMessage = "Emergency vent open! Releasing high-pressure methane fumes into upper atmosphere..."
    )
  }

  fun dismissNasaModal() {
    _uiState.value = _uiState.value.copy(showNasaLostControlModal = false)
  }

  // --- FLIGHT SIMULATOR LOGIC ---

  fun startFlightSimulation() {
    flightSimulationJob?.cancel()
    val initialThrust = thrustMetrics.value

    _flightTelemetry.value = FlightTelemetry(
      altitudeMeters = 5.0,
      velocityMs = initialThrust.effectiveExhaustVelocity * 0.15,
      gForce = initialThrust.thrustToWeightRatio,
      stage = FlightStage.LIFTOFF,
      isFlying = true,
      recentMilestone = "Booster ignition: Burrito shockwave detected!"
    )

    flightSimulationJob = viewModelScope.launch {
      var alt = 5.0
      var vel = initialThrust.effectiveExhaustVelocity * 0.15
      var fuel = 100f
      var timeSec = 0.0

      while (isActive && fuel > 0f) {
        delay(80) // 12.5 physics steps per second
        timeSec += 0.08

        val currentThrust = thrustMetrics.value
        val twr = currentThrust.thrustToWeightRatio
        val acc = (twr - 1.0) * PropulsionPhysics.GRAVITY_M_S2

        vel += acc * 0.08
        alt += vel * 0.08
        fuel = max(0f, fuel - 0.35f)

        // Stage milestones based on altitude
        val stage = when {
          alt >= 350000 -> FlightStage.DEEP_SPACE_MONGO
          alt >= 280000 -> FlightStage.ISS_FLYBY
          alt >= 160000 -> FlightStage.LOW_EARTH_ORBIT
          alt >= 50000 -> FlightStage.MESOSPHERE
          alt >= 15000 -> FlightStage.STRATOSPHERE
          alt >= 9000 -> FlightStage.MAX_Q
          alt >= 2000 -> FlightStage.TRANSONIC_MULLET
          alt > 20 -> FlightStage.LIFTOFF
          else -> FlightStage.LAUNCHPAD
        }

        val milestoneText = when (stage) {
          FlightStage.DEEP_SPACE_MONGO -> "🚨 NASA HAS LOST CONTROL OF JOE! Heading to Mars!"
          FlightStage.ISS_FLYBY -> "🛰️ Buzzed ISS Cupola! Astronauts taking selfies of Joe's mullet!"
          FlightStage.LOW_EARTH_ORBIT -> "🌍 Stable Orbit achieved! Mullet floating gracefully in zero-g."
          FlightStage.MESOSPHERE -> "🔥 Extreme atmospheric friction! Sunglasses glowing red hot!"
          FlightStage.STRATOSPHERE -> "✈️ Exceeded U-2 spy plane altitude! Burrito condensation trail visible."
          FlightStage.MAX_Q -> "💨 Max-Q drag reached! Mullet hair follicles under 850 lbs shear force!"
          FlightStage.TRANSONIC_MULLET -> "💥 Mach 1 sonic boom! Mullet snapping at sound barrier!"
          FlightStage.LIFTOFF -> "🚀 Cleared launch pad! Taco Bell drive-thru in the rearview mirror."
          FlightStage.LAUNCHPAD -> "Standing by on pad."
        }

        val orbitAchieved = alt >= 160000

        _flightTelemetry.value = _flightTelemetry.value.copy(
          altitudeMeters = alt,
          velocityMs = vel,
          gForce = twr,
          maxAltitudeMeters = max(_flightTelemetry.value.maxAltitudeMeters, alt),
          maxVelocityMs = max(_flightTelemetry.value.maxVelocityMs, vel),
          flightTimeSeconds = timeSec,
          burritoFuelRemaining = fuel,
          stage = stage,
          isFlying = true,
          isOrbitAchieved = orbitAchieved,
          recentMilestone = milestoneText
        )
      }

      // Flight ended / engines cut off
      _flightTelemetry.value = _flightTelemetry.value.copy(
        isFlying = false,
        recentMilestone = if (_flightTelemetry.value.isOrbitAchieved) {
          "Joe is now a permanent satellite of Earth! Mullet perpetual orbit confirmed."
        } else {
          "Engine burp complete. Capsule parachuting back to Taco Bell parking lot."
        }
      )
    }
  }

  fun applyBurritoBoost() {
    triggerHapticBurst(200)
    val cur = _flightTelemetry.value
    if (cur.isFlying) {
      _flightTelemetry.value = cur.copy(
        velocityMs = cur.velocityMs + 180.0,
        altitudeMeters = cur.altitudeMeters + 500.0,
        recentMilestone = "💥 BURRITO BOOST FIRED! Nitrous-salsa injection delivered +180 m/s!"
      )
    }
  }

  fun resetFlight() {
    flightSimulationJob?.cancel()
    _flightTelemetry.value = FlightTelemetry()
    _uiState.value = _uiState.value.copy(
      isIgnited = false,
      statusMessage = "Flight simulator reset. Booster restaged on launchpad."
    )
  }

  // --- HAPTICS ---

  private fun triggerHapticTick() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(30)
      }
    } catch (_: Exception) {}
  }

  private fun triggerHapticBurst(durationMs: Long) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }
}
