package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.FullMongoRed
import com.example.ui.theme.OrbitronFontFamily

@Composable
fun NasaLostControlBanner(
  visible: Boolean,
  onLaunchFlightSimulator: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "nasaFlash")
  val flashAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(250),
      repeatMode = RepeatMode.Reverse
    ),
    label = "flashAlpha"
  )

  AnimatedVisibility(visible = visible, modifier = modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(
          Brush.horizontalGradient(
            colors = listOf(
              FullMongoRed.copy(alpha = 0.35f * flashAlpha),
              FlameOrange.copy(alpha = 0.35f * flashAlpha)
            )
          )
        )
        .border(2.dp, FullMongoRed.copy(alpha = flashAlpha), RoundedCornerShape(12.dp))
        .padding(14.dp)
        .testTag("nasa_lost_control_banner")
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CrisisAlert,
            contentDescription = null,
            tint = FullMongoRed,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "NASA HAS LOST CONTROL OF JOE!",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = FullMongoRed,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "NORAD: High-velocity mullet detected ascending at escape velocity! Houston flight controllers have evacuated the room!",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onLaunchFlightSimulator,
          colors = ButtonDefaults.buttonColors(
            containerColor = FullMongoRed,
            contentColor = Color.White
          ),
          modifier = Modifier.testTag("track_joe_orbit_btn")
        ) {
          Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "TRACK JOE IN FLIGHT SIMULATOR",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
fun NasaLostControlModal(
  show: Boolean,
  onDismiss: () -> Unit,
  onOpenFlightSim: () -> Unit
) {
  if (!show) return

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("nasa_lost_control_dialog"),
    icon = {
      Icon(
        imageVector = Icons.Default.CrisisAlert,
        contentDescription = null,
        tint = FullMongoRed,
        modifier = Modifier.size(48.dp)
      )
    },
    title = {
      Text(
        text = "NASA HAS LOST CONTROL OF JOE!",
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = FullMongoRed,
        textAlign = TextAlign.Center
      )
    },
    text = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "Maximum internal gas pressure reached (1200+ PSI)!\n\nThe Cape Canaveral radar array has lost telemetry lock on Joe's aerodynamic mullet as he breaks orbital velocity powered exclusively by hypergolic bean propellant.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onDismiss()
          onOpenFlightSim()
        },
        colors = ButtonDefaults.buttonColors(containerColor = FullMongoRed)
      ) {
        Text("LAUNCH SIMULATOR", fontFamily = OrbitronFontFamily)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("STAND BY")
      }
    }
  )
}
