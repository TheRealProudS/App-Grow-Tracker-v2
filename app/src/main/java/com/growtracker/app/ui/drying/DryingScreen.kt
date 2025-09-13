package com.growtracker.app.ui.drying

import com.growtracker.app.data.FermentationMethod
import com.growtracker.app.data.GrowDataManager
import com.growtracker.app.data.Plant
import com.growtracker.app.data.PlantPhoto
import com.growtracker.app.data.PlantType
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
// using fully-qualified icon references in this file
// avoid wildcard material icon imports; use fully-qualified references where needed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.growtracker.app.ui.language.LanguageManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DryingScreen(
    modifier: Modifier = Modifier,
    languageManager: LanguageManager,
    onNavigateUp: () -> Unit = {},
    onPlantClick: (Plant) -> Unit = {}
) {
    val context = LocalContext.current
    val dataManager: GrowDataManager = remember { GrowDataManager(context) }
    
    // Get all plants that are currently drying
    val dryingPlants = remember(dataManager) {
        dataManager.loadGrowboxes()
            .flatMap { it.plants }
            .filter { it.isDrying }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trocknung",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Zurück")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (dryingPlants.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(30.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Keine Pflanzen in Trocknung",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Geerntete Pflanzen können in den Einstellungen zur Trocknung geschickt werden.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                // Show drying plants
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(dryingPlants) { plant ->
                        DryingPlantCard(
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
fun DryingPlantCard(
    plant: Plant,
    onClick: () -> Unit = {}
) {
    // Calculate various time periods
    val harvestDate = plant.harvestDate ?: System.currentTimeMillis()
    val dryingStartDate = plant.dryingStartDate ?: System.currentTimeMillis()
    val currentTime = System.currentTimeMillis()
    
    val daysSinceDryingStart = ((currentTime - dryingStartDate) / (1000 * 60 * 60 * 24)).toInt()
    
    // Optimal drying information based on cannabis cultivation knowledge
    val optimalDryingDays = 7 // 7-14 days is optimal
    val optimalTemperature = "18-20°C"
    val optimalHumidity = "50-60%"
    val remainingDryingDays = maxOf(0, optimalDryingDays - daysSinceDryingStart)
    
    // Calculate estimated date for curing (fermentation)
    val estimatedCuringStartDate = dryingStartDate + (optimalDryingDays * 24 * 60 * 60 * 1000)
    // Format dates
    val harvestDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(harvestDate)
    val curingDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(estimatedCuringStartDate)

    Card(
        onClick = onClick,
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
                
                // Drying Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        daysSinceDryingStart >= optimalDryingDays -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else -> Color(0xFFFF9800).copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                daysSinceDryingStart >= optimalDryingDays -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                daysSinceDryingStart >= optimalDryingDays -> "Fertig"
                                else -> "Tag $daysSinceDryingStart"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                daysSinceDryingStart >= optimalDryingDays -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Harvest Information
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
                            text = "Erntedatum",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = harvestDateFormatted,
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
                    InfoChip(
                        label = "THC/CBD",
                        value = "${plant.thcContent} / ${plant.cbdContent}",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    InfoChip(
                        label = "Typ",
                        value = plant.type.displayName,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Drying Progress Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        label = "Trocknungstag",
                        value = "$daysSinceDryingStart / $optimalDryingDays",
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    InfoChip(
                        label = if (remainingDryingDays > 0) "Noch" else "Fermentierung",
                        value = if (remainingDryingDays > 0) "$remainingDryingDays Tage" else curingDateFormatted,
                        backgroundColor = if (remainingDryingDays > 0) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Optimal Drying Conditions
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
                            text = "Optimale Trocknungsbedingungen",
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
                            text = "🕐 $optimalDryingDays Tage",
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
fun InfoChip(
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
fun DryingPlantDetailDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onUpdatePlant: (Plant) -> Unit
) {
    var showEditDryingDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showFermentationDialog by remember { mutableStateOf(false) }
    
    // Calculate drying information
    val harvestDate = plant.harvestDate ?: System.currentTimeMillis()
    val dryingStartDate = plant.dryingStartDate ?: System.currentTimeMillis()
    val currentTime = System.currentTimeMillis()
    val daysSinceDryingStart = ((currentTime - dryingStartDate) / (1000 * 60 * 60 * 24)).toInt()
    val optimalDryingDays = 7
    val estimatedCuringStartDate = dryingStartDate + (optimalDryingDays * 24 * 60 * 60 * 1000)
    
    // Format dates
    val harvestDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(harvestDate)
    val dryingStartFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(dryingStartDate)
    val curingDateFormatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(estimatedCuringStartDate)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                                            imageVector = Icons.Filled.PhotoCamera,
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
                    text = "Pflanzeninformationen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlantInfoItem("THC Gehalt", plant.thcContent)
                    PlantInfoItem("CBD Gehalt", plant.cbdContent)
                    PlantInfoItem("Typ", plant.type.displayName)
                    PlantInfoItem("Erntedatum", harvestDateFormatted)
                    PlantInfoItem("Trocknungsstart", dryingStartFormatted)
                    PlantInfoItem("Trocknungstag", "$daysSinceDryingStart / $optimalDryingDays")
                    PlantInfoItem("Fermentierung ab", curingDateFormatted)
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
                        Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Foto hinzufügen")
                    }
                    
                    OutlinedButton(
                        onClick = { showEditDryingDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trocknungsdatum bearbeiten")
                    }
                    
                    Button(
                        onClick = { 
                            showFermentationDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.Science, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zur Fermentierung")
                    }
                }
            }
        }
    }
    
    // Edit Drying Date Dialog
    if (showEditDryingDialog) {
        EditDryingDateDialog(
            plant = plant,
            onDismiss = { showEditDryingDialog = false },
            onConfirm = { updatedPlant ->
                onUpdatePlant(updatedPlant)
                showEditDryingDialog = false
            }
        )
    }
    
    // Add Photo Dialog
    if (showAddPhotoDialog) {
        AddPhotoDialog(
            plant = plant,
            onDismiss = { showAddPhotoDialog = false },
            onConfirm = { updatedPlant ->
                onUpdatePlant(updatedPlant)
                showAddPhotoDialog = false
            }
        )
    }
    
    // Fermentation Dialog
    if (showFermentationDialog) {
        FermentationDialog(
            plant = plant,
            onDismiss = { showFermentationDialog = false },
            onConfirm = { fermentationMethod ->
                // Move plant to fermentation - update plant status
                val updatedPlant = plant.copy(
                    isDrying = false,
                    isFermenting = true,
                    fermentationStartDate = System.currentTimeMillis(),
                    fermentationMethod = fermentationMethod
                )
                onUpdatePlant(updatedPlant)
                showFermentationDialog = false
                onDismiss() // Close the detail dialog as well
            }
        )
    }
}

@Composable
fun PlantInfoItem(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDryingDateDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (Plant) -> Unit
) {
    var selectedDate by remember {
        mutableStateOf(plant.dryingStartDate ?: System.currentTimeMillis())
    }
    
    val dateFormatter = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trocknungsdatum bearbeiten") },
        text = {
            Column {
                Text("Aktuelles Datum: ${dateFormatter.format(selectedDate)}")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedDate = System.currentTimeMillis()
                        }
                    ) {
                        Text("Heute")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            selectedDate = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        }
                    ) {
                        Text("Gestern")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedPlant = plant.copy(dryingStartDate = selectedDate)
                    onConfirm(updatedPlant)
                }
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
fun AddPhotoDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (Plant) -> Unit
) {
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto hinzufügen") },
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
                        phase = "Trocknung"
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FermentationDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (FermentationMethod) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(FermentationMethod.MASON_JAR) }
    
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
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Zur Fermentierung",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Möchtest du '${plant.strain}' zur Fermentierung überführen?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fermentation Method Selection
                Text(
                    text = "Wähle deine Fermentierungsmethode:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Method Cards
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(FermentationMethod.values()) { method ->
                        FermentationMethodCard(
                            method = method,
                            isSelected = selectedMethod == method,
                            onClick = { selectedMethod = method }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info Text
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Hinweis: Die Fermentierung erfolgt normalerweise nach 7-14 Tagen Trocknung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                        onClick = { onConfirm(selectedMethod) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Zur Fermentierung")
                    }
                }
            }
        }
    }
}

@Composable
fun FermentationMethodCard(
    method: FermentationMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = when (method) {
                    FermentationMethod.MASON_JAR -> Icons.Filled.LocalBar
                    FermentationMethod.HUMIDOR -> Icons.Filled.Inventory
                    FermentationMethod.TERPLOC_BAG -> Icons.Filled.LocalMall
                    FermentationMethod.VACUUM_CONTAINER -> Icons.Filled.LocalHospital
                },
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
