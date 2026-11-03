package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OrbitronFontFamily = FontFamily(
  Font(R.font.orbitron, FontWeight.Normal),
  Font(R.font.orbitron, FontWeight.Bold)
)

val RobotoMonoFontFamily = FontFamily(
  Font(R.font.roboto_mono, FontWeight.Normal),
  Font(R.font.roboto_mono, FontWeight.Bold)
)

val LabTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.5.sp
  ),
  displayMedium = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.5.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.25.sp
  ),
  titleLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.2.sp
  ),
  titleMedium = TextStyle(
    fontFamily = RobotoMonoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = RobotoMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = RobotoMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.25.sp
  ),
  labelLarge = TextStyle(
    fontFamily = OrbitronFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.5.sp
  ),
  labelSmall = TextStyle(
    fontFamily = RobotoMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.4.sp
  )
)
