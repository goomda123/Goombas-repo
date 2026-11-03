package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.EvacuationAlarmModal
import com.example.ui.components.NasaLostControlModal
import com.example.ui.theme.DarkLabBackground
import com.example.ui.theme.DarkLabSurface
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.OrbitronFontFamily
import com.example.viewmodel.RocketLabViewModel

enum class AppScreen {
  LAB_CONTROL,
  FLIGHT_SIMULATOR
}

@Composable
fun MainScreen(viewModel: RocketLabViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  val thrustMetrics by viewModel.thrustMetrics.collectAsState()
  val flightTelemetry by viewModel.flightTelemetry.collectAsState()

  var currentScreen by remember { mutableStateOf(AppScreen.LAB_CONTROL) }

  // Automatically switch to Flight Simulator when rocket ignites
  if (uiState.isIgnited && currentScreen == AppScreen.LAB_CONTROL && flightTelemetry.isFlying) {
    currentScreen = AppScreen.FLIGHT_SIMULATOR
  }

  // Handle system back navigation
  BackHandler(enabled = currentScreen == AppScreen.FLIGHT_SIMULATOR) {
    currentScreen = AppScreen.LAB_CONTROL
  }

  Scaffold(
    modifier = Modifier.fillMaxSize().background(DarkLabBackground),
    containerColor = DarkLabBackground,
    bottomBar = {
      NavigationBar(
        containerColor = DarkLabSurface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        NavigationBarItem(
          selected = currentScreen == AppScreen.LAB_CONTROL,
          onClick = { currentScreen = AppScreen.LAB_CONTROL },
          icon = { Icon(Icons.Default.Science, contentDescription = "Laboratory") },
          label = {
            Text(
              "PROPULSION LAB",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = FlameOrange,
            selectedTextColor = FlameOrange,
            indicatorColor = FlameOrange.copy(alpha = 0.2f),
            unselectedIconColor = Color(0xFF8892B0),
            unselectedTextColor = Color(0xFF8892B0)
          ),
          modifier = Modifier.testTag("nav_lab_tab")
        )

        NavigationBarItem(
          selected = currentScreen == AppScreen.FLIGHT_SIMULATOR,
          onClick = {
            if (!flightTelemetry.isFlying) {
              viewModel.startFlightSimulation()
            }
            currentScreen = AppScreen.FLIGHT_SIMULATOR
          },
          icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = "Flight Simulator") },
          label = {
            Text(
              "FLIGHT SIM",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = FlameOrange,
            selectedTextColor = FlameOrange,
            indicatorColor = FlameOrange.copy(alpha = 0.2f),
            unselectedIconColor = Color(0xFF8892B0),
            unselectedTextColor = Color(0xFF8892B0)
          ),
          modifier = Modifier.testTag("nav_sim_tab")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.LAB_CONTROL -> {
          LabControlScreen(
            state = uiState,
            thrustMetrics = thrustMetrics,
            onBurritoCountChange = viewModel::setBurritoCount,
            onBurritoTypeChange = viewModel::setBurritoType,
            onPressureChange = viewModel::setInternalPressure,
            onExhaustVelocityChange = viewModel::setExhaustVelocity,
            onOperatingModeChange = viewModel::setOperatingMode,
            onStartCountdown = viewModel::startCountdown,
            onAbortCountdown = viewModel::abortCountdown,
            onTriggerAlarm = viewModel::triggerEvacuationAlarm,
            onEmergencyVent = viewModel::triggerEmergencyVenting,
            onOpenFlightSim = {
              if (!flightTelemetry.isFlying) {
                viewModel.startFlightSimulation()
              }
              currentScreen = AppScreen.FLIGHT_SIMULATOR
            }
          )
        }

        AppScreen.FLIGHT_SIMULATOR -> {
          FlightSimulatorScreen(
            telemetry = flightTelemetry,
            thrustMetrics = thrustMetrics,
            onBurritoBoost = viewModel::applyBurritoBoost,
            onResetFlight = viewModel::resetFlight,
            onBackToLab = { currentScreen = AppScreen.LAB_CONTROL }
          )
        }
      }

      // Emergency Evacuation Alarm Modal
      EvacuationAlarmModal(
        show = uiState.isEvacuationAlarmActive,
        currentPsi = uiState.internalPressurePsi,
        onDismiss = viewModel::dismissEvacuationAlarm,
        onEmergencyVent = viewModel::triggerEmergencyVenting
      )

      // NASA Has Lost Control Takeover Modal
      NasaLostControlModal(
        show = uiState.showNasaLostControlModal,
        onDismiss = viewModel::dismissNasaModal,
        onOpenFlightSim = {
          if (!flightTelemetry.isFlying) {
            viewModel.startFlightSimulation()
          }
          currentScreen = AppScreen.FLIGHT_SIMULATOR
        }
      )
    }
  }
}
