package com.growtracker.app.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GrowDataManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("grow_tracker_data", Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val KEY_GROWBOXES = "Growboxes"
    }

    fun saveGrowboxes(growboxes: List<Growbox>) {
        try {
            val jsonString = json.encodeToString(growboxes)
            sharedPreferences.edit()
                .putString(KEY_GROWBOXES, jsonString)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadGrowboxes(): List<Growbox> {
        return try {
            val jsonString = sharedPreferences.getString(KEY_GROWBOXES, null)
            if (jsonString != null) {
                json.decodeFromString<List<Growbox>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addGrowbox(growbox: Growbox) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        currentGrowboxes.add(growbox)
        saveGrowboxes(currentGrowboxes)
    }

    fun updateGrowbox(updatedGrowbox: Growbox) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val index = currentGrowboxes.indexOfFirst { it.id == updatedGrowbox.id }
        if (index != -1) {
            currentGrowboxes[index] = updatedGrowbox
            saveGrowboxes(currentGrowboxes)
        }
    }

    fun addPlantToGrowbox(growboxId: String, plant: Plant) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val growboxIndex = currentGrowboxes.indexOfFirst { it.id == growboxId }
        if (growboxIndex != -1) {
            val updatedGrowbox = currentGrowboxes[growboxIndex].copy(
                plants = currentGrowboxes[growboxIndex].plants + plant
            )
            currentGrowboxes[growboxIndex] = updatedGrowbox
            saveGrowboxes(currentGrowboxes)
        }
    }

    fun updatePlant(growboxId: String, updatedPlant: Plant) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val growboxIndex = currentGrowboxes.indexOfFirst { it.id == growboxId }
        if (growboxIndex != -1) {
            val updatedPlants = currentGrowboxes[growboxIndex].plants.map { plant ->
                if (plant.id == updatedPlant.id) updatedPlant else plant
            }
            val updatedGrowbox = currentGrowboxes[growboxIndex].copy(plants = updatedPlants)
            currentGrowboxes[growboxIndex] = updatedGrowbox
            saveGrowboxes(currentGrowboxes)
        }
    }

    fun addEntryToPlant(growboxId: String, plantId: String, entry: PlantEntry) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val growboxIndex = currentGrowboxes.indexOfFirst { it.id == growboxId }
        if (growboxIndex != -1) {
            val plantIndex = currentGrowboxes[growboxIndex].plants.indexOfFirst { it.id == plantId }
            if (plantIndex != -1) {
                val updatedPlant = currentGrowboxes[growboxIndex].plants[plantIndex].copy(
                    entries = currentGrowboxes[growboxIndex].plants[plantIndex].entries + entry
                )
                val updatedPlants = currentGrowboxes[growboxIndex].plants.toMutableList()
                updatedPlants[plantIndex] = updatedPlant
                val updatedGrowbox = currentGrowboxes[growboxIndex].copy(plants = updatedPlants)
                currentGrowboxes[growboxIndex] = updatedGrowbox
                saveGrowboxes(currentGrowboxes)
            }
        }
    }

    fun deletePlant(growboxId: String, plantId: String) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val growboxIndex = currentGrowboxes.indexOfFirst { it.id == growboxId }
        if (growboxIndex != -1) {
            val updatedPlants = currentGrowboxes[growboxIndex].plants.filter { it.id != plantId }
            val updatedGrowbox = currentGrowboxes[growboxIndex].copy(plants = updatedPlants)
            currentGrowboxes[growboxIndex] = updatedGrowbox
            saveGrowboxes(currentGrowboxes)
        }
    }

    fun deleteGrowbox(growboxId: String) {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val updatedGrowboxes = currentGrowboxes.filter { it.id != growboxId }
        saveGrowboxes(updatedGrowboxes)
    }

    fun updateBudVoyageThcValues() {
        val currentGrowboxes = loadGrowboxes().toMutableList()
        val seedManufacturers = getSeedManufacturers()
        val budVoyageManufacturer = seedManufacturers.find { it.name == "BudVoyage" }

        if (budVoyageManufacturer != null) {
            var hasUpdates = false

            for (growboxIndex in currentGrowboxes.indices) {
                val updatedPlants = currentGrowboxes[growboxIndex].plants.map { plant ->
                    if (plant.manufacturer == "BudVoyage") {
                        val matchingStrain = budVoyageManufacturer.strains.find { strain ->
                            strain.name.equals(plant.strain, ignoreCase = true)
                        }

                        if (matchingStrain != null &&
                            (plant.thcContent != matchingStrain.thcContent ||
                                    plant.cbdContent != matchingStrain.cbdContent)) {
                            hasUpdates = true
                            plant.copy(
                                thcContent = matchingStrain.thcContent,
                                cbdContent = matchingStrain.cbdContent,
                                type = matchingStrain.type
                            )
                        } else {
                            plant
                        }
                    } else {
                        plant
                    }
                }

                if (hasUpdates) {
                    currentGrowboxes[growboxIndex] = currentGrowboxes[growboxIndex].copy(plants = updatedPlants)
                }
            }

            if (hasUpdates) {
                saveGrowboxes(currentGrowboxes)
            }
        }
    }
}
