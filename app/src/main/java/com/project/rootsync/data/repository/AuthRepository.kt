package com.project.rootsync.data.repository

import com.project.rootsync.service.NotificationService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@Serializable
private data class UserEmailRow(val email: String)

@Serializable
private data class UserIdRow(val id: String)

/**
 * Repository for authentication-related operations.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val notificationService: NotificationService
) {
    val authState: Flow<AuthState> = supabase.auth.sessionStatus.map { status ->
        when (val currentStatus = status) {
            is SessionStatus.Authenticated -> {
                AuthState.Authenticated(currentStatus.session.user?.id ?: "")
            }
            is SessionStatus.NotAuthenticated -> {
                AuthState.Unauthenticated
            }
            is SessionStatus.LoadingFromStorage -> {
                AuthState.Loading
            }
            else -> AuthState.Unauthenticated
        }
    }

    /**
     * Looks up email by username from the users table.
     * Returns null if no user found with that username.
     */
    suspend fun getEmailByUsername(username: String): String? {
        return try {
            val rows = supabase.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                    limit(1)
                }
                .decodeList<UserEmailRow>()
            rows.firstOrNull()?.email
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a username is already taken.
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val rows = supabase.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                    limit(1)
                }
                .decodeList<UserIdRow>()
            rows.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Signs in with email and password.
     */
    suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Signs in with email and password (wrapper for compatibility).
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            signInWithEmail(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs up with email and password.
     * Returns Pair(userExists, sessionExists) for flow control.
     */
    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the username for a given user ID in the users table.
     */
    suspend fun updateUsername(userId: String, username: String) {
        supabase.postgrest["users"]
            .update(mapOf("username" to username)) {
                filter {
                    eq("id", userId)
                }
            }
    }

    /**
     * Signs out the current user.
     */
    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * Gets the current user's ID.
     */
    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    /**
     * Gets the current session.
     */
    fun getCurrentSession() = supabase.auth.currentSessionOrNull()

    /**
     * Called after successful login/signup to initialize notifications.
     */
    suspend fun onUserLogin() {
        notificationService.onUserLogin()
    }
}
