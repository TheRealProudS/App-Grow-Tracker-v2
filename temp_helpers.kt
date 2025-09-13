// Helper functions (moved from helper_functions.kt to avoid import issues)
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
// using Icons.Filled in this file

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

fun getEntryTypeIcon(type: EntryType): ImageVector {
    return when (type) {
    EntryType.HEIGHT -> androidx.compose.material.icons.Icons.Filled.Height
    EntryType.WATERING -> androidx.compose.material.icons.Icons.Filled.WaterDrop
    EntryType.FERTILIZING -> androidx.compose.material.icons.Icons.Filled.Eco
    EntryType.PHOTO -> androidx.compose.material.icons.Icons.Filled.Camera
    EntryType.TEMPERATURE -> androidx.compose.material.icons.Icons.Filled.Thermostat
    EntryType.HUMIDITY -> androidx.compose.material.icons.Icons.Filled.Water
    EntryType.LIGHT -> androidx.compose.material.icons.Icons.Filled.LightMode
    EntryType.TOPPING -> androidx.compose.material.icons.Icons.Filled.ContentCut
    EntryType.LOLLIPOPPING -> androidx.compose.material.icons.Icons.Filled.Grass
    EntryType.LST -> androidx.compose.material.icons.Icons.Filled.Transform
    EntryType.TASK -> androidx.compose.material.icons.Icons.Filled.Task
    EntryType.NOTE -> androidx.compose.material.icons.Icons.Filled.Note
    }
}

fun getEntryTypeColor(type: EntryType): Color {
    return when (type) {
        EntryType.HEIGHT -> Color(0xFF4CAF50)
        EntryType.WATERING -> Color(0xFF2196F3)
        EntryType.FERTILIZING -> Color(0xFF8BC34A)
        EntryType.PHOTO -> Color(0xFF9C27B0)
        EntryType.TEMPERATURE -> Color(0xFFF44336)
        EntryType.HUMIDITY -> Color(0xFF00BCD4)
        EntryType.LIGHT -> Color(0xFFFF9800)
        EntryType.TOPPING -> Color(0xFF795548)
        EntryType.LOLLIPOPPING -> Color(0xFF607D8B)
        EntryType.LST -> Color(0xFF3F51B5)
        EntryType.TASK -> Color(0xFF9E9E9E)
        EntryType.NOTE -> Color(0xFF673AB7)
    }
}

fun getValueLabel(type: EntryType): String {
    return when (type) {
        EntryType.HEIGHT -> "Höhe (cm)"
        EntryType.WATERING -> "Wassermenge (L)"
        EntryType.FERTILIZING -> "Wassermenge (L)"
        EntryType.PHOTO -> "Foto-Beschreibung"
        EntryType.TEMPERATURE -> "Temperatur (°C)"
        EntryType.HUMIDITY -> "Luftfeuchtigkeit (%)"
        EntryType.LIGHT -> "Lichtstunden"
        EntryType.TOPPING -> "Beschreibung"
        EntryType.LOLLIPOPPING -> "Beschreibung"
        EntryType.LST -> "Beschreibung"
        EntryType.TASK -> "Aufgabe"
        EntryType.NOTE -> "Notiz"
    }
}

fun needsNumericInput(type: EntryType): Boolean {
    return when (type) {
        EntryType.HEIGHT, EntryType.TEMPERATURE, EntryType.HUMIDITY, EntryType.LIGHT -> true
        else -> false
    }
}
