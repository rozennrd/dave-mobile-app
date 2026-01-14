/*

TODO : A utiliser avec firebase si besoin -> sinon à supprimer
package com.example.dave.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlantDetailViewModel : ViewModel() {

    private val database = FirebaseFirestore.getInstance()

    // ----- UI STATE -----
    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        object Success : ActionState()
        data class Error(val message: String) : ActionState()
    }

    // ----- UPDATE PLANT -----
    fun updatePlant(plantId: Int, surname: String, notes: String?) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                val updates = hashMapOf<String, Any>(
                    "scientific_name" to listOf(surname)
                )
                if (notes != null) updates["notes"] = notes

                database.collection("plants")
                    .document(plantId.toString())
                    .update(updates)
                    .addOnSuccessListener {
                        _actionState.value = ActionState.Success
                    }
                    .addOnFailureListener { e ->
                        _actionState.value =
                            ActionState.Error(e.localizedMessage ?: "Update failed")
                    }

            } catch (e: Exception) {
                _actionState.value =
                    ActionState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }

    // ----- DELETE PLANT -----
    fun deletePlant(plantId: Int) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                database.collection("plants")
                    .document(plantId.toString())
                    .delete()
                    .addOnSuccessListener {
                        _actionState.value = ActionState.Success
                    }
                    .addOnFailureListener { e ->
                        _actionState.value =
                            ActionState.Error(e.localizedMessage ?: "Delete failed")
                    }

            } catch (e: Exception) {
                _actionState.value =
                    ActionState.Error(e.localizedMessage ?: "Delete failed")
            }
        }
    }
}
*/
