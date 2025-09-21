package com.growtracker.app.ui.fermentation

import com.growtracker.app.data.GrowDataManager
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantPhoto
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.growtracker.app.ui.language.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FermentationScreen(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager,
    onNavigateUp: () -> Unit = {},
    onPlantClick: (Plant) -> Unit = {}
) {
    val context = LocalContext.current
    val dataManager: GrowDataManager = remember { GrowDataManager(context) }
    
    // Get all plants that are currently fermenting
    val fermentingPlants = remember(dataManager) {
        dataManager.loadGrowboxes()
            .flatMap { it.plants }
            .filter { it.isFermenting }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fermentierung",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zurück"
                            )
                        }
                },
                actions = {
                    IconButton(onClick = { /* TODO: camera action */ }) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Foto")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (fermentingPlants.isEmpty()) {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(120.dp)
                        ) {
                            // Placeholder camera icon
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Foto",
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(30.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Keine Pflanzen in Fermentierung",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Getrocknete Pflanzen können zur Fermentierung überführt werden.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                // Show fermenting plants
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(fermentingPlants) { plant ->
                        FermentingPlantCard(
                            plant = plant,
                            onClick = { onPlantClick(plant) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FermentingPlantCard(
    plant: Plant,
    onClick: () -> Unit = {}
) {
    // Calculate fermentation time periods
    val fermentationStartDate = plant.fermentationStartDate ?: System.currentTimeMillis()
    val currentTime = System.currentTimeMillis()
    
    val daysSinceFermentationStart = ((currentTime - fermentationStartDate) / (1000 * 60 * 60 * 24)).toInt()
    
    // Optimal fermentation information based on cannabis cultivation knowledge
    val optimalFermentationWeeks = 4 // 4-8 weeks is optimal for curing
    val optimalFermentationDays = optimalFermentationWeeks * 7
    val optimalTemperature = "18-21°C"
    val optimalHumidity = "55-65%"
    val remainingFermentationDays = maxOf(0, optimalFermentationDays - daysSinceFermentationStart)
    
    // Calculate estimated completion date
    val estimatedCompletionDate = fermentationStartDate + (optimalFermentationDays * 24 * 60 * 60 * 1000)
    
    // Format dates
    val fermentationStartFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(fermentationStartDate)
    val completionDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(estimatedCompletionDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.strain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (plant.manufacturer.isNotEmpty()) {
                        Text(
                            text = plant.manufacturer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Fermentation Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        daysSinceFermentationStart >= optimalFermentationDays -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        daysSinceFermentationStart >= optimalFermentationDays / 2 -> Color(0xFF2196F3).copy(alpha = 0.2f)
                        else -> Color(0xFF795548).copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalBar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                daysSinceFermentationStart >= optimalFermentationDays -> Color(0xFF4CAF50)
                                daysSinceFermentationStart >= optimalFermentationDays / 2 -> Color(0xFF2196F3)
                                else -> Color(0xFF795548)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                daysSinceFermentationStart >= optimalFermentationDays -> "Fertig"
                                daysSinceFermentationStart >= optimalFermentationDays / 2 -> "Halbzeit"
                                else -> "Woche ${daysSinceFermentationStart / 7 + 1}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                daysSinceFermentationStart >= optimalFermentationDays -> Color(0xFF4CAF50)
                                daysSinceFermentationStart >= optimalFermentationDays / 2 -> Color(0xFF2196F3)
                                else -> Color(0xFF795548)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fermentation Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fermentierungsstart",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fermentationStartFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Plant Information Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Row: THC/CBD and Plant Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FermentationInfoChip(
                        label = "THC/CBD",
                        value = "${plant.thcContent} / ${plant.cbdContent}",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    FermentationInfoChip(
                        label = "Typ",
                        value = plant.type.displayName,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fermentation Progress Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FermentationInfoChip(
                        label = "Fermentierungstag",
                        value = "$daysSinceFermentationStart / $optimalFermentationDays",
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    FermentationInfoChip(
                        label = if (remainingFermentationDays > 0) "Noch" else "Abgeschlossen",
                        value = if (remainingFermentationDays > 0) "$remainingFermentationDays Tage" else completionDateFormatted,
                        backgroundColor = if (remainingFermentationDays > 0) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Optimal Fermentation Conditions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optimale Fermentierungsbedingungen",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌡️ $optimalTemperature",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "💧 $optimalHumidity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "🗓️ $optimalFermentationWeeks Wochen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FermentationInfoChip(
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FermentationPlantDetailDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onUpdatePlant: (Plant) -> Unit
) {
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    
    // Calculate fermentation information
    val fermentationStartDate = plant.fermentationStartDate ?: System.currentTimeMillis()
    val currentTime = System.currentTimeMillis()
    val daysSinceFermentationStart = ((currentTime - fermentationStartDate) / (1000 * 60 * 60 * 24)).toInt()
    val optimalFermentationDays = 28 // 4 weeks
    val estimatedCompletionDate = fermentationStartDate + (optimalFermentationDays * 24 * 60 * 60 * 1000)
    
    // Format dates
    val fermentationStartFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(fermentationStartDate)
    val completionDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(estimatedCompletionDate)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 24.dp, 24.dp, 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plant.strain,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (plant.manufacturer.isNotEmpty()) {
                            Text(
                                text = plant.manufacturer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Schließen"
                        )
                    }
                }
                
                // Plant Photos
                if (plant.photos.isNotEmpty()) {
                    Text(
                        text = "Fotos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    
                    LazyRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(plant.photos) { photo ->
                            Card(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                            Icon(
                                                imageVector = Icons.Filled.CameraAlt,
                                                contentDescription = photo.description,
                                                modifier = Modifier.size(32.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        if (photo.description.isNotEmpty()) {
                                            Text(
                                                text = photo.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Plant Information
                Text(
                    text = "Fermentierungsinformationen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FermentationInfoItem("THC Gehalt", plant.thcContent)
                    FermentationInfoItem("CBD Gehalt", plant.cbdContent)
                    FermentationInfoItem("Typ", plant.type.displayName)
                    FermentationInfoItem("Fermentierungsstart", fermentationStartFormatted)
                    FermentationInfoItem("Fermentierungstag", "$daysSinceFermentationStart / $optimalFermentationDays")
                    FermentationInfoItem("Geschätzte Fertigstellung", completionDateFormatted)
                }
                
                // Action Buttons
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddPhotoDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Foto hinzufügen")
                    }
                    
                    Button(
                        onClick = { 
                            // TODO: Implement completion functionality
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fermentierung abschließen")
                    }
                }
            }
        }
    }
    
    // Add Photo Dialog
    if (showAddPhotoDialog) {
        AddFermentationPhotoDialog(
            plant = plant,
            onDismiss = { showAddPhotoDialog = false },
            onConfirm = { updatedPlant ->
                onUpdatePlant(updatedPlant)
                showAddPhotoDialog = false
            }
        )
    }
}

@Composable
fun FermentationInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AddFermentationPhotoDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (Plant) -> Unit
) {
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fermentierungsfoto hinzufügen") },
        text = {
            Column {
                Text("Foto-Funktionalität")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Die Foto-Upload-Funktionalität wird in einer zukünftigen Version implementiert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // For now, add a placeholder photo
                    val newPhoto = PlantPhoto(
                        id = "photo_${System.currentTimeMillis()}",
                        uri = "placeholder",
                        description = description,
                        phase = "Fermentierung"
                    )
                    val updatedPlant = plant.copy(
                        photos = plant.photos + newPhoto
                    )
                    onConfirm(updatedPlant)
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
