package com.growtracker.app.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.growtracker.app.data.EntryType
import com.growtracker.app.data.FertilizerEntry
import com.growtracker.app.data.Plant
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString
import com.growtracker.app.data.PowerDevice
import com.growtracker.app.data.DeviceScheduleType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantStatisticsScreen(
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit
) {
    val plants = remember { GrowDataStore.plants }
    // Shared per-second ticker for the whole screen to avoid multiple concurrent loops
    val nowTick = rememberNowTicker()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = getString(Strings.statistics_title, languageManager),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            }
        )

        if (plants.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants, key = { it.id }) { plant ->
                    PlantStatsCard(plant = plant, languageManager = languageManager, nowTick = nowTick)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Analytics,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keine Pflanzen gefunden",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Lege zuerst eine Pflanze an, um Statistiken zu sehen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlantStatsCard(plant: Plant, languageManager: LanguageManager, nowTick: Long) {
    val waterLiters = plant.entries
        .filter { it.type == EntryType.WATERING }
        .sumOf { it.value.trim().lowercase(Locale.getDefault()).replace("ml", "").replace("l", "").toDoubleOrNull() ?: 0.0 }

    // Fertilizer totals per product. Strategy:
    // 1) Prefer structured fertilizerEntries if present.
    // 2) Otherwise, parse product + dosage from entry.value (format: "Product - X ml/l").
    // 3) Multiply dosage (ml/l) by watering liters on the same day (if available); fallback 1L.
    val entriesByDay: Map<Long, List<com.growtracker.app.data.PlantEntry>> = plant.entries.groupBy { normalizeToDay(it.date) }
    val fertTotals: Map<String, Double> = plant.entries
        .filter { it.type == EntryType.FERTILIZING }
        .flatMap { entry ->
            val day = normalizeToDay(entry.date)
            val wateringLitersSameDay = entriesByDay[day]
                ?.filter { it.type == EntryType.WATERING }
                ?.sumOf { it.value.trim().lowercase(Locale.getDefault()).replace("ml", "").replace("l", "").toDoubleOrNull() ?: 0.0 }
                ?: 1.0

            if (entry.fertilizerEntries.isNotEmpty()) {
                entry.fertilizerEntries.map { f -> f.product.name to ((f.dosage.extractMlPerL()) * wateringLitersSameDay) }
            } else {
                // Parse from value string: "Product - X ml/l"
                val parsed = parseFertilizerFromValue(entry.value)
                if (parsed != null) listOf(parsed.first to (parsed.second * wateringLitersSameDay)) else emptyList()
            }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, amounts) -> amounts.sum() }

    // Height points over time for a simple sparkline (date to height cm)
    val heightPoints = plant.entries
        .filter { it.type == EntryType.HEIGHT }
        .sortedBy { it.date }
        .mapNotNull { e -> e.value.toDoubleOrNull()?.let { e.date to it } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(plant.name.ifBlank { plant.strain.ifBlank { "Pflanze" } }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallStat(title = "Wasser gesamt", value = String.format(Locale.getDefault(), "%.1f L", waterLiters), icon = Icons.Filled.Water, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                SmallStat(title = "Dünger gesamt", value = String.format(Locale.getDefault(), "%.0f ml", fertTotals.values.sum()), icon = Icons.Filled.Science, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            }

            if (fertTotals.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Düngerverteilung", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                fertTotals.toList().sortedByDescending { it.second }.forEach { (product, ml) ->
                    LinearProgressRow(label = product, amount = ml)
                }
            }

            if (heightPoints.size >= 2) {
                Spacer(Modifier.height(12.dp))
                Text("Wachstum (cm)", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                HeightSparkline(points = heightPoints)
            }

            // Energy statistics section (always visible, live updates)
            Spacer(Modifier.height(12.dp))
            EnergyStatCard(plant = plant, languageManager = languageManager, nowTick = nowTick)
        }
    }
}

@Composable
private fun EnergyStatCard(plant: Plant, languageManager: LanguageManager, nowTick: Long) {
    val price = plant.electricityPrice
    val startBase = plant.germinationDate ?: plant.plantingDate
    val now = nowTick
    val ts = plant.harvestDate?.let { kotlin.math.min(now, it) } ?: now
    if (ts < startBase) return
    // Daily kWh from devices only
    var dailyKwh = 0.0
    plant.devices.forEach { dev ->
        val w = dev.watt ?: return@forEach
        val eff = w * ((dev.powerPercent ?: 100).coerceIn(0, 100) / 100.0)
        val dSec = deviceDailySeconds(dev)
        dailyKwh += (eff * (dSec / 3600.0)) / 1000.0
    }
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = ts }
    val secondOfDay = cal.get(java.util.Calendar.HOUR_OF_DAY) * 3600 + cal.get(java.util.Calendar.MINUTE) * 60 + cal.get(java.util.Calendar.SECOND)
    var kwhSoFar = 0.0
    plant.devices.forEach { dev ->
        val w = dev.watt ?: return@forEach
        val eff = w * ((dev.powerPercent ?: 100).coerceIn(0, 100) / 100.0)
        val onSoFarSec = deviceSecondsSoFar(dev, secondOfDay)
        val hoursSoFar = onSoFarSec / 3600.0
        kwhSoFar += (eff * hoursSoFar) / 1000.0
    }
    val costSoFar = if (price != null) kwhSoFar * price else 0.0

    val daysActive = fullDaysBetween(startBase, ts)
    val totalKwh = dailyKwh * daysActive + kwhSoFar
    val totalCost = if (price != null) totalKwh * price else 0.0

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(getString(Strings.power_stats_title, languageManager), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                LiveIndicator(nowTick = nowTick)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${String.format("%.2f", kwhSoFar)} kWh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(getString(Strings.power_stats_today_so_far, languageManager), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.2f", costSoFar)} €", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${String.format("%.2f", totalKwh)} kWh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(getString(Strings.power_stats_total_to_date, languageManager), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.2f", totalCost)} €", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("${getString(Strings.power_stats_daily_usage, languageManager)}: ${String.format("%.2f", dailyKwh)} kWh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun rememberNowTicker(periodMs: Long = 1000L): Long {
    val state = remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(periodMs) {
        while (true) {
            kotlinx.coroutines.delay(periodMs)
            state.value = System.currentTimeMillis()
        }
    }
    return state.value
}

@Composable
internal fun LiveIndicator(nowTick: Long) {
    // Blink every second using the shared ticker
    val on = ((nowTick / 1000L) % 2L) == 0L
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Red dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (on) Color.Red else Color.Red.copy(alpha = 0.25f))
                .testTag("live_dot")
                .semantics { contentDescription = if (on) "live_on" else "live_off" }
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "LIVE",
            modifier = Modifier.testTag("live_text"),
            style = MaterialTheme.typography.labelSmall,
            color = if (on) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun fullDaysBetween(startMillis: Long, endMillis: Long): Int {
    if (endMillis <= startMillis) return 0
    val calStart = java.util.Calendar.getInstance().apply { timeInMillis = startMillis; set(java.util.Calendar.HOUR_OF_DAY,0); set(java.util.Calendar.MINUTE,0); set(java.util.Calendar.SECOND,0); set(java.util.Calendar.MILLISECOND,0) }
    val calEnd = java.util.Calendar.getInstance().apply { timeInMillis = endMillis; set(java.util.Calendar.HOUR_OF_DAY,0); set(java.util.Calendar.MINUTE,0); set(java.util.Calendar.SECOND,0); set(java.util.Calendar.MILLISECOND,0) }
    val diff = calEnd.timeInMillis - calStart.timeInMillis
    return (diff / (24L*60*60*1000L)).toInt().coerceAtLeast(0)
}

private fun secondsOnSoFarToday(secondOfDay: Int, startMin: Int, endMin: Int): Int {
    val startSec = startMin * 60
    val endSec = endMin * 60
    return if (endSec >= startSec) {
        when {
            secondOfDay <= startSec -> 0
            secondOfDay >= endSec -> endSec - startSec
            else -> secondOfDay - startSec
        }
    } else { // crosses midnight
        val part1 = if (secondOfDay >= startSec) secondOfDay - startSec else 0
        val part2 = if (secondOfDay <= endSec) secondOfDay else endSec
        part1 + part2
    }
}

private fun deviceDailySeconds(dev: PowerDevice): Double {
    return when (dev.scheduleType) {
        DeviceScheduleType.ALWAYS_ON -> 24.0 * 3600.0
        DeviceScheduleType.WINDOW ->
            if (dev.startMinutes != null && dev.endMinutes != null)
                secondsOnSoFarToday(24 * 3600, dev.startMinutes, dev.endMinutes).toDouble()
            else 0.0
        DeviceScheduleType.DUTY_CYCLE -> ((dev.dutyCyclePercent ?: 0).coerceIn(0, 100) / 100.0) * 24.0 * 3600.0
    }
}

private fun deviceSecondsSoFar(dev: PowerDevice, secondOfDay: Int): Int {
    return when (dev.scheduleType) {
        DeviceScheduleType.ALWAYS_ON -> secondOfDay
        DeviceScheduleType.WINDOW -> if (dev.startMinutes != null && dev.endMinutes != null)
            secondsOnSoFarToday(secondOfDay, dev.startMinutes, dev.endMinutes) else 0
        DeviceScheduleType.DUTY_CYCLE -> ((dev.dutyCyclePercent ?: 0).coerceIn(0, 100) * secondOfDay) / 100
    }
}

@Composable
private fun SmallStat(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = title, tint = color)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LinearProgressRow(label: String, amount: Double) {
    val pct = 1f // not relative per row; display as a simple bar with label and absolute ml
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(String.format(Locale.getDefault(), "%.0f ml", amount), style = MaterialTheme.typography.labelMedium)
        }
        LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun HeightSparkline(points: List<Pair<Long, Double>>) {
    val sdf = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }
    val min = points.minOfOrNull { it.second } ?: 0.0
    val max = points.maxOfOrNull { it.second } ?: 0.0
    val range = (max - min).takeIf { it > 0 } ?: 1.0

    Column {
        val lineColor = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)) {
            val path = Path()
            val n = points.size
            val stepX = if (n > 1) size.width / (n - 1) else size.width
            points.forEachIndexed { idx, p ->
                val x = stepX * idx
                val y = size.height - ((p.second - min) / range * size.height).toFloat()
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 3f))
        }

        // Labels: start/end dates and min/max values
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(sdf.format(Date(points.first().first)), style = MaterialTheme.typography.bodySmall)
            Text("min ${min.toInt()} cm", style = MaterialTheme.typography.bodySmall)
            Text("max ${max.toInt()} cm", style = MaterialTheme.typography.bodySmall)
            Text(sdf.format(Date(points.last().first)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun String.extractMlPerL(): Double {
    // Accept formats like "2", "2ml", "2 ml", "2ml/l"
    return this.lowercase(Locale.getDefault())
        .replace("ml/l", "")
        .replace("ml", "")
        .trim()
        .toDoubleOrNull() ?: 0.0
}

private fun extractWaterAmount(waterValue: String): Double {
    val v = waterValue.trim().lowercase(Locale.getDefault())
        .replace("ml", "")
        .replace("l", "")
    return v.toDoubleOrNull() ?: 1.0
}

private fun normalizeToDay(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun parseFertilizerFromValue(value: String): Pair<String, Double>? {
    // Expected format: "Product - X ml/l" or "Product X ml/l"
    val lower = value.lowercase(Locale.getDefault())
    val mlIndex = lower.indexOf("ml/l")
    if (mlIndex <= 0) return null
    // find dosage number before ml/l
    val before = lower.substring(0, mlIndex).trim()
    val digits = before.takeLastWhile { it.isDigit() || it == '.' }
    val dosage = digits.toDoubleOrNull() ?: return null
    val product = value.substring(0, lower.indexOf(digits)).trim().trimEnd('-').trim()
    return product to dosage
}
