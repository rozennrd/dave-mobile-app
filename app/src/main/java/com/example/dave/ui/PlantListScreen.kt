package com.example.dave.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dave.models.LoginModel
import com.example.dave.models.api.PlantViewModel
import com.example.dave.ui.components.DaveNavItem
import com.example.dave.ui.components.NavBar
import com.example.dave.ui.components.PlantList

@Composable
fun PlantListScreen(modifier: Modifier = Modifier,
                    onHomeClick: () -> Unit = {},
                    onAddClick: () -> Unit = {},
                    onAccountClick: () -> Unit = {},
                    navController: NavController,
                    loginModel: LoginModel = viewModel(),
                    plantViewModel: PlantViewModel = viewModel { PlantViewModel(loginModel) }
                    ){
    // Handle redirect on user not logged in
    val currentUser by loginModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("account") { inclusive = true }
            }
        }
    }
    // Use loaded plants
    val plantState by plantViewModel.plantState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        PlantList(
            state = plantState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Leave space for navbar
        )

        NavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onAccountClick = onAccountClick,
            selected = DaveNavItem.HOME
        )
    }

}
