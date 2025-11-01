package com.growtracker.app.ui.grow

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.growtracker.app.data.Plant
import com.growtracker.app.data.StrainRepository
import com.growtracker.app.data.Growbox
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Preferences DataStore instance attached to Context
private val Context.growDataStore by preferencesDataStore(name = "grow_prefs")

/**
 * GrowDataStore persisted to Preferences DataStore as a JSON payload under key "plants_json".
 * Keeps an in-memory mutableStateListOf<Plant> so Compose UI can observe changes.
 */
object GrowDataStore {
	private val _plants = mutableStateListOf<Plant>()
	val plants: List<Plant> = _plants
	// simple in-memory growbox list to satisfy screens that expect growbox APIs
	private val _growboxes = mutableStateListOf<Growbox>()
	val growboxes: List<Growbox> = _growboxes

    // Recent search memory (in-memory + persisted)
    private val _recentManufacturers = mutableStateListOf<String>()
    val recentManufacturers: List<String> = _recentManufacturers
    private val _recentStrains = mutableStateListOf<RecentStrain>()
    val recentStrains: List<RecentStrain> = _recentStrains

	// Feature toggle for remembering/search suggestions (default OFF until user opts in)
	private val _rememberSearch = mutableStateOf(false)
    val rememberSearchEnabled: Boolean get() = _rememberSearch.value

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private lateinit var appContext: Context
	private val PLANTS_KEY = stringPreferencesKey("plants_json")
    private val RECENT_MANU_KEY = stringPreferencesKey("recent_manufacturers_json")
    private val RECENT_STRAIN_KEY = stringPreferencesKey("recent_strains_json")
    private val REMEMBER_SEARCH_KEY = booleanPreferencesKey("remember_search_history")
	private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

	fun initialize(context: Context) {
		appContext = context.applicationContext
		// load persisted plants once
		scope.launch {
			val prefs = appContext.growDataStore.data.first()
			val jsonStr = prefs[PLANTS_KEY] ?: "[]"
			val list = runCatching { json.decodeFromString<List<Plant>>(jsonStr) }.getOrElse { emptyList() }

			// Backfill missing THC/CBD from StrainRepository for existing plants
			val filled = list.map { plant ->
				val needThc = plant.thcContent.isBlank()
				val needCbd = plant.cbdContent.isBlank()
				if (!needThc && !needCbd) plant
				else {
					val found = StrainRepository.manufacturers
						.find { it.name.equals(plant.manufacturer, ignoreCase = true) }
						?.strains
						?.find { it.name.equals(plant.strain, ignoreCase = true) }
					if (found != null) {
						plant.copy(
							thcContent = plant.thcContent.takeIf { it.isNotBlank() } ?: found.thcContent,
							cbdContent = plant.cbdContent.takeIf { it.isNotBlank() } ?: found.cbdContent
						)
					} else plant
				}
			}

			withContext(Dispatchers.Main) {
				_plants.clear()
				_plants.addAll(filled)
			}

			// persist back if we filled values
			scope.launch {
				val jsonStrOut = json.encodeToString(filled)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStrOut }
			}

			// Load recent manufacturers and strains (best-effort)
			val manuJson = prefs[RECENT_MANU_KEY] ?: "[]"
			val strainJson = prefs[RECENT_STRAIN_KEY] ?: "[]"
			val manuList = runCatching { json.decodeFromString<List<String>>(manuJson) }.getOrElse { emptyList() }
			val strainList = runCatching { json.decodeFromString<List<RecentStrain>>(strainJson) }.getOrElse { emptyList() }
			// Default to false (disabled) unless user explicitly enabled
			val rememberFlag = prefs[REMEMBER_SEARCH_KEY] ?: false
			withContext(Dispatchers.Main) {
				_recentManufacturers.clear(); _recentManufacturers.addAll(manuList.distinct().take(20))
				_recentStrains.clear(); _recentStrains.addAll(strainList.distinctBy { it.manufacturer.lowercase() + "|" + it.strain.lowercase() }.take(30))
                _rememberSearch.value = rememberFlag
			}
		}
	}

	fun addPlant(plant: Plant) {
 		// Auto-fill missing THC/CBD from the seeded StrainRepository when possible
 		val filled = run {
 			val hasThc = plant.thcContent.isNotBlank()
 			val hasCbd = plant.cbdContent.isNotBlank()
 			if (hasThc && hasCbd) plant
 			else {
 				val manufacturer = plant.manufacturer
 				val strain = plant.strain
 				val found = StrainRepository.manufacturers
 						.find { it.name.equals(manufacturer, ignoreCase = true) }
 						?.strains
 						?.find { it.name.equals(strain, ignoreCase = true) }
 				if (found != null) {
 					plant.copy(
 						thcContent = plant.thcContent.takeIf { it.isNotBlank() } ?: found.thcContent,
 						cbdContent = plant.cbdContent.takeIf { it.isNotBlank() } ?: found.cbdContent
 					)
 				} else plant
 			}
 		}

		// Insert at index 0 so newly added plants appear at the top of the active list
		_plants.add(0, filled)

		// persist in background
		val snapshot = _plants.toList()
		scope.launch {
			val jsonStr = json.encodeToString(snapshot)
			appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
		}
	}

	fun updatePlant(plant: Plant) {
		val idx = _plants.indexOfFirst { it.id == plant.id }
		if (idx >= 0) {
			_plants[idx] = plant
			// persist
			val snapshot = _plants.toList()
			scope.launch {
				val jsonStr = json.encodeToString(snapshot)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
			}

		}
	}

	// Growbox helper APIs (in-memory)
	fun getGrowbox(id: String): Growbox? = _growboxes.find { it.id == id }

	fun updateGrowbox(updated: Growbox) {
		val idx = _growboxes.indexOfFirst { it.id == updated.id }
		if (idx >= 0) {
			_growboxes[idx] = updated
		} else {
			_growboxes.add(updated)
		}
	}

	/**
	 * Add a PlantEntry to a plant by id and persist the change.
	 */
	fun addEntryToPlant(plantId: String, entry: com.growtracker.app.data.PlantEntry) {
		val idx = _plants.indexOfFirst { it.id == plantId }
		if (idx >= 0) {
			val updated = _plants[idx].copy(entries = _plants[idx].entries + entry)
			_plants[idx] = updated
			// persist
			val snapshot = _plants.toList()
			scope.launch {
				val jsonStr = json.encodeToString(snapshot)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
			}
		}
	}

	/**
	 * Remove an entry by id from a plant and persist the change.
	 */
	fun removeEntryFromPlant(plantId: String, entryId: String) {
		val idx = _plants.indexOfFirst { it.id == plantId }
		if (idx >= 0) {
			val updatedEntries = _plants[idx].entries.filterNot { it.id == entryId }
			val updated = _plants[idx].copy(entries = updatedEntries)
			_plants[idx] = updated
			// persist
			val snapshot = _plants.toList()
			scope.launch {
				val jsonStr = json.encodeToString(snapshot)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
			}
		}
	}

	/**
	 * Update an existing entry inside a plant and persist the change.
	 */
	fun updateEntryInPlant(plantId: String, entry: com.growtracker.app.data.PlantEntry) {
		val idx = _plants.indexOfFirst { it.id == plantId }
		if (idx >= 0) {
			val updatedEntries = _plants[idx].entries.map { if (it.id == entry.id) entry else it }
			val updated = _plants[idx].copy(entries = updatedEntries)
			_plants[idx] = updated
			// persist
			val snapshot = _plants.toList()
			scope.launch {
				val jsonStr = json.encodeToString(snapshot)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
			}
		}
	}

	fun removePlant(plantId: String) {
		val idx = _plants.indexOfFirst { it.id == plantId }
		if (idx >= 0) {
			_plants.removeAt(idx)
			// persist
			val snapshot = _plants.toList()
			scope.launch {
				val jsonStr = json.encodeToString(snapshot)
				appContext.growDataStore.edit { prefs: MutablePreferences -> prefs[PLANTS_KEY] = jsonStr }
				// Also purge from legacy GrowDataManager growboxes to avoid ghosts in statistics
				try {
					val mgr = com.growtracker.app.data.GrowDataManager(appContext)
					val boxes = mgr.loadGrowboxes()
					if (boxes.isNotEmpty()) {
						val updated = boxes.map { gb ->
							val cleanedPlants = gb.plants.filterNot { it.id == plantId }
							if (cleanedPlants.size != gb.plants.size) gb.copy(plants = cleanedPlants) else gb
						}
						// Only write back if any change occurred
						if (updated != boxes) {
							mgr.saveGrowboxes(updated)
						}
					}
				} catch (_: Exception) {
					// Best-effort cleanup; ignore failures
				}
			}
		}
	}

	fun clearGrowboxes() {
		_plants.clear()
		scope.launch {
			appContext.growDataStore.edit { prefs: MutablePreferences -> prefs.remove(PLANTS_KEY) }
		}
	}

	/** Persist a manufacturer name to recent list (move to front, cap size). */
	fun addRecentManufacturer(name: String) {
		if (!rememberSearchEnabled) return
		if (name.isBlank()) return
		val key = name.trim()
		// Update in-memory (main thread safe enough as Compose calls this on UI events)
		_recentManufacturers.removeAll { it.equals(key, ignoreCase = true) }
		_recentManufacturers.add(0, key)
		while (_recentManufacturers.size > 20) _recentManufacturers.removeLast()
		// Persist snapshot
		val snapshot = _recentManufacturers.toList()
		scope.launch {
			val out = json.encodeToString(snapshot)
			appContext.growDataStore.edit { p: MutablePreferences -> p[RECENT_MANU_KEY] = out }
		}
	}

	/** Persist a (manufacturer,strain) pair to recent strains. */
	fun addRecentStrain(manufacturer: String, strain: String) {
		if (!rememberSearchEnabled) return
		if (manufacturer.isBlank() || strain.isBlank()) return
		val entry = RecentStrain(manufacturer.trim(), strain.trim())
		_recentStrains.removeAll { it.manufacturer.equals(entry.manufacturer, true) && it.strain.equals(entry.strain, true) }
		_recentStrains.add(0, entry)
		while (_recentStrains.size > 30) _recentStrains.removeLast()
		val snapshot = _recentStrains.toList()
		scope.launch {
			val out = json.encodeToString(snapshot)
			appContext.growDataStore.edit { p: MutablePreferences -> p[RECENT_STRAIN_KEY] = out }
		}
	}

    /** Enable/disable remembering search history and suggestions. */
    fun setRememberSearchEnabled(enabled: Boolean) {
        _rememberSearch.value = enabled
        // Persist flag
        scope.launch {
            appContext.growDataStore.edit { p: MutablePreferences -> p[REMEMBER_SEARCH_KEY] = enabled }
        }
    }

    /** Clear recent manufacturer and strain suggestions. */
    fun clearRecentSearches() {
        // Clear in-memory
        _recentManufacturers.clear()
        _recentStrains.clear()
        // Persist empty arrays
        scope.launch {
            appContext.growDataStore.edit { p: MutablePreferences ->
                p[RECENT_MANU_KEY] = json.encodeToString(emptyList<String>())
                p[RECENT_STRAIN_KEY] = json.encodeToString(emptyList<RecentStrain>())
            }
        }
    }
}

@Serializable
data class RecentStrain(val manufacturer: String, val strain: String)

