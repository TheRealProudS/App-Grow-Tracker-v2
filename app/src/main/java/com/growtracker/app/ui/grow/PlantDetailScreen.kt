package com.growtracker.app.ui.grow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growtracker.app.data.Plant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: Plant, // You would pass the actual plant object here
    onBack: () -> Unit
) {
    var settingsOpen by remember { mutableStateOf(false) }
    var actionsOpen by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings") }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PlantInfoCard(plant) }
            item { CalendarView() }
            item { EntriesSection(entries = plant.entries) }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            AnimatedVisibility(visible = actionsOpen) {
                Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 6.dp) {
                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showWaterDialog = true; actionsOpen = false }) { Icon(imageVector = Icons.Filled.Schedule, contentDescription = "Water") }
                        IconButton(onClick = { showNotesDialog = true; actionsOpen = false }) { Icon(imageVector = Icons.Filled.Info, contentDescription = "Notes") }
                    }
                }
            }
            FloatingActionButton(onClick = { actionsOpen = !actionsOpen }) { Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = "Actions") }
        }
    }

    if (settingsOpen) {
        PlantSettingsDialog(plant = plant, onClose = { settingsOpen = false }, onUpdate = { updated -> /* persist if store available */ }, onDelete = { /* delete handler */ })
    }

    if (showWaterDialog) {
        var liters by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            title = { Text("Gießen") },
            text = { Column { OutlinedTextField(value = liters, onValueChange = { liters = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Liter") }) } },
            confirmButton = { TextButton(onClick = { /* add entry */; showWaterDialog = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showWaterDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showNotesDialog) {
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Notizen") },
            text = { Column { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notiz") }) } },
            confirmButton = { TextButton(onClick = { /* save note */; showNotesDialog = false }) { Text("Speichern") } },
            dismissButton = { TextButton(onClick = { showNotesDialog = false }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun PlantInfoCard(plant: Plant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pflanzen-Informationen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            InfoRow(icon = Icons.Filled.FilterVintage, label = "Sorte", value = plant.strain.ifBlank { "Unbenannt" })
            InfoRow(icon = Icons.Filled.Schedule, label = "Alter", value = deriveAgeWeeks(plant))
            InfoRow(icon = Icons.Filled.CalendarMonth, label = "Phase", value = derivePhase(plant))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                plant.manufacturer.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                PhasePill(derivePhase(plant), deriveAgeWeeks(plant))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CalendarView() {
    var isExpanded by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val days = if (isExpanded) 30 else 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kalender", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expand Calendar"
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            AnimatedVisibility(
                visible = true, // Always visible to show the grid
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(if(isExpanded) 300.dp else 60.dp), // Adjust height based on expansion
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        items(List(days) { today.plusDays(it.toLong()) }) { date ->
                            CalendarDay(date, date == today)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(date: LocalDate, isToday: Boolean) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN).take(2)
    val dayOfMonth = date.dayOfMonth

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                color = if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dayOfWeek, fontSize = 10.sp, color = if(isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(dayOfMonth.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}
