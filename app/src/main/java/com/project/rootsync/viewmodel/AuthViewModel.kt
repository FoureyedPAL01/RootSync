package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.repository.AuthRepository
import com.project.rootsync.data.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for authentication.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val obscureSignIn: Boolean = true,
    val obscureSignUp: Boolean = true
)

sealed class SessionState {
    data object Loading : SessionState()
    data object Authenticated : SessionState()
    data object Unauthenticated : SessionState()
}

/**
 * ViewModel for authentication.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val sessionState: StateFlow<AuthState> = authRepository.authState
        .let { flow ->
            val state = MutableStateFlow<AuthState>(AuthState.Loading)
            viewModelScope.launch {
                flow.collect { authState ->
                    state.value = authState
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = authState is AuthState.Authenticated,
                        isLoading = authState is AuthState.Loading
                    )
                }
            }
            state.asStateFlow()
        }

    /**
     * Sign in with email or username.
     * Auto-detects email vs username by checking for '@'.
     * If username, queries users table to get the associated email.
     */
    fun signIn(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val raw = identifier.trim()
                val email = if (raw.contains("@")) {
                    raw
                } else {
                    // Username provided - look up email
                    authRepository.getEmailByUsername(raw)
                        ?: throw Exception("No account found with that username.")
                }

                authRepository.signInWithEmail(email, password)
                authRepository.onUserLogin()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Unexpected error. Please try again.")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Sign up with email, username, and password.
     * Checks username availability before creating account.
     */
    fun signUp(email: String, username: String, password: String) {
        viewModelScope.launch {
            // Validate username first (before setting loading)
            if (username.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please enter a username.") }
                return@launch
            }

            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                // Check if username is taken
                if (authRepository.isUsernameTaken(username)) {
                    _uiState.update {
                        it.copy(errorMessage = "That username is already taken.")
                    }
                    return@launch
                }

                // Create auth account
                authRepository.signUp(email.trim(), password)

                // Update username after account creation
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    authRepository.updateUsername(userId, username)
                    authRepository.onUserLogin()
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Unexpected error. Please try again.")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleSignInObscure() {
        _uiState.update { it.copy(obscureSignIn = !it.obscureSignIn) }
    }

    fun toggleSignUpObscure() {
        _uiState.update { it.copy(obscureSignUp = !it.obscureSignUp) }
    }

    /**
     * Clear error state - called when switching tabs.
     */
    fun clearError() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                obscureSignIn = true,
                obscureSignUp = true
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
