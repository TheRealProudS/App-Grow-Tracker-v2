package com.growtracker.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LightingSettingsTest {
    @Test
    fun dailyKwh_atDifferentPowerLevels() {
        val settings = LightingSettings(
            lightSchedule = LightSchedule.VEGETATIVE,
            powerLevel = 100,
            electricityPrice = 0.30,
            dailyOperatingHours = 18
        )
        // 100% of 200W for 18h => 3.6 kWh
        assertEquals(3.6, settings.getDailyKwh(200), 1e-6)

        val half = settings.copy(powerLevel = 50)
        // 50% of 200W => 100W for 18h => 1.8 kWh
        assertEquals(1.8, half.getDailyKwh(200), 1e-6)

        val off = settings.copy(powerLevel = 0)
        assertEquals(0.0, off.getDailyKwh(200), 1e-6)
    }

    @Test
    fun dailyCost_scalesWithPrice() {
        val settings = LightingSettings(
            lightSchedule = LightSchedule.FLOWERING,
            powerLevel = 75,
            electricityPrice = 0.50,
            dailyOperatingHours = 12
        )
        // 75% of 400W = 300W, 12h => 3.6 kWh; cost = 3.6 * 0.5 = 1.8
        assertEquals(1.8, settings.getDailyCost(400), 1e-6)
    }
}