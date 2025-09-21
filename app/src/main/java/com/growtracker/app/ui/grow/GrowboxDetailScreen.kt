package com.growtracker.app.ui.grow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.data.Plant
import com.growtracker.app.data.Growbox

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

    LaunchedEffect(key1 = GrowDataStore.growboxes) { box = GrowDataStore.getGrowbox(growboxId) }

    if (selectedPlant != null) {
        PlantDetailScreen(plant = selectedPlant!!, onBack = { selectedPlant = null })
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(box?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = { /* settings placeholder */ }) { Icon(Icons.Filled.MoreVert, contentDescription = "Settings") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddPlantDialog = true }, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("") })
        }
    ) { inner ->
        box?.let { growbox ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(inner), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Card {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Column {
                                Text("${growbox.width} x ${growbox.height} x ${growbox.depth} cm")
                                Text("${growbox.lightType} ${growbox.lightPower}W")
                            }
                        }
                    }
                }
                item {
                    Text("Pflanzen (${growbox.plants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (growbox.plants.isEmpty()) item {
                    Text("Noch keine Pflanzen", style = MaterialTheme.typography.bodyMedium)
                }
                else items(growbox.plants, key = { it.id }) { plant ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { selectedPlant = plant }) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plant.name, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    plant.thcContent.takeIf { it.isNotBlank() }?.let { Text("THC: $it%", style = MaterialTheme.typography.labelSmall) }
                                    plant.cbdContent.takeIf { it.isNotBlank() }?.let { Text("CBD: $it%", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPlantDialog && box != null) {
        AddPlantDialog(onCancel = { showAddPlantDialog = false }, onAdd = { count, name, manufacturer, strain, potSize, fertilizer, phase, type, bloomStart, harvest, thc, cbd, germ ->
            val newPlants = (1..count).map {
                Plant(id = java.util.UUID.randomUUID().toString(), name = name, manufacturer = manufacturer, strain = strain, thcContent = thc ?: "", cbdContent = cbd ?: "", germinationDate = null)
            }
            val updated = box!!.copy(plants = box!!.plants + newPlants)
            GrowDataStore.updateGrowbox(updated)
            box = updated
            showAddPlantDialog = false
        })
    }
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
