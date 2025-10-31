@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.growtracker.app.ui.grow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
// Removed LazyVerticalGrid to avoid nested lazy measurement issues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.BitmapPainter
import com.growtracker.app.ui.language.getString
import com.growtracker.app.data.EntryType
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantEntry
import com.growtracker.app.data.FertilizerProduct
import com.growtracker.app.data.PowerDevice
import com.growtracker.app.data.DeviceType
import com.growtracker.app.data.DeviceScheduleType
import java.text.SimpleDateFormat
import java.util.*

// Single, clean PlantDetailScreen implementation
@Composable
fun PlantDetailScreen(plant: Plant, onBack: () -> Unit = {}) {
    var settingsOpen by remember { mutableStateOf(false) }
    var addOpen by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    // Initialize selectedDate with today's date (normalized to midnight)
    var selectedDate by remember { 
        mutableStateOf<Long?>(
            Calendar.getInstance().apply { 
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
            }.timeInMillis
        ) 
    }

    Scaffold(
        topBar = {
            // Use default TopAppBar spacing so icons are properly centered
            TopAppBar(
                title = { /* intentionally empty per UI change */ },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück") }
                },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) { Icon(Icons.Filled.Settings, contentDescription = "Einstellungen") }
                }
            )
        },
    floatingActionButton = { ExtendedFloatingActionButton(onClick = { addOpen = true }, icon = { Icon(Icons.Filled.Add, contentDescription = "Eintrag") }, text = { Text("Eintrag") }) }
    ) { inner ->
        LazyColumn(modifier = Modifier.padding(inner).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { PlantInfoCard(plant = plant, isSmallScreen = isSmallScreen) }
            item {
                // Compute approximate harvest date based on first bloom day and expected bloom duration
                val estHarvestMidnight = remember(plant.floweringStartDate, plant.type) {
                    plant.floweringStartDate?.let { start ->
                        val days = expectedBloomDays(plant)
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = start + days.toLong() * 24L * 60 * 60 * 1000
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        cal.timeInMillis
                    }
                }
                SimpleCalendarView(
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it },
                    estimatedHarvestMidnight = estHarvestMidnight,
                    isSmallScreen = isSmallScreen
                )
            }
            item { EntriesSection(entries = plant.entries ?: emptyList(), plantId = plant.id, preferredFertilizerManufacturer = plant.preferredFertilizerManufacturer, selectedDate = selectedDate, isSmallScreen = isSmallScreen) }
            // Add bottom spacer so the FloatingActionButton never covers the last list items
            item { Spacer(modifier = Modifier.height(if (isSmallScreen) 80.dp else 96.dp)) }
        }
    }

    if (settingsOpen) {
        PlantSettingsDialog(
            plant = plant,
            selectedDate = selectedDate,
            onClose = { settingsOpen = false },
            onUpdate = { updated -> GrowDataStore.updatePlant(updated) },
            onDelete = { id -> GrowDataStore.removePlant(id); onBack() }
        )
    }

    if (addOpen) {
        AddEntryDialog(
            plantId = plant.id,
            selectedDate = selectedDate,
            onClose = { addOpen = false },
            preferredManufacturer = plant.preferredFertilizerManufacturer,
            onAdd = { entry ->
                GrowDataStore.addEntryToPlant(plant.id, entry)
                addOpen = false
            },
            isSmallScreen = isSmallScreen
        )
    }
}



@Composable
private fun PlantInfoCard(plant: Plant, isSmallScreen: Boolean = false) {
    val cardPadding = if (isSmallScreen) 6.dp else 8.dp
    val headerPadding = if (isSmallScreen) 8.dp else 10.dp
    
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(cardPadding)) {
            // Compact colored header with strain above the type
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(headerPadding), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Strain displayed prominently but compact
                        if (plant.strain.isNotBlank()) {
                            Text(
                                plant.strain, 
                                style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall, 
                                maxLines = 1
                            )
                        }
                        Text(
                            plant.type.displayName, 
                            style = MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // Right side compact summary
                    // right side intentionally left empty (remove static Tage display)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Precompute remaining days to reuse below
            val remainingDays = daysToHarvest(plant)

            // Chips row with THC, CBD, pot size, optional seedling label, germ day, bloom day
            FlowRow(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp), 
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp)
            ) {
                plant.thcContent.takeIf { it.isNotBlank() }?.let { ChipSmall(label = "THC", value = "$it%") }
                plant.cbdContent.takeIf { it.isNotBlank() }?.let { ChipSmall(label = "CBD", value = "$it%") }

                val potLabel = plant.customPotSize?.takeIf { it.isNotBlank() }?.let {
                    val t = it.trim(); if (t.lowercase().endsWith("l")) t else "$t L"
                } ?: (plant.potSize.displayName ?: "-")
                ChipSmall(label = getString(com.growtracker.app.ui.language.GrowStrings.chip_pot), value = potLabel)

                // Phase chip (only show as distinct chip for Sämling to match overview)
                val phase = derivePhase(plant)
                if (phase == "Sämling") {
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.FilterVintage, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(phase, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Germ day computed from germinationDate if set, otherwise from plantingDate
                val keimBase = plant.germinationDate ?: plant.plantingDate
                val keimDays = ((System.currentTimeMillis() - keimBase) / (24L * 60 * 60 * 1000)).toInt()
                ChipSmall(label = getString(com.growtracker.app.ui.language.GrowStrings.chip_germ_day), value = keimDays.toString())

                // Bloom day if floweringStartDate is present
                plant.floweringStartDate?.let {
                    val bloomDays = ((System.currentTimeMillis() - it) / (24L * 60 * 60 * 1000)).toInt()
                    ChipSmall(label = getString(com.growtracker.app.ui.language.GrowStrings.chip_bloom_day), value = bloomDays.toString())
                }

                // Estimated remaining days to harvest if in bloom and no harvest set
                if (remainingDays != null && remainingDays > 0) {
                    val unit = getString(com.growtracker.app.ui.language.GrowStrings.unit_days)
                    ChipSmall(label = getString(com.growtracker.app.ui.language.GrowStrings.chip_days_to_harvest), value = "$remainingDays $unit")
                }
            }

            // Action when harvest ETA has passed and plant is not yet in drying
            if ((remainingDays == null || remainingDays <= 0) && !plant.isDrying) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val updated = plant.copy(isDrying = true, dryingStartDate = System.currentTimeMillis())
                    GrowDataStore.updatePlant(updated)
                }) {
                    Text(getString(com.growtracker.app.ui.language.GrowStrings.to_drying_button))
                }
            }
        }
    }
}

@Composable
private fun SimpleCalendarView(
    selectedDate: Long? = null,
    onSelectDate: (Long) -> Unit = {},
    estimatedHarvestMidnight: Long? = null,
    isSmallScreen: Boolean = false
) {
    // Expanded = month grid (30+ days). Collapsed = week row.
    var expanded by remember { mutableStateOf(false) }

    // Helper to normalize millis to midnight
    fun normalizeMidnight(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val todayMidnight = remember { normalizeMidnight(System.currentTimeMillis()) }
    val selectedMidnight = selectedDate?.let { normalizeMidnight(it) }

    // Displayed month is based on selected date (or today), set to first day of month
    var monthCal by remember(selectedMidnight) {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = selectedMidnight ?: todayMidnight
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        })
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header with month/year and controls
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val monthTitle = remember(monthCal.timeInMillis) {
                SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(monthCal.time)
            }
            Text(monthTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            Spacer(modifier = Modifier.weight(1f))
                if (expanded) {
                IconButton(onClick = {
                    monthCal = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Vorheriger Monat") }
                IconButton(onClick = {
                    monthCal = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Nächster Monat") }
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = if (expanded) "Einklappen" else "Ausklappen")
            }
        }

    if (expanded) {
            // Use fixed cell sizes and center the grid so it is always horizontally centered
            val days = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
            val cellSize = if (isSmallScreen) 40.dp else 48.dp

            // Prepare swipe handling state once for the whole expanded calendar area (headers + grid)
            val density = LocalDensity.current
            val swipeThresholdPx = with(density) { 56.dp.toPx() }
            var accumulatedDx by remember(monthCal.timeInMillis) { mutableStateOf(0f) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(monthCal.timeInMillis) {
                        detectDragGestures(
                            onDragEnd = {
                                when {
                                    accumulatedDx > swipeThresholdPx -> {
                                        // swipe right -> previous month
                                        monthCal = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                    }
                                    accumulatedDx < -swipeThresholdPx -> {
                                        // swipe left -> next month
                                        monthCal = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                    }
                                }
                                accumulatedDx = 0f
                            },
                            onDrag = { _, dragAmount ->
                                accumulatedDx += dragAmount.x
                            }
                        )
                    }
            ) {
                // Weekday headers (Mon-Sun)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        days.forEach { d ->
                            Text(
                                d,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(cellSize),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Build month grid with fixed-sized cells
                val firstOfMonth = (monthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                val firstWeekdayMon0 = (firstOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7
                val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val totalCells = run {
                    val used = firstWeekdayMon0 + daysInMonth
                    val remainder = used % 7
                    if (remainder == 0) used else used + (7 - remainder)
                }
                val rows = (totalCells + 6) / 7

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.wrapContentWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(rows) { r ->
                            Row(modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(7) { cIdx ->
                                    val index = r * 7 + cIdx
                                    if (index < firstWeekdayMon0 || index >= firstWeekdayMon0 + daysInMonth) {
                                        Box(modifier = Modifier.size(cellSize))
                                    } else {
                                        val dayNum = index - firstWeekdayMon0 + 1
                                        val cellCal = (firstOfMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                                        val cellMillis = normalizeMidnight(cellCal.timeInMillis)
                                        val isSelected = selectedMidnight != null && selectedMidnight == cellMillis
                                        val isToday = todayMidnight == cellMillis
                                        val bg = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant

                                        Surface(shape = RoundedCornerShape(8.dp), color = bg, modifier = Modifier.size(cellSize).clickable { onSelectDate(cellMillis) }) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                    Text(dayNum.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    if (isToday && !isSelected) Text("heute", style = MaterialTheme.typography.labelSmall)
                                                    // Harvest marker label
                                                    if (estimatedHarvestMidnight != null && estimatedHarvestMidnight == cellMillis) {
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = getString(com.growtracker.app.ui.language.GrowStrings.harvest_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Collapsed weekly view: show week of selected date (or current week)
            val base = Calendar.getInstance().apply {
                timeInMillis = selectedMidnight ?: todayMidnight
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            // Center the weekly row (compact)
            val cellWidth = if (isSmallScreen) 72.dp else 84.dp
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.wrapContentWidth().padding(vertical = 4.dp)) {
                    items(7) { offset ->
                        val c = (base.clone() as Calendar).apply { add(Calendar.DATE, offset) }
                        val weekday = SimpleDateFormat("E", Locale.getDefault()).format(c.time)
                        val day = SimpleDateFormat("dd.MM", Locale.getDefault()).format(c.time)
                        val cMid = normalizeMidnight(c.timeInMillis)
                        val isSelected = selectedMidnight != null && selectedMidnight == cMid
                        val bg = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant

                        Surface(shape = RoundedCornerShape(10.dp), color = bg, modifier = Modifier.width(cellWidth).height(if (isSmallScreen) 48.dp else 56.dp).clickable { onSelectDate(cMid) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(weekday, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                if (estimatedHarvestMidnight != null && estimatedHarvestMidnight == cMid) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = getString(com.growtracker.app.ui.language.GrowStrings.harvest_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Ensure the calendar shows and selects today's date and scrolls into view
            LaunchedEffect(base.timeInMillis) {
                val todayMid = normalizeMidnight(System.currentTimeMillis())
                // find today's index in the displayed week
                val idx = (0 until 7).indexOfFirst {
                    val c = (base.clone() as Calendar).apply { add(Calendar.DATE, it) }
                    normalizeMidnight(c.timeInMillis) == todayMid
                }
                if (idx >= 0) {
                    // select today's date in the parent
                    onSelectDate(todayMid)
                    // scroll to center today's index
                    listState.animateScrollToItem(idx)
                }
            }
        }
    }
}

@Composable
private fun EntriesSection(entries: List<PlantEntry>, plantId: String, preferredFertilizerManufacturer: String?, selectedDate: Long? = null, isSmallScreen: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
    // align left padding with month/year title which uses 8.dp
    Text(getString(com.growtracker.app.ui.language.GrowStrings.entries_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp))

        val displayed = remember(entries, selectedDate) {
            if (selectedDate == null) entries.sortedByDescending { it.date }
            else {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = start + 24L * 60 * 60 * 1000 - 1
                entries.filter { it.date in start..end }.sortedByDescending { it.date }
            }
        }

        if (displayed.isEmpty()) {
            Text(getString(com.growtracker.app.ui.language.GrowStrings.no_entries_today), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp))
        } else {
            val plant = GrowDataStore.plants.find { it.id == plantId }
            var fullScreenUri by remember { mutableStateOf<Uri?>(null) }
            var entryToDelete by remember { mutableStateOf<PlantEntry?>(null) }

                displayed.forEach { e ->
                    if (e.type == EntryType.PHOTO) {
                        // resolve photo by id or uri
                        val photoId = e.value.takeIf { it.isNotBlank() } ?: e.notes
                        val photo = plant?.photos?.find { it.id == photoId } ?: plant?.photos?.find { it.uri == e.value }

                        val bmp = remember(photo?.uri) { photo?.uri?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it) }.getOrNull() } }
                        val thumbPainter = remember(bmp) { bmp?.asImageBitmap()?.let { BitmapPainter(it) } }

                        ListItem(
                            headlineContent = { Text(e.type.displayName) },
                            supportingContent = {
                                if (photo != null) Text(SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(photo.timestamp)))
                                else Text(e.value.takeIf { it.isNotBlank() } ?: e.notes)
                            },
                            leadingContent = {
                                if (thumbPainter != null) {
                                    Image(painter = thumbPainter, contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 40.dp else 48.dp).clickable { fullScreenUri = Uri.fromFile(File(photo!!.uri)) }, contentScale = ContentScale.Crop)
                                } else {
                                    androidx.compose.foundation.Image(painter = loadEntryIconOrPlaceholder("camera"), contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 20.dp else 28.dp))
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { entryToDelete = e }) { Icon(Icons.Filled.Delete, contentDescription = "Löschen") }
                            },
                            modifier = Modifier.clickable { if (photo != null) fullScreenUri = Uri.fromFile(File(photo.uri)) }
                        )
                        HorizontalDivider()
                    } else {
                    val iconName = when (e.type) {
                        EntryType.WATERING -> "waterdrop"
                        EntryType.FERTILIZING -> "fertilizer"
                        EntryType.HEIGHT -> "plant_size"
                        EntryType.TEMPERATURE -> "temperature"
                        EntryType.HUMIDITY -> "fan"
                        EntryType.TOPPING -> "topping"
                        EntryType.LST -> "low_stress_training"
                        EntryType.NOTE -> "notiz"
                        else -> "ic_menu_help"
                    }

                    val displayText = when (e.type) {
                        EntryType.HUMIDITY -> e.value.takeIf { it.isNotBlank() }?.let { "$it %" } ?: ""
                        EntryType.HEIGHT -> e.value.takeIf { it.isNotBlank() }?.let { "$it cm" } ?: ""
                        EntryType.TEMPERATURE -> e.value.takeIf { it.isNotBlank() }?.let { "$it °C" } ?: ""
                        EntryType.WATERING -> e.value.takeIf { it.isNotBlank() }?.let { "$it L" } ?: ""
                        EntryType.FERTILIZING -> e.value.takeIf { it.isNotBlank() } ?: e.notes
                        EntryType.TOPPING -> e.notes
                        EntryType.LOLLIPOPPING, EntryType.LST -> e.notes
                        EntryType.NOTE -> e.notes
                        else -> e.value.takeIf { it.isNotBlank() } ?: e.notes
                    }

                    ListItem(
                        headlineContent = { Text(e.type.displayName) },
                        supportingContent = { Text(displayText) },
                        leadingContent = { androidx.compose.foundation.Image(painter = loadEntryIconOrPlaceholder(iconName), contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 20.dp else 28.dp)) },
                        trailingContent = { IconButton(onClick = { entryToDelete = e }) { Icon(Icons.Filled.Delete, contentDescription = "Löschen") } },
                        modifier = Modifier.clickable { }
                    )
                        HorizontalDivider()
                }
            }

            if (fullScreenUri != null) {
                val fullBmp = remember(fullScreenUri) { fullScreenUri?.path?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it) }.getOrNull() } }
                Dialog(onDismissRequest = { fullScreenUri = null }) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (fullBmp != null) {
                                Image(bitmap = fullBmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            } else {
                                Text("Bild nicht verfügbar")
                            }
                        }
                    }
                }
            }

            // Deletion confirmation dialog
            if (entryToDelete != null) {
                AlertDialog(
                    onDismissRequest = { entryToDelete = null },
                    title = { Text("Eintrag löschen?") },
                    text = { Text("Möchtest du diesen Eintrag dauerhaft entfernen?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val id = entryToDelete!!.id
                            GrowDataStore.removeEntryFromPlant(plantId, id)
                            entryToDelete = null
                        }) { Text("Löschen") }
                    },
                    dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("Abbrechen") } }
                )
            }
        }
    }
}

@Composable
private fun AddEntryDialog(plantId: String, selectedDate: Long? = null, onClose: () -> Unit, preferredManufacturer: String? = null, onAdd: (PlantEntry) -> Unit, isSmallScreen: Boolean = false) {
    var selectedType by remember { mutableStateOf(EntryType.WATERING) }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Fertilizer-specific state
    data class FertSel(val productName: String, val amountMlPerL: String)
    var fertProductExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<FertilizerProduct?>(null) }
    var fertAmount by remember { mutableStateOf("") }
    var fertilizerList by remember { mutableStateOf(listOf<FertSel>()) }

    // Photo picker state
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    val ctx = LocalContext.current
    val getImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        pickedImageUri = uri
    }

    val manufacturers = com.growtracker.app.data.getFertilizerManufacturers()
    val productsForPref = preferredManufacturer?.takeIf { it.isNotBlank() }?.let { name ->
        manufacturers.find { it.name == name }?.products ?: emptyList()
    } ?: emptyList()

    AlertDialog(onDismissRequest = onClose, title = { Text("Neuer Eintrag") }, text = {
        Column {
            DropdownMenuBox(selected = selectedType, onSelect = { selectedType = it }, isSmallScreen = isSmallScreen)

            // Fertilizer flow: allow adding multiple product entries
            if (selectedType == EntryType.FERTILIZING) {
                if (preferredManufacturer.isNullOrBlank()) {
                    Text("Kein bevorzugter Dünger-Hersteller gesetzt. Bitte wähle einen in den Einstellungen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else {
                    // Product selector (from preferred manufacturer)
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        label = { Text("Produkt") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { fertProductExpanded = !fertProductExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } }
                    )

                    DropdownMenu(expanded = fertProductExpanded, onDismissRequest = { fertProductExpanded = false }) {
                        productsForPref.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProduct = p; fertProductExpanded = false })
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fertAmount, onValueChange = { fertAmount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("ml/l") }, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            val prod = selectedProduct?.name ?: return@TextButton
                            if (fertAmount.isBlank()) return@TextButton
                            fertilizerList = fertilizerList + FertSel(prod, fertAmount)
                            // reset
                            selectedProduct = null
                            fertAmount = ""
                        }) { Text("Zur Liste hinzufügen") }
                    }

                    // show current fertilizer list
                    if (fertilizerList.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            fertilizerList.forEachIndexed { idx, it ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("${it.productName} - ${it.amountMlPerL} ml/l", modifier = Modifier.weight(1f))
                                    IconButton(onClick = { fertilizerList = fertilizerList.filterIndexed { i, _ -> i != idx } }) { Icon(Icons.Filled.Delete, contentDescription = "Entfernen") }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            } else if (selectedType == EntryType.PHOTO) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { getImageLauncher.launch("image/*") }) { Text("Foto auswählen") }
                    if (pickedImageUri != null) Text("Ausgewählt: ${pickedImageUri?.lastPathSegment}")
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Beschreibung") }, modifier = Modifier.fillMaxWidth())
            } else if (selectedType == EntryType.WATERING) {
                OutlinedTextField(value = value, onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Liter (L)") }, placeholder = { Text("z.B. 0.5") }, modifier = Modifier.fillMaxWidth())
            } else if (selectedType == EntryType.HUMIDITY) {
                // Luftfeuchtigkeit in Prozent, keine Notizen
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Luftfeuchtigkeit (%)") },
                    placeholder = { Text("z.B. 55") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedType == EntryType.HEIGHT) {
                // Pflanzengröße in cm, keine Notizen
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Pflanzengröße (cm)") },
                    placeholder = { Text("z.B. 45") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedType == EntryType.TEMPERATURE) {
                // Temperatur in Celsius, keine Notizen
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Temperatur (°C)") },
                    placeholder = { Text("z.B. 23") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedType == EntryType.TOPPING) {
                // Topping only as info: notes optional, no value
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notizen (optional)") }, modifier = Modifier.fillMaxWidth())
            } else if (selectedType == EntryType.LOLLIPOPPING || selectedType == EntryType.LST) {
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notizen (optional)") }, modifier = Modifier.fillMaxWidth())
            } else if (selectedType == EntryType.NOTE) {
                // Notes-only entry, no value
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notizen") }, modifier = Modifier.fillMaxWidth())
            } else {
                // Fallback for any other types: keep value + notes
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Wert") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notizen") }, modifier = Modifier.fillMaxWidth())
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            // Determine entry date (normalize to midnight of selected day if provided)
            val dateMillis = run {
                val cal = Calendar.getInstance()
                if (selectedDate != null) cal.timeInMillis = selectedDate
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            if (selectedType == EntryType.FERTILIZING) {
                // emit one PlantEntry per fertilizer list item
                fertilizerList.forEach { fs ->
                    val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}_${fs.productName.hashCode()}", date = dateMillis, type = EntryType.FERTILIZING, value = "${fs.productName} - ${fs.amountMlPerL} ml/l", notes = "${preferredManufacturer ?: ""}")
                    onAdd(entry)
                }
            } else if (selectedType == EntryType.PHOTO) {
                // If an image was picked, save it to app files and create PlantPhoto + PlantEntry
                if (pickedImageUri != null) {
                    try {
                        val input = ctx.contentResolver.openInputStream(pickedImageUri!!)
                        val destDir = File(ctx.filesDir, "plant_photos")
                        if (!destDir.exists()) destDir.mkdirs()
                        val fileName = "photo_${System.currentTimeMillis()}.jpg"
                        val outFile = File(destDir, fileName)
                        val out = FileOutputStream(outFile)
                        input?.copyTo(out)
                        input?.close(); out.close()

                        val photo = com.growtracker.app.data.PlantPhoto(id = "photo_${System.currentTimeMillis()}", uri = outFile.absolutePath, timestamp = System.currentTimeMillis(), description = notes)
                        // attach photo to plant
                        val plantIdx = GrowDataStore.plants.indexOfFirst { it.id == plantId }
                        if (plantIdx >= 0) {
                            val p = GrowDataStore.plants[plantIdx]
                            val updated = p.copy(photos = p.photos + photo)
                            GrowDataStore.updatePlant(updated)
                        }

                        // Create an entry referencing the photo id
                        val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.PHOTO, value = photo.id, notes = photo.uri)
                        onAdd(entry)
                    } catch (ex: Exception) {
                        // fallback: create entry without photo
                        val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.PHOTO, value = "", notes = notes)
                        onAdd(entry)
                    }
                } else {
                    val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.PHOTO, value = "", notes = notes)
                    onAdd(entry)
                }
            } else if (selectedType == EntryType.WATERING) {
                val liters = value.takeIf { it.isNotBlank() } ?: "0"
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.WATERING, value = liters, notes = "")
                onAdd(entry)
            } else if (selectedType == EntryType.HUMIDITY) {
                val v = value.takeIf { it.isNotBlank() } ?: ""
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.HUMIDITY, value = v, notes = "")
                onAdd(entry)
            } else if (selectedType == EntryType.HEIGHT) {
                val v = value.takeIf { it.isNotBlank() } ?: ""
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.HEIGHT, value = v, notes = "")
                onAdd(entry)
            } else if (selectedType == EntryType.TEMPERATURE) {
                val v = value.takeIf { it.isNotBlank() } ?: ""
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.TEMPERATURE, value = v, notes = "")
                onAdd(entry)
            } else if (selectedType == EntryType.TOPPING) {
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.TOPPING, value = "", notes = notes)
                onAdd(entry)
            } else if (selectedType == EntryType.LOLLIPOPPING || selectedType == EntryType.LST) {
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = selectedType, value = "", notes = notes)
                onAdd(entry)
            } else if (selectedType == EntryType.NOTE) {
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = EntryType.NOTE, value = "", notes = notes)
                onAdd(entry)
            } else {
                val entry = PlantEntry(id = "entry_${System.currentTimeMillis()}", date = dateMillis, type = selectedType, value = value, notes = notes)
                onAdd(entry)
            }
            onClose()
        }) { Text("Hinzufügen") }
    }, dismissButton = { TextButton(onClick = onClose) { Text("Abbrechen") } })
}

@Composable
private fun DropdownMenuBox(selected: EntryType, onSelect: (EntryType) -> Unit, isSmallScreen: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        // determine icon for the currently selected type
        val selectedIconName = when (selected) {
            EntryType.WATERING -> "waterdrop"
            EntryType.FERTILIZING -> "fertilizer"
            EntryType.PHOTO -> "camera"
            EntryType.HEIGHT -> "plant_size"
            EntryType.TEMPERATURE -> "temperature"
            EntryType.HUMIDITY -> "fan"
            EntryType.TOPPING -> "topping"
            EntryType.LOLLIPOPPING -> "lolli_popping"
            EntryType.LST -> "low_stress_training"
            EntryType.NOTE -> "notiz"
            else -> "ic_menu_help"
        }

        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            leadingIcon = { androidx.compose.foundation.Image(painter = loadEntryIconOrPlaceholder(selectedIconName), contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 16.dp else 20.dp)) },
            trailingIcon = { IconButton(onClick = { expanded = !expanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Only show the specific entry types requested (exclude LIGHT and TASK)
            val allowed = listOf(
                EntryType.WATERING,
                EntryType.FERTILIZING,
                EntryType.PHOTO,
                EntryType.HEIGHT,
                EntryType.TEMPERATURE,
                EntryType.HUMIDITY,
                EntryType.TOPPING,
                EntryType.LOLLIPOPPING,
                EntryType.LST,
                EntryType.NOTE
            ).sortedBy { it.displayName.lowercase(Locale.getDefault()) }

            allowed.forEach { t ->
                val iconName = when (t) {
                    EntryType.WATERING -> "waterdrop"
                    EntryType.FERTILIZING -> "fertilizer"
                    EntryType.PHOTO -> "camera"
                    EntryType.HEIGHT -> "plant_size"
                    EntryType.TEMPERATURE -> "temperature"
                    EntryType.HUMIDITY -> "fan"
                    EntryType.TOPPING -> "topping"
                    EntryType.LOLLIPOPPING -> "lolli_popping"
                    EntryType.LST -> "low_stress_training"
                    EntryType.NOTE -> "notiz"
                    else -> "ic_menu_help"
                }

                DropdownMenuItem(
                    text = { Text(t.displayName) },
                    leadingIcon = { androidx.compose.foundation.Image(painter = loadEntryIconOrPlaceholder(iconName), contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 16.dp else 20.dp)) },
                    onClick = { onSelect(t); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun loadEntryIconOrPlaceholder(name: String): androidx.compose.ui.graphics.painter.Painter {
    val ctx = LocalContext.current
    val resId = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
    val drawableId = if (resId != 0) resId else android.R.drawable.ic_menu_help
    return painterResource(id = drawableId)
}

@Composable
private fun PlantSettingsDialog(plant: Plant, selectedDate: Long?, onClose: () -> Unit, onUpdate: (Plant) -> Unit, onDelete: (String) -> Unit) {
    var preferredFert by remember { mutableStateOf(plant.preferredFertilizerManufacturer ?: "") }
    var potLiters by remember { mutableStateOf(plant.customPotSize ?: "") }
    var fertExpanded by remember { mutableStateOf(false) }
    // Power settings state
    var priceText by remember { mutableStateOf(plant.electricityPrice?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "") }
    // Removed legacy lamp config from plant-level settings (watt, percent, time window)
    // Devices state (per-plant)
    val devicesState = remember { mutableStateListOf<PowerDevice>().apply { addAll(plant.devices) } }
    var deviceEditorOpen by remember { mutableStateOf(false) }
    var editingDeviceIndex by remember { mutableStateOf<Int?>(null) }
    val ctx = LocalContext.current
    var qrDialogOpen by remember { mutableStateOf(false) }
    fun formatHm(mins: Int?): String = mins?.let { String.format(Locale.getDefault(), "%02d:%02d", it / 60, it % 60) } ?: "--:--"
    fun pickTime(initial: Int?, onSet: (Int) -> Unit) {
        val initH = (initial ?: 18 * 60) / 60
        val initM = (initial ?: 18 * 60) % 60
        android.app.TimePickerDialog(ctx, { _, h, m -> onSet(h * 60 + m) }, initH, initM, true).show()
    }

    AlertDialog(onDismissRequest = onClose, title = { Text(getString(com.growtracker.app.ui.language.GrowStrings.edit_plant_title)) }, text = {
        // Make settings scrollable to fit on small screens
        androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
            item {
            // Preferred fertilizer manufacturer dropdown
            val manufacturers = com.growtracker.app.data.getFertilizerManufacturers()
            OutlinedTextField(
                value = preferredFert,
                onValueChange = {},
                label = { Text(getString(com.growtracker.app.ui.language.GrowStrings.preferred_fertilizer)) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { fertExpanded = !fertExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = fertExpanded, onDismissRequest = { fertExpanded = false }) {
                manufacturers.forEach { m ->
                    DropdownMenuItem(text = { Text(m.name) }, onClick = { preferredFert = m.name; fertExpanded = false })
                }
                DropdownMenuItem(text = { Text(getString(com.growtracker.app.ui.language.GrowStrings.no_selection)) }, onClick = { preferredFert = ""; fertExpanded = false })
            }

            // Custom pot size in liters
            OutlinedTextField(
                value = potLiters,
                onValueChange = { potLiters = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text(getString(com.growtracker.app.ui.language.GrowStrings.pot_size_liters)) },
                placeholder = { Text("z.B. 5.5") },
                modifier = Modifier.fillMaxWidth()
            )
            }
            item {

            // Divider before power section
            HorizontalDivider()
            Text(text = getString(com.growtracker.app.ui.language.Strings.power_section_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = getString(com.growtracker.app.ui.language.Strings.power_section_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Explainer removed per request

            // Electricity price
            OutlinedTextField(
                value = priceText,
                onValueChange = { v -> priceText = v.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.') },
                label = { Text(getString(com.growtracker.app.ui.language.Strings.power_label_price)) },
                placeholder = { Text("0.30") },
                trailingIcon = { Text("€/kWh", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Removed plant-level lamp watt/percent/time. Use devices below instead.
            }
            // QR Label section
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Text("QR-Label", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Erzeuge einen QR-Code, der diese Pflanze in der App öffnet (offline).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { qrDialogOpen = true }) {
                    Icon(Icons.Filled.QrCode, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("QR-Code anzeigen & speichern")
                }
            }
            // Devices section
            item {
                Spacer(Modifier.height(8.dp))
                Text("Geräte", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (devicesState.isEmpty()) {
                    Text("Keine Geräte hinzugefügt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // Pretty, sortable list with inline power slider
                    val orderOf: (DeviceType) -> Int = {
                        when (it) {
                            DeviceType.LAMP -> 0
                            DeviceType.FAN -> 1
                            DeviceType.HEATER -> 2
                            DeviceType.HUMIDIFIER -> 3
                            DeviceType.DEHUMIDIFIER -> 4
                            else -> 5
                        }
                    }
                    val viewList = devicesState.sortedWith(compareBy({ orderOf(it.type) }, { it.name }, { it.watt ?: Int.MAX_VALUE }))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewList.forEach { dev ->
                            val idx = devicesState.indexOfFirst { it.id == dev.id }
                            if (idx < 0) return@forEach
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.ElectricalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(10.dp))
                                            val typeLabel = when (dev.type) {
                                                DeviceType.LAMP -> "Lampe"
                                                DeviceType.FAN -> "Ventilator"
                                                DeviceType.HEATER -> "Heizung"
                                                DeviceType.HUMIDIFIER -> "Luftbefeuchter"
                                                DeviceType.DEHUMIDIFIER -> "Entfeuchter"
                                                DeviceType.VENTILATION -> "Ventilation"
                                                DeviceType.PUMP -> "Pumpe"
                                                DeviceType.OTHER -> "Sonstige"
                                            }
                                            Text(dev.name.ifBlank { typeLabel }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = { editingDeviceIndex = idx; deviceEditorOpen = true }) { Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten") }
                                            IconButton(onClick = { devicesState.removeAt(idx) }) { Icon(Icons.Filled.Delete, contentDescription = "Löschen") }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    // Secondary info: watt + window
                                    val pct = (dev.powerPercent ?: 100).coerceIn(0, 100)
                                    val wattStr = dev.watt?.let { "$it W" } ?: "—"
                                    val sched = when (dev.scheduleType) {
                                        DeviceScheduleType.ALWAYS_ON -> "Immer an"
                                        DeviceScheduleType.WINDOW -> {
                                            val s = formatHm(dev.startMinutes)
                                            val e = formatHm(dev.endMinutes)
                                            "$s–$e"
                                        }
                                        DeviceScheduleType.DUTY_CYCLE -> "Duty ${dev.dutyCyclePercent ?: 0}%"
                                    }
                                    Text("$wattStr • $sched", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(6.dp))
                                    // Inline power slider
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Leistung", style = MaterialTheme.typography.labelMedium)
                                        Text("$pct%", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Slider(
                                        value = pct.toFloat(),
                                        onValueChange = { v ->
                                            val newPct = v.toInt().coerceIn(0, 100)
                                            devicesState[idx] = devicesState[idx].copy(powerPercent = newPct)
                                        },
                                        valueRange = 0f..100f,
                                        steps = 99
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { editingDeviceIndex = null; deviceEditorOpen = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Gerät hinzufügen")
                }
            }
            item {
                // Bloom controls
                Spacer(Modifier.height(8.dp))
                Text(getString(com.growtracker.app.ui.language.GrowStrings.bloom_section_title), style = MaterialTheme.typography.titleSmall)
                if (plant.floweringStartDate == null) {
                    Text(getString(com.growtracker.app.ui.language.GrowStrings.bloom_not_started), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        // Use the currently selected calendar day as day 1; normalize to midnight.
                        val start = selectedDate ?: kotlin.run {
                            val cal = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            cal.timeInMillis
                        }
                        val updated = plant.copy(floweringStartDate = start)
                        onUpdate(updated)
                    }) { Text(getString(com.growtracker.app.ui.language.GrowStrings.bloom_start_on_selected)) }
                } else {
                    val days = bloomDays(plant) ?: 0
                    val remaining = daysToHarvest(plant)
                    val info = buildString {
                        append("Seit ")
                        append(days)
                        append(" ")
                        append(getString(com.growtracker.app.ui.language.GrowStrings.days_suffix))
                        append(" in ")
                        append(getString(com.growtracker.app.ui.language.GrowStrings.bloom_section_title))
                        if (remaining != null) {
                            append(" · ")
                            append(getString(com.growtracker.app.ui.language.GrowStrings.eta_prefix))
                            append(remaining)
                            append(" ")
                            append(getString(com.growtracker.app.ui.language.GrowStrings.eta_days_to_harvest))
                        }
                    }
                    Text(info, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            val updated = plant.copy(floweringStartDate = null)
                            onUpdate(updated)
                        }) { Text(getString(com.growtracker.app.ui.language.GrowStrings.bloom_reset)) }
                    }
                }
            }
        }
    }, confirmButton = {
            TextButton(onClick = {
            val updated = plant.copy(
                preferredFertilizerManufacturer = preferredFert.takeIf { it.isNotBlank() },
                customPotSize = potLiters.takeIf { it.isNotBlank() },
                electricityPrice = when {
                    priceText.isBlank() -> null
                    else -> priceText.toDoubleOrNull() ?: plant.electricityPrice
                },
                devices = devicesState.toList()
            )
            onUpdate(updated)
            onClose()
        }) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_save)) }
    }, dismissButton = {
        Row {
            TextButton(onClick = { onDelete(plant.id); onClose() }) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_delete), color = MaterialTheme.colorScheme.error) }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onClose) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_cancel)) }
        }
    })

    // Device editor dialog overlay
    DeviceEditorHost(
        open = deviceEditorOpen,
        editingDeviceIndex = editingDeviceIndex,
        devicesState = devicesState,
        onClose = { deviceEditorOpen = false; editingDeviceIndex = null }
    )

    if (qrDialogOpen) {
        PlantQrDialog(plantId = plant.id, onDismiss = { qrDialogOpen = false })
    }
}

@Composable
private fun PlantQrDialog(plantId: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val payload = remember(plantId) { com.growtracker.app.util.QrUtils.buildPlantQrPayload(plantId, ctx.packageName) }
    val qrBitmap = remember(payload) { com.growtracker.app.util.QrUtils.generateQrBitmap(payload, sizePx = 1024) }
    val imageBitmap = remember(qrBitmap) { qrBitmap.asImageBitmap() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR-Code für Pflanze") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Scannen öffnet die App und die Pflanze. Ohne App führt der Link zu Discord.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val fileName = "plant_${plantId}_qr.png"
                com.growtracker.app.util.QrUtils.saveBitmapToDownloads(ctx, qrBitmap, fileName)
                onDismiss()
            }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Schließen") }
        }
    )
}

@Composable
private fun DeviceEditorDialog(
    initial: PowerDevice?,
    onDismiss: () -> Unit,
    onSave: (PowerDevice) -> Unit
) {
    var type by remember { mutableStateOf(initial?.type ?: DeviceType.OTHER) }
    var typeMenu by remember { mutableStateOf(false) }
    var wattText by remember { mutableStateOf(initial?.watt?.toString() ?: "") }
    var powerPercent by remember { mutableStateOf((initial?.powerPercent ?: 100).coerceIn(0, 100)) }
    var startMinutes by remember { mutableStateOf(initial?.startMinutes) }
    var endMinutes by remember { mutableStateOf(initial?.endMinutes) }
    var alwaysOn by remember { mutableStateOf(initial?.scheduleType == DeviceScheduleType.ALWAYS_ON) }

    val ctx = LocalContext.current
    fun formatHm(mins: Int?): String = mins?.let { String.format(Locale.getDefault(), "%02d:%02d", it / 60, it % 60) } ?: "--:--"
    fun pickTime(initial: Int?, onSet: (Int) -> Unit) {
        val initH = (initial ?: 12 * 60) / 60
        val initM = (initial ?: 12 * 60) % 60
        android.app.TimePickerDialog(ctx, { _, h, m -> onSet(h * 60 + m) }, initH, initM, true).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerät konfigurieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Type selector
                OutlinedTextField(
                    value = when (type) {
                        DeviceType.LAMP -> "Lampe"
                        DeviceType.FAN -> "Ventilator"
                        DeviceType.HEATER -> "Heizung"
                        DeviceType.HUMIDIFIER -> "Luftbefeuchter"
                        DeviceType.DEHUMIDIFIER -> "Entfeuchter"
                        else -> "Sonstige"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gerät") },
                    leadingIcon = { Icon(Icons.Filled.ElectricalServices, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { typeMenu = true }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) } },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Lampe") } },
                        onClick = { type = DeviceType.LAMP; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Ventilator") } },
                        onClick = { type = DeviceType.FAN; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Lüfter") } },
                        onClick = { type = DeviceType.FAN; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Heizung") } },
                        onClick = { type = DeviceType.HEATER; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Luftbefeuchter") } },
                        onClick = { type = DeviceType.HUMIDIFIER; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Entfeuchter") } },
                        onClick = { type = DeviceType.DEHUMIDIFIER; typeMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ElectricalServices, null); Spacer(Modifier.width(8.dp)); Text("Sonstige") } },
                        onClick = { type = DeviceType.OTHER; typeMenu = false }
                    )
                }
                OutlinedTextField(value = wattText, onValueChange = { wattText = it.filter { ch -> ch.isDigit() } }, label = { Text("Watt") }, trailingIcon = { Text("W") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                // Power percent
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Leistung")
                        Text("$powerPercent%", style = MaterialTheme.typography.labelMedium)
                    }
                    Slider(value = powerPercent.toFloat(), onValueChange = { powerPercent = it.toInt().coerceIn(0, 100) }, valueRange = 0f..100f, steps = 99)
                }
                // Immer an Toggle
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Immer an")
                    Switch(checked = alwaysOn, onCheckedChange = { checked ->
                        alwaysOn = checked
                        if (checked) {
                            // set to full day
                            startMinutes = 0
                            endMinutes = 24 * 60
                        }
                    })
                }
                if (!alwaysOn) {
                    // Laufzeit: Von - Bis
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = formatHm(startMinutes), onValueChange = {}, readOnly = true, label = { Text("Von") }, trailingIcon = { IconButton(onClick = { pickTime(startMinutes) { startMinutes = it } }) { Icon(Icons.Filled.Schedule, contentDescription = null) } }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = formatHm(endMinutes), onValueChange = {}, readOnly = true, label = { Text("Bis") }, trailingIcon = { IconButton(onClick = { pickTime(endMinutes) { endMinutes = it } }) { Icon(Icons.Filled.Schedule, contentDescription = null) } }, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dev = PowerDevice(
                    name = "",
                    type = type,
                    watt = wattText.toIntOrNull(),
                    powerPercent = powerPercent,
                    scheduleType = if (alwaysOn) DeviceScheduleType.ALWAYS_ON else DeviceScheduleType.WINDOW,
                    startMinutes = if (alwaysOn) 0 else startMinutes,
                    endMinutes = if (alwaysOn) 24 * 60 else endMinutes,
                    dutyCyclePercent = null
                )
                onSave(dev)
                onDismiss()
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

// Render the device editor dialog when requested (helper within settings scope)
@Composable
private fun DeviceEditorHost(
    open: Boolean,
    editingDeviceIndex: Int?,
    devicesState: androidx.compose.runtime.snapshots.SnapshotStateList<PowerDevice>,
    onClose: () -> Unit
) {
    if (!open) return
    val initial = editingDeviceIndex?.let { idx -> devicesState.getOrNull(idx) }
    DeviceEditorDialog(initial = initial, onDismiss = onClose) { dev ->
        if (editingDeviceIndex != null && editingDeviceIndex in devicesState.indices) {
            devicesState[editingDeviceIndex] = dev
        } else {
            devicesState.add(dev)
        }
    }
}


@Composable
private fun EnergyCostsCard(plant: Plant, costs: com.growtracker.app.data.ElectricityCosts) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(getString(com.growtracker.app.ui.language.Strings.power_stats_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            val minutes = dailyLightMinutes(plant.lightOnStartMinutes, plant.lightOnEndMinutes) ?: 0
            val hours = minutes / 60.0
            val baseW = (plant.lightWatt ?: 0)
            val percent = (plant.lightPowerPercent ?: 100).coerceIn(0, 100)
            val effW = baseW * (percent / 100.0)
            val kwh = effW * hours / 1000.0
            Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_daily_usage)}: ${String.format(Locale.getDefault(), "%.2f", kwh)} kWh")
            Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_daily_cost)}: ${String.format(Locale.getDefault(), "%.2f", costs.dailyCost)} €")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_weekly)}: ${String.format(Locale.getDefault(), "%.2f", costs.weeklyCost)} €")
                Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_monthly)}: ${String.format(Locale.getDefault(), "%.2f", costs.monthlyCost)} €")
                Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_yearly)}: ${String.format(Locale.getDefault(), "%.2f", costs.yearlyCost)} €")
            }
            Text("${getString(com.growtracker.app.ui.language.Strings.power_stats_effective_watt)}: ${String.format(Locale.getDefault(), "%.0f", effW)} W (${percent}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// PlantStatisticsSection removed: per requirement, stats appear on main Statistics screen.
