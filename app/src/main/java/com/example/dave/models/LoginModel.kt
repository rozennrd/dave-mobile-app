package com.example.dave.models

import androidx.lifecycle.ViewModel
import android.net.Uri
import com.example.dave.utils.ImageUtils
import com.example.dave.utils.StorageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await


class LoginModel : ViewModel() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
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

            // Set user ID for Crashlytics
            authResult.user?.uid?.let { uid ->
                crashlytics.setUserId(uid)
                crashlytics.setCustomKey("user_email", email)
                crashlytics.log("User signed in successfully")
            }

            Result.success(authResult.user!!)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("sign_in_error", e.message ?: "Unknown error")
            crashlytics.log("Sign in failed for email: $email")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String? = null): Result<FirebaseUser> {
        return try {
            _authState.value = AuthState.Loading

            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()

            // Optionnel : définir le displayName
            name?.trim()?.takeIf { it.isNotEmpty() }?.let { displayName ->
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                authResult.user?.updateProfile(updates)?.await()
            }

            authResult.user?.reload()?.await()
            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            // Set user ID for Crashlytics after successful signup
            authResult.user?.uid?.let { uid ->
                crashlytics.setUserId(uid)
                crashlytics.setCustomKey("user_email", email)
                crashlytics.setCustomKey("user_name", name ?: "")
                crashlytics.log("User signed up successfully")
            }

            Result.success(authResult.user!!)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("sign_up_error", "${e.errorCode}: ${e.message}")
            crashlytics.setCustomKey("sign_up_email", email)
            crashlytics.log("Sign up failed for email: $email")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("sign_up_error", e.message ?: "Unknown error")
            crashlytics.setCustomKey("sign_up_email", email)
            crashlytics.log("Sign up failed for email: $email")
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

            crashlytics.log("User updated password successfully")

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("password_update_error", "${e.errorCode}: ${e.message}")
            crashlytics.log("Password update failed")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update password")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("password_update_error", e.message ?: "Unknown error")
            crashlytics.log("Password update failed")
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

            crashlytics.setCustomKey("user_name", trimmed)
            crashlytics.log("User updated display name successfully")

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("display_name_update_error", "${e.errorCode}: ${e.message}")
            crashlytics.log("Display name update failed")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update name")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("display_name_update_error", e.message ?: "Unknown error")
            crashlytics.log("Display name update failed")
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

            crashlytics.log("User updated avatar successfully")

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("avatar_update_error", "${e.errorCode}: ${e.message}")
            crashlytics.log("Avatar update failed - Firebase Auth error")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update avatar")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("avatar_update_error", e.message ?: "Unknown error")
            crashlytics.log("Avatar update failed")
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
                crashlytics.log("Storage avatar deletion failed, but continuing with profile update")
            }

            // Update Firebase Auth profile to remove photo
            val updates = UserProfileChangeRequest.Builder()
                .setPhotoUri(null)
                .build()

            user.updateProfile(updates).await()
            user.reload().await()

            _currentUser.value = auth.currentUser
            _authState.value = AuthState.Success(auth.currentUser)

            crashlytics.log("User deleted avatar successfully")

            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("avatar_delete_error", "${e.errorCode}: ${e.message}")
            crashlytics.log("Avatar delete failed - Firebase Auth error")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to delete avatar")
            crashlytics.recordException(e)
            crashlytics.setCustomKey("avatar_delete_error", e.message ?: "Unknown error")
            crashlytics.log("Avatar delete failed")
            Result.failure(e)
        }
    }

    // Sign Out
    fun signOut() {
        val currentUserId = auth.currentUser?.uid
        auth.signOut()
        _authState.value = AuthState.Success(null)

        crashlytics.log("User signed out successfully")
        // Clear user-specific data from Crashlytics
        crashlytics.setUserId("")
        crashlytics.setCustomKey("user_email", "")
        crashlytics.setCustomKey("user_name", "")
    }
}
