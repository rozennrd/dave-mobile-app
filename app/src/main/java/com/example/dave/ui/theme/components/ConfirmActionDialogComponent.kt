package com.example.dave.ui.theme.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.dave.ui.theme.BrownPrimary
import com.example.dave.ui.theme.GreenPrimary
import com.example.dave.ui.theme.Red
import com.example.dave.ui.theme.SulphurPoint


@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDelete: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = SulphurPoint) },
        text = { Text(message, fontFamily = SulphurPoint) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDelete) Red else GreenPrimary
                )
            ) {
                Text(confirmLabel, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BrownPrimary)
            }
        }
    )
}