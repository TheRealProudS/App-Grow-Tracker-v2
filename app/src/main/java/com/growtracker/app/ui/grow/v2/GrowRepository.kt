package com.growtracker.app.ui.grow.v2

import com.growtracker.app.data.Growbox
import com.growtracker.app.data.Plant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Small in-memory repository used by the new Grow v2 screens. Later this can be
 * replaced by a DataStore/Room repository.
 */
object GrowRepository {
    private val now = System.currentTimeMillis()

    private fun daysAgo(days: Long): Long = LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val samplePlants = listOf(
        Plant(id = "p1", name = "Blue Dream", strain = "Blue Dream", manufacturer = "SeedCo", thcContent = "18%", cbdContent = "0.5%", germinationDate = daysAgo(30)),
        Plant(id = "p2", name = "OG Kush", strain = "OG Kush", manufacturer = "SeedCo", thcContent = "22%", cbdContent = "0.3%", germinationDate = daysAgo(90)),
        Plant(id = "p3", name = "NRG Autoflower", strain = "NRG", manufacturer = "AutoSeeds", thcContent = "15%", cbdContent = "1%", germinationDate = daysAgo(7))
    )

    private val sampleGrowboxes = listOf(
        Growbox(id = "g1", name = "Zelt 60x60", isActive = true, plants = listOf(samplePlants[0], samplePlants[2])),
        Growbox(id = "g2", name = "Keller Rack", isActive = false, plants = listOf(samplePlants[1]))
    )

    fun getAllGrowboxes(): List<Growbox> = sampleGrowboxes

    fun getActiveGrowboxes(): List<Growbox> = sampleGrowboxes.filter { it.isActive }

    fun getArchivedGrowboxes(): List<Growbox> = sampleGrowboxes.filter { !it.isActive }
}
