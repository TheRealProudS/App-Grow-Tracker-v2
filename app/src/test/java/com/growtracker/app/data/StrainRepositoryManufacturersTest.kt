package com.growtracker.app.data

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class StrainRepositoryManufacturersTest {

    @Test
    fun aliasMerges_BarneysFarm_into_BarneysWithApostrophe() {
        // Accessing public manufacturers triggers merge of base + SeedFinderAdditions
        val list = StrainRepository.manufacturers
        val barneys = list.firstOrNull { it.name.equals("Barney's Farm", ignoreCase = true) }
        assertNotNull("Expected Barney's Farm manufacturer present", barneys)

        // From base: Pineapple Chunk
        assertTrue(barneys!!.strains.any { it.name.equals("Pineapple Chunk", ignoreCase = true) })
        // From additions (alias 'Barneys Farm'): Tangerine Dream should have been merged into canonical key
        assertTrue(barneys.strains.any { it.name.equals("Tangerine Dream", ignoreCase = true) })

        // Dedup within same manufacturer: Trainwreck appears in additions and base for Barneys Farm -> should be unique
        val trainwreckCount = barneys.strains.count { it.name.equals("Trainwreck", ignoreCase = true) }
        assertEquals("Duplicate strains should be de-duplicated by name (case-insensitive)", 1, trainwreckCount)
    }

    @Test
    fun aliasMerges_THSeeds_into_T_H_Seeds() {
        val list = StrainRepository.manufacturers
        val thSeeds = list.firstOrNull { it.name.equals("T.H.Seeds", ignoreCase = true) }
        assertNotNull("Expected T.H.Seeds manufacturer present", thSeeds)
        // From additions under alias "TH Seeds"
        assertTrue(thSeeds!!.strains.any { it.name.equals("UltraSour", ignoreCase = true) })
    }

    @Test
    fun curatedAdditions_WhiteLabelSeeds_X_Haze_present() {
        val list = StrainRepository.manufacturers
        val whiteLabel = list.firstOrNull { it.name.equals("White Label Seeds", ignoreCase = true) }
        assertNotNull(whiteLabel)
        assertTrue(whiteLabel!!.strains.any { it.name.equals("X-Haze", ignoreCase = true) })
    }
}