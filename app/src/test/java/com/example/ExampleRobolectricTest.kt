package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BurritoType
import com.example.model.OperatingMode
import com.example.model.PressureLevel
import com.example.model.PropulsionPhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Joe's Rocket Lab", appName)
  }

  @Test
  fun `verify thrust increases with burrito count and spicy propellant`() {
    val mildThrust = PropulsionPhysics.calculateThrust(
      burritoCount = 2,
      burritoType = BurritoType.BEAN_AND_CHEESE,
      internalPressurePsi = 200f,
      baseExhaustVelocityMs = 1500.0,
      mode = OperatingMode.IDLE
    )

    val atomicThrust = PropulsionPhysics.calculateThrust(
      burritoCount = 10,
      burritoType = BurritoType.CHIMICHANGA_CATACLYSM,
      internalPressurePsi = 1000f,
      baseExhaustVelocityMs = 3000.0,
      mode = OperatingMode.FULL_MONGO_LAUNCH
    )

    assertTrue("Cataclysm thrust must significantly exceed mild thrust", atomicThrust.thrustNewtons > mildThrust.thrustNewtons * 5)
    assertTrue("TWR should be greater than 1.0 for liftoff", atomicThrust.thrustToWeightRatio > 1.0)
    assertTrue("Mullet flap frequency should scale with speed and pressure", atomicThrust.mulletFlapFrequencyHz > mildThrust.mulletFlapFrequencyHz)
  }

  @Test
  fun `verify pressure levels trigger overload and nasa lost control thresholds`() {
    assertEquals(PressureLevel.SAFE, PropulsionPhysics.evaluatePressureLevel(250f))
    assertEquals(PressureLevel.ELEVATED, PropulsionPhysics.evaluatePressureLevel(500f))
    assertEquals(PressureLevel.METHANE_OVERLOAD, PropulsionPhysics.evaluatePressureLevel(850f))
    assertEquals(PressureLevel.MAXIMUM_NASA_LOST_CONTROL, PropulsionPhysics.evaluatePressureLevel(1250f))
  }

  @Test
  fun `verify operating modes have ascending thrust multipliers`() {
    assertTrue(OperatingMode.FULL_MONGO_LAUNCH.thrustMultiplier > OperatingMode.TACO_BELL_AFTERBURNER.thrustMultiplier)
    assertTrue(OperatingMode.TACO_BELL_AFTERBURNER.thrustMultiplier > OperatingMode.IDLE.thrustMultiplier)
  }
}
