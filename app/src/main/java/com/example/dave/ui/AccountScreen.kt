package com.example.dave.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dave.R
import com.example.dave.models.LoginModel
import com.example.dave.ui.components.NavBar
import com.example.dave.ui.components.DaveNavItem
import com.example.dave.ui.components.RoundedTextField
import com.example.dave.ui.theme.*

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    initialName: String = "Prénom Nom",
    initialEmail: String = "email@blabla.com",
    onHomeClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    navController: NavController,
    loginModel: LoginModel = viewModel()
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("••••••••") }

    val currentUser by loginModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("account") { inclusive = true }
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(bottom = 120.dp), // laisse de la place pour la navbar + bouton +
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = "Back/Logout",
                    tint = BlueSoft,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { loginModel.signOut() }
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD6D8C8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.photo_profil),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = name.ifBlank { "Name" },
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Outfit,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(47.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Personnalize",
                    fontSize = 20.sp,
                    color = BlueSoft,
                    fontFamily = Jost,
                    fontWeight = FontWeight.ExtraLight
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldWithEdit(
                value = name,
                onValueChange = { name = it },
                placeholder = "Nom  Prénom",
                leadingDrawable = R.drawable.ic_user,
                onEditClick = {}
            )

            Spacer(Modifier.height(12.dp))

            FieldWithEdit(
                value = email,
                onValueChange = { email = it },
                placeholder = "email@blabla.com",
                leadingDrawable = R.drawable.ic_at,
                onEditClick = {}
            )

            Spacer(Modifier.height(12.dp))

            FieldWithEdit(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                leadingDrawable = R.drawable.ic_lock,
                onEditClick = {},
                isPassword = true
            )
        }

        NavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onAccountClick = onAccountClick,
            selected = DaveNavItem.ACCOUNT
        )
    }
}


@Composable
private fun FieldWithEdit(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingDrawable: Int,
    onEditClick: () -> Unit,
    isPassword: Boolean = false
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        RoundedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = painterResource(leadingDrawable),
            fieldColor = BrownPrimary,
            hintColor = Color(0xFFEADAC0), // beige clair pour la hint
            textColor = Color.White,
            iconSize = 22.dp,
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None
        )

        // Petit crayon à droite (superposé)
        Icon(
            painter = painterResource(R.drawable.ic_edit), // crayon
            contentDescription = "Edit",
            tint = BlueSoft,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(20.dp)
                .clickable { onEditClick() }
        )
    }
}
