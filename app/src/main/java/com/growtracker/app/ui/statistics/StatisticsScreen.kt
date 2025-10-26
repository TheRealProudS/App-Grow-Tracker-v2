@file:OptIn(ExperimentalMaterial3Api::class)

package com.growtracker.app.ui.statistics

import com.growtracker.app.data.EntryType
import com.growtracker.app.data.GrowDataManager
import com.growtracker.app.data.Growbox
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString
import kotlin.math.roundToInt
import com.growtracker.app.ui.ai.AnalyzerHolder
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.growtracker.app.data.history.ScanHistoryRepository
import kotlinx.serialization.decodeFromString
import com.growtracker.app.data.history.ScanRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val dataManager = remember { GrowDataManager(context) }
    val growboxes = remember { dataManager.loadGrowboxes() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        if (growboxes.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = getString(Strings.statistics_empty_title, languageManager),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = getString(Strings.statistics_empty_subtitle, languageManager),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(growboxes, key = { it.id }) { growbox ->
                    GrowboxStatisticsCard(growbox = growbox, languageManager = languageManager)
                }

                // Overall statistics
                item {
                    OverallStatisticsCard(
                        growboxes = growboxes,
                        languageManager = languageManager
                    )
                }
                // Recent Scan History (captured analyses)
                item {
                    RecentScanHistoryCard()
                }
            }
        }
    }
}

@Composable
fun GrowboxStatisticsCard(
    growbox: Growbox,
    languageManager: LanguageManager
) {
    val waterStats = calculateWaterConsumption(growbox)
    val fertilizerStats = calculateFertilizerConsumption(growbox)
    val powerStats = calculatePowerConsumption(growbox)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = growbox.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                    color = if (growbox.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (growbox.isActive) getString(Strings.grow_details_active, languageManager) else getString(Strings.grow_details_inactive, languageManager),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (growbox.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatisticCard(
                    title = getString(Strings.statistics_water_consumption, languageManager),
                    value = "${waterStats.roundToInt()} L",
                    icon = Icons.Filled.Water,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatisticCard(
                    title = getString(Strings.statistics_fertilizer, languageManager),
                    value = "${fertilizerStats.roundToInt()} ml",
                    icon = Icons.Filled.Science,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                StatisticCard(
                    title = getString(Strings.statistics_power_consumption, languageManager),
                    value = "${String.format("%.1f", powerStats.totalKwh)} kWh",
                    icon = Icons.Filled.ElectricalServices,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                StatisticCard(
                    title = getString(Strings.statistics_total_cost, languageManager),
                    value = "${String.format("%.2f", powerStats.totalCost)} €",
                    icon = Icons.Filled.Euro,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Power Settings Info
            PowerSettingsInfo(
                growbox = growbox,
                languageManager = languageManager
            )
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PowerSettingsInfo(
    growbox: Growbox,
    languageManager: LanguageManager
) {
    val lightingSettings = growbox.lightingSettings
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getString(Strings.statistics_power_settings, languageManager),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = getString(Strings.power_label_light_cycle, languageManager),
                    value = lightingSettings.lightSchedule.displayName
                )
                InfoItem(
                    label = getString(Strings.power_label_power, languageManager),
                    value = "${lightingSettings.powerLevel}%"
                )
                InfoItem(
                    label = getString(Strings.power_label_price, languageManager),
                    value = "${String.format("%.2f", lightingSettings.electricityPrice)} €/kWh"
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OverallStatisticsCard(
    growboxes: List<Growbox>,
    languageManager: LanguageManager
) {
    val scope = rememberCoroutineScope()
    var modelHash by remember { mutableStateOf<String?>(null) }
    var integrityOk by remember { mutableStateOf<Boolean?>(null) }
    var mismatch by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        scope.launch {
            AnalyzerHolder.get()?.let { analyzer ->
                // Access directly (analyzer handles lazy load internally when first used)
                modelHash = analyzer.modelRuntimeHash()
                integrityOk = analyzer.modelIntegrityVerified()
                mismatch = analyzer.modelIntegrityMismatch()
            }
        }
    }
    val totalWater = growboxes.sumOf { calculateWaterConsumption(it) }
    val totalFertilizer = growboxes.sumOf { calculateFertilizerConsumption(it) }
    val totalPowerStats = growboxes.map { calculatePowerConsumption(it) }
        .fold(PowerConsumption(0.0, 0.0)) { acc, stats ->
            PowerConsumption(acc.totalKwh + stats.totalKwh, acc.totalCost + stats.totalCost)
        }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getString(Strings.overall_title, languageManager),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverallStatItem(
                    label = getString(Strings.overall_water_total, languageManager),
                    value = "${totalWater.roundToInt()} L",
                    icon = Icons.Filled.Water,
                    modifier = Modifier.weight(1f)
                )
                OverallStatItem(
                    label = getString(Strings.overall_fertilizer_total, languageManager),
                    value = "${totalFertilizer.roundToInt()} ml",
                    icon = Icons.Filled.Science,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverallStatItem(
                    label = getString(Strings.overall_power_total, languageManager),
                    value = "${String.format("%.1f", totalPowerStats.totalKwh)} kWh",
                    icon = Icons.Filled.ElectricalServices,
                    modifier = Modifier.weight(1f)
                )
                OverallStatItem(
                    label = getString(Strings.overall_cost_total, languageManager),
                    value = "${String.format("%.2f", totalPowerStats.totalCost)} €",
                    icon = Icons.Filled.Euro,
                    modifier = Modifier.weight(1f)
                )
            }

            // Model Integrity Section (only if hash known or state resolved)
            ModelIntegrityStatus(modelHash = modelHash, integrityOk = integrityOk, mismatch = mismatch)
        }
    }
}

@Composable
private fun ModelIntegrityStatus(modelHash: String?, integrityOk: Boolean?, mismatch: Boolean, languageManager: LanguageManager = LanguageManager()) {
    if (modelHash == null && integrityOk == null) return
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = getString(Strings.model_integrity_title, languageManager),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    val statusText = when {
        mismatch -> getString(Strings.model_integrity_warning_mismatch, languageManager)
        integrityOk == true -> getString(Strings.model_integrity_verified, languageManager)
        integrityOk == false -> getString(Strings.model_integrity_unverified, languageManager)
        else -> getString(Strings.model_integrity_unknown, languageManager)
    }
    val statusColor = when {
        mismatch -> MaterialTheme.colorScheme.error
        integrityOk == true -> MaterialTheme.colorScheme.primary
        integrityOk == false -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = "${getString(Strings.model_integrity_status_label, languageManager)} $statusText",
        style = MaterialTheme.typography.bodyMedium,
        color = statusColor
    )
    if (modelHash != null) {
        Text(
            text = "${getString(Strings.model_integrity_sha_prefix, languageManager)} ${modelHash.take(12)}…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OverallStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Calculation functions
private fun calculateWaterConsumption(growbox: Growbox): Double {
    return growbox.plants.sumOf { plant ->
        plant.entries.filter { it.type == EntryType.WATERING }
            .sumOf { entry ->
                // Extract water amount from entry value (assume format like "2L" or "2000ml")
                val value = entry.value.replace("L", "", ignoreCase = true)
                    .replace("ML", "", ignoreCase = true).trim()
                value.toDoubleOrNull() ?: 0.0
            }
    }
}

private fun calculateFertilizerConsumption(growbox: Growbox): Double {
    return growbox.plants.sumOf { plant ->
        plant.entries.filter { it.type == EntryType.FERTILIZING }
            .sumOf { entry ->
                entry.fertilizerEntries.sumOf { fertEntry ->
                    val dosage = fertEntry.dosage.replace("ml/L", "", ignoreCase = true).replace("ml", "", ignoreCase = true).trim()
                    val waterAmount = extractWaterAmount(entry.value)
                    (dosage.toDoubleOrNull() ?: 0.0) * waterAmount
                }
            }
    }
}

private fun extractWaterAmount(waterValue: String): Double {
    val value = waterValue.replace("L", "", ignoreCase = true)
        .replace("ML", "", ignoreCase = true).trim()
    return value.toDoubleOrNull() ?: 1.0 // Default to 1L if parsing fails
}

data class PowerConsumption(
    val totalKwh: Double,
    val totalCost: Double
)

private fun calculatePowerConsumption(growbox: Growbox): PowerConsumption {
    if (!growbox.isActive || growbox.plants.isEmpty()) {
        return PowerConsumption(0.0, 0.0)
    }
    
    val lightWattage = growbox.lightPower.replace("W", "", ignoreCase = true).trim().toIntOrNull() ?: 0
    val daysActive = calculateActiveDays(growbox)
    val lightingSettings = growbox.lightingSettings
    
    val dailyKwh = lightingSettings.getDailyKwh(lightWattage)
    val dailyCost = lightingSettings.getDailyCost(lightWattage)
    
    val totalKwh = dailyKwh * daysActive
    val totalCost = dailyCost * daysActive
    
    return PowerConsumption(totalKwh, totalCost)
}

private fun calculateActiveDays(growbox: Growbox): Int {
    if (growbox.plants.isEmpty()) return 0
    
    val oldestPlantDate = growbox.plants.minOfOrNull { it.plantingDate } ?: System.currentTimeMillis()
    val daysSincePlanting = ((System.currentTimeMillis() - oldestPlantDate) / (1000 * 60 * 60 * 24)).toInt()
    return maxOf(1, daysSincePlanting) // At least 1 day
}

@Composable
private fun RecentScanHistoryCard(languageManager: LanguageManager = LanguageManager()) {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<ScanRecord>>(emptyList()) }
    LaunchedEffect(Unit) {
        records = loadRecentScanHistory(context, maxItems = 10)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(getString(Strings.recent_scans_title, languageManager), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (records.isEmpty()) {
                Text(getString(Strings.recent_scans_empty, languageManager), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                records.forEach { r ->
                    RecentScanRow(r, languageManager)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    }
}

@Composable
private fun RecentScanRow(r: ScanRecord, languageManager: LanguageManager) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(r.label ?: "—", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            val meta = listOfNotNull(
                r.pipelineMode?.let { "${getString(Strings.recent_scans_mode_prefix, languageManager)} $it" },
                r.stage0Probability?.let { "${getString(Strings.recent_scans_filter_prefix, languageManager)}${String.format("%.2f", it)}" }
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(r.confidence?.let { "${(it * 100).toInt()}%" } ?: "", style = MaterialTheme.typography.labelMedium)
    }
}

private fun loadRecentScanHistory(context: android.content.Context, maxItems: Int): List<ScanRecord> {
    // Prefer encrypted records; repository handles legacy fallback automatically
    return ScanHistoryRepository.loadRecent(context, maxItems)
}
