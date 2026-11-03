package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RocketLabColorScheme = darkColorScheme(
  primary = FlameOrange,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF3E1C00),
  onPrimaryContainer = FlameYellow,
  secondary = MethaneGreen,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF00381B),
  onSecondaryContainer = MethaneGreen,
  tertiary = TacoBellPurple,
  onTertiary = Color.White,
  error = FullMongoRed,
  onError = Color.White,
  background = DarkLabBackground,
  onBackground = TextPrimary,
  surface = DarkLabSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkLabSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkLabBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = RocketLabColorScheme,
    typography = LabTypography,
    content = content
  )
}
