package com.project.rootsync.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    val authState: Flow<AuthState> = supabase.auth.sessionStatus.map { status ->
        when (val currentStatus = status) {
            is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> {
                AuthState.Authenticated(currentStatus.session.user?.id ?: "")
            }
            is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> {
                AuthState.Unauthenticated
            }
            is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage -> {
                AuthState.Loading
            }
            else -> AuthState.Unauthenticated
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
}
