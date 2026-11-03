package com.example.model

enum class BurritoType(
  val displayName: String,
  val subtitle: String,
  val baseMassFlow: Double, // kg/s
  val gasMultiplier: Double,
  val scovilleRating: Int, // SHU
  val iconEmoji: String,
  val description: String
) {
  BEAN_AND_CHEESE(
    displayName = "Bean & Cheese Classic",
    subtitle = "Standard Propellant",
    baseMassFlow = 1.2,
    gasMultiplier = 1.0,
    scovilleRating = 800,
    iconEmoji = "🌯",
    description = "Reliable low-pressure propellant. Recommended for atmospheric test bench idling."
  ),
  SEVEN_LAYER_NUCLEAR(
    displayName = "7-Layer Nuclear Supreme",
    subtitle = "Hypergolic Mixture",
    baseMassFlow = 2.4,
    gasMultiplier = 2.1,
    scovilleRating = 45000,
    iconEmoji = "🌶️",
    description = "Generates dense methane fumes. Guaranteed to activate laboratory exhaust fans."
  ),
  DIABLO_GHOST_PEPPER(
    displayName = "Diablo Ghost Pepper Megaton",
    subtitle = "Supersonic Catalyst",
    baseMassFlow = 4.1,
    gasMultiplier = 3.6,
    scovilleRating = 950000,
    iconEmoji = "🔥",
    description = "Exhaust exceeds Mach 3. Extreme gastrointestinal expansion warning."
  ),
  CHIMICHANGA_CATACLYSM(
    displayName = "Quad-Chimichanga Cataclysm",
    subtitle = "Deep Space Heavy Lift",
    baseMassFlow = 6.5,
    gasMultiplier = 5.2,
    scovilleRating = 2500000,
    iconEmoji = "☢️",
    description = "Restricted by international treaties. Mullet flap turbulence exceeds safety limits."
  )
}
