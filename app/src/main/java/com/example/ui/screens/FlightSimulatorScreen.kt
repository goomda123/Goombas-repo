package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlightStage
import com.example.model.FlightTelemetry
import com.example.model.ThrustMetrics
import com.example.ui.theme.DarkLabBackground
import com.example.ui.theme.DarkLabSurface
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.FlameYellow
import com.example.ui.theme.FullMongoRed
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.MethaneGreen
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TacoBellPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlightSimulatorScreen(
  telemetry: FlightTelemetry,
  thrustMetrics: ThrustMetrics,
  onBurritoBoost: () -> Unit,
  onResetFlight: () -> Unit,
  onBackToLab: () -> Unit,
  modifier: Modifier = Modifier
) {
  var foilJettisoned by remember { mutableStateOf(false) }
  var mulletTrimActive by remember { mutableStateOf(false) }

  // Mullet flapping oscillation
  val infiniteTransition = rememberInfiniteTransition(label = "mulletPhysics")
  val flapCycle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28318f, // 2 * PI
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 180, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "flapCycle"
  )

  // Cloud/space stars parallax shift
  val scrollAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "starScroll"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkLabBackground)
      .testTag("flight_simulator_screen")
  ) {
    // 1. SKY / SPACE / ASCENT CANVAS
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawFlightAtmosphere(
        altitudeMeters = telemetry.altitudeMeters,
        starOffset = scrollAnim
      )

      drawRocketAndJoe(
        telemetry = telemetry,
        flapCycle = flapCycle,
        mulletTrim = mulletTrimActive,
        foilJettisoned = foilJettisoned
      )
    }

    // 2. FLIGHT TELEMETRY HUD OVERLAY (TOP)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .align(Alignment.TopCenter)
    ) {
      // Top Navigation & Stage Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackToLab,
          modifier = Modifier
            .background(DarkLabSurface.copy(alpha = 0.85f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .testTag("back_to_lab_btn")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back to Lab", tint = Color.White)
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = when (telemetry.stage) {
            FlightStage.DEEP_SPACE_MONGO -> FullMongoRed.copy(alpha = 0.9f)
            FlightStage.LOW_EARTH_ORBIT, FlightStage.ISS_FLYBY -> TacoBellPurple.copy(alpha = 0.9f)
            else -> DarkLabSurface.copy(alpha = 0.9f)
          },
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (telemetry.stage == FlightStage.DEEP_SPACE_MONGO) FullMongoRed else FlameOrange
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (telemetry.stage == FlightStage.DEEP_SPACE_MONGO) Icons.Default.CrisisAlert else Icons.Default.Rocket,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = telemetry.stage.stageName.uppercase(),
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color.White
            )
          }
        }

        IconButton(
          onClick = onResetFlight,
          modifier = Modifier
            .background(DarkLabSurface.copy(alpha = 0.85f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .testTag("reset_flight_btn")
        ) {
          Icon(Icons.Default.LockReset, contentDescription = "Reset Flight", tint = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // NASA LOST CONTROL SPECIAL BANNER IN SIMULATOR
      if (telemetry.stage == FlightStage.DEEP_SPACE_MONGO || telemetry.altitudeMeters >= 300000) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = FullMongoRed,
          modifier = Modifier.fillMaxWidth().testTag("sim_nasa_lost_control_banner")
        ) {
          Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(Icons.Default.CrisisAlert, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "NASA HAS LOST CONTROL OF JOE!",
              fontFamily = OrbitronFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // HUD Telemetry Metrics Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkLabSurface.copy(alpha = 0.85f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Row 1: Altitude & Velocity
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("ALTITUDE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              val altText = if (telemetry.altitudeMeters >= 1000) {
                String.format("%.1f km", telemetry.altitudeMeters / 1000.0)
              } else {
                String.format("%.0f m", telemetry.altitudeMeters)
              }
              Text(
                text = altText,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = FlameYellow
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("VELOCITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              val mach = telemetry.velocityMs / 343.0
              Text(
                text = String.format("Mach %.1f", mach),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = FlameOrange
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("MULLET FLAP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              val flapHz = thrustMetrics.mulletFlapFrequencyHz
              Text(
                text = String.format("%.1f Hz", flapHz),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MethaneGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Row 2: G-Force and Fuel Progress
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = String.format("G-FORCE: %.1f G", telemetry.gForce),
              style = MaterialTheme.typography.bodySmall,
              fontFamily = RobotoMonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "BURRITO FUEL: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              LinearProgressIndicator(
                progress = { telemetry.burritoFuelRemaining / 100f },
                modifier = Modifier
                  .width(90.dp)
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = if (telemetry.burritoFuelRemaining < 25f) FullMongoRed else GaugeSafe,
                trackColor = Color(0xFF2A3142),
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Recent Milestone Log
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF0F131D),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = telemetry.recentMilestone,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              maxLines = 2
            )
          }
        }
      }
    }

    // 3. FLIGHT COCKPIT ACTION BUTTONS (BOTTOM)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .align(Alignment.BottomCenter)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Burrito Boost Button
        Button(
          onClick = onBurritoBoost,
          enabled = telemetry.isFlying,
          colors = ButtonDefaults.buttonColors(
            containerColor = FlameOrange,
            contentColor = Color.White
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("burrito_boost_btn")
        ) {
          Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("BOOST", fontFamily = OrbitronFontFamily, fontSize = 12.sp)
        }

        // Mullet Aerodynamic Trim Toggle
        FilledTonalButton(
          onClick = { mulletTrimActive = !mulletTrimActive },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (mulletTrimActive) MethaneGreen.copy(alpha = 0.3f) else DarkLabSurface,
            contentColor = if (mulletTrimActive) MethaneGreen else Color.White
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("mullet_trim_btn")
        ) {
          Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (mulletTrimActive) "TRIM: ON" else "MULLET TRIM", fontFamily = OrbitronFontFamily, fontSize = 11.sp)
        }

        // Jettison Foil Stage
        OutlinedButton(
          onClick = { foilJettisoned = true },
          enabled = !foilJettisoned && telemetry.isFlying,
          modifier = Modifier
            .weight(1f)
            .testTag("jettison_foil_btn")
        ) {
          Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (foilJettisoned) "STAGE 2" else "JETTISON", fontFamily = OrbitronFontFamily, fontSize = 11.sp)
        }
      }
    }
  }
}

// Canvas Drawer: Sky, Atmosphere, Stars, Earth Horizon
private fun DrawScope.drawFlightAtmosphere(altitudeMeters: Double, starOffset: Float) {
  val width = size.width
  val height = size.height

  // Background Gradient based on Altitude
  val skyBrush = when {
    altitudeMeters < 12000 -> {
      // Launchpad & Troposphere (Light blue to bright horizon)
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF1E88E5),
          Color(0xFF64B5F6),
          Color(0xFFBBDEFB)
        )
      )
    }
    altitudeMeters < 60000 -> {
      // Stratosphere (Deep navy transitioning to black)
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF0D1B2A),
          Color(0xFF1B263B),
          Color(0xFF415A77)
        )
      )
    }
    altitudeMeters < 160000 -> {
      // Mesosphere & Upper Atmosphere (Cosmic deep purple-black)
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF05050A),
          Color(0xFF0F0E26),
          Color(0xFF2E1C4D)
        )
      )
    }
    else -> {
      // Low Earth Orbit & Deep Space (Pitch black with stars and curved Earth)
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF020205),
          Color(0xFF050714),
          Color(0xFF0A122A)
        )
      )
    }
  }

  drawRect(brush = skyBrush)

  // Draw Passing Clouds (if below 25 km)
  if (altitudeMeters < 25000) {
    val cloudColor = Color.White.copy(alpha = 0.5f)
    for (i in 0..4) {
      val y = (starOffset * 1.5f + (i * 240f)) % height
      val x = (i * 110f + 40f) % width
      drawCircle(
        color = cloudColor,
        radius = 45f + (i * 8f),
        center = Offset(x, y)
      )
      drawCircle(
        color = cloudColor,
        radius = 35f,
        center = Offset(x + 35f, y - 8f)
      )
    }
  }

  // Draw Stars & Satellites (if above 40 km)
  if (altitudeMeters > 40000) {
    val starColor = Color.White.copy(alpha = 0.85f)
    for (i in 0..35) {
      val sx = (i * 47f * 3.7f) % width
      val sy = (i * 83f * 2.1f + starOffset * 0.3f) % height
      val radius = if (i % 5 == 0) 2.5f else 1.5f
      drawCircle(color = starColor, radius = radius, center = Offset(sx, sy))
    }

    // Distant Moon
    drawCircle(
      color = Color(0xFFF5F5DC),
      radius = 28f,
      center = Offset(width - 60f, 130f)
    )

    // Curved Earth Horizon at High Orbit
    if (altitudeMeters > 100000) {
      val earthArcY = height + 400f
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF00E5FF), Color(0xFF1565C0), Color(0xFF0D47A1)),
          center = Offset(width / 2f, earthArcY),
          radius = 550f
        ),
        radius = 550f,
        center = Offset(width / 2f, earthArcY)
      )
    }
  }
}

// Canvas Drawer: Joe, The Burrito Rocket, Flapping Mullet, and Exhaust Plume
private fun DrawScope.drawRocketAndJoe(
  telemetry: FlightTelemetry,
  flapCycle: Float,
  mulletTrim: Boolean,
  foilJettisoned: Boolean
) {
  val centerX = size.width / 2f
  val centerY = size.height * 0.52f

  // Rocket angle: slight tilt as Joe ascends
  val mach = (telemetry.velocityMs / 343.0).toFloat().coerceIn(0.5f, 15f)

  // 1. ROCKET EXHAUST PLUME (Flames & Green Methane Vapor)
  val plumeLength = 110f + (mach * 12f)
  val nozzleX = centerX - 18f
  val nozzleY = centerY + 105f

  // Fire plume (Orange / Yellow)
  val flamePath = Path().apply {
    moveTo(nozzleX - 18f, nozzleY)
    quadraticBezierTo(
      nozzleX - 25f + sin(flapCycle) * 12f,
      nozzleY + (plumeLength * 0.6f),
      nozzleX + sin(flapCycle * 1.5f) * 15f,
      nozzleY + plumeLength
    )
    quadraticBezierTo(
      nozzleX + 25f - sin(flapCycle) * 12f,
      nozzleY + (plumeLength * 0.6f),
      nozzleX + 18f,
      nozzleY
    )
    close()
  }

  drawPath(
    path = flamePath,
    brush = Brush.verticalGradient(
      colors = listOf(FlameYellow, FlameOrange, Color(0xFFFF1744)),
      startY = nozzleY,
      endY = nozzleY + plumeLength
    )
  )

  // Neon-Green Methane Gas Vapor Trail
  for (i in 0..4) {
    val puffY = nozzleY + plumeLength + (i * 35f)
    val puffRadius = 18f + (i * 12f)
    val puffX = nozzleX + sin(flapCycle + i) * 20f
    drawCircle(
      color = MethaneGreen.copy(alpha = 0.35f - (i * 0.06f)),
      radius = puffRadius,
      center = Offset(puffX, puffY)
    )
  }

  // 2. THE BURRITO ROCKET BODY
  val rocketWidth = 44f
  val rocketHeight = 135f
  val rocketLeft = nozzleX - (rocketWidth / 2f)
  val rocketTop = nozzleY - rocketHeight

  // Burrito Tortilla Body
  val tortillaColor = if (foilJettisoned) Color(0xFFE2C290) else Color(0xFFD6D9DF) // Foil vs Naked Tortilla
  drawRoundRect(
    color = tortillaColor,
    topLeft = Offset(rocketLeft, rocketTop),
    size = Size(rocketWidth, rocketHeight),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
  )

  // Aluminum Foil Wrap Shading / Taco Bell Purple accent stripe
  drawRect(
    color = TacoBellPurple,
    topLeft = Offset(rocketLeft, rocketTop + 40f),
    size = Size(rocketWidth, 14f)
  )

  // Rocket Nozzle Bell
  val nozzlePath = Path().apply {
    moveTo(rocketLeft + 4f, rocketTop + rocketHeight)
    lineTo(rocketLeft - 8f, rocketTop + rocketHeight + 18f)
    lineTo(rocketLeft + rocketWidth + 8f, rocketTop + rocketHeight + 18f)
    lineTo(rocketLeft + rocketWidth - 4f, rocketTop + rocketHeight)
    close()
  }
  drawPath(path = nozzlePath, color = Color(0xFF37474F))

  // 3. JOE STRAPPED TO THE ROCKET
  val joeX = centerX + 12f
  val joeY = rocketTop + 30f

  // Joe's Body (Denim Vest)
  drawRoundRect(
    color = Color(0xFF1E3A8A), // Blue denim
    topLeft = Offset(joeX - 16f, joeY),
    size = Size(32f, 42f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
  )

  // Arms gripping handlebar
  drawLine(
    color = Color(0xFFF5CBA7), // Skin tone
    start = Offset(joeX - 10f, joeY + 12f),
    end = Offset(rocketLeft + 10f, rocketTop + 24f),
    strokeWidth = 8f,
    cap = StrokeCap.Round
  )

  // Joe's Head
  val headCenter = Offset(joeX, joeY - 18f)
  drawCircle(
    color = Color(0xFFF5CBA7), // Face
    radius = 18f,
    center = headCenter
  )

  // Big Grin
  drawArc(
    color = Color(0xFF4A2810),
    startAngle = 10f,
    sweepAngle = 160f,
    useCenter = false,
    topLeft = Offset(headCenter.x - 10f, headCenter.y - 4f),
    size = Size(20f, 16f),
    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
  )

  // Aviator Sunglasses
  drawRoundRect(
    color = Color.Black,
    topLeft = Offset(headCenter.x - 12f, headCenter.y - 10f),
    size = Size(13f, 9f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
  )
  drawRoundRect(
    color = Color.Black,
    topLeft = Offset(headCenter.x + 2f, headCenter.y - 10f),
    size = Size(13f, 9f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
  )
  drawLine(
    color = Color(0xFFFFD700), // Gold bridge
    start = Offset(headCenter.x - 12f, headCenter.y - 8f),
    end = Offset(headCenter.x + 15f, headCenter.y - 8f),
    strokeWidth = 2f
  )

  // Front Hair (Bangs / Top)
  val frontHairPath = Path().apply {
    moveTo(headCenter.x - 18f, headCenter.y - 10f)
    lineTo(headCenter.x - 4f, headCenter.y - 24f)
    lineTo(headCenter.x + 16f, headCenter.y - 20f)
    lineTo(headCenter.x + 14f, headCenter.y - 8f)
    close()
  }
  drawPath(path = frontHairPath, color = Color(0xFF5D4037))

  // 4. THE LEGENDARY MULLET (DYNAMIC FLAPPING PHYSICS!)
  // Mullet strands fly backward (downward and leftward relative to ascent)
  val mulletOriginX = headCenter.x - 8f
  val mulletOriginY = headCenter.y - 6f
  val hairColor = Color(0xFF6D4C41)
  val highlightColor = Color(0xFF8D6E63)

  val flapAmp = if (mulletTrim) 14f else 28f + (mach * 3.5f)
  val flapSpeedMod = flapCycle

  for (strand in 0..6) {
    val strandOffset = strand * 5f
    val strandPath = Path().apply {
      moveTo(mulletOriginX - (strand * 2f), mulletOriginY + (strand * 3f))

      val wave1 = sin(flapSpeedMod + (strand * 0.8f)) * flapAmp
      val wave2 = cos(flapSpeedMod * 1.3f + (strand * 0.5f)) * (flapAmp * 1.2f)

      // Segment 1 (Shoulder flap)
      val cp1X = mulletOriginX - 25f + wave1
      val cp1Y = mulletOriginY + 25f + strandOffset
      val p1X = mulletOriginX - 45f + (wave1 * 0.8f)
      val p1Y = mulletOriginY + 50f + strandOffset

      // Segment 2 (Tail whip flap)
      val cp2X = p1X - 25f + wave2
      val cp2Y = p1Y + 30f
      val p2X = p1X - 55f + wave2
      val p2Y = p1Y + 65f + (strand * 4f)

      cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2X, p2Y)
    }

    drawPath(
      path = strandPath,
      color = if (strand % 2 == 0) hairColor else highlightColor,
      style = Stroke(
        width = 5.5f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )
  }
}
