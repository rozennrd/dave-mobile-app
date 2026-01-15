package com.example.dave.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dave.R
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePicker(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onRemoveAvatar: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Permission states
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val storagePermission = rememberPermissionState(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    // Activity result launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Convert bitmap to URI and pass it
            val uri = bitmapToUri(context, bitmap)
            uri?.let { onImageSelected(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Avatar Source",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Camera option
                    OutlinedButton(
                        onClick = {
                            when {
                                cameraPermission.status.isGranted -> {
                                    cameraLauncher.launch(null)
                                    onDismiss()
                                }
                                cameraPermission.status.shouldShowRationale -> {
                                    // Show rationale and request again
                                    cameraPermission.launchPermissionRequest()
                                }
                                else -> {
                                    cameraPermission.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text("📷 Take Photo")
                    }

                    // Gallery option
                    OutlinedButton(
                        onClick = {
                            when {
                                storagePermission.status.isGranted -> {
                                    galleryLauncher.launch("image/*")
                                    onDismiss()
                                }
                                storagePermission.status.shouldShowRationale -> {
                                    storagePermission.launchPermissionRequest()
                                }
                                else -> {
                                    storagePermission.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text("🖼️ Choose from Gallery")
                    }

                    // Remove avatar option
                    onRemoveAvatar?.let {
                        OutlinedButton(
                            onClick = {
                                it()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("🗑️ Remove Avatar")
                        }
                    }

                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }

                    // Permission status messages
                    if (!cameraPermission.status.isGranted) {
                        Text(
                            text = "Camera permission needed to take photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (!storagePermission.status.isGranted) {
                        Text(
                            text = "Storage permission needed to select photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Converts a Bitmap to a Uri by saving it to cache
 */
private fun bitmapToUri(context: Context, bitmap: android.graphics.Bitmap): Uri? {
    return try {
        val file = java.io.File(context.cacheDir, "temp_avatar.jpg")
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}
