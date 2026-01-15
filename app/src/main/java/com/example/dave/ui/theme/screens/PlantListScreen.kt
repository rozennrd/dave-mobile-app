package com.example.dave.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dave.models.LoginModel
import com.example.dave.models.api.PlantViewModel
import com.example.dave.ui.components.DaveNavItem
import com.example.dave.ui.components.NavBar
import com.example.dave.ui.components.PlantList
import com.example.dave.ui.theme.GreenPrimary
import com.example.dave.ui.theme.SulphurPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(modifier: Modifier = Modifier,
                    onHomeClick: () -> Unit = {},
                    onAddClick: () -> Unit = {},
                    onAccountClick: () -> Unit = {},
                    navController: NavController,
                    loginModel: LoginModel = viewModel(),
                    plantViewModel: PlantViewModel = viewModel { PlantViewModel(loginModel) }
                    ){
    // Use loaded plants
    val plantState by plantViewModel.plantState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {  // ← Only run once
        loginModel.currentUser.collect { user ->
            if (user == null) {
                navController.navigate("login") {
                    popUpTo("account") { inclusive = true }
                }
            }
        }
    }



    Box(Modifier.fillMaxSize()) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My plants",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SulphurPoint
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    plantViewModel.refreshPlants()
                    // Set a timeout to stop refreshing indicator
                    coroutineScope.launch {
                        delay(1500)
                        isRefreshing = false
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp)
            ) {
                // Plant list
                PlantList(
                    state = plantState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp), // Leave space for navbar
                    onPlantClick = { plant ->
                        navController.navigate("plantDetail/${plant.id}")
                    }
                )
            }
        }

        // Navigation bar
        NavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onAccountClick = onAccountClick,
            selected = DaveNavItem.HOME
        )
    }

}
