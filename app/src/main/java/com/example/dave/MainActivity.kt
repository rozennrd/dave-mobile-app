package com.example.dave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dave.models.LoginModel
import com.example.dave.models.api.PlantViewModel
import com.example.dave.models.api.PlantViewModelFactory
import com.example.dave.ui.screens.AccountScreen
import com.example.dave.ui.screens.LoginScreen
import com.example.dave.ui.screens.PlantListScreen
import com.example.dave.ui.components.PlantListState
import com.example.dave.ui.screens.PlantDetail
import com.example.dave.ui.screens.PlantDetailScreen
import com.example.dave.ui.theme.DaveTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaveTheme {
                // Instantiate your ViewModels
                val loginModel: LoginModel = viewModel()
                val plantViewModel: PlantViewModel = viewModel(
                    factory = PlantViewModelFactory(loginModel)
                )
                val navController = rememberNavController()



                NavHost(
                    navController = navController,
                    startDestination = "myPlants"
                ) {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }

                    composable("account") {
                        AccountScreen(
                            navController = navController,
                            onHomeClick = { navController.navigate("myPlants") },
                            onAccountClick = { /* already on account */ },
                            onAddClick = { navController.navigate("addPlant") }
                        )
                    }

                    composable("myPlants") {
                        // Initialize sample plants ONCE at app level, not on every navigation
                        LaunchedEffect(Unit) {
                            plantViewModel.initializeSamplePlants()
                        }
                        PlantListScreen(
                            navController = navController,
                            onHomeClick = { /* already on home */ },
                            onAccountClick = { navController.navigate("account") },
                            onAddClick = { navController.navigate("addPlant") }
                        )
                    }

                    composable("addPlant") {
                        PlantDetailScreen(
                            plant = PlantDetail(
                                id = 0,
                                surname = null,
                                commonName = "Plant name",
                                scientificName = emptyList(),
                                family = null,
                                type = null,
                                imageUrl = null,
                                careLevel = null,
                                sunlight = null,
                                watering = null,
                                indoor = null,
                                poisonousToHumans = null,
                                poisonousToPets = null,
                                droughtTolerant = null,
                                soil = null,
                                notes = null
                            ),
                            isAddMode = true,
                            onHomeClick = { navController.navigate("myPlants") },
                            onAddClick = { /* already on addPlant */ },
                            onAccountClick = { navController.navigate("account") },
                            plantViewModel = plantViewModel,  // Pass the plantViewModel
                            onAddPlantClick = {
                                // Navigate back to plant list after adding
                                navController.navigate("myPlants") {
                                    popUpTo("myPlants") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        "plantDetail/{plantId}",
                        arguments = listOf(navArgument("plantId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId")

                        // Find the plant from the current plant list
                        val plantState by plantViewModel.plantState.collectAsState()
                        val plant = if (plantState is PlantListState.Success) {
                            (plantState as PlantListState.Success)
                                .plants.find { it.id == plantId }
                        } else null

                        plant?.let {
                            PlantDetailScreen(
                                plant = PlantDetail(
                                    id = it.id?.toIntOrNull() ?: 0,
                                    surname = it.plantName,
                                    commonName = it.commonName,
                                    scientificName = it.scientificName,
                                    family = it.family,
                                    type = it.type,
                                    imageUrl = it.imageUrl,
                                    careLevel = it.careLevel,
                                    sunlight = it.sunlight,
                                    watering = it.watering,
                                    indoor = it.indoor,
                                    poisonousToHumans = it.poisonousToHumans,
                                    poisonousToPets = it.poisonousToPets,
                                    droughtTolerant = it.droughtTolerant,
                                    soil = it.soil,
                                    notes = it.notes
                                ),
                                onDeleteClick = {
                                    plantViewModel.deletePlant(it.id ?: "")
                                    navController.popBackStack()
                                },
                                onModifyClick = { surname, notes ->
                                    plantViewModel.updatePlant(
                                        plantId = it.id ?: "",
                                        plantName = surname,
                                        notes = notes
                                    )
                                },
                                onHomeClick = {
                                    navController.navigate("myPlants") {
                                        popUpTo("myPlants") { inclusive = true }
                                    }
                                },
                                onAddClick = {
                                    navController.navigate("addPlant")
                                },
                                onAccountClick = {
                                    navController.navigate("account")
                                },
                                plantViewModel = plantViewModel,
                            )
                        } ?: run {
                            // Plant not found, show loading or go back
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                            // Only navigate back if we're sure the plant doesn't exist
                            if (plantState is PlantListState.Success || plantState is PlantListState.Empty) {
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen(navController = rememberNavController())
}