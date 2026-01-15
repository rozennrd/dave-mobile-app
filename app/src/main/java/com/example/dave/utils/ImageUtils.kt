package com.example.dave.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageUtils(private val context: Context) {

    companion object {
        private const val MAX_AVATAR_SIZE = 1024 // Max width/height in pixels
        private const val JPEG_QUALITY = 85 // JPEG compression quality (0-100)
        private const val MAX_FILE_SIZE_BYTES = 500 * 1024 // 500KB max file size
    }

    /**
     * Compresses and resizes an image for avatar upload
     * @param imageUri The URI of the image to process
     * @return Uri of the compressed image file, or null if processing failed
     */
    suspend fun compressAvatarImage(imageUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Load bitmap from URI
                val bitmap = loadBitmapFromUri(imageUri) ?: return@withContext null

                // Resize if needed
                val resizedBitmap = resizeBitmapIfNeeded(bitmap)

                // Compress to JPEG
                val compressedBytes = compressBitmapToJpeg(resizedBitmap)

                // Save to temporary file
                val tempFile = createTempFile("avatar", ".jpg")
                FileOutputStream(tempFile).use { outputStream ->
                    outputStream.write(compressedBytes)
                }

                Uri.fromFile(tempFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // If image is already small enough, return as-is
        if (width <= MAX_AVATAR_SIZE && height <= MAX_AVATAR_SIZE) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            // Landscape
            newWidth = MAX_AVATAR_SIZE
            newHeight = (MAX_AVATAR_SIZE / ratio).toInt()
        } else {
            // Portrait or square
            newHeight = MAX_AVATAR_SIZE
            newWidth = (MAX_AVATAR_SIZE * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun compressBitmapToJpeg(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()

        // Start with high quality
        var quality = JPEG_QUALITY
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // If file is too large, reduce quality until it's small enough
        while (outputStream.size() > MAX_FILE_SIZE_BYTES && quality > 10) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        return outputStream.toByteArray()
    }

    private fun createTempFile(prefix: String, suffix: String): File {
        val tempDir = File(context.cacheDir, "avatars")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return File.createTempFile(prefix, suffix, tempDir)
    }

    /**
     * Gets the file size of a URI in human readable format
     */
    fun getFileSize(uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (sizeIndex != -1) {
                        val size = it.getLong(sizeIndex)
                        return formatFileSize(size)
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        return "%.1f MB".format(mb)
    }
}
