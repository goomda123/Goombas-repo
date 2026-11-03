package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BurritoType
import com.example.model.OperatingMode
import com.example.model.PressureLevel
import com.example.model.PropulsionPhysics
import com.example.model.ThrustMetrics
import com.example.ui.components.MethaneWarningBanner
import com.example.ui.components.NasaLostControlBanner
import com.example.ui.components.PressureGauge
import com.example.ui.theme.DarkLabSurface
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.FlameYellow
import com.example.ui.theme.FullMongoRed
import com.example.ui.theme.GaugeElevated
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.MethaneGreen
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TacoBellPurple
import com.example.viewmodel.RocketLabUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabControlScreen(
  state: RocketLabUiState,
  thrustMetrics: ThrustMetrics,
  onBurritoCountChange: (Int) -> Unit,
  onBurritoTypeChange: (BurritoType) -> Unit,
  onPressureChange: (Float) -> Unit,
  onExhaustVelocityChange: (Double) -> Unit,
  onOperatingModeChange: (OperatingMode) -> Unit,
  onStartCountdown: () -> Unit,
  onAbortCountdown: () -> Unit,
  onTriggerAlarm: () -> Unit,
  onEmergencyVent: () -> Unit,
  onOpenFlightSim: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val pressureLevel = PropulsionPhysics.evaluatePressureLevel(state.internalPressurePsi)

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. HERO BANNER & LAB IDENTITY
    Card(
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      colors = CardDefaults.cardColors(containerColor = DarkLabSurface),
      modifier = Modifier.fillMaxWidth().testTag("hero_lab_card")
    ) {
      Column {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.joe_lab_header),
            contentDescription = "Joe's Laboratory Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )

          // Dark gradient overlay for text readability
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color(0xCC0B0D13), Color(0xFF0B0D13)),
                  startY = 40f
                )
              )
          )

          // Header Text
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(12.dp)
          ) {
            Text(
              text = "JOE'S ROCKET PROPULSION LABORATORY",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = FlameYellow
            )
            Text(
              text = "Burrito-Methane Thermodynamics & Mullet Aerodynamics",
              style = MaterialTheme.typography.bodySmall,
              color = Color.LightGray
            )
          }
        }

        // Live Status Marquee Bar
        Surface(
          color = Color(0xFF0F131D),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (state.isCountingDown) FlameOrange else MethaneGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = state.statusMessage,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
          }
        }
      }
    }

    // 2. NASA LOST CONTROL WARNING (CRITICAL REQUIREMENT)
    NasaLostControlBanner(
      visible = state.showNasaLostControlBanner,
      onLaunchFlightSimulator = onOpenFlightSim
    )

    // 3. METHANE OVERLOAD WARNING BANNER
    MethaneWarningBanner(
      pressureLevel = pressureLevel,
      currentPsi = state.internalPressurePsi,
      onEmergencyVent = onEmergencyVent
    )

    // 4. THREE OPERATING MODES SELECTOR (CRITICAL REQUIREMENT)
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkLabSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth().testTag("operating_modes_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PROPULSION OPERATING MODE",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OperatingMode.values().forEach { mode ->
            val isSelected = state.operatingMode == mode
            val modeColor = when (mode) {
              OperatingMode.IDLE -> GaugeSafe
              OperatingMode.TACO_BELL_AFTERBURNER -> TacoBellPurple
              OperatingMode.FULL_MONGO_LAUNCH -> FullMongoRed
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) modeColor.copy(alpha = 0.22f) else Color(0xFF1B202D),
              border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) modeColor else Color(0xFF2C3446)
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { onOperatingModeChange(mode) }
                .testTag("mode_${mode.name.lowercase()}")
            ) {
              Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = when (mode) {
                    OperatingMode.IDLE -> "IDLE"
                    OperatingMode.TACO_BELL_AFTERBURNER -> "AFTERBURNER"
                    OperatingMode.FULL_MONGO_LAUNCH -> "FULL MONGO"
                  },
                  fontFamily = OrbitronFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (isSelected) modeColor else Color.White,
                  textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = when (mode) {
                    OperatingMode.IDLE -> "Standby"
                    OperatingMode.TACO_BELL_AFTERBURNER -> "Taco Bell"
                    OperatingMode.FULL_MONGO_LAUNCH -> "Launch!"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }
    }

    // 5. LIVE PRESSURE GAUGE (CRITICAL REQUIREMENT)
    PressureGauge(
      currentPressurePsi = state.internalPressurePsi,
      onPressureChange = onPressureChange,
      onVentTap = onEmergencyVent
    )

    // 6. PROPULSION PHYSICS & ROCKET THRUST CALCULATOR
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkLabSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth().testTag("thrust_calculator_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "CALCULATED ROCKET THRUST",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          Text(
            text = "F = ṁ·Ve + (Pe-P0)·Ae",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = RobotoMonoFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Big Thrust Readout
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F131D),
          border = androidx.compose.foundation.BorderStroke(1.dp, FlameOrange.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL BURRITO THRUST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format("%.2f kN", thrustMetrics.thrustKiloNewtons),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = FlameOrange
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = String.format("%.0f lbf", thrustMetrics.thrustPoundForce),
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FlameYellow
              )
              Text(
                text = String.format("%.0f bN (Burrito-N)", thrustMetrics.burritoNewtons),
                style = MaterialTheme.typography.labelSmall,
                color = MethaneGreen
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Aerospace Metrics
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          MetricTile(
            title = "MASS FLOW (ṁ)",
            value = String.format("%.2f kg/s", thrustMetrics.massFlowRate),
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            title = "SPECIFIC IMPULSE",
            value = String.format("%.0f s (Isp)", thrustMetrics.specificImpulseSeconds),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          MetricTile(
            title = "JOE'S TWR",
            value = String.format("%.2f : 1", thrustMetrics.thrustToWeightRatio),
            highlight = thrustMetrics.thrustToWeightRatio > 1.0,
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            title = "MULLET FLAP RATE",
            value = String.format("%.1f Hz", thrustMetrics.mulletFlapFrequencyHz),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 7. BURRITO CONSUMPTION & FUEL STAGING
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkLabSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth().testTag("burrito_fuel_card")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🌯", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "BURRITO CONSUMPTION STAGE",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          Text(
            text = "${state.burritoCount} BURRITOS",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = FlameOrange
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Burrito Stepper & Slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { onBurritoCountChange(state.burritoCount - 1) },
            enabled = state.burritoCount > 1,
            modifier = Modifier.testTag("burrito_minus_btn")
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Fewer burritos")
          }

          Slider(
            value = state.burritoCount.toFloat(),
            onValueChange = { onBurritoCountChange(it.toInt()) },
            valueRange = 1f..25f,
            steps = 23,
            modifier = Modifier.weight(1f).testTag("burrito_slider"),
            colors = SliderDefaults.colors(
              thumbColor = FlameOrange,
              activeTrackColor = FlameOrange,
              inactiveTrackColor = Color(0xFF2B3345)
            )
          )

          IconButton(
            onClick = { onBurritoCountChange(state.burritoCount + 1) },
            enabled = state.burritoCount < 25,
            modifier = Modifier.testTag("burrito_plus_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = "More burritos")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "PROPELLANT GRADE SELECTION",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Propellant Selection Chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          BurritoType.values().forEach { bType ->
            val selected = state.burritoType == bType
            FilterChip(
              selected = selected,
              onClick = { onBurritoTypeChange(bType) },
              label = {
                Text(
                  text = "${bType.iconEmoji} ${bType.displayName}",
                  fontSize = 11.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = FlameOrange.copy(alpha = 0.25f),
                selectedLabelColor = FlameOrange
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = Color(0xFF2C3446),
                selectedBorderColor = FlameOrange
              ),
              modifier = Modifier.testTag("propellant_${bType.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exhaust Velocity Slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "EXHAUST VELOCITY (Ve)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format("%.0f m/s (Mach %.1f)", state.exhaustVelocityMs, state.exhaustVelocityMs / 343.0),
            fontFamily = RobotoMonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = FlameYellow,
            fontSize = 13.sp
          )
        }

        Slider(
          value = state.exhaustVelocityMs.toFloat(),
          onValueChange = { onExhaustVelocityChange(it.toDouble()) },
          valueRange = 500f..4500f,
          modifier = Modifier.fillMaxWidth().testTag("exhaust_velocity_slider"),
          colors = SliderDefaults.colors(
            thumbColor = FlameYellow,
            activeTrackColor = FlameYellow,
            inactiveTrackColor = Color(0xFF2B3345)
          )
        )
      }
    }

    // 8. COUNTDOWN TO IGNITION STATION
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkLabSurface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth().testTag("countdown_station_card")
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Rocket, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "IGNITION SEQUENCE CONSOLE",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          if (state.isCountingDown) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = FlameOrange.copy(alpha = 0.2f),
              border = androidx.compose.foundation.BorderStroke(1.dp, FlameOrange)
            ) {
              Text(
                text = "COUNTING DOWN",
                style = MaterialTheme.typography.labelSmall,
                color = FlameOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Giant Countdown Timer
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F131D),
          border = androidx.compose.foundation.BorderStroke(2.dp, if (state.isCountingDown) FlameOrange else Color(0xFF2C3446)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (state.countdownSeconds == 0) "LIFTOFF!" else "T - ${state.countdownSeconds}",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 38.sp,
              color = if (state.countdownSeconds <= 3) FullMongoRed else if (state.isCountingDown) FlameOrange else Color.White
            )
            Text(
              text = if (state.isCountingDown) "IGNITION CLOCK ACTIVE" else "READY FOR LAUNCH SEQUENCE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Start / Abort / Open Flight Sim
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (!state.isCountingDown) {
            Button(
              onClick = onStartCountdown,
              colors = ButtonDefaults.buttonColors(
                containerColor = FlameOrange,
                contentColor = Color.White
              ),
              modifier = Modifier.weight(1f).testTag("start_countdown_btn")
            ) {
              Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("START T-10", fontFamily = OrbitronFontFamily, fontSize = 12.sp)
            }
          } else {
            Button(
              onClick = onAbortCountdown,
              colors = ButtonDefaults.buttonColors(
                containerColor = FullMongoRed,
                contentColor = Color.White
              ),
              modifier = Modifier.weight(1f).testTag("abort_countdown_btn")
            ) {
              Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("ABORT LAUNCH", fontFamily = OrbitronFontFamily, fontSize = 12.sp)
            }
          }

          FilledTonalButton(
            onClick = onOpenFlightSim,
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = TacoBellPurple.copy(alpha = 0.25f),
              contentColor = TacoBellPurple
            ),
            modifier = Modifier.weight(1f).testTag("view_flight_sim_btn")
          ) {
            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("FLIGHT SIM", fontFamily = OrbitronFontFamily, fontSize = 12.sp)
          }
        }
      }
    }

    // 9. BIG RED EMERGENCY EVACUATION ALARM BUTTON
    Button(
      onClick = onTriggerAlarm,
      colors = ButtonDefaults.buttonColors(
        containerColor = FullMongoRed,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("emergency_evac_alarm_btn")
    ) {
      Icon(Icons.Default.Campaign, contentDescription = "Evacuation Alarm", modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "EMERGENCY EVACUATION ALARM",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun MetricTile(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
  highlight: Boolean = false
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = Color(0xFF181D2A),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (highlight) MethaneGreen else Color(0xFF2C3446)
    ),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontFamily = RobotoMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = if (highlight) MethaneGreen else Color.White
      )
    }
  }
}
