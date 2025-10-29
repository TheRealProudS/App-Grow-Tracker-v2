package com.growtracker.app.ui.grow

import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantType
import org.junit.Assert.*
import org.junit.Test

class PlantUiHelpersTest {

    @Test
    fun dailyLightMinutes_handlesNormalAndCrossMidnight() {
        assertEquals(60, dailyLightMinutes(0, 60))        // 00:00 - 01:00
        assertEquals(60, dailyLightMinutes(23*60, 60))    // 23:00 - 01:00 (cross midnight)
        assertEquals(18*60, dailyLightMinutes(6*60, 0))   // 06:00 -> 00:00 next day = 18h
    }

    @Test
    fun expectedBloomDays_byType() {
        assertEquals(70, expectedBloomDays(Plant(type = PlantType.AUTOFLOWER)))
        assertEquals(63, expectedBloomDays(Plant(type = PlantType.FEMINIZED_SATIVA)))
        assertEquals(56, expectedBloomDays(Plant(type = PlantType.FEMINIZED_INDICA)))
        assertEquals(56, expectedBloomDays(Plant(type = PlantType.FEMINIZED_HYBRID)))
    }

    @Test
    fun bloomDays_and_daysToHarvest_basic() {
        val tenDaysAgo = System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000
        val plant = Plant(type = PlantType.FEMINIZED_HYBRID, floweringStartDate = tenDaysAgo)
        val bloom = bloomDays(plant)
        assertNotNull(bloom)
        assertTrue(bloom!! in 9..10) // allow 1 day tolerance due to millis truncation
        val remaining = daysToHarvest(plant)
        assertNotNull(remaining)
        assertTrue(remaining!! in 45..47) // 56 - ~10, allow tolerance
    }

    @Test
    fun daysToHarvest_zeroWhenHarvested_orNullBeforeFlower() {
        val harvested = Plant(type = PlantType.FEMINIZED_INDICA, floweringStartDate = null, harvestDate = System.currentTimeMillis())
        assertEquals(0, daysToHarvest(harvested))
        val notStarted = Plant(type = PlantType.AUTOFLOWER, floweringStartDate = null, harvestDate = null)
        assertNull(daysToHarvest(notStarted))
    }

    @Test
    fun derivePhase_transitions() {
        val now = System.currentTimeMillis()
        val unknown = Plant(germinationDate = null)
        assertEquals("Unbekannt", derivePhase(unknown))

        val flowering = Plant(germinationDate = now - 21L*24*60*60*1000, floweringStartDate = now - 2L*24*60*60*1000)
        assertEquals("Blüte", derivePhase(flowering))

        val harvested = flowering.copy(harvestDate = now)
        assertEquals("Ernte", derivePhase(harvested))
    }

    @Test
    fun deriveAgeWeeks_basic() {
        val start = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000 // exactly 14 days => Woche 3
        val plant = Plant(germinationDate = start)
        assertEquals("Woche 3", deriveAgeWeeks(plant))
    }

    @Test
    fun computeElectricityCostsForPlant_returnsNullWhenMissingInputs() {
        val p = Plant()
        assertNull(computeElectricityCostsForPlant(p))
    }

    @Test
    fun computeElectricityCostsForPlant_calculatesExpectedCosts() {
        val plant = Plant(
            lightWatt = 200,
            lightPowerPercent = 50, // 100W effective
            electricityPrice = 0.30,
            lightOnStartMinutes = 6 * 60,
            lightOnEndMinutes = 0 // 06:00 -> 00:00 next day
        )
        val costs = computeElectricityCostsForPlant(plant)!!
        // daily kWh = 100W * 18h / 1000 = 1.8; daily cost = 0.54
        val daily = 1.8 * 0.3
        assertEquals(daily, costs.dailyCost, 1e-6)
        assertEquals(daily * 7.0, costs.weeklyCost, 1e-6)
        assertEquals(daily * 30.0, costs.monthlyCost, 1e-6)
        assertEquals(daily * 365.0, costs.yearlyCost, 1e-6)
    }
}