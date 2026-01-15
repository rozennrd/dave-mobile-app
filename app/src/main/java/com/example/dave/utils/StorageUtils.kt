package com.example.dave.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class StorageUtils {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Uploads an image to Firebase Storage for the given user
     * @param userId The user's Firebase Auth UID
     * @param imageUri The URI of the image to upload
     * @param onProgress Callback for upload progress (0.0 to 1.0)
     * @return The download URL of the uploaded image
     */
    suspend fun uploadAvatar(
        userId: String,
        imageUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        return try {
            val avatarRef = storageRef.child("avatars/$userId/avatar.jpg")

            val uploadTask = avatarRef.putFile(imageUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                onProgress(progress.toFloat() / 100f)
            }

            // Wait for upload to complete
            uploadTask.await()

            // Get download URL
            val downloadUrl = avatarRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: CancellationException) {
            Result.failure(Exception("Upload was cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes the user's avatar from Firebase Storage
     * @param userId The user's Firebase Auth UID
     */
    suspend fun deleteAvatar(userId: String): Result<Unit> {
        return try {
            val avatarRef = storageRef.child("avatars/$userId/avatar.jpg")
            avatarRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if a user has an avatar in storage
     * @param userId The user's Firebase Auth UID
     */
    suspend fun hasAvatar(userId: String): Boolean {
        return try {
            val avatarRef = storageRef.child("avatars/$userId/avatar.jpg")
            avatarRef.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
