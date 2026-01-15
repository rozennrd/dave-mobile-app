package com.example.dave.models.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dave.models.LoginModel
import com.example.dave.models.api.PlantViewModel

class PlantViewModelFactory(
    private val loginModel: LoginModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(loginModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}