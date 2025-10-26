package com.growtracker.app.ui.grow

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var potSize by remember { mutableStateOf("") }

    // Get unique manufacturers from StrainRepository
    val availableManufacturers = StrainRepository.manufacturers.map { it.name }.distinct().sortedBy { it.lowercase() }
    val strains = if (manufacturer.isNotEmpty()) {
        StrainRepository.manufacturers.find { it.name.equals(manufacturer, ignoreCase = true) }?.strains ?: emptyList()
    } else emptyList()

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
                            strain = ""
                        },
                        label = { Text("Hersteller") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = false,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is androidx.compose.foundation.interaction.PressInteraction.Press) {
                                            manufacturerExpanded = !manufacturerExpanded
                                        }
                                    }
                                }
                            },
                        trailingIcon = {
                            IconButton(onClick = { manufacturerExpanded = !manufacturerExpanded }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = manufacturerExpanded,
                        onDismissRequest = { manufacturerExpanded = false }
                    ) {
                        availableManufacturers.forEach { manu ->
                            DropdownMenuItem(
                                text = { Text(manu) },
                                onClick = {
                                    manufacturer = manu
                                    manufacturerExpanded = false
                                    strain = ""
                                }
                            )
                        }
                    }
                }

                // Strain selection with dropdown
                Box {
                    OutlinedTextField(
                        value = strain,
                        onValueChange = { strain = it },
                        label = { Text("Sorte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = false,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is androidx.compose.foundation.interaction.PressInteraction.Press) {
                                            strainExpanded = !strainExpanded
                                        }
                                    }
                                }
                            },
                        trailingIcon = {
                            IconButton(onClick = { strainExpanded = !strainExpanded }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = strainExpanded,
                        onDismissRequest = { strainExpanded = false }
                    ) {
                        strains.forEach { strainData ->
                            DropdownMenuItem(
                                text = { Text(strainData.name) },
                                onClick = {
                                    strain = strainData.name
                                    strainExpanded = false
                                    thc = strainData.thcContent
                                    cbd = strainData.cbdContent
                                    plantType = strainData.type
                                }
                            )
                        }
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
            }, enabled = countInt > 0) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_ok)) }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text(getString(com.growtracker.app.ui.language.GrowStrings.generic_cancel)) } }
    )
}