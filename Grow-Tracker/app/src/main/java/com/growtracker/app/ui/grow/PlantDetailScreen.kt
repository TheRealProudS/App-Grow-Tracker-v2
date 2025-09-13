@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.growtracker.app.ui.grow

// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.*
// using fully-qualified icon references in this file
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
// keyboard input options will be referenced with fully-qualified names to avoid import issues
import androidx.compose.ui.unit.dp
import com.growtracker.app.data.Plant
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PlantDetailScreen(
    plant: Plant,
    onBack: () -> Unit
) {
    var settingsOpen by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var weekOffset by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth().height(48.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back") }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { settingsOpen = true }) { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings") }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PlantInfoCard(plant) }
            item { SimpleCalendarView(weekOffset = weekOffset, selectedDate = selectedDate, onSelectDate = { d -> selectedDate = d }, onWeekChange = { delta -> weekOffset += delta }) }
            item { EntriesSection(entries = plant.entries, plantId = plant.id, preferredFertilizerManufacturer = plant.preferredFertilizerManufacturer, selectedDate = selectedDate, onEntryAdded = { selectedDate = null }) }
        }
    }

    // Bottom-right action FAB removed to avoid duplicate entry actions; use the entries "Hinzufügen" button instead.

    if (settingsOpen) {
        PlantSettingsDialog(plant = plant, onClose = { settingsOpen = false }, onUpdate = { updated -> GrowDataStore.updatePlant(updated) }, onDelete = { id -> GrowDataStore.removePlant(id); onBack() })
    }

    if (showWaterDialog) {
        var liters by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            title = { Text("Gießen") },
            text = {
                Column { OutlinedTextField(value = liters, onValueChange = { liters = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Liter") }) }
            },
            confirmButton = {
                TextButton(onClick = {
                    val entry = com.growtracker.app.data.PlantEntry(id = java.util.UUID.randomUUID().toString(), date = System.currentTimeMillis(), type = com.growtracker.app.data.EntryType.WATERING, value = liters.ifBlank { "0" }, notes = "", fertilizerEntries = emptyList())
                    GrowDataStore.addEntryToPlant(plant.id, entry)
                    showWaterDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showWaterDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showNotesDialog) {
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Notizen") },
            text = { Column { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notiz") }) } },
            confirmButton = {
                TextButton(onClick = {
                    val entry = com.growtracker.app.data.PlantEntry(id = java.util.UUID.randomUUID().toString(), date = System.currentTimeMillis(), type = com.growtracker.app.data.EntryType.NOTE, value = notes.ifBlank { "" }, notes = notes)
                    GrowDataStore.addEntryToPlant(plant.id, entry)
                    showNotesDialog = false
                }) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { showNotesDialog = false }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun PlantInfoCard(plant: Plant) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Title row: name/strain and manufacturer
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(plant.name.ifBlank { plant.strain.ifBlank { "Unbenannt" } }, style = MaterialTheme.typography.titleLarge)
                    // show strain and manufacturer combined as a single slightly larger secondary line
                    val secondary = when {
                        plant.strain.isNotBlank() && plant.manufacturer.isNotBlank() -> "${plant.strain} • ${plant.manufacturer}"
                        plant.strain.isNotBlank() -> plant.strain
                        plant.manufacturer.isNotBlank() -> plant.manufacturer
                        else -> ""
                    }
                    if (secondary.isNotBlank()) Text(secondary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PhasePill(derivePhase(plant), deriveAgeWeeks(plant))
            }

            // Badges: THC / CBD / Pot / Fertilizer (avoid duplicate manufacturer)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (plant.thcContent.isNotBlank()) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) { Text("THC ${plant.thcContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer) }
                if (plant.cbdContent.isNotBlank()) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) { Text("CBD ${plant.cbdContent}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer) }
                // show preferred fertilizer only if it's different from manufacturer to avoid duplication
                plant.preferredFertilizerManufacturer?.takeIf { it.isNotBlank() && it != plant.manufacturer }?.let { Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(it, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) } }
            }

            // Dates and light/pot info
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val germText = plant.germinationDate?.let {
                    val d = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").format(d)
                } ?: "-"
                Column { Text("Keimdatum", style = MaterialTheme.typography.labelSmall); Text(germText, style = MaterialTheme.typography.bodySmall) }

                val bloomText = plant.floweringStartDate?.let {
                    val d = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").format(d)
                } ?: "-"
                Column { Text("Blütebeginn", style = MaterialTheme.typography.labelSmall); Text(bloomText, style = MaterialTheme.typography.bodySmall) }

                if (!plant.lightType.isNullOrBlank() || plant.lightWatt != null) {
                    Column { Text("Licht", style = MaterialTheme.typography.labelSmall); Text("${plant.lightType ?: "-"} ${plant.lightWatt?.let { "${it}W" } ?: ""}", style = MaterialTheme.typography.bodySmall) }
                }

                if (plant.customPotSize != null && plant.customPotSize.isNotBlank()) {
                    Column { Text("Topf", style = MaterialTheme.typography.labelSmall); Text(plant.customPotSize, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun PhasePill(phase: String, age: String) {
    Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.FilterVintage, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(phase, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun deriveAgeWeeks(plant: Plant): String {
    val start = plant.germinationDate ?: plant.plantingDate
    if (start <= 0L) return "Woche 1"
    val days = ((System.currentTimeMillis() - start) / (1000L * 60 * 60 * 24)).toInt()
    return "Woche ${days / 7 + 1}"
}

private fun derivePhase(plant: Plant): String = when {
    plant.harvestDate != null -> "Ernte"
    plant.floweringStartDate != null -> "Blüte"
    else -> "Wachstum"
}

@Composable
private fun maxAllowedFuture(): Long {
    val todayCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    return todayCal.timeInMillis + 7L * 24 * 60 * 60 * 1000
}

@Composable
private fun SimpleCalendarView(
    weekOffset: Int = 0,
    selectedDate: Long? = null,
    onSelectDate: (Long) -> Unit = {},
    onWeekChange: (Int) -> Unit = {}
) {
    val base = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, weekOffset); set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    IconButton(onClick = { onWeekChange(-1) }) { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Vorwoche") }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Woche", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
    IconButton(onClick = { onWeekChange(1) }) { Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Nächste Woche") }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(7) { offset ->
            val c = (base.clone() as Calendar).apply { add(Calendar.DATE, offset) }
            val label = SimpleDateFormat("dd.MM", Locale.getDefault()).format(c.time)
            val isSelected = selectedDate != null && selectedDate == c.timeInMillis
            val maxAllowed = maxAllowedFuture()
            val enabled = c.timeInMillis <= maxAllowed
            Button(onClick = { if (enabled) onSelectDate(c.timeInMillis) }, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary), enabled = enabled) { Text(label) }
        }
    }
}



@Composable
private fun EntriesSection(entries: List<com.growtracker.app.data.PlantEntry>, plantId: String, preferredFertilizerManufacturer: String?, selectedDate: Long? = null, onEntryAdded: () -> Unit = {}) {
    val context = LocalContext.current
    var addOpen by remember { mutableStateOf(false) }
    // Determine which entries to show: entries for the selected day; if none selected, default to today
    val displayDateMillis = selectedDate ?: Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

    fun sameDay(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    val displayedEntries = entries.filter { e -> sameDay(e.date, displayDateMillis) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val headerDate = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(java.util.Date(displayDateMillis))
            Text("Einträge — $headerDate", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { addOpen = true }) { Text("Hinzufügen") }
        }

        if (displayedEntries.isEmpty()) {
            Text("Keine Einträge für diesen Tag", modifier = Modifier.padding(12.dp))
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 180.dp), modifier = Modifier.heightIn(max = 320.dp)) {
                items(displayedEntries) { e ->
                    var showEntryAction by remember { mutableStateOf(false) }
                    var showEditDialog by remember { mutableStateOf(false) }
                    Card(modifier = Modifier.padding(6.dp)) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        // long press triggers action sheet
                                        showEntryAction = true
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconPainter = loadEntryIconOrPlaceholder(e.type.name.lowercase())
                            Image(painter = iconPainter, contentDescription = e.type.displayName, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(e.type.displayName, style = MaterialTheme.typography.titleSmall)
                                Text(e.value, style = MaterialTheme.typography.bodySmall)
                                if (e.notes.isNotBlank()) Text(e.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Action AlertDialog for Edit/Delete
                        if (showEntryAction) {
                            AlertDialog(
                                onDismissRequest = { showEntryAction = false },
                                title = { Text("Eintrag") },
                                text = { Text("Möchtest du diesen Eintrag bearbeiten oder löschen?") },
                                confirmButton = {
                                    TextButton(onClick = { showEditDialog = true; showEntryAction = false }) { Text("Bearbeiten") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { 
                                        // delete
                                        GrowDataStore.removeEntryFromPlant(plantId, e.id)
                                        showEntryAction = false
                                    }) { Text("Löschen") }
                                }
                            )
                        }

                        // Simple edit dialog (edit value + notes)
                        if (showEditDialog) {
                            var editValue by remember { mutableStateOf(e.value) }
                            var editNotes by remember { mutableStateOf(e.notes) }
                            AlertDialog(
                                onDismissRequest = { showEditDialog = false },
                                title = { Text("Eintrag bearbeiten") },
                                text = {
                                    Column {
                                        OutlinedTextField(value = editValue, onValueChange = { editValue = it }, label = { Text("Wert") })
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(value = editNotes, onValueChange = { editNotes = it }, label = { Text("Notizen") })
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val updated = e.copy(value = editValue, notes = editNotes)
                                        GrowDataStore.updateEntryInPlant(plantId, updated)
                                        showEditDialog = false
                                    }) { Text("Speichern") }
                                },
                                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Abbrechen") } }
                            )
                        }
                    }
                }
            }
        }

        if (addOpen) {
            AddEntryDialog(onClose = { addOpen = false }, preferredManufacturer = preferredFertilizerManufacturer, onAdd = { entry ->
                // If a date was selected in the calendar, use it (but only up to +7 days allowed)
                val dateToUse = selectedDate ?: System.currentTimeMillis()
                val safeDate = run {
                    val todayCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                    val maxAllowed = todayCal.timeInMillis + 7L * 24 * 60 * 60 * 1000
                    if (dateToUse <= maxAllowed) dateToUse else System.currentTimeMillis()
                }
                val entryWithDate = entry.copy(date = safeDate)
                GrowDataStore.addEntryToPlant(plantId, entryWithDate)
                addOpen = false
                onEntryAdded()
            })
        }
    }
}

@Composable
private fun AddEntryDialog(onClose: () -> Unit, preferredManufacturer: String? = null, onAdd: (com.growtracker.app.data.PlantEntry) -> Unit) {
    var selectedType by remember { mutableStateOf(com.growtracker.app.data.EntryType.WATERING) }
    var liters by remember { mutableStateOf("") }
    var fertilizers by remember { mutableStateOf(listOf<com.growtracker.app.data.FertilizerEntry>()) }
    var fertilizerProduct by remember { mutableStateOf("") }
    var fertilizerDosage by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val typeOptions = listOf(
        com.growtracker.app.data.EntryType.WATERING,
        com.growtracker.app.data.EntryType.FERTILIZING,
        com.growtracker.app.data.EntryType.HEIGHT,
        com.growtracker.app.data.EntryType.TEMPERATURE,
        com.growtracker.app.data.EntryType.HUMIDITY,
        com.growtracker.app.data.EntryType.TOPPING,
        com.growtracker.app.data.EntryType.LST,
        com.growtracker.app.data.EntryType.NOTE
    )

    AlertDialog(onDismissRequest = onClose, title = { Text("Eintrag hinzufügen") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // select type
            var typeExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(value = selectedType.displayName, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { typeExpanded = !typeExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } })
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    typeOptions.forEach { t -> DropdownMenuItem(text = { Text(t.displayName) }, onClick = { selectedType = t; typeExpanded = false }) }
                }
            }

            when (selectedType) {
                com.growtracker.app.data.EntryType.WATERING -> {
                    OutlinedTextField(value = liters, onValueChange = { liters = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Liter") })
                }
                com.growtracker.app.data.EntryType.FERTILIZING -> {
                    // add multiple fertilizers
                    Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Product: if preferredManufacturer is set, show dropdown of products from DataSource; otherwise free text
                                if (!preferredManufacturer.isNullOrBlank()) {
                                    // load manufacturer products
                                    val manufacturers = com.growtracker.app.data.getFertilizerManufacturers()
                                    val products = manufacturers.find { it.name.equals(preferredManufacturer, ignoreCase = true) }?.products ?: emptyList()
                                    var prodExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(value = fertilizerProduct, onValueChange = {}, label = { Text("Produkt") }, readOnly = true, trailingIcon = { IconButton(onClick = { prodExpanded = !prodExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } }, modifier = Modifier.fillMaxWidth())
                                        DropdownMenu(expanded = prodExpanded, onDismissRequest = { prodExpanded = false }) {
                                            products.forEach { p ->
                                                DropdownMenuItem(text = { Text(p.name) }, onClick = { fertilizerProduct = p.name; prodExpanded = false })
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedTextField(value = fertilizerProduct, onValueChange = { fertilizerProduct = it }, label = { Text("Produkt") }, modifier = Modifier.weight(1f))
                                }
                            }
                        Row { OutlinedTextField(value = fertilizerDosage, onValueChange = { fertilizerDosage = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Dosierung (ml/L)") }) }
                        Row { TextButton(onClick = {
                            if (fertilizerProduct.isNotBlank()) {
                                val prod = com.growtracker.app.data.FertilizerProduct(name = fertilizerProduct, category = "", npk = null)
                                val fe = com.growtracker.app.data.FertilizerEntry(product = prod, dosage = fertilizerDosage, manufacturer = preferredManufacturer ?: "")
                                fertilizers = fertilizers + fe
                                fertilizerProduct = ""; fertilizerDosage = ""
                            }
                        }) { Text("Dünger hinzufügen") } }
                        if (fertilizers.isNotEmpty()) Column { fertilizers.forEach { f -> Text("- ${if (f.manufacturer.isNotBlank()) "${f.manufacturer} " else ""}${f.product.name}: ${f.dosage} ml/L") } }
                    }
                }
                com.growtracker.app.data.EntryType.HEIGHT -> { OutlinedTextField(value = heightCm, onValueChange = { heightCm = it.filter { ch -> ch.isDigit() } }, label = { Text("cm") }) }
                com.growtracker.app.data.EntryType.TEMPERATURE -> { OutlinedTextField(value = temperature, onValueChange = { temperature = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("°C") }) }
                com.growtracker.app.data.EntryType.HUMIDITY -> { OutlinedTextField(value = humidity, onValueChange = { humidity = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("%") }) }
                com.growtracker.app.data.EntryType.TOPPING -> { Text("Topping eingetragen") }
                com.growtracker.app.data.EntryType.LST -> { Text("Low Stress Training eingetragen") }
                com.growtracker.app.data.EntryType.NOTE -> { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notiz") }) }
                else -> {}
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            val id = java.util.UUID.randomUUID().toString()
            val entry = when (selectedType) {
                com.growtracker.app.data.EntryType.WATERING -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = liters.ifBlank { "0" }, notes = "")
                com.growtracker.app.data.EntryType.FERTILIZING -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = "${fertilizers.size} Dünger", notes = fertilizers.joinToString { "${it.manufacturer}:${it.product.name}@${it.dosage}" }, fertilizerEntries = fertilizers)
                com.growtracker.app.data.EntryType.HEIGHT -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = heightCm.ifBlank { "0" }, notes = "")
                com.growtracker.app.data.EntryType.TEMPERATURE -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = temperature.ifBlank { "" }, notes = "")
                com.growtracker.app.data.EntryType.HUMIDITY -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = humidity.ifBlank { "" }, notes = "")
                com.growtracker.app.data.EntryType.TOPPING -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = "Topping", notes = "")
                com.growtracker.app.data.EntryType.LST -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = "LST", notes = "")
                com.growtracker.app.data.EntryType.NOTE -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = notes.ifBlank { "" }, notes = notes)
                else -> com.growtracker.app.data.PlantEntry(id = id, date = System.currentTimeMillis(), type = selectedType, value = "", notes = notes)
            }
            onAdd(entry)
        }) { Text("Hinzufügen") }
    }, dismissButton = { TextButton(onClick = onClose) { Text("Abbrechen") } })
}

@Composable
private fun loadEntryIconOrPlaceholder(name: String): androidx.compose.ui.graphics.painter.Painter {
    val ctx = LocalContext.current
    val resId = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
    // Avoid try/catch around composable calls; decide drawable id first and then call painterResource once.
    val drawableId = if (resId != 0) resId else android.R.drawable.ic_menu_help
    return painterResource(id = drawableId)
}

@Composable
private fun PlantSettingsDialog(
    plant: Plant,
    onClose: () -> Unit,
    onUpdate: (Plant) -> Unit,
    onDelete: (String) -> Unit
) {
    var pot by remember { mutableStateOf(plant.customPotSize ?: "") }
    var fertilizer by remember { mutableStateOf(plant.preferredFertilizerManufacturer ?: "") }
    var lightType by remember { mutableStateOf(plant.lightType ?: "") }
    var lightWattStr by remember { mutableStateOf(plant.lightWatt?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Pflanze bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = pot, onValueChange = { pot = it }, label = { Text("Topf (L)") })
                // Dünger-Hersteller dropdown
                val fertilizerOptions = listOf("BioBizz", "Hesi", "Advanced Nutrients", "Eigen")
                var fertilizerExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = fertilizer,
                        onValueChange = { fertilizer = it },
                        label = { Text("Dünger-Hersteller") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = { IconButton(onClick = { fertilizerExpanded = !fertilizerExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } }
                    )
                    DropdownMenu(expanded = fertilizerExpanded, onDismissRequest = { fertilizerExpanded = false }) {
                        fertilizerOptions.forEach { opt -> 
                            DropdownMenuItem(text = { Text(opt) }, onClick = { fertilizer = opt; fertilizerExpanded = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Lichtart dropdown (larger field) and Watt (smaller)
                    val lightOptions = listOf("LED", "HPS", "CMH", "Leuchtstoffröhre", "Sonstige")
                    var lightExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(0.7f)) {
                        OutlinedTextField(
                            value = lightType,
                            onValueChange = { lightType = it },
                            label = { Text("Lichtart") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { IconButton(onClick = { lightExpanded = !lightExpanded }) { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null) } }
                        )
                        DropdownMenu(expanded = lightExpanded, onDismissRequest = { lightExpanded = false }) {
                            lightOptions.forEach { opt -> 
                                DropdownMenuItem(text = { Text(opt) }, onClick = { lightType = opt; lightExpanded = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = lightWattStr,
                        onValueChange = { lightWattStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Watt") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val lightWatt = lightWattStr.toIntOrNull()
                val updated = plant.copy(
                    customPotSize = pot,
                    preferredFertilizerManufacturer = fertilizer.ifBlank { null },
                    lightType = lightType.ifBlank { null },
                    lightWatt = lightWatt
                )
                onUpdate(updated)
                onClose()
            }) { Text("Speichern") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(plant.id); onClose() }) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onClose) { Text("Abbrechen") }
            }
        }
    )
}
