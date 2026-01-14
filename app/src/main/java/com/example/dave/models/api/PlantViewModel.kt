package com.example.dave.models.api

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dave.models.LoginModel
import com.example.dave.ui.components.PlantListState
import com.example.dave.models.Plant
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlantViewModel(private val loginModel: LoginModel) : ViewModel() {

    private val _plantState = MutableStateFlow<PlantListState>(PlantListState.Loading)
    val plantState: StateFlow<PlantListState> = _plantState

    private val functions = FirebaseFunctions.Companion.getInstance("europe-southwest1")

    init {
        viewModelScope.launch {
            loginModel.currentUser.collectLatest { user ->
                if (user != null) {
                    Log.d("DEBUG", "Current User ID: ${user.uid}") // ← Regardez les logs
                    fetchPlants()
                }
            }
        }
    }

    private fun fetchPlants() {
        functions.getHttpsCallable("getPlants").call()
            .addOnSuccessListener { result ->
                try {
                    val data = result.data

                    // Ajouter RETURN après avoir set l'état
                    if (data == null) {
                        _plantState.value = PlantListState.Empty
                        return@addOnSuccessListener  // ← IMPORTANT : sortir ici
                    }

                    // Vérifier si c'est une liste vide
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

    // Dans PlantViewModel
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

        functions.getHttpsCallable("addPlant").call(data)
            .addOnSuccessListener { result ->
                Log.d("PlantViewModel", "Plant added successfully: ${result.data}")
                fetchPlants() // Recharger la liste
            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Failed to add plant: ${e.message}")
            }
    }
}
