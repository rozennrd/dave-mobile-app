package com.example.dave.models.api

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dave.models.LoginModel
import com.example.dave.models.Plant
import com.example.dave.ui.components.PlantListState
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlantViewModel(private val loginModel: LoginModel) : ViewModel() {

    private val _plantState = MutableStateFlow<PlantListState>(PlantListState.Loading)
    val plantState: StateFlow<PlantListState> = _plantState

    private val _currentPlant = MutableStateFlow<Plant?>(null)
    val currentPlant: StateFlow<Plant?> = _currentPlant

    fun setCurrentPlant(plantId: String) {
        val state = _plantState.value
        if (state is PlantListState.Success) {
            _currentPlant.value = state.plants.find { it.id == plantId }
        }
    }

    fun clearCurrentPlant() {
        _currentPlant.value = null
    }

    private val functions = FirebaseFunctions.Companion.getInstance("europe-southwest1")

    init {
        viewModelScope.launch {
            loginModel.currentUser.collectLatest { user ->
                if (user != null) {
                    Log.d("DEBUG", "Current User ID: ${user.uid}")
                    fetchPlants()
                }
            }
        }
    }

    // Make this PUBLIC so it can be called from UI
    fun fetchPlants() {
        functions.getHttpsCallable("getPlants").call()
            .addOnSuccessListener { result ->
                try {
                    val data = result.data

                    if (data == null) {
                        _plantState.value = PlantListState.Empty
                        return@addOnSuccessListener
                    }

                    val dataTyped = data as? List<Map<String, Any>>
                    if (dataTyped == null || dataTyped.isEmpty()) {
                        _plantState.value = PlantListState.Empty
                        return@addOnSuccessListener
                    }

                    val plants = dataTyped.mapNotNull { map ->
                        try {
                            Plant(
                                id = map["id"] as? String ?: return@mapNotNull null,
                                commonName = map["common_name"] as? String ?: "",
                                scientificName = map["scientific_name"] as? List<String>
                                    ?: emptyList(),
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
                            )
                        } catch (e: Exception) {
                            Log.e("PlantViewModel", "Error parsing plant: ${e.message}")
                            null
                        }
                    }

                    _plantState.value = if (plants.isEmpty()) {
                        PlantListState.Empty
                    } else {
                        PlantListState.Success(plants)
                    }
                } catch (e: Exception) {
                    Log.e("PlantViewModel", "Error in fetchPlants: ${e.message}", e)
                    _plantState.value = PlantListState.Error(e.message ?: "Parse error")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Network error: ${e.message}", e)
                _plantState.value = PlantListState.Error(e.message ?: "Network error")
            }
    }

    fun addPlant(
        commonName: String,
        scientificName: List<String>,
        plantName: String? = null,
        family: String? = null,
        type: String? = null,
        imageUrl: String? = null,
        careLevel: String? = null,
        sunlight: List<String>? = null,
        watering: String? = null,
        indoor: Boolean? = null,
        poisonousToHumans: Boolean? = null,
        poisonousToPets: Boolean? = null,
        droughtTolerant: Boolean? = null,
        soil: List<String>? = null,
        notes: String? = null
    ) {
        val data = hashMapOf(
            "common_name" to commonName,
            "scientific_name" to scientificName,
            "plant_name" to plantName,
            "family" to family,
            "type" to type,
            "image_url" to imageUrl,
            "care_level" to careLevel,
            "sunlight" to sunlight,
            "watering" to watering,
            "indoor" to indoor,
            "poisonous_to_humans" to poisonousToHumans,
            "poisonous_to_pets" to poisonousToPets,
            "drought_tolerant" to droughtTolerant,
            "soil" to soil,
            "notes" to notes
        )

        functions.getHttpsCallable("createPlant").call(data)
            .addOnSuccessListener { result ->
                Log.d("PlantViewModel", "Plant added successfully: ${result.data}")
                fetchPlants()
            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Failed to add plant: ${e.message}")
                _plantState.value = PlantListState.Error(e.message ?: "Failed to add plant")
            }
    }

    fun updatePlant(
        plantId: String,
        commonName: String? = null,
        scientificName: List<String>? = null,
        plantName: String? = null,
        family: String? = null,
        type: String? = null,
        imageUrl: String? = null,
        careLevel: String? = null,
        sunlight: List<String>? = null,
        watering: String? = null,
        indoor: Boolean? = null,
        poisonousToHumans: Boolean? = null,
        poisonousToPets: Boolean? = null,
        droughtTolerant: Boolean? = null,
        soil: List<String>? = null,
        notes: String? = null
    ) {
        val updates = hashMapOf<String, Any?>()

        commonName?.let { updates["common_name"] = it }
        scientificName?.let { updates["scientific_name"] = it }
        plantName?.let { updates["plant_name"] = it }
        family?.let { updates["family"] = it }
        type?.let { updates["type"] = it }
        imageUrl?.let { updates["image_url"] = it }
        careLevel?.let { updates["care_level"] = it }
        sunlight?.let { updates["sunlight"] = it }
        watering?.let { updates["watering"] = it }
        indoor?.let { updates["indoor"] = it }
        poisonousToHumans?.let { updates["poisonous_to_humans"] = it }
        poisonousToPets?.let { updates["poisonous_to_pets"] = it }
        droughtTolerant?.let { updates["drought_tolerant"] = it }
        soil?.let { updates["soil"] = it }
        notes?.let { updates["notes"] = it }

        val data = hashMapOf(
            "id" to plantId,
            "updates" to updates
        )

        functions.getHttpsCallable("updatePlant").call(data)
            .addOnSuccessListener { result ->
                Log.d("PlantViewModel", "Plant updated successfully: ${result.data}")
                fetchPlants()
            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Failed to update plant: ${e.message}")
                _plantState.value = PlantListState.Error(e.message ?: "Failed to update plant")
            }
    }

    // Delete Plant with proper state handling
    fun deletePlant(plantId: String) {


        val data = hashMapOf(
            "id" to plantId
        )

        functions.getHttpsCallable("deletePlant").call(data)
            .addOnSuccessListener { result ->
                Log.d("PlantViewModel", "Plant deleted successfully: ${result.data}")

            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Failed to delete plant: ${e.message}")

                // Revert any optimistic updates by refetching
                fetchPlants()
            }
    }

    fun initializeSamplePlants() {
        viewModelScope.launch {
            try {
                val functions = FirebaseFunctions.Companion.getInstance("europe-southwest1")
                val initFunction = functions.getHttpsCallable("initializeSamplePlants")

                initFunction.call()
                    .addOnSuccessListener { result ->
                        val data = result.data as? Map<*, *>
                        val message = data?.get("message") as? String
                        val plantsAdded = (data?.get("plantsAdded") as? Number)?.toInt() ?: 0

                        Log.d("PlantViewModel", "Init result: $message (Added: $plantsAdded)")

                        if (plantsAdded > 0) {
                            fetchPlants()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PlantViewModel", "Failed to initialize plants", exception)
                    }
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Error initializing sample plants", e)
            }
        }
    }

    fun refreshPlants() {
        fetchPlants()
    }
}