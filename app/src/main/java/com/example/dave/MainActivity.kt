package com.example.dave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dave.components.PlantList
import com.example.dave.components.PlantListState
import com.example.dave.models.api.PlantViewModel
import com.example.dave.ui.AccountScreen
import com.example.dave.ui.LoginScreen
import com.example.dave.ui.theme.DaveTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Successfully signed in anonymously
            } else {
                // Handle error
            }
        }
        super.onCreate(savedInstanceState)

        setContent {
            DaveTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }

                    composable("account") {
                        AccountScreen(navController = navController)
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

@PreviewScreenSizes
@Composable
fun DaveApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> PlantListScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.FAVORITES -> Greeting("Favorites", modifier = Modifier.padding(innerPadding))
                AppDestinations.PROFILE -> Greeting("Profile", modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun PlantListScreen(modifier: Modifier = Modifier) {
    val viewModel: PlantViewModel = viewModel()
    val state by viewModel.plantState.collectAsState()
    PlantList(state = state, modifier = modifier)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DaveTheme {
        Greeting("Android")
    }
}