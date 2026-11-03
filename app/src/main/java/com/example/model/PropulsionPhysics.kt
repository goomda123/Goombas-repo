package com.example.model

import kotlin.math.max

data class ThrustMetrics(
  val thrustNewtons: Double,
  val thrustKiloNewtons: Double,
  val thrustPoundForce: Double,
  val burritoNewtons: Double, // Joe's proprietary laboratory unit
  val massFlowRate: Double, // kg/s
  val effectiveExhaustVelocity: Double, // m/s
  val machNumber: Double,
  val specificImpulseSeconds: Double,
  val thrustToWeightRatio: Double,
  val mulletFlapFrequencyHz: Double
)

enum class PressureLevel {
  SAFE,
  ELEVATED,
  METHANE_OVERLOAD,
  MAXIMUM_NASA_LOST_CONTROL
}

object PropulsionPhysics {
  const val BASE_ATMOSPHERIC_PA = 101325.0 // 1 atm in Pascals
  const val PSI_TO_PASCALS = 6894.757
  const val NOZZLE_AREA_M2 = 0.018 // 15 cm exhaust nozzle
  const val JOE_EMPTY_WEIGHT_KG = 84.0
  const val BURRITO_MASS_KG = 0.45
  const val GRAVITY_M_S2 = 9.80665
  const val SPEED_OF_SOUND_M_S = 343.0

  // Pressure thresholds in PSI
  const val MIN_PRESSURE_PSI = 50f
  const val SAFE_PRESSURE_MAX_PSI = 400f
  const val ELEVATED_PRESSURE_MAX_PSI = 750f
  const val METHANE_OVERLOAD_PSI = 750f
  const val MAX_PRESSURE_PSI = 1200f // NASA HAS LOST CONTROL TRIGGER!

  fun evaluatePressureLevel(pressurePsi: Float): PressureLevel {
    return when {
      pressurePsi >= MAX_PRESSURE_PSI -> PressureLevel.MAXIMUM_NASA_LOST_CONTROL
      pressurePsi >= METHANE_OVERLOAD_PSI -> PressureLevel.METHANE_OVERLOAD
      pressurePsi >= SAFE_PRESSURE_MAX_PSI -> PressureLevel.ELEVATED
      else -> PressureLevel.SAFE
    }
  }

  fun calculateThrust(
    burritoCount: Int,
    burritoType: BurritoType,
    internalPressurePsi: Float,
    baseExhaustVelocityMs: Double,
    mode: OperatingMode
  ): ThrustMetrics {
    // 1. Mass flow rate based on burritos and digestion mode
    val countFactor = max(1, burritoCount).toDouble()
    val massFlow = (countFactor * 0.18 * burritoType.baseMassFlow * mode.thrustMultiplier)

    // 2. Effective exhaust velocity scaled by nozzle tuning & mode
    val exhaustVelocity = (baseExhaustVelocityMs * mode.exhaustVelocityMultiplier * (1.0 + (burritoType.scovilleRating / 2000000.0)))

    // 3. Pressure thrust component: (Pe - P0) * Ae
    val internalPa = internalPressurePsi * PSI_TO_PASCALS
    val pressureDifference = max(0.0, internalPa - BASE_ATMOSPHERIC_PA)
    val pressureThrust = pressureDifference * NOZZLE_AREA_M2

    // 4. Fundamental Rocket Thrust Equation: F = m_dot * ve + (pe - p0) * Ae
    val totalThrustNewtons = (massFlow * exhaustVelocity) + pressureThrust

    // 5. Associated Aerospace metrics
    val thrustKn = totalThrustNewtons / 1000.0
    val thrustLbf = totalThrustNewtons * 0.224809
    val burritoNewtons = totalThrustNewtons * 1.337 // proprietary unit!

    val totalJoeMass = JOE_EMPTY_WEIGHT_KG + (burritoCount * BURRITO_MASS_KG)
    val twr = totalThrustNewtons / (totalJoeMass * GRAVITY_M_S2)
    val specificImpulse = exhaustVelocity / GRAVITY_M_S2
    val mach = exhaustVelocity / SPEED_OF_SOUND_M_S

    // Mullet flap frequency is directly correlated to airspeed and gas pressure
    val mulletHz = 6.0 + (mach * 5.5) + (internalPressurePsi / 18.0)

    return ThrustMetrics(
      thrustNewtons = totalThrustNewtons,
      thrustKiloNewtons = thrustKn,
      thrustPoundForce = thrustLbf,
      burritoNewtons = burritoNewtons,
      massFlowRate = massFlow,
      effectiveExhaustVelocity = exhaustVelocity,
      machNumber = mach,
      specificImpulseSeconds = specificImpulse,
      thrustToWeightRatio = twr,
      mulletFlapFrequencyHz = mulletHz
    )
  }
}
