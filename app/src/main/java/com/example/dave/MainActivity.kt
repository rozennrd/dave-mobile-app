package com.example.dave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dave.ui.AccountScreen
import com.example.dave.ui.LoginScreen
import com.example.dave.ui.PlantListScreen
import com.example.dave.ui.screens.PlantDetail
import com.example.dave.ui.screens.PlantDetailScreen
import com.example.dave.ui.theme.DaveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaveTheme {
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
                            onAccountClick = { navController.navigate("account") }
                        )
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