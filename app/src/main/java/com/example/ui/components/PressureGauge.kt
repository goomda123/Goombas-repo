package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PressureLevel
import com.example.model.PropulsionPhysics
import com.example.ui.theme.GaugeCritical
import com.example.ui.theme.GaugeElevated
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.GaugeWarning
import com.example.ui.theme.OrbitronFontFamily
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PressureGauge(
  currentPressurePsi: Float,
  onPressureChange: (Float) -> Unit,
  onVentTap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val pressureLevel = PropulsionPhysics.evaluatePressureLevel(currentPressurePsi)

  // Needle jitter animation when in danger zone
  val infiniteTransition = rememberInfiniteTransition(label = "gaugeJitter")
  val jitter by infiniteTransition.animateFloat(
    initialValue = -2.5f,
    targetValue = 2.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 60, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "jitterAnim"
  )

  val activeJitter = if (pressureLevel == PressureLevel.MAXIMUM_NASA_LOST_CONTROL) {
    jitter * 2.2f
  } else if (pressureLevel == PressureLevel.METHANE_OVERLOAD) {
    jitter
  } else {
    0f
  }

  // Animated sweep angle for needle (from -210 deg to +30 deg -> 240 deg span)
  val clampedPressure = currentPressurePsi.coerceIn(0f, 1400f)
  val normalized = clampedPressure / 1400f
  val targetAngle = -210f + (normalized * 240f) + activeJitter
  val animatedAngle by animateFloatAsState(
    targetValue = targetAngle,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "needleAngle"
  )

  val statusColor = when (pressureLevel) {
    PressureLevel.SAFE -> GaugeSafe
    PressureLevel.ELEVATED -> GaugeElevated
    PressureLevel.METHANE_OVERLOAD -> GaugeWarning
    PressureLevel.MAXIMUM_NASA_LOST_CONTROL -> GaugeCritical
  }

  val statusLabel = when (pressureLevel) {
    PressureLevel.SAFE -> "NORMAL DIGESTION"
    PressureLevel.ELEVATED -> "TURBOPUMP ENGAGED"
    PressureLevel.METHANE_OVERLOAD -> "METHANE OVERLOAD"
    PressureLevel.MAXIMUM_NASA_LOST_CONTROL -> "MAXIMUM - NASA LOST CONTROL"
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
      .testTag("pressure_gauge_card"),
    color = MaterialTheme.colorScheme.surface
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
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = "LIVE INTERNAL GAS PRESSURE",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = statusColor.copy(alpha = 0.2f),
          border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
        ) {
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Custom Arc Gauge Canvas
      Box(
        modifier = Modifier.size(240.dp, 160.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(230.dp, 230.dp)) {
          val strokeWidth = 18.dp.toPx()
          val center = Offset(size.width / 2f, size.height / 2f)
          val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
          val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

          // Background track (240 deg sweep starting at 150 deg)
          drawArc(
            color = Color(0xFF232A3A),
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Safe zone (0 to 400 PSI -> 28.5% of 240 deg = 68.4 deg)
          drawArc(
            color = GaugeSafe,
            startAngle = 150f,
            sweepAngle = 68.4f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
          )

          // Elevated zone (400 to 750 PSI -> 25% of 240 deg = 60 deg)
          drawArc(
            color = GaugeElevated,
            startAngle = 150f + 68.4f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
          )

          // Warning zone (750 to 1200 PSI -> 32% of 240 deg = 76.8f deg)
          drawArc(
            color = GaugeWarning,
            startAngle = 150f + 68.4f + 60f,
            sweepAngle = 76.8f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
          )

          // Maximum / Critical zone (1200 to 1400 PSI -> 14.5% = 34.8 deg)
          drawArc(
            color = GaugeCritical,
            startAngle = 150f + 68.4f + 60f + 76.8f,
            sweepAngle = 34.8f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Gauge Needle
          val angleRad = Math.toRadians(animatedAngle.toDouble())
          val needleLength = (size.width / 2f) - strokeWidth - 6.dp.toPx()
          val needleEnd = Offset(
            x = center.x + (needleLength * cos(angleRad)).toFloat(),
            y = center.y + (needleLength * sin(angleRad)).toFloat()
          )

          // Needle shadow
          drawLine(
            color = Color.Black.copy(alpha = 0.5f),
            start = center + Offset(2f, 2f),
            end = needleEnd + Offset(2f, 2f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
          )

          // Needle line
          drawLine(
            color = statusColor,
            start = center,
            end = needleEnd,
            strokeWidth = 3.5f.dp.toPx(),
            cap = StrokeCap.Round
          )

          // Center Hub
          drawCircle(
            color = Color(0xFF1F2433),
            radius = 16.dp.toPx(),
            center = center
          )
          drawCircle(
            color = statusColor,
            radius = 8.dp.toPx(),
            center = center
          )
        }

        // Digital Readout centered inside gauge
        Column(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = String.format("%.0f", clampedPressure),
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = statusColor
          )
          Text(
            text = "PSI (INTERNAL GAS)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Gauge Pressure Adjuster & Vent Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { onPressureChange(max(PropulsionPhysics.MIN_PRESSURE_PSI, currentPressurePsi - 100f)) },
          modifier = Modifier.testTag("decrease_pressure_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Decrease Pressure",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        OutlinedButton(
          onClick = onVentTap,
          modifier = Modifier.testTag("vent_valve_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Air,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.size(6.dp))
          Text("VENT GAS")
        }

        FilledTonalButton(
          onClick = { onPressureChange(min(1350f, currentPressurePsi + 150f)) },
          modifier = Modifier.testTag("pump_pressure_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.size(4.dp))
          Text("+150 PSI")
        }
      }
    }
  }
}
