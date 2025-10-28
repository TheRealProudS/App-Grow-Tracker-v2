package com.growtracker.app.ui.grow

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.language.getString
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantType
import com.growtracker.app.data.StrainRepository
import com.growtracker.app.ui.language.LanguageManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowScreenV2(
    languageManager: LanguageManager,
    snackbarHostState: SnackbarHostState,
    onOpenGrowbox: (String) -> Unit,
    onOpenGrowGuide: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { GrowDataStore.initialize(context) }

    var showAdd by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(getString(com.growtracker.app.ui.language.GrowStrings.add_plant_button, languageManager)) }
            )
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner).padding(if (isSmallScreen) 8.dp else 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = getString(com.growtracker.app.ui.language.GrowStrings.my_plants_title, languageManager), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenGrowGuide) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(getString(com.growtracker.app.ui.language.Strings.overview_grow_guide, languageManager))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val plants by remember { derivedStateOf { GrowDataStore.plants.filter { !it.isDrying } } }

            if (plants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(getString(com.growtracker.app.ui.language.GrowStrings.no_plants, languageManager))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(plants, key = { it.id }) { plant ->
                        PlantRow(plant = plant, onClick = { onOpenGrowbox(it) })
                    }
                }
            }
        }

        if (showAdd) {
            AddPlantDialog(onCancel = { showAdd = false }, onAdd = { count, plant ->
                repeat(count.coerceAtLeast(1)) { GrowDataStore.addPlant(plant.copy(id = java.util.UUID.randomUUID().toString())) }
                showAdd = false
            })
        }
    }
}

@Composable
private fun PlantRow(plant: Plant, onClick: (String) -> Unit) {
    val isSmall = LocalConfiguration.current.screenWidthDp < 360
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick(plant.id) }, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val title = plant.name.ifBlank { plant.manufacturer.ifBlank { "Unbenannt" } }
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (!plant.strain.isNullOrBlank()) Text(text = plant.strain ?: "", style = MaterialTheme.typography.bodySmall)
                // Bloom status and ETA
                val bDays = bloomDays(plant)
                val eta = daysToHarvest(plant)
                if (bDays != null && plant.harvestDate == null) {
                    val line = buildString {
                        append(getString(com.growtracker.app.ui.language.GrowStrings.bloom_since_prefix, com.growtracker.app.ui.language.LocalLanguageManager.current))
                        append(" ")
                        append(bDays)
                        append(" ")
                        append(getString(com.growtracker.app.ui.language.GrowStrings.days_suffix, com.growtracker.app.ui.language.LocalLanguageManager.current))
                        if (eta != null) {
                            append(" · ")
                            append(getString(com.growtracker.app.ui.language.GrowStrings.eta_prefix, com.growtracker.app.ui.language.LocalLanguageManager.current))
                            append(" ")
                            append(eta)
                            append(" ")
                            append(getString(com.growtracker.app.ui.language.GrowStrings.eta_days_to_harvest, com.growtracker.app.ui.language.LocalLanguageManager.current))
                        }
                    }
                    Text(text = line, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (!plant.thcContent.isNullOrBlank()) Text(text = "THC: ${plant.thcContent}%", style = MaterialTheme.typography.labelSmall)
                if (!plant.cbdContent.isNullOrBlank()) Text(text = "CBD: ${plant.cbdContent}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlantDialog(onCancel: () -> Unit, onAdd: (Int, Plant) -> Unit) {
    var countText by remember { mutableStateOf("1") }
    val countInt = countText.toIntOrNull() ?: 0

    var manufacturer by remember { mutableStateOf("") }
    var strain by remember { mutableStateOf("") }
    var thc by remember { mutableStateOf("") }
    var cbd by remember { mutableStateOf("") }
    var plantType by remember { mutableStateOf(PlantType.FEMINIZED_HYBRID) }
    var germ by remember { mutableStateOf("") }
    var germinationEpoch by remember { mutableStateOf(0L) }

    var manufacturerExpanded by remember { mutableStateOf(false) }
    var strainExpanded by remember { mutableStateOf(false) }
    var strainQuery by remember { mutableStateOf("") }
    var potSize by remember { mutableStateOf("") }

    // Get unique manufacturers from StrainRepository and add a custom option
    val customManufacturerLabel = "Eigener Strain"
    val availableManufacturers = remember {
        // Always show the custom option at the top
        listOf(customManufacturerLabel) +
            StrainRepository.manufacturers
                .map { it.name }
                .distinct()
                .sortedBy { it.lowercase() }
    }
    val isCustomStrain = manufacturer == customManufacturerLabel
    // Separate query from the selected manufacturer so we can always show full list on open
    var manufacturerQuery by remember { mutableStateOf("") }
    // Precompute manufacturer -> strain count to avoid repeated lookups during list rendering
    val manufacturerCounts = remember {
        StrainRepository.manufacturers.associate { it.name.lowercase() to it.strains.size }
    }
    // Filter manufacturers by typed query; when query is blank, show the full list
    val filteredManufacturers = remember(manufacturerQuery, availableManufacturers) {
        val query = manufacturerQuery.trim()
        if (query.isBlank()) availableManufacturers
        else {
            val filtered = availableManufacturers
                .filter { it != customManufacturerLabel && it.contains(query, ignoreCase = true) }
            listOf(customManufacturerLabel) + filtered
        }
    }
    val strains = if (!isCustomStrain && manufacturer.isNotEmpty()) {
        (StrainRepository.manufacturers
            .find { it.name.equals(manufacturer, ignoreCase = true) }
            ?.strains ?: emptyList())
            .sortedBy { it.name.lowercase() }
    } else emptyList()

    // Do not auto-open strains; close it on manufacturer change to require manual open
    LaunchedEffect(manufacturer) {
        strainExpanded = false
        strainQuery = ""
    }

    if (strains.isNotEmpty()) {
        val selectedStrain = strains.find { it.name.equals(strain, ignoreCase = true) }
        selectedStrain?.let {
            LaunchedEffect(it) {
                thc = it.thcContent
                cbd = it.cbdContent
                plantType = it.type
            }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    fun showDatePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            context, { _, year, month, day ->
                germ = String.format("%02d.%02d.%04d", day, month + 1, year)
                germinationEpoch = java.time.LocalDate.of(year, month + 1, day).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            }, currentYear, currentMonth, currentDay
        )
        datePicker.show()
        Toast.makeText(context, "Wähle das Keimdatum", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(getString(com.growtracker.app.ui.language.GrowStrings.add_plant_button)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) countText = it },
                    label = { Text("Anzahl") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Manufacturer selection with dropdown
                Box {
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = {
                            manufacturer = it
                            manufacturerQuery = it
                            strain = ""
                        },
                        label = { Text("Hersteller") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        readOnly = false,
                        // Remove press-based toggling to avoid opening before anchor is ready
                        trailingIcon = {
                            IconButton(onClick = {
                                manufacturerExpanded = !manufacturerExpanded
                                if (manufacturerExpanded) manufacturerQuery = "" // always show full list on open
                            }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = manufacturerExpanded,
                        onDismissRequest = { manufacturerExpanded = false }
                    ) {
                        // Search box inside dropdown for manufacturers
                        OutlinedTextField(
                            value = manufacturerQuery,
                            onValueChange = { manufacturerQuery = it },
                            label = { Text("Suchen…") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // Show recent manufacturer suggestions matching query (if any)
                        val manuSuggestions = remember(manufacturerQuery, GrowDataStore.recentManufacturers, GrowDataStore.rememberSearchEnabled) {
                            if (!GrowDataStore.rememberSearchEnabled) emptyList() else {
                                val q = manufacturerQuery.trim()
                                val pool = GrowDataStore.recentManufacturers.filter { it != customManufacturerLabel }
                                if (q.isBlank()) pool.take(5) else pool.filter { it.startsWith(q, ignoreCase = true) }.take(5)
                            }
                        }
                        if (manuSuggestions.isNotEmpty()) {
                            HorizontalDivider()
                            manuSuggestions.forEach { sugg ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                                    text = { Text(sugg) },
                                    onClick = {
                                        manufacturer = sugg
                                        manufacturerExpanded = false
                                        strain = ""
                                        manufacturerQuery = ""
                                    }
                                )
                            }
                        }
                        HorizontalDivider()
                        // Scrollable list of manufacturers (safer inside DropdownMenu)
                        val manuList = filteredManufacturers
                        val manuScroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .heightIn(max = 320.dp)
                                .verticalScroll(manuScroll)
                        ) {
                            manuList.forEach { manu ->
                                val manuCount = if (manu == customManufacturerLabel) null else manufacturerCounts[manu.lowercase()] ?: 0
                                DropdownMenuItem(
                                    text = { Text(manu) },
                                    trailingIcon = {
                                        if (manuCount != null) {
                                            Text(
                                                text = manuCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        manufacturer = manu
                                        manufacturerExpanded = false
                                        strain = ""
                                        manufacturerQuery = ""
                                        if (manu == customManufacturerLabel) {
                                            // Reset auto-filled values for custom entry
                                            thc = ""
                                            cbd = ""
                                            strainExpanded = false
                                        } else {
                                            // Keep strains closed; user opens manually
                                            strainExpanded = false
                                            // persist to recents
                                            if (GrowDataStore.rememberSearchEnabled) {
                                                GrowDataStore.addRecentManufacturer(manu)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Strain selection with dropdown
                Box {
                    OutlinedTextField(
                        value = strain,
                        onValueChange = { strain = it },
                        label = { Text("Sorte") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        readOnly = !isCustomStrain && strains.isNotEmpty(),
                        enabled = true,
                        // Avoid press-based toggling; use trailing icon to open
                        trailingIcon = {
                            if (!isCustomStrain && strains.isNotEmpty()) {
                                IconButton(onClick = { strainExpanded = !strainExpanded }) {
                                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            }
                        }
                    )
                    if (!isCustomStrain && strains.isNotEmpty()) {
                        DropdownMenu(
                            expanded = strainExpanded,
                            onDismissRequest = { strainExpanded = false }
                        ) {
                            // Search box inside dropdown
                            OutlinedTextField(
                                value = strainQuery,
                                onValueChange = { strainQuery = it },
                                label = { Text("Suchen…") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            // Show recent strain suggestions (across manufacturers) matching query
                            val strainSuggestions = remember(strainQuery, GrowDataStore.recentStrains, GrowDataStore.rememberSearchEnabled) {
                                if (!GrowDataStore.rememberSearchEnabled) emptyList() else {
                                    val q = strainQuery.trim()
                                    val pool = GrowDataStore.recentStrains
                                    val list = if (q.isBlank()) pool else pool.filter { it.strain.startsWith(q, ignoreCase = true) }
                                    list.take(6)
                                }
                            }
                            if (strainSuggestions.isNotEmpty()) {
                                HorizontalDivider()
                                strainSuggestions.forEach { rs ->
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                                        text = { Text("${rs.strain} — ${rs.manufacturer}") },
                                        onClick = {
                                            // Apply manufacturer and strain from suggestion
                                            manufacturer = rs.manufacturer
                                            strain = rs.strain
                                            manufacturerExpanded = false
                                            strainExpanded = false
                                            // backfill THC/CBD/type if known
                                            val selectedManu = StrainRepository.manufacturers.find { it.name.equals(rs.manufacturer, ignoreCase = true) }
                                            val selectedStrain = selectedManu?.strains?.find { it.name.equals(rs.strain, ignoreCase = true) }
                                            if (selectedStrain != null) {
                                                thc = selectedStrain.thcContent
                                                cbd = selectedStrain.cbdContent
                                                plantType = selectedStrain.type
                                            }
                                        }
                                    )
                                }
                            }
                            HorizontalDivider()
                            val filteredStrains = remember(strainQuery, strains) {
                                val q = strainQuery.trim()
                                if (q.isBlank()) strains else strains.filter { it.name.contains(q, ignoreCase = true) }
                            }
                            // Scrollable list of strains (safer inside DropdownMenu)
                            val strainScroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 320.dp)
                                    .verticalScroll(strainScroll)
                            ) {
                                filteredStrains.forEach { strainData ->
                                    DropdownMenuItem(
                                        text = { Text(strainData.name) },
                                        onClick = {
                                            strain = strainData.name
                                            strainExpanded = false
                                            thc = strainData.thcContent
                                            cbd = strainData.cbdContent
                                            plantType = strainData.type
                                            strainQuery = ""
                                            // persist to recents (pair)
                                            if (manufacturer.isNotBlank() && GrowDataStore.rememberSearchEnabled) {
                                                GrowDataStore.addRecentStrain(manufacturer, strainData.name)
                                                GrowDataStore.addRecentManufacturer(manufacturer)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Strain count is shown inline in the manufacturer dropdown trailing icon

                // For custom strain: allow manual THC/CBD entry
                if (isCustomStrain) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = thc,
                            onValueChange = { thc = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' || ch == '-' || ch == '<' || ch == '>' } },
                            label = { Text("THC (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cbd,
                            onValueChange = { cbd = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' || ch == '-' || ch == '<' || ch == '>' } },
                            label = { Text("CBD (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Show the stored formatted germ value directly; avoid parsing a dd.MM.yyyy string with ISO parser (crash fix)
                OutlinedTextField(
                    value = germ,
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
                val defaultName = if (manufacturer.isNotBlank() && strain.isNotBlank()) "$manufacturer - $strain" else "Neue Pflanze"
                val plant = Plant(id = "", name = defaultName, manufacturer = manufacturer, strain = strain, thcContent = thc, cbdContent = cbd, germinationDate = if (germinationEpoch == 0L) null else germinationEpoch)
                onAdd(countInt.coerceAtLeast(1), plant)
            }, enabled = countInt > 0 && (!isCustomStrain || strain.isNotBlank())) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_ok)) }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_cancel)) } }
    )
}