package com.example.dave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun FailsafeAsyncImage(
    url: String?,
    fallbackImage: Painter, // Local image resource
    modifier: Modifier = Modifier,
    contentDescription: String,

) {
    var hasError by remember { mutableStateOf(false) }

    if (hasError || url?.isBlank() == true) {
        // Show fallback
        Image(
            painter = fallbackImage,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            onError = { hasError = true } // ← This triggers fallback
        )
    }
}