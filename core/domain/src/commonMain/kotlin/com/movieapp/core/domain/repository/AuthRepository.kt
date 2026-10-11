package com.movieapp.core.domain.repository

import com.movieapp.core.common.Result
import kotlinx.coroutines.flow.Flow

/** TMDB account session (optional). OSS default: no Firebase or app-owned backend. */
interface AuthRepository {

    /** Current sign-in state; [AuthState.SignedOut] when logged out or session invalid. */
    val authState: Flow<AuthState>

    /**
     * Creates a request token and returns the TMDB URL where the user must approve access. Open
     * this URL in a browser or Custom Tab.
     */
    suspend fun createLoginApprovalUrl(): Result<String>

    /**
     * Completes login after TMDB redirects with an approved [requestToken] and matching [state].
     */
    suspend fun completeLoginWithRequestToken(requestToken: String, state: String): Result<Unit>

    /** True when a TMDB session is already active locally. */
    fun isSignedIn(): Boolean

    /** Clears session locally and on TMDB when possible. */
    suspend fun logout()
}

sealed interface AuthState {

    data object SignedOut : AuthState

    data class SignedIn(
        val accountId: Int,
        val username: String,
        val name: String?,
        val avatarPath: String?,
    ) : AuthState
}
