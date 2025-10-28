@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.growtracker.app.ui.grow

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.growtracker.app.data.Plant
import com.growtracker.app.data.ElectricityCosts

@Composable
fun PhasePill(phase: String, age: String) {
    Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.FilterVintage, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(phase, style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun deriveAgeWeeks(plant: Plant): String {
    val start = plant.germinationDate ?: plant.plantingDate
    if (start <= 0L) return "Woche 1"
    val days = ((System.currentTimeMillis() - start) / (1000L * 60 * 60 * 24)).toInt()
    return "Woche ${days / 7 + 1}"
}

fun derivePhase(plant: Plant): String = when {
    plant.harvestDate != null -> "Ernte"
    plant.floweringStartDate != null && System.currentTimeMillis() >= plant.floweringStartDate!! -> "Blüte"
    plant.germinationDate == null -> "Unbekannt"
    else -> {
        val weeks = ((System.currentTimeMillis() - (plant.germinationDate ?: System.currentTimeMillis())) / (1000L * 60 * 60 * 24 * 7)).toInt()
        when {
            weeks < 1 -> "Keimung"
            weeks < 3 -> "Sämling"
            weeks < 6 -> "Wachstum"
            else -> "Reif"
        }
    }
}

/**
 * Returns number of days since flowering started, or null if not started.
 */
fun bloomDays(plant: Plant): Int? = plant.floweringStartDate?.let {
    (((System.currentTimeMillis() - it) / (1000L * 60 * 60 * 24))).toInt().coerceAtLeast(0)
}

/**
 * Heuristic expected bloom duration in days based on plant type.
 * Defaults are conservative and can be refined later or made configurable.
 */
fun expectedBloomDays(plant: Plant): Int = when (plant.type) {
    com.growtracker.app.data.PlantType.AUTOFLOWER -> 70
    com.growtracker.app.data.PlantType.FEMINIZED_SATIVA -> 63
    com.growtracker.app.data.PlantType.FEMINIZED_INDICA -> 56
    com.growtracker.app.data.PlantType.FEMINIZED_HYBRID -> 56
}

/**
 * Estimated remaining days to harvest relative to expected bloom duration.
 * Returns null if flowering hasn't started yet or harvest already set.
 */
fun daysToHarvest(plant: Plant): Int? {
    if (plant.harvestDate != null) return 0
    val started = bloomDays(plant) ?: return null
    val rem = expectedBloomDays(plant) - started
    return rem.coerceAtLeast(0)
}

/**
 * Calculate minutes of light per day based on a start and end minute-of-day window.
 * Supports windows that cross midnight (e.g., 18:00 -> 12:00 next day).
 */
fun dailyLightMinutes(startMinutes: Int?, endMinutes: Int?): Int? {
    if (startMinutes == null || endMinutes == null) return null
    val s = startMinutes.coerceIn(0, 23 * 60 + 59)
    val e = endMinutes.coerceIn(0, 23 * 60 + 59)
    return if (e >= s) e - s else (24 * 60 - s + e)
}

/**
 * Compute electricity consumption and costs for a plant if settings are present.
 * Returns null if required inputs are missing (wattage, price, light window).
 */
fun computeElectricityCostsForPlant(plant: Plant): ElectricityCosts? {
    val wattBase = plant.lightWatt ?: return null
    val price = plant.electricityPrice ?: return null
    val minutes = dailyLightMinutes(plant.lightOnStartMinutes, plant.lightOnEndMinutes) ?: return null
    val hours = minutes / 60.0
    val percent = (plant.lightPowerPercent ?: 100).coerceIn(0, 100)
    val effectiveWatt = wattBase * (percent / 100.0)
    val dailyKwh = (effectiveWatt * hours) / 1000.0
    val dailyCost = dailyKwh * price
    return ElectricityCosts(
        dailyCost = dailyCost,
        weeklyCost = dailyCost * 7.0,
        monthlyCost = dailyCost * 30.0,
        yearlyCost = dailyCost * 365.0
    )
}
