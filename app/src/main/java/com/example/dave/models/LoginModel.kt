package com.example.dave.models

import androidx.lifecycle.ViewModel
import android.net.Uri
import com.example.dave.utils.ImageUtils
import com.example.dave.utils.StorageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await


class LoginModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    // State Flows for UI observation
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: FirebaseUser?) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    // Email/Password Sign In
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            _authState.value = AuthState.Loading
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.Success(authResult.user)
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }

    // Update email
    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))

            val trimmed = newEmail.trim()

            user.updateEmail(trimmed).await()

            // ✅ IMPORTANT : reload pour récupérer l’état serveur
            user.reload().await()

            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            // ✅ On remonte le code + message Firebase (super utile)
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update email")
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))
            val trimmed = newPassword.trim()

            user.updatePassword(trimmed).await()

            user.reload().await()
            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update password")
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(newName: String): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))
            val trimmed = newName.trim()
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmed)
                .build()

            user.updateProfile(updates).await()
            user.reload().await()

            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update name")
            Result.failure(e)
        }
    }


    suspend fun updateAvatar(imageUri: Uri, context: android.content.Context): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))

            // Compress the image
            val imageUtils = ImageUtils(context)
            val compressedUri = imageUtils.compressAvatarImage(imageUri)
                ?: return Result.failure(Exception("Failed to compress image"))

            // Upload to Firebase Storage
            val storageUtils = StorageUtils()
            val uploadResult = storageUtils.uploadAvatar(user.uid, compressedUri) { progress ->
                // Could emit progress updates here if needed
            }

            if (uploadResult.isFailure) {
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))
            }

            val downloadUrl = uploadResult.getOrNull()!!

            // Update Firebase Auth profile
            val updates = UserProfileChangeRequest.Builder()
                .setPhotoUri(android.net.Uri.parse(downloadUrl))
                .build()

            user.updateProfile(updates).await()
            user.reload().await()

            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update avatar")
            Result.failure(e)
        }
    }

    suspend fun deleteAvatar(): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))

            // Delete from Firebase Storage
            val storageUtils = StorageUtils()
            val deleteResult = storageUtils.deleteAvatar(user.uid)
            if (deleteResult.isFailure) {
                // Continue anyway - storage deletion failure shouldn't block profile update
            }

            // Update Firebase Auth profile to remove photo
            val updates = UserProfileChangeRequest.Builder()
                .setPhotoUri(null)
                .build()

            user.updateProfile(updates).await()
            user.reload().await()

            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to delete avatar")
            Result.failure(e)
        }
    }

    // Sign Out
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Success(null)
    }
}
