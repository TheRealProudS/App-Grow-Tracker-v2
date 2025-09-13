// Helper functions for fertilizer calculations
fun calculateTotalFertilizerAmount(waterAmount: String, dosage: String): Double? {
    return try {
        // Extract numeric value from water amount (handles "5L", "5 L", "5 Liter", etc.)
        val waterLiters = waterAmount.replace(Regex("[^0-9.,]"), "")
            .replace(",", ".")
            .toDoubleOrNull()
        
        // Extract numeric value from dosage (handles "5ml/L", "5 ml/L", etc.)
        val dosagePerLiter = dosage.replace(Regex("[^0-9.,]"), "")
            .replace(",", ".")
            .toDoubleOrNull()
        
        if (waterLiters != null && dosagePerLiter != null) {
            waterLiters * dosagePerLiter
        } else null
    } catch (e: Exception) {
        null
    }
}

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

fun getEntryTypeIcon(type: EntryType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
    EntryType.HEIGHT -> androidx.compose.material.icons.Icons.Filled.Height
    EntryType.PHOTO -> androidx.compose.material.icons.Icons.Filled.Camera
    EntryType.TEMPERATURE -> androidx.compose.material.icons.Icons.Filled.Thermostat
    EntryType.LIGHT -> androidx.compose.material.icons.Icons.Filled.LightMode
    EntryType.TOPPING -> androidx.compose.material.icons.Icons.Filled.ContentCut
    EntryType.LOLLIPOPPING -> androidx.compose.material.icons.Icons.Filled.Grass
    EntryType.LST -> androidx.compose.material.icons.Icons.Filled.Transform
    EntryType.TASK -> androidx.compose.material.icons.Icons.Filled.Task
    EntryType.WATERING -> androidx.compose.material.icons.Icons.Filled.WaterDrop
    EntryType.HUMIDITY -> androidx.compose.material.icons.Icons.Filled.Water
    EntryType.NOTE -> androidx.compose.material.icons.Icons.Filled.Note
    }
}

fun getEntryTypeColor(type: EntryType): androidx.compose.ui.graphics.Color {
    return when (type) {
        EntryType.HEIGHT -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        EntryType.WATERING -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        EntryType.FERTILIZING -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
        EntryType.PHOTO -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        EntryType.TEMPERATURE -> androidx.compose.ui.graphics.Color(0xFFF44336)
        EntryType.HUMIDITY -> androidx.compose.ui.graphics.Color(0xFF00BCD4)
        EntryType.LIGHT -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        EntryType.TOPPING -> androidx.compose.ui.graphics.Color(0xFF795548)
        EntryType.LOLLIPOPPING -> androidx.compose.ui.graphics.Color(0xFF607D8B)
        EntryType.LST -> androidx.compose.ui.graphics.Color(0xFF3F51B5)
        EntryType.TASK -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
        EntryType.NOTE -> androidx.compose.ui.graphics.Color(0xFF673AB7)
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
