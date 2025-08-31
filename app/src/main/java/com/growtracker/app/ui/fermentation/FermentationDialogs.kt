package com.growtracker.app.ui.fermentation

import com.growtracker.app.data.FermentationEntry
import com.growtracker.app.data.FermentationEntryType
import com.growtracker.app.data.Plant
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FermentationSettingsDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onUpdatePlant: (Plant) -> Unit
) {
    var showCompletionConfirmation by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var fermentationStartDate by remember { mutableStateOf(plant.fermentationStartDate ?: System.currentTimeMillis()) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fermentierung Einstellungen",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Plant Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = plant.strain,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Methode: ${plant.fermentationMethod.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Settings Options
                SettingsOption(
                    icon = Icons.Filled.CalendarToday,
                    title = "Fermentierungsstart bearbeiten",
                    description = "Startdatum der Fermentierung ändern",
                    onClick = { showDatePicker = true }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsOption(
                    icon = Icons.Filled.CheckCircle,
                    title = "Fermentierung abschließen",
                    description = "Pflanze als fertig markieren",
                    onClick = { showCompletionConfirmation = true },
                    iconTint = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Current Settings Info
                Text(
                    text = "Aktuelle Einstellungen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
                val fermentationDays = ((System.currentTimeMillis() - fermentationStartDate) / (1000 * 60 * 60 * 24)).toInt()
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Fermentierungsstart: ${dateFormat.format(Date(fermentationStartDate))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Fermentierungstage: $fermentationDays",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Einträge: ${plant.fermentationEntries.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Schließen")
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = fermentationStartDate,
            onDateSelected = { newDate ->
                fermentationStartDate = newDate
                val updatedPlant = plant.copy(fermentationStartDate = newDate)
                onUpdatePlant(updatedPlant)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Completion Confirmation Dialog
    if (showCompletionConfirmation) {
        CompletionConfirmationDialog(
            plant = plant,
            onDismiss = { showCompletionConfirmation = false },
            onConfirm = {
                val updatedPlant = plant.copy(
                    isFermenting = false,
                    // Could add a completion date field if needed
                )
                onUpdatePlant(updatedPlant)
                showCompletionConfirmation = false
                onDismiss()
            }
        )
    }
}

@Composable
fun SettingsOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Fermentierungsstart bearbeiten")
        },
        text = {
            Column {
                Text(
                    text = "Aktuelles Datum: ${dateFormat.format(Date(selectedDate))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple date adjustment buttons for now
                // In a full implementation, you might want to use a proper DatePicker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            calendar.add(Calendar.DAY_OF_MONTH, -1)
                            selectedDate = calendar.timeInMillis
                        }
                    ) {
                        Text("-1 Tag")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            selectedDate = calendar.timeInMillis
                        }
                    ) {
                        Text("+1 Tag")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            calendar.add(Calendar.WEEK_OF_YEAR, -1)
                            selectedDate = calendar.timeInMillis
                        }
                    ) {
                        Text("-1 Woche")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            calendar.add(Calendar.WEEK_OF_YEAR, 1)
                            selectedDate = calendar.timeInMillis
                        }
                    ) {
                        Text("+1 Woche")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected(selectedDate) }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun CompletionConfirmationDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fermentierung abschließen")
            }
        },
        text = {
            Column {
                Text(
                    text = "Möchtest du die Fermentierung von '${plant.strain}' abschließen?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Die Pflanze wird aus der Fermentierung entfernt und als fertig markiert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diese Aktion kann nicht rückgängig gemacht werden.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Abschließen", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFermentationEntryDialog(
    onDismiss: () -> Unit,
    onAddEntry: (FermentationEntry) -> Unit,
    initialDate: Long? = null
) {
    var selectedType by remember { mutableStateOf(FermentationEntryType.VENTILATION) }
    var notes by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf(initialDate ?: System.currentTimeMillis()) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Neuer Eintrag",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Entry Type Selection
                Text(
                    text = "Eintragstyp",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FermentationEntryType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    placeholder = { Text("Beschreibung des Eintrags...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Conditional fields based on type
                if (selectedType in listOf(FermentationEntryType.HUMIDITY_CHECK, FermentationEntryType.VENTILATION)) {
                    OutlinedTextField(
                        value = humidity,
                        onValueChange = { humidity = it },
                        label = { Text("Luftfeuchtigkeit (%)") },
                        placeholder = { Text("z.B. 62") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("%", style = MaterialTheme.typography.bodyMedium) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    // Selected date with +/- controls (limit future date to +7 days)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val todayCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        val maxAllowed = todayCal.timeInMillis + 7L * 24 * 60 * 60 * 1000

                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = entryDate }
                            cal.add(Calendar.DAY_OF_MONTH, -1)
                            entryDate = cal.timeInMillis
                        }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "-1 Tag")
                        }

                        Text(text = dateFormat.format(Date(entryDate)), style = MaterialTheme.typography.titleSmall)

                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = entryDate }
                            cal.add(Calendar.DAY_OF_MONTH, 1)
                            val newTime = cal.timeInMillis
                            if (newTime <= maxAllowed) entryDate = newTime
                        }) {
                            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "+1 Tag")
                        }
                    }

                }
                
                if (selectedType in listOf(FermentationEntryType.TEMPERATURE_CHECK, FermentationEntryType.VENTILATION)) {
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = { Text("Temperatur (°C)") },
                        placeholder = { Text("z.B. 20") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("°C", style = MaterialTheme.typography.bodyMedium) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen")
                    }
                    
                    Button(
                        onClick = {
                            val entry = FermentationEntry(
                                id = UUID.randomUUID().toString(),
                                date = entryDate,
                                type = selectedType,
                                notes = notes,
                                humidity = humidity,
                                temperature = temperature,
                                isVentilated = selectedType == FermentationEntryType.VENTILATION
                            )
                            onAddEntry(entry)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = notes.isNotEmpty() || humidity.isNotEmpty() || temperature.isNotEmpty()
                    ) {
                        Text("Hinzufügen")
                    }
                }
            }
        }
    }
}
