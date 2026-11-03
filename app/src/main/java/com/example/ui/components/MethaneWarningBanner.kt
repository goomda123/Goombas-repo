package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PressureLevel
import com.example.ui.theme.FullMongoRed
import com.example.ui.theme.GaugeWarning
import com.example.ui.theme.OrbitronFontFamily

@Composable
fun MethaneWarningBanner(
  pressureLevel: PressureLevel,
  currentPsi: Float,
  onEmergencyVent: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isOverload = pressureLevel == PressureLevel.METHANE_OVERLOAD ||
    pressureLevel == PressureLevel.MAXIMUM_NASA_LOST_CONTROL

  val infiniteTransition = rememberInfiniteTransition(label = "methanePulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.45f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  AnimatedVisibility(
    visible = isOverload,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier.fillMaxWidth()
  ) {
    val borderColor = if (pressureLevel == PressureLevel.MAXIMUM_NASA_LOST_CONTROL) FullMongoRed else GaugeWarning
    val bgColor = borderColor.copy(alpha = 0.15f * pulseAlpha)

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(bgColor)
        .border(2.dp, borderColor.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
        .padding(14.dp)
        .testTag("methane_overload_warning")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = "Methane Overload Warning",
            tint = borderColor,
            modifier = Modifier
              .size(36.dp)
              .alpha(pulseAlpha)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "METHANE OVERLOAD WARNING",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = borderColor
            )
            Text(
              text = "Atmospheric gas density at ${(currentPsi / 12f).toInt()}%. Immediate ignition risk! Extinguish all pilot lights!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalButton(
          onClick = onEmergencyVent,
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = borderColor.copy(alpha = 0.25f),
            contentColor = borderColor
          ),
          modifier = Modifier.testTag("methane_vent_btn")
        ) {
          Text(
            text = "PURGE",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
