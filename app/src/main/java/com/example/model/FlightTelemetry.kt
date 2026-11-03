package com.example.model

enum class FlightStage(val stageName: String, val milestoneDesc: String) {
  LAUNCHPAD("Launchpad 39-B (Behind Taco Bell)", "Resting on asphalt pad"),
  LIFTOFF("Ignition & Cleared Tower", "Plume of refried beans detected"),
  TRANSONIC_MULLET("Transonic Hair Flap", "Hairline breaking sound barrier"),
  MAX_Q("Max-Q Dynamic Pressure", "Maximum aerodynamic drag on Joe's ears"),
  STRATOSPHERE("Stratosphere Ascent", "Commercial pilots reporting mullet sighting"),
  MESOSPHERE("Mesosphere Ignition", "Glowing friction trail on aviator sunglasses"),
  LOW_EARTH_ORBIT("Low Earth Orbit", "Stable orbit achieved! Mullet in microgravity"),
  ISS_FLYBY("ISS Orbital Intercept", "Joe waved at ISS cupola window!"),
  DEEP_SPACE_MONGO("Deep Space Trajectory", "NASA HAS LOST CONTROL OF JOE!")
}

data class FlightTelemetry(
  val altitudeMeters: Double = 0.0,
  val velocityMs: Double = 0.0,
  val gForce: Double = 1.0,
  val maxAltitudeMeters: Double = 0.0,
  val maxVelocityMs: Double = 0.0,
  val flightTimeSeconds: Double = 0.0,
  val burritoFuelRemaining: Float = 100f,
  val stage: FlightStage = FlightStage.LAUNCHPAD,
  val isFlying: Boolean = false,
  val isOrbitAchieved: Boolean = false,
  val recentMilestone: String = "Engine cold on pad"
)
