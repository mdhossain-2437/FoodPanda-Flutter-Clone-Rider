package com.example.foodpanda_rider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.foodpanda_rider.model.Helper
import com.example.foodpanda_rider.ui.components.evaluatePasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun appName_matchesMetadata() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FoodPanda Rider", appName)
    }

    @Test
    fun calculateDistance_returnsExpectedKilometers() {
        val d = Helper.calculateDistance(11.5558, 104.9278, 11.5518, 104.9261)
        assertTrue("Expected positive distance in km, got $d", d in 0.3..0.8)
    }

    @Test
    fun passwordStrengthValidator_enforcesAllRules() {
        assertFalse(evaluatePasswordStrength("weak").isValid)
        assertTrue(evaluatePasswordStrength("Rider@123").isValid)
    }
}
