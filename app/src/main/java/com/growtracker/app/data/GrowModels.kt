package com.growtracker.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Data classes for Growbox management
@Serializable
data class Growbox(
    val id: String = "",
    val name: String = "",
    val width: String = "",
    val height: String = "",
    val depth: String = "",
    val lightType: String = "",
    val lightPower: String = "",
    val isActive: Boolean = true,
    val plants: List<Plant> = emptyList(),
    val lightingSettings: LightingSettings = LightingSettings(),
    // Optional shared devices (e.g., Abluft/Lüftung) for this growbox
    val devices: List<PowerDevice> = emptyList()
)

@Serializable
data class LightingSettings(
    val lightSchedule: LightSchedule = LightSchedule.VEGETATIVE, // 18/6 or 12/12
    val powerLevel: Int = 100, // 0-100% in 10% increments
    val electricityPrice: Double = 0.30, // Price per kWh in EUR
    val dailyOperatingHours: Int = 18, // Calculated based on schedule
    val settingsHistory: List<LightingSettingsChange> = emptyList() // Track setting changes over time
) {
    fun getDailyKwh(lightWattage: Int): Double {
        val actualWattage = lightWattage * (powerLevel / 100.0)
        return (actualWattage * dailyOperatingHours) / 1000.0
    }
    
    fun getDailyCost(lightWattage: Int): Double {
        return getDailyKwh(lightWattage) * electricityPrice
    }
}

@Serializable
data class LightingSettingsChange(
    val timestamp: Long = System.currentTimeMillis(),
    val lightSchedule: LightSchedule,
    val powerLevel: Int,
    val electricityPrice: Double,
    val reason: String = "Manual change" // Track why settings changed
)

@Serializable
enum class LightSchedule(val displayName: String, val hoursOn: Int, val hoursOff: Int) {
    VEGETATIVE("18/6 (Wachstum)", 18, 6),
    FLOWERING("12/12 (Blüte)", 12, 12)
}

@Serializable
data class Plant(
    val id: String = "",
    val name: String = "",
    val strain: String = "",
    val manufacturer: String = "",
    val thcContent: String = "",
    val cbdContent: String = "",
    val germinationDate: Long? = null,
    val type: PlantType = PlantType.FEMINIZED_INDICA,
    val daysSinceGermination: Int = 0,
    val plantingDate: Long = System.currentTimeMillis(),
    val lightType: String? = null,
    val lightWatt: Int? = null,
    // Optional dimming level for the lamp in percent (0..100). If null, treated as 100%.
    val lightPowerPercent: Int? = null,
    // Per-plant energy settings for electricity cost calculator
    val electricityPrice: Double? = null, // EUR per kWh
    val lightOnStartMinutes: Int? = null, // minutes since midnight (0..1439)
    val lightOnEndMinutes: Int? = null,   // minutes since midnight (can be <= start to indicate crossing midnight)
    val floweringStartDate: Long? = null,
    val harvestDate: Long? = null,
    val dryingStartDate: Long? = null,
    val isDrying: Boolean = false,
    val fermentationStartDate: Long? = null,
    val isFermenting: Boolean = false,
    val fermentationMethod: FermentationMethod = FermentationMethod.MASON_JAR,
    val fermentationEntries: List<FermentationEntry> = emptyList(),
    val entries: List<PlantEntry> = emptyList(),
    val preferredFertilizerManufacturer: String? = null,
    val potSize: PotSize = PotSize.MEDIUM,
    val customPotSize: String? = null, // Custom pot size in liters, e.g., "5.5L"
    val photos: List<PlantPhoto> = emptyList(), // List of plant photos
    // Optional per-plant devices (e.g., Clip-Ventilator, Umluft)
    val devices: List<PowerDevice> = emptyList()
)

@Serializable
data class PlantPhoto(
    val id: String,
    val uri: String, // File path or URI to the photo
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val phase: String = "" // Growth phase when photo was taken
)

@Serializable
enum class PlantType(val displayName: String) {
    AUTOFLOWER("Autoflower"),
    FEMINIZED_INDICA("Feminisiert Indica"),
    FEMINIZED_SATIVA("Feminisiert Sativa"),
    FEMINIZED_HYBRID("Feminisiert Hybrid")
}

@Serializable
enum class PotSize(val displayName: String, val liters: String) {
    SMALL("Klein", "3-5L"),
    MEDIUM("Mittel", "7-11L"),
    LARGE("Groß", "15-20L"),
    EXTRA_LARGE("Extra Groß", "25L+"),
    CUSTOM("Benutzerdefiniert", "")
}

@Serializable
enum class PlantPhase {
    GERMINATION,
    SEEDLING,
    VEGETATIVE,
    FLOWERING,
    HARVEST,
    DRYING,
    FINISHED
}

@Serializable
data class PlantEntry(
    val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: EntryType,
    val value: String = "",
    val notes: String = "",
    val fertilizerEntries: List<FertilizerEntry> = emptyList()
)

@Serializable
enum class EntryType(val displayName: String) {
    HEIGHT("Pflanzengröße"),
    WATERING("Gießen"),
    FERTILIZING("Düngen"),
    PHOTO("Foto"),
    TEMPERATURE("Temperatur"),
    HUMIDITY("Luftfeuchtigkeit"),
    LIGHT("Licht"),
    TOPPING("Topping"),
    LOLLIPOPPING("Lolli Popping"),
    LST("Low Stress Training"),
    TASK("Aufgaben"),
    NOTE("Notizen")
}

@Serializable
data class SeedManufacturer(
    val name: String,
    val strains: List<StrainInfo>
)

@Serializable
data class StrainInfo(
    val name: String,
    val thcContent: String,
    val cbdContent: String,
    val type: PlantType
)

@Serializable
data class FertilizerManufacturer(
    val name: String,
    val products: List<FertilizerProduct>
)

@Serializable
data class FertilizerProduct(
    val name: String,
    val category: String, // e.g. "Grunddünger", "Blütedünger", "Zusätze"
    val npk: String? = null, // e.g. "10-5-7"
    val description: String = ""
)

@Serializable
data class FertilizerEntry(
    val product: FertilizerProduct,
    val dosage: String, // ml/L
    val manufacturer: String
)

@Serializable
enum class FermentationMethod(val displayName: String, val description: String) {
    MASON_JAR("Einwegglas", "Klassische Methode mit Einmachgläsern"),
    HUMIDOR("Humidor", "Hochwertige Holzbox mit Feuchtigkeitsregulierung"),
    TERPLOC_BAG("TerpLoc Bag", "Spezielle Beutel für Terpene-Erhaltung"),
    VACUUM_CONTAINER("Vakuum Dosen", "Luftdichte Vakuumbehälter")
}

@Serializable
data class FermentationEntry(
    val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: FermentationEntryType,
    val notes: String = "",
    val isVentilated: Boolean = false,
    val humidity: String = "",
    val temperature: String = ""
)

@Serializable
enum class FermentationEntryType(val displayName: String) {
    VENTILATION("Belüftung"),
    NOTE("Notizen"),
    TASK("Aufgaben"),
    HUMIDITY_CHECK("Feuchtigkeitskontrolle"),
    TEMPERATURE_CHECK("Temperaturkontrolle")
}

@Serializable
data class ElectricityCosts(
    val dailyCost: Double,
    val weeklyCost: Double,
    val monthlyCost: Double,
    val yearlyCost: Double
)

@Serializable
enum class DeviceType { LAMP, FAN, VENTILATION, HEATER, HUMIDIFIER, DEHUMIDIFIER, PUMP, OTHER }

@Serializable
enum class DeviceScheduleType { ALWAYS_ON, WINDOW, DUTY_CYCLE }

@Serializable
data class PowerDevice(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val type: DeviceType = DeviceType.OTHER,
    val watt: Int? = null,
    // Optional dimming/level in percent (0..100), default 100
    val powerPercent: Int? = 100,
    // How this device runs: always on, fixed window, or duty-cycle percent of day
    val scheduleType: DeviceScheduleType = DeviceScheduleType.ALWAYS_ON,
    val startMinutes: Int? = null, // for WINDOW: minutes since midnight
    val endMinutes: Int? = null,   // for WINDOW
    val dutyCyclePercent: Int? = null // for DUTY_CYCLE: 0..100
)
