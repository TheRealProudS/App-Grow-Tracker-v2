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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.growtracker.app.data.EntryType
import com.growtracker.app.data.FertilizerEntry
import com.growtracker.app.data.Plant
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantStatisticsScreen(
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit
) {
    val plants = remember { GrowDataStore.plants }

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
                    PlantStatsCard(plant = plant)
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
private fun PlantStatsCard(plant: Plant) {
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
        }
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
