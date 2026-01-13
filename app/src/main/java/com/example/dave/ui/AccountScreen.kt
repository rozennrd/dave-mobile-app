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

enum class EditableField { NAME, EMAIL, PASSWORD }

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


    // Drafts (ce qu’on modifie pendant l’édition)
    var draftName by remember { mutableStateOf(name) }
    var draftEmail by remember { mutableStateOf(email) }
    var draftPassword by remember { mutableStateOf(password) }

    // Quel champ est en édition (null = aucun)
    var editingField by remember { mutableStateOf<EditableField?>(null) }
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
                value = if (editingField == EditableField.NAME) draftName else name,
                onValueChange = { draftName = it },
                placeholder = "Nom  Prénom",
                leadingDrawable = R.drawable.ic_user,
                isEditing = editingField == EditableField.NAME,
                onEditClick = {
                    // quand on clique le crayon : on ouvre l’édition et on init le draft
                    draftName = name
                    editingField = EditableField.NAME
                },
                onValidateClick = {
                    name = draftName
                    editingField = null
                }
            )

            Spacer(Modifier.height(12.dp))

            FieldWithEdit(
                value = if (editingField == EditableField.EMAIL) draftEmail else email,
                onValueChange = { draftEmail = it },
                placeholder = "email@blabla.com",
                leadingDrawable = R.drawable.ic_at,
                isEditing = editingField == EditableField.EMAIL,
                onEditClick = {
                    draftEmail = email
                    editingField = EditableField.EMAIL
                },
                onValidateClick = {
                    email = draftEmail
                    editingField = null
                }
            )

            Spacer(Modifier.height(12.dp))

            FieldWithEdit(
                value = if (editingField == EditableField.PASSWORD) draftPassword else password,
                onValueChange = { draftPassword = it },
                placeholder = "••••••••",
                leadingDrawable = R.drawable.ic_lock,
                isPassword = true,
                isEditing = editingField == EditableField.PASSWORD,
                onEditClick = {
                    draftPassword = password
                    editingField = EditableField.PASSWORD
                },
                onValidateClick = {
                    password = draftPassword
                    editingField = null
                }
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
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onValidateClick: () -> Unit,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxWidth()) {
            RoundedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                leadingIcon = painterResource(leadingDrawable),
                fieldColor = BrownPrimary,
                hintColor = Color(0xFFEADAC0),
                textColor = Color.White,
                iconSize = 22.dp,
                readOnly = !isEditing, // ✅ IMPORTANT
                visualTransformation = if (isPassword)
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                else
                    androidx.compose.ui.text.input.VisualTransformation.None
            )

            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "Edit",
                tint = BlueSoft,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 18.dp)
                    .size(20.dp)
                    .clickable { onEditClick() }
            )
        }

        if (isEditing) {
            Spacer(Modifier.height(10.dp))

            androidx.compose.material3.Button(
                onClick = onValidateClick,
                shape = RoundedCornerShape(50),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(40.dp)
            ) {
                Text("Valider", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

