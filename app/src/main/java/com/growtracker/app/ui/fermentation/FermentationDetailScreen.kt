package com.growtracker.app.ui.fermentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.growtracker.app.data.FermentationEntry
import com.growtracker.app.data.FermentationEntryType
import com.growtracker.app.data.FermentationMethod
import com.growtracker.app.data.GrowDataManager
import com.growtracker.app.data.Plant
import com.growtracker.app.ui.language.LanguageManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FermentationDetailScreen(
    plant: Plant,
    languageManager: LanguageManager,
    onNavigateUp: () -> Unit = {},
    onUpdatePlant: (Plant) -> Unit = {}
) {
    val context = LocalContext.current
    val dataManager: GrowDataManager = remember { GrowDataManager(context) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var weekOffset by remember { mutableStateOf(0) }
    
    // Calculate fermentation data
    val fermentationStartDate = plant.fermentationStartDate ?: System.currentTimeMillis()
    val currentTime = System.currentTimeMillis()
    val daysSinceFermentationStart = ((currentTime - fermentationStartDate) / (1000 * 60 * 60 * 24)).toInt()
    
    // Get optimal ventilation schedule based on method
    val ventilationSchedule = getOptimalVentilationSchedule(plant.fermentationMethod, daysSinceFermentationStart)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = plant.strain,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fermentierung - ${plant.fermentationMethod.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                                    Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Einstellungen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
                FloatingActionButton(
                onClick = { showAddEntryDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Eintrag hinzufügen"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Fermentation Status Card
            item {
                FermentationStatusCard(
                    plant = plant,
                    daysSinceFermentationStart = daysSinceFermentationStart
                )
            }
            
            // Weekly Calendar (supports week navigation and selecting a day)
            item {
                WeeklyCalendarCard(
                    fermentationStartDate = fermentationStartDate,
                    ventilationSchedule = ventilationSchedule,
                    fermentationEntries = plant.fermentationEntries,
                    weekOffset = weekOffset,
                    selectedDate = selectedDate,
                    onSelectDate = { date ->
                        selectedDate = date
                        showAddEntryDialog = true
                    },
                    onWeekChange = { delta -> weekOffset += delta }
                )
            }
            // Recent Entries
            item {
                Text(
                    text = "Fermentierungseinträge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (plant.fermentationEntries.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Noch keine Einträge",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Column {
                        plant.fermentationEntries.sortedByDescending { it.date }.forEach { entry ->
                            FermentationEntryCard(entry = entry)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        FermentationSettingsDialog(
            plant = plant,
            onDismiss = { showSettingsDialog = false },
            onUpdatePlant = onUpdatePlant
        )
    }
    
    // Add Entry Dialog
    if (showAddEntryDialog) {
        AddFermentationEntryDialog(
            onDismiss = { showAddEntryDialog = false },
            initialDate = selectedDate,
            onAddEntry = { entry ->
                val updatedEntries = plant.fermentationEntries + entry
                val updatedPlant = plant.copy(fermentationEntries = updatedEntries)
                onUpdatePlant(updatedPlant)
                showAddEntryDialog = false
                selectedDate = null
            }
        )
    }
}

}

// Helper composables and functions are implemented in FermentationScreen.kt to avoid duplication.

@Composable
fun FermentationStatusCard(
    plant: Plant,
    daysSinceFermentationStart: Int
) {
    val optimalFermentationDays = when (plant.fermentationMethod) {
        FermentationMethod.MASON_JAR -> 28 // 4 weeks
        FermentationMethod.HUMIDOR -> 42 // 6 weeks
        FermentationMethod.TERPLOC_BAG -> 21 // 3 weeks
        FermentationMethod.VACUUM_CONTAINER -> 35 // 5 weeks
    }
    
    val progress = (daysSinceFermentationStart.toFloat() / optimalFermentationDays).coerceAtMost(1f)
    val remainingDays = maxOf(0, optimalFermentationDays - daysSinceFermentationStart)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fermentierungsfortschritt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Tag $daysSinceFermentationStart von $optimalFermentationDays",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = when {
                        progress >= 1f -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        progress >= 0.5f -> Color(0xFF2196F3).copy(alpha = 0.2f)
                        else -> Color(0xFF795548).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            progress >= 1f -> Color(0xFF4CAF50)
                            progress >= 0.5f -> Color(0xFF2196F3)
                            else -> Color(0xFF795548)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    progress >= 1f -> Color(0xFF4CAF50)
                    progress >= 0.5f -> Color(0xFF2196F3)
                    else -> Color(0xFF795548)
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (remainingDays > 0) {
                Text(
                    text = "Noch $remainingDays Tage bis zur optimalen Fermentierung",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Optimale Fermentierungszeit erreicht! 🎉",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WeeklyCalendarCard(
    fermentationStartDate: Long,
    ventilationSchedule: List<Boolean>,
    fermentationEntries: List<FermentationEntry>
    , weekOffset: Int = 0
    , selectedDate: Long? = null
    , onSelectDate: (Long) -> Unit = {}
    , onWeekChange: (Int) -> Unit = {}
) {
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE\ndd.MM", Locale.GERMAN)
    
    // Calculate current week based on fermentation start
    val weekStart = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        add(Calendar.WEEK_OF_YEAR, weekOffset)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onWeekChange(-1) }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Vorwoche")
                    }
                    Text(
                        text = "Diese Woche",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Next week button - will be disabled if moving past allowed future window
                    val todayCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    val lastAllowed = todayCal.timeInMillis + 7L * 24 * 60 * 60 * 1000
                    val isNextDisabled = weekStart.timeInMillis > lastAllowed
                    IconButton(onClick = { if (!isNextDisabled) onWeekChange(1) }, enabled = !isNextDisabled) {
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Nächste Woche")
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Belüftung empfohlen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(7) { dayIndex ->
                    val dayCalendar = Calendar.getInstance().apply {
                        timeInMillis = weekStart.timeInMillis
                        add(Calendar.DAY_OF_WEEK, dayIndex)
                    }
                    
                    val isToday = dayCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                            dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    
                    val dayOfFermentation = ((dayCalendar.timeInMillis - fermentationStartDate) / (1000 * 60 * 60 * 24)).toInt()
                    val shouldVentilate = dayOfFermentation >= 0 && dayOfFermentation < ventilationSchedule.size && ventilationSchedule[dayOfFermentation]
                    
                    val hasVentilationEntry = fermentationEntries.any { entry ->
                        val entryCalendar = Calendar.getInstance().apply { timeInMillis = entry.date }
                        entryCalendar.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR) &&
                                entryCalendar.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
                                entry.type == FermentationEntryType.VENTILATION
                    }
                    
                    CalendarDayCard(
                        date = dayCalendar.timeInMillis,
                        isToday = isToday,
                        shouldVentilate = shouldVentilate,
                        hasVentilationEntry = hasVentilationEntry,
                        dateFormat = dateFormat,
                        isSelected = selectedDate != null && selectedDate == dayCalendar.timeInMillis,
                        onSelect = { onSelectDate(dayCalendar.timeInMillis) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayCard(
    date: Long,
    isToday: Boolean,
    shouldVentilate: Boolean,
    hasVentilationEntry: Boolean,
    dateFormat: SimpleDateFormat
    , isSelected: Boolean = false
    , onSelect: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .size(width = 80.dp, height = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                hasVentilationEntry -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                shouldVentilate -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // Allow selecting days up to 7 days in the future (from today)
                    val todayCal = Calendar.getInstance()
                    todayCal.set(Calendar.HOUR_OF_DAY, 0); todayCal.set(Calendar.MINUTE, 0); todayCal.set(Calendar.SECOND, 0); todayCal.set(Calendar.MILLISECOND, 0)
                    val todayStart = todayCal.timeInMillis
                    val maxAllowed = todayStart + 7L * 24 * 60 * 60 * 1000
                    if (date <= maxAllowed) onSelect()
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(Date(date)),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onSecondary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (shouldVentilate || hasVentilationEntry) {
                    Icon(
                        imageVector = if (hasVentilationEntry) Icons.Filled.CheckCircle else Icons.Filled.Air,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (hasVentilationEntry) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary
                    )
            }
        }
    }
}

@Composable
fun FermentationEntryCard(entry: FermentationEntry) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = when (entry.type) {
                    FermentationEntryType.VENTILATION -> Color(0xFF2196F3).copy(alpha = 0.2f)
                    FermentationEntryType.NOTE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    FermentationEntryType.TASK -> Color(0xFFFF9800).copy(alpha = 0.2f)
                    FermentationEntryType.HUMIDITY_CHECK -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                    FermentationEntryType.TEMPERATURE_CHECK -> Color(0xFFF44336).copy(alpha = 0.2f)
                    else -> Color.Gray
                }
            ) {
            Icon(
            imageVector = when (entry.type) {
        FermentationEntryType.VENTILATION -> Icons.Filled.AcUnit
                FermentationEntryType.NOTE -> Icons.Filled.Note
                FermentationEntryType.TASK -> Icons.Filled.Task
                FermentationEntryType.HUMIDITY_CHECK -> Icons.Filled.WaterDrop
                FermentationEntryType.TEMPERATURE_CHECK -> Icons.Filled.Thermostat
                else -> Icons.Filled.Help
            },
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(10.dp),
                    tint = when (entry.type) {
                        FermentationEntryType.VENTILATION -> Color(0xFF2196F3)
                        FermentationEntryType.NOTE -> MaterialTheme.colorScheme.primary
                        FermentationEntryType.TASK -> Color(0xFFFF9800)
                        FermentationEntryType.HUMIDITY_CHECK -> Color(0xFF9C27B0)
                        FermentationEntryType.TEMPERATURE_CHECK -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.type.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = dateFormat.format(Date(entry.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (entry.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (entry.humidity.isNotEmpty() || entry.temperature.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        if (entry.humidity.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "💧 ${entry.humidity}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        if (entry.temperature.isNotEmpty()) {
                            if (entry.humidity.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "🌡️ ${entry.temperature}°C",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Optimal ventilation schedule based on fermentation method
fun getOptimalVentilationSchedule(method: FermentationMethod, currentDay: Int): List<Boolean> {
    return when (method) {
        FermentationMethod.MASON_JAR -> {
            // Daily for first week, then every 2-3 days
            (0..41).map { day ->
                when {
                    day <= 7 -> true // Daily first week
                    day <= 21 -> day % 2 == 0 // Every 2 days weeks 2-3
                    else -> day % 3 == 0 // Every 3 days weeks 4-6
                }
            }
        }
        FermentationMethod.HUMIDOR -> {
            // Less frequent due to better humidity control
            (0..41).map { day ->
                when {
                    day <= 7 -> day % 2 == 0 // Every 2 days first week
                    day <= 21 -> day % 3 == 0 // Every 3 days weeks 2-3
                    else -> day % 5 == 0 // Every 5 days weeks 4-6
                }
            }
        }
        FermentationMethod.TERPLOC_BAG -> {
            // Less ventilation needed, designed to preserve terpenes
            (0..27).map { day ->
                when {
                    day <= 7 -> day % 3 == 0 // Every 3 days first week
                    day <= 14 -> day % 4 == 0 // Every 4 days week 2
                    else -> day % 7 == 0 // Weekly weeks 3-4
                }
            }
        }
        FermentationMethod.VACUUM_CONTAINER -> {
            // Minimal ventilation due to controlled environment
            (0..34).map { day ->
                when {
                    day <= 7 -> day % 2 == 0 // Every 2 days first week
                    day <= 21 -> day % 4 == 0 // Every 4 days weeks 2-3
                    else -> day % 7 == 0 // Every 7 days weeks 4-5
                }
            }
        }
        else -> emptyList()
    }
}

