package com.example.dave.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest

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

            Result.success(authResult.user!!)
        } catch (e: FirebaseAuthException) {
            _authState.value = AuthState.Error("${e.errorCode}: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign up failed")
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


    // Sign Out
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Success(null)
    }
}