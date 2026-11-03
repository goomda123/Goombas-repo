package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkLabSurface
import com.example.ui.theme.FullMongoRed
import com.example.ui.theme.GaugeElevated
import com.example.ui.theme.OrbitronFontFamily

@Composable
fun EvacuationAlarmModal(
  show: Boolean,
  currentPsi: Float,
  onDismiss: () -> Unit,
  onEmergencyVent: () -> Unit
) {
  if (!show) return

  val infiniteTransition = rememberInfiniteTransition(label = "beaconRotation")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "beaconRot"
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(300, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alarmPulse"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("evacuation_alarm_dialog"),
    title = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Rotating siren beacon
        Box(
          modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(FullMongoRed.copy(alpha = 0.2f))
            .border(2.dp, FullMongoRed.copy(alpha = pulseAlpha), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Campaign,
            contentDescription = "Evacuation Siren",
            tint = FullMongoRed,
            modifier = Modifier
              .size(42.dp)
              .rotate(if (rotation > 180f) 10f else -10f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "EMERGENCY EVACUATION ALARM",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = FullMongoRed,
          textAlign = TextAlign.Center
        )

        Text(
          text = "DEFCON-1 BURRITO CATACLYSM",
          fontFamily = OrbitronFontFamily,
          fontSize = 11.sp,
          color = GaugeElevated,
          letterSpacing = 1.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF2A1519),
          border = androidx.compose.foundation.BorderStroke(1.dp, FullMongoRed.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "EVACUATION RADIUS: 15 MILES",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = FullMongoRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Internal Pressure: ${currentPsi.toInt()} PSI\nAll laboratory technicians, bystanders, and local drive-thru staff must immediately evacuate upwind.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Pulling the Emergency Purge Valve will immediately vent all combustible methane into the upper atmosphere to prevent structural containment rupture.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onEmergencyVent()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = FullMongoRed,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("emergency_purge_valve_btn")
      ) {
        Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "PULL EMERGENCY PURGE VALVE",
          fontFamily = OrbitronFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("SILENCE ALARM (AT YOUR OWN RISK)")
      }
    }
  )
}
