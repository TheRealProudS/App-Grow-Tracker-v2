

@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.growtracker.app.ui.grow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.ExperimentalFoundationApi
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
// MenuAnchorType removed for compatibility with this Compose version
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import android.widget.Toast
import android.app.DatePickerDialog
import java.util.Calendar
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantType
import com.growtracker.app.data.StrainRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GrowScreenV2(
    languageManager: LanguageManager,
    snackbarHostState: SnackbarHostState,
    onOpenGrowbox: (String) -> Unit,
    onOpenGrowGuide: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    LaunchedEffect(Unit) { GrowDataStore.initialize(context) }

    val tabs = listOf("Aktive Pflanzen", "Archiv")

    var showAdd by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf("Keimdatum") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(floatingActionButton = { ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) }, text = { Text("Pflanze hinzufügen") }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(12.dp)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }, icon = {
                            Icon(imageVector = if (index == 0) Icons.Filled.List else Icons.Filled.Archive, contentDescription = null)
                    })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Guide shortcut button (navigates to Grow Guide)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onOpenGrowGuide() }) {
                    Icon(imageVector = Icons.Filled.MenuBook, contentDescription = "Grow Guide")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grow-Guide")
                }
            }
            // Simple sort selector for active tab
            if (selectedTab == 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Sortieren:", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(8.dp))
                    var expandSort by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expandSort = true }) { Text(sortMode) }
                        DropdownMenu(expanded = expandSort, onDismissRequest = { expandSort = false }) {
                            listOf("Keimdatum", "THC", "Name").forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { sortMode = s; expandSort = false })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    var plants = GrowDataStore.plants
                    plants = when (sortMode) {
                        "Keimdatum" -> plants.sortedByDescending { it.germinationDate ?: 0L }
                        "THC" -> plants.sortedByDescending { parseThcSortKey(it.thcContent) }
                        "Name" -> plants.sortedBy { it.name }
                        else -> plants
                    }
                    if (plants.isEmpty()) Text("Keine Pflanzen", modifier = Modifier.padding(16.dp))
                    else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 180.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(plants, key = { it.id }) { p -> PlantCard(plant = p, onClick = { onOpenGrowbox(it) }) }
                        }
                    }
                }
                else -> {
                    val plants = GrowDataStore.plants.filter { it.isDrying || it.isFermenting || it.harvestDate != null }
                    if (plants.isEmpty()) Text("Archiv ist leer", modifier = Modifier.padding(16.dp))
                    else { LazyColumn { items(plants, key = { it.id }) { p -> PlantRow(p) { onOpenGrowbox(it) } } } }
                }
            }

            if (showAdd) {
                AddPlantDialog(onCancel = { showAdd = false }, onAdd = { count, plant ->
                    // create `count` distinct plants (each with a new id)
                    repeat(count.coerceAtLeast(1)) {
                        val withId = if (plant.id.isBlank()) plant.copy(id = java.util.UUID.randomUUID().toString()) else plant.copy(id = java.util.UUID.randomUUID().toString())
                        GrowDataStore.addPlant(withId)
                    }
                    // don't clear growboxes here; persistence updated internal list
                    showAdd = false
                    // show a short snackbar via the top-level hostState so it's visible from both tabs
                    coroutineScope.launch { snackbarHostState.showSnackbar("${'$'}count Pflanzen hinzugefügt") }
                })
            }
        }
    }
}

// helper to parse THC strings like "18%" or "18-22%" into a numeric key (max value), returns 0.0 on parse failure
private fun parseThcSortKey(s: String): Double {
    val cleaned = s.replace("%", "").trim()
    if (cleaned.isBlank()) return 0.0
    return try {
        if (cleaned.contains("-")) cleaned.split("-").mapNotNull { it.toDoubleOrNull() }.maxOrNull() ?: cleaned.toDoubleOrNull() ?: 0.0
        else cleaned.toDoubleOrNull() ?: 0.0
    } catch (_: Exception) { 0.0 }
}
// PhasePill, deriveAgeWeeks and derivePhase are provided by PlantUiHelpers.kt

// removed scaffold helper - using SnackbarHostState and coroutineScope directly

@Composable
private fun PlantRow(plant: Plant, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable { onClick(plant.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                // Compact rectangular avatar (initial)
                val avatarText = plant.manufacturer.ifBlank { plant.name }.firstOrNull()?.uppercaseChar() ?: '?'
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(avatarText.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Show manufacturer as title (fallback to plant.name) and strain as subtitle
                    val title = plant.manufacturer.takeIf { it.isNotBlank() } ?: plant.name
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (plant.strain.isNotBlank()) Text(plant.strain, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges row (more minimal)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (plant.thcContent.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("THC ${plant.thcContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        if (plant.cbdContent.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                Text("CBD ${plant.cbdContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                        // Pot size chip similar to THC styling
                        val potLabel = plant.customPotSize?.takeIf { it.isNotBlank() }?.let { if (it.trim().lowercase().endsWith("l")) it.trim() else "${it.trim()} L" }
                        if (!potLabel.isNullOrBlank()) {
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("Topf $potLabel", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    
                                Spacer(modifier = Modifier.height(6.dp))
                                // Show derived phase and flowering day if available
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val phase = derivePhase(plant)
                                    Text("Phase: $phase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    plant.floweringStartDate?.let { fsd ->
                                        val days = ((System.currentTimeMillis() - fsd) / (1000L * 60 * 60 * 24)).toInt()
                                        Text("Blütetag: $days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                }

                // Right side: compact calendar icon + date (keeps info but uses icon instead of full label)
                val germText = plant.germinationDate?.let {
                    val d = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").format(d)
                } ?: "--"

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Keimdatum", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(germText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clickable { onClick(plant.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // left image/letter
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val avatar = plant.manufacturer.ifBlank { plant.name }.firstOrNull()?.uppercaseChar() ?: '?'
                        Text(avatar.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Title shows name first, then manufacturer/strain in lighter style
                    val titleText = plant.name.ifBlank { plant.manufacturer.ifBlank { "Unbenannt" } }
                    Text(titleText, style = MaterialTheme.typography.titleMedium)
                    // Avoid duplicating manufacturer/strain if already present in the title (e.g., "Manufacturer - Strain")
                    val titleLower = titleText.lowercase()
                    val secondaryParts = listOfNotNull(
                        plant.manufacturer.takeIf { it.isNotBlank() },
                        plant.strain.takeIf { it.isNotBlank() }
                    ).filter { part -> part.lowercase() !in titleLower }
                    val secondary = secondaryParts.joinToString(" • ")
                    if (secondary.isNotBlank()) Text(secondary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Badges row: THC, CBD, Pot size, and show phase chip when it's 'Sämling'
                    // Wrap chips so they flow nicely on small screens
                    val phase = derivePhase(plant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (plant.thcContent.isNotBlank()) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) { Text("THC ${plant.thcContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer) }
                        if (plant.cbdContent.isNotBlank()) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) { Text("CBD ${plant.cbdContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer) }
                        val potLabel2 = plant.customPotSize?.takeIf { it.isNotBlank() }?.let { if (it.trim().lowercase().endsWith("l")) it.trim() else "${it.trim()} L" }
                        if (!potLabel2.isNullOrBlank()) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) { Text("Topf $potLabel2", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer) }
                        if (phase == "Sämling") Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.FilterVintage, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(phase, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        // Keimtag chip
                        run {
                            val keimBase = plant.germinationDate ?: plant.plantingDate
                            if (keimBase > 0L) {
                                val keimDays = ((System.currentTimeMillis() - keimBase) / (1000L * 60 * 60 * 24)).toInt()
                                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text("Keimtag $keimDays", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        // Blütetag chip if flowering started
                        plant.floweringStartDate?.let { fsd ->
                            val bloomDays = ((System.currentTimeMillis() - fsd) / (1000L * 60 * 60 * 24)).toInt()
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("Blütetag $bloomDays", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            // compact bottom line with light info (pot size and blütetag chips are already shown above)
            val parts = mutableListOf<String>()
            plant.lightType?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
            plant.lightWatt?.let { parts.add("${it}W") }
            // Removed raw pot size and blütetag here to avoid duplication; both are shown as chips above
            if (parts.isNotEmpty()) Text(parts.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlantDialog(onCancel: () -> Unit, onAdd: (Int, Plant) -> Unit) {
    var count by remember { mutableStateOf("1") }
    val countInt = count.toIntOrNull() ?: 1
    var manufacturer by remember { mutableStateOf("") }
    var strain by remember { mutableStateOf("") }
    var thc by remember { mutableStateOf("") }
    var cbd by remember { mutableStateOf("") }
    var strainType by remember { mutableStateOf<PlantType?>(null) }
    var germ by remember { mutableStateOf("") }
    // store epoch millis separately so we can set Plant.germinationDate correctly
    var germinationEpoch by remember { mutableStateOf<Long?>(null) }
    var expandManufacturer by remember { mutableStateOf(false) }
    var expandStrain by remember { mutableStateOf(false) }

    // load manufacturers from repository and present alphabetically
    val baseManufacturers = StrainRepository.manufacturers
    // Move "Lucky Hemp" to the top so it's always visible in the dropdown
    val manufacturers = run {
        val (lucky, others) = baseManufacturers.partition { it.name.equals("Lucky Hemp", ignoreCase = true) }
        val sortedOthers = others.sortedBy { it.name.lowercase() }
        if (lucky.isNotEmpty()) lucky + sortedOthers else listOf(com.growtracker.app.data.SeedManufacturer(name = "Lucky Hemp", strains = emptyList())) + sortedOthers
    }
    val strainsForSelectedManufacturer = manufacturers.find { it.name.equals(manufacturer, ignoreCase = true) }?.strains?.sortedBy { it.name.lowercase() } ?: emptyList()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Pflanze anlegen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = count, onValueChange = { if (it.all(Char::isDigit) && it.isNotBlank()) count = it }, label = { Text("Anzahl") })
                // Manufacturer dropdown (simple Box + DropdownMenu to work reliably inside AlertDialog)
                Box {
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { expandManufacturer = !expandManufacturer },
                        label = { Text("Hersteller") },
                        trailingIcon = {
                                IconButton(onClick = { expandManufacturer = !expandManufacturer }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expandManufacturer, onDismissRequest = { expandManufacturer = false }) {
                        manufacturers.forEach { m ->
                            DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                manufacturer = m.name
                                strain = ""
                                thc = ""
                                cbd = ""
                                strainType = null
                                expandManufacturer = false
                            })
                        }
                    }
                }

                // Strain dropdown (depends on selected manufacturer) - same Box + DropdownMenu pattern
                Box {
                    OutlinedTextField(
                        value = strain,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { expandStrain = !expandStrain },
                        label = { Text("Sorte") },
                        trailingIcon = {
                            IconButton(onClick = { expandStrain = !expandStrain }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expandStrain, onDismissRequest = { expandStrain = false }) {
                        strainsForSelectedManufacturer.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = {
                                strain = s.name
                                thc = s.thcContent
                                cbd = s.cbdContent
                                strainType = s.type
                                expandStrain = false
                            })
                        }
                    }
                }

                // show strain details if selected
                if (strain.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val parts = mutableListOf<String>()
                        strainType?.let { parts.add("Typ: ${it.displayName}") }
                        if (thc.isNotBlank()) parts.add("THC: ${if (thc.endsWith("%")) thc else "$thc%"}")
                        if (cbd.isNotBlank()) parts.add("CBD: ${if (cbd.endsWith("%")) cbd else "$cbd%"}")
                        Text(parts.joinToString(" • "))
                    }
                }

                // Germination date input: read-only field that opens a DatePickerDialog
                val ctx = LocalContext.current
                val calendar = Calendar.getInstance()
                germinationEpoch?.let { calendar.timeInMillis = it }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // Use a clickable parent so taps reliably register inside AlertDialog
                val showDatePicker = {
                    DatePickerDialog(ctx, { _, y, m, d ->
                        val picked = java.time.LocalDate.of(y, m + 1, d)
                        germ = picked.toString()
                        germinationEpoch = picked.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }, year, month, day).show()
                    Toast.makeText(ctx, "DatePicker geöffnet", Toast.LENGTH_SHORT).show()
                }

                OutlinedTextField(
                    value = if (germ.isBlank()) "" else java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").format(java.time.LocalDate.parse(germ)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Keimdatum") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) { detectTapGestures { showDatePicker() } },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) { Icon(imageVector = Icons.Filled.DateRange, contentDescription = null) }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val germEpoch = germinationEpoch
                val defaultName = if (manufacturer.isNotBlank() && strain.isNotBlank()) "$manufacturer - $strain" else "Neue Pflanze"
                val plant = Plant(id = "", name = defaultName, manufacturer = manufacturer, strain = strain, thcContent = thc, cbdContent = cbd, germinationDate = germEpoch)
                onAdd(countInt.coerceAtLeast(1), plant)
            }, enabled = countInt > 0) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Abbrechen") } }
    )
}
