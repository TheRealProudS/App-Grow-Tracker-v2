package com.growtracker.app.ui.grow.v2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlantsScreen(onOpenGrowbox: (String) -> Unit) {
    val growboxes = GrowRepository.getActiveGrowboxes()

    if (growboxes.isEmpty()) {
        Text(text = "Keine aktiven Growboxen", modifier = Modifier.padding(16.dp))
        return
    }

    LazyColumn {
        items(growboxes) { box ->
            Card(modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .clickable { onOpenGrowbox(box.id) }) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = box.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Pflanzen: ${box.plants.size}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
