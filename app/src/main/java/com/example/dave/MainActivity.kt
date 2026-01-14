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
                            onAddClick = { /* TODO: add plant screen */ }
                        )
                    }

                    composable("myPlants") {
                        PlantListScreen(
                            navController = navController,
                            onHomeClick = { /* already on home */ },
                            onAccountClick = { navController.navigate("account") },
                            onAddClick = { /* TODO: add plant screen */ }
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
