package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * UI state for profile screen.
 */
data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSignedOut: Boolean = false,
    val isDeleted: Boolean = false
)

/**
 * ViewModel for profile screen.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            val email = user.email.orEmpty()

            // Fetch username from users table
            runCatching {
                supabase.postgrest["users"]
                    .select {
                        filter { eq("id", user.id) }
                        limit(1)
                    }
                    .decodeSingle<UserProfileRow>()
            }.onSuccess { row ->
                _uiState.update { it.copy(username = row.username, email = email) }
            }.onFailure {
                _uiState.update { it.copy(email = email) }
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                _uiState.update { it.copy(isSaving = false, saveError = "Not logged in") }
                return@launch
            }

            runCatching {
                supabase.postgrest["users"].upsert(
                    mapOf("id" to userId, "username" to newUsername)
                )
            }.onSuccess {
                _uiState.update { it.copy(username = newUsername, isSaving = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            val email = _uiState.value.email
            runCatching {
                // Re-authenticate to verify current password
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = currentPassword
                }
                // Now update to new password
                supabase.auth.updateUser {
                    password = newPassword
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { supabase.auth.signOut() }
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            runCatching {
                supabase.functions.invoke("delete-account")
                supabase.auth.signOut()
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isDeleted = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(saveError = null) }
    }
}

@Serializable
private data class UserProfileRow(
    @SerialName("username") val username: String = ""
)
