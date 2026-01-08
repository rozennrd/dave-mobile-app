package com.example.dave.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dave.R
import com.example.dave.ui.components.RoundedTextField
import com.example.dave.ui.theme.*

@Composable
fun LoginScreen(
    onSignIn: (username: String, password: String) -> Unit = { _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(150.dp))

        // Rond vert + logo blanc
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_dave),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(62.dp)
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Welcome on DAVE",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Dark
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Let’s grow together 💚",
            fontSize = 14.sp,
            color = BlueSoft
        )

        Spacer(Modifier.height(60.dp))

        RoundedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = "Username",
            leadingIcon = painterResource(R.drawable.ic_user),
            fieldColor = BrownPrimary,
            hintColor = Color.White,
            textColor = Color.White
        )

        Spacer(Modifier.height(14.dp))

        RoundedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            leadingIcon = painterResource(R.drawable.ic_lock),
            fieldColor = BrownPrimary,
            hintColor = Color.White,
            textColor = Color.White,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(60.dp))

        Button(
            onClick = { onSignIn(username, password) },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .width(160.dp)
                .height(48.dp)
        ) {
            Text("Sign in", fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DaveTheme {
        LoginScreen()
    }
}
