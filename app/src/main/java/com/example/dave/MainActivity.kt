package com.example.dave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.dave.ui.AccountScreen
import com.example.dave.ui.LoginScreen
import com.example.dave.ui.theme.DaveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DaveTheme() {
                LoginScreen()
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    AccountScreen()
}

@Preview
@Composable
fun AccountScreenPreview() {
    LoginScreen()
}