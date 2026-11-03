package com.example.model

enum class OperatingMode(
  val title: String,
  val subtitle: String,
  val thrustMultiplier: Double,
  val exhaustVelocityMultiplier: Double,
  val pressureRatePerSec: Float, // PSI change per second
  val description: String
) {
  IDLE(
    title = "Idle",
    subtitle = "Control Room Standby",
    thrustMultiplier = 0.2,
    exhaustVelocityMultiplier = 0.6,
    pressureRatePerSec = -5f, // naturally depressurizes slightly toward baseline
    description = "Joe is sitting on his lawn chair reading hot rod magazines while venting mild digestive vapor."
  ),
  TACO_BELL_AFTERBURNER(
    title = "Taco Bell Afterburner",
    subtitle = "Supersonic Turbopumps",
    thrustMultiplier = 1.4,
    exhaustVelocityMultiplier = 1.5,
    pressureRatePerSec = 18f, // builds pressure rapidly
    description = "Dual fire-sauce injectors active! Gas plume exhibits visible purple shock diamonds."
  ),
  FULL_MONGO_LAUNCH(
    title = "Full Mongo Launch",
    subtitle = "Orbital Ascent Overdrive",
    thrustMultiplier = 2.8,
    exhaustVelocityMultiplier = 2.6,
    pressureRatePerSec = 45f, // builds extreme pressure towards Max Limit!
    description = "Structural integrity failing! Mullet flapping at supersonic frequency! Houston is praying!"
  )
}
