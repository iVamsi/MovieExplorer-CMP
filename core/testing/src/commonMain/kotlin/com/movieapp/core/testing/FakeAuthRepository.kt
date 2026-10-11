package com.movieapp.core.testing

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.domain.repository.AuthState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    override val authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    var loginUrlResult: Result<String> = Result.Success("https://www.themoviedb.org/authenticate/token")
    var completeLoginResult: Result<Unit> = Result.Success(Unit)
    val completedLogins = mutableListOf<Pair<String, String>>()
    var logoutCalls = 0

    override suspend fun createLoginApprovalUrl(): Result<String> = loginUrlResult

    override suspend fun completeLoginWithRequestToken(requestToken: String, state: String): Result<Unit> {
        completedLogins += requestToken to state
        return completeLoginResult
    }

    override fun isSignedIn(): Boolean = authState.value is AuthState.SignedIn

    override suspend fun logout() {
        logoutCalls++
        authState.value = AuthState.SignedOut
    }
}
