package com.growtracker.app.ui.grow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricChip(
    label: String,
    value: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(end = 6.dp, bottom = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = contentColor, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, color = contentColor, fontSize = 13.sp)
        }
    }
}

@Composable
fun ChipSmall(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    MetricChip(label = label, value = value, modifier = modifier)
}
