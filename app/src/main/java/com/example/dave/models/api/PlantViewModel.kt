package com.example.dave.models.api

import androidx.lifecycle.ViewModel
import com.example.dave.components.PlantListState
import com.example.dave.models.Plant
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlantViewModel : ViewModel() {

    private val _plantState = MutableStateFlow<PlantListState>(PlantListState.Loading)
    val plantState: StateFlow<PlantListState> = _plantState

    private val functions = FirebaseFunctions.Companion.getInstance()

    init {
        fetchPlants()
    }

    private fun fetchPlants() {
        functions.getHttpsCallable("getPlants").call()
            .addOnSuccessListener { result ->
                try {
                    val data = result.data as List<Map<String, Any>>
                    val plants = data.map { map ->
                        Plant(
                            id = map["id"] as String,
                            commonName = map["common_name"] as String,
                            scientificName = map["scientific_name"] as List<String>,
                            plantName = map["plant_name"] as? String,
                            family = map["family"] as? String,
                            type = map["type"] as? String,
                            imageUrl = map["image_url"] as? String,
                            careLevel = map["care_level"] as? String,
                            sunlight = map["sunlight"] as? List<String>,
                            watering = map["watering"] as? String,
                            indoor = map["indoor"] as? Boolean,
                            poisonousToHumans = map["poisonous_to_humans"] as? Boolean,
                            poisonousToPets = map["poisonous_to_pets"] as? Boolean,
                            droughtTolerant = map["drought_tolerant"] as? Boolean,
                            soil = map["soil"] as? List<String>,
                            notes = map["notes"] as? String,
                            cuteName = map["cute_name"] as? String
                        )
                    }
                    _plantState.value = PlantListState.Success(plants)
                } catch (e: Exception) {
                    _plantState.value = PlantListState.Error(e.message ?: "Parse error")
                }
            }
            .addOnFailureListener { e ->
                _plantState.value = PlantListState.Error(e.message ?: "Network error")
            }
    }
}