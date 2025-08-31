package com.growtracker.app.ui.grow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// using fully-qualified icon references in this file
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantEntry as DataPlantEntry
import com.growtracker.app.data.EntryType as DataEntryType
import com.growtracker.app.data.Growbox
import com.growtracker.app.ui.grow.PlantDetailScreen

// Local simplified EntryType for UI dialogs
enum class EntryType {
    WATER, FERTILIZER, SIZE, TEMPERATURE, HUMIDITY, TOPPING, LOLLIPOPPING, LST, NOTE
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowboxDetailScreen(
    languageManager: LanguageManager,
    growboxId: String,
    onBack: () -> Unit
) {
    var box by remember { mutableStateOf<Growbox?>(GrowDataStore.getGrowbox(growboxId)) }
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }

    LaunchedEffect(key1 = GrowDataStore.growboxes) {
        box = GrowDataStore.getGrowbox(growboxId)
    }

    if (selectedPlant != null) {
        PlantDetailScreen(plant = selectedPlant!!, onBack = { selectedPlant = null })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(box?.name ?: "") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back") }
                    },
                    actions = {
                        IconButton(onClick = { /* settings placeholder */ }) { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Settings") }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddPlantDialog = true },
                    icon = { Icon(imageVector = Icons.Filled.Add, null) },
                    text = { Text("") }
                )
            }
        ) { inner ->
            box?.let { growbox ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card { BoxInfo(languageManager, growbox) }
                    }
                    item {
                        Text("Pflanzen (${growbox.plants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (growbox.plants.isEmpty()) {
                        item { Text("Noch keine Pflanzen", style = MaterialTheme.typography.bodyMedium) }
                    } else {
                        items(growbox.plants, key = { it.id }) { plant ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth().clickable { selectedPlant = plant }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(plant.name, style = MaterialTheme.typography.bodyLarge)
                                        Spacer(Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            plant.thcContent.takeIf { it.isNotBlank() }?.let { Text("THC: $it%", style = MaterialTheme.typography.labelSmall) }
                                            plant.cbdContent.takeIf { it.isNotBlank() }?.let { Text("CBD: $it%", style = MaterialTheme.typography.labelSmall) }
                                            plant.germinationDate?.let {
                                                val dateStr = runCatching {
                                                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                                }.getOrNull()?.let { d -> java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").format(d) }
                                                if (dateStr != null) Text("Keimung: $dateStr", style = MaterialTheme.typography.labelSmall)
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

    if (showAddPlantDialog && box != null) {
        AddPlantDialog(onCancel = { showAddPlantDialog = false }, onAdd = { count, plantName, manufacturer, strain, potSize, fertilizer, phase, type, bloomStartDate, harvestDate, thc, cbd, germinationDate ->
            val germinationEpoch: Long? = germinationDate?.let {
                runCatching { java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
            }
            val newPlants = (1..count).map {
                Plant(
                    name = plantName,
                    manufacturer = manufacturer,
                    strain = strain,
                    preferredFertilizerManufacturer = fertilizer.ifBlank { null },
                    potSize = com.growtracker.app.data.PotSize.MEDIUM, // TODO map string potSize to enum / custom
                    floweringStartDate = bloomStartDate?.let { runCatching { java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull() },
                    harvestDate = harvestDate?.let { runCatching { java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull() },
                    thcContent = thc ?: "",
                    cbdContent = cbd ?: "",
                    germinationDate = germinationEpoch,
                    type = when(type.lowercase()) {
                        "auto","autoflower" -> com.growtracker.app.data.PlantType.AUTOFLOWER
                        "sativa" -> com.growtracker.app.data.PlantType.FEMINIZED_SATIVA
                        "indica" -> com.growtracker.app.data.PlantType.FEMINIZED_INDICA
                        else -> com.growtracker.app.data.PlantType.FEMINIZED_HYBRID
                    }
                )
            }
            val updated = box!!.copy(plants = box!!.plants + newPlants)
            GrowDataStore.updateGrowbox(updated)
            box = updated
            showAddPlantDialog = false
        })
    }
}


@Composable
private fun SpeedDialIcon(type: EntryType, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(onClick = onClick, modifier = Modifier.size(40.dp), containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = label, modifier = Modifier.size(20.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AddEntryDialog(type: EntryType, onCancel: () -> Unit, onAdd: (DataPlantEntry) -> Unit) {
    var value by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val today = remember { java.time.LocalDate.now().toString() }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Eintrag hinzufügen: $type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (type != EntryType.NOTE) {
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Wert") })
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Notiz") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val mapped = when(type){
                    EntryType.WATER -> DataEntryType.WATERING
                    EntryType.FERTILIZER -> DataEntryType.FERTILIZING
                    EntryType.SIZE -> DataEntryType.HEIGHT
                    EntryType.TEMPERATURE -> DataEntryType.TEMPERATURE
                    EntryType.HUMIDITY -> DataEntryType.HUMIDITY
                    EntryType.TOPPING -> DataEntryType.TOPPING
                    EntryType.LOLLIPOPPING -> DataEntryType.LOLLIPOPPING
                    EntryType.LST -> DataEntryType.LST
                    EntryType.NOTE -> DataEntryType.NOTE
                }
                val epochDay = java.time.LocalDate.parse(today).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                onAdd(DataPlantEntry(type = mapped, value = value, notes = note, date = epochDay))
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Abbrechen") } }
    )
}

@Composable
private fun BoxInfo(languageManager: LanguageManager, growbox: Growbox) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${growbox.width} x ${growbox.height} x ${growbox.depth} cm", style = MaterialTheme.typography.bodyMedium)
            Text("${growbox.lightType} ${growbox.lightPower}W", style = MaterialTheme.typography.bodyMedium)
            val status = if (growbox.isActive) Strings.grow_details_active[languageManager.currentLanguage] else Strings.grow_details_inactive[languageManager.currentLanguage]
            Text(status ?: "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlantDialog(
    onCancel: () -> Unit,
    onAdd: (
        Int, String, String, String, String, String, String, String, String?, String?, String, String, String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("1") }
    var manufacturer by remember { mutableStateOf("") }
    var strain by remember { mutableStateOf("") }
    var potSize by remember { mutableStateOf("") }
    var fertilizer by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("Wachstum") }
    var type by remember { mutableStateOf("Fem") }
    var bloomStartDate by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }
    var thc by remember { mutableStateOf("") }
    var cbd by remember { mutableStateOf("") }
    var germinationDate by remember { mutableStateOf("") }
    val countInt = count.toIntOrNull() ?: 1
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Pflanze hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = count, onValueChange = { if (it.all(Char::isDigit) && it.isNotBlank()) count = it }, label = { Text("Anzahl") })
                OutlinedTextField(value = manufacturer, onValueChange = { manufacturer = it }, label = { Text("Samen-Hersteller") })
                OutlinedTextField(value = strain, onValueChange = { strain = it }, label = { Text("Sorte") })
                OutlinedTextField(value = potSize, onValueChange = { potSize = it }, label = { Text("Topfgröße (L)") })
                OutlinedTextField(value = fertilizer, onValueChange = { fertilizer = it }, label = { Text("Dünger-Hersteller") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = thc, onValueChange = { thc = it }, label = { Text("THC (%)") })
                    OutlinedTextField(value = cbd, onValueChange = { cbd = it }, label = { Text("CBD (%)") })
                }
                OutlinedTextField(value = germinationDate, onValueChange = { germinationDate = it }, label = { Text("Keimdatum (YYYY-MM-DD)") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                        OutlinedTextField(
                            value = phase,
                            onValueChange = { phase = it },
                            label = { Text("Phase") },
                            readOnly = true
                        )
                    }
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Art") },
                            readOnly = true
                        )
                    }
                }
                OutlinedTextField(value = bloomStartDate, onValueChange = { bloomStartDate = it }, label = { Text("Blütebeginn (YYYY-MM-DD)") })
                OutlinedTextField(value = harvestDate, onValueChange = { harvestDate = it }, label = { Text("Erntedatum (optional)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && countInt > 0 && potSize.isNotBlank() && fertilizer.isNotBlank())
                        onAdd(countInt, name, manufacturer, strain, potSize, fertilizer, phase, type, bloomStartDate.ifBlank { null }, harvestDate.ifBlank { null }, thc, cbd, germinationDate.ifBlank { null })
                },
                enabled = name.isNotBlank() && countInt > 0 && potSize.isNotBlank() && fertilizer.isNotBlank()
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Abbrechen") } }
    )
}
