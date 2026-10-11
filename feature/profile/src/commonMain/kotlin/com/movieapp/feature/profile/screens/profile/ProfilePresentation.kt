package com.movieapp.feature.profile.screens.profile

import com.movieapp.core.domain.repository.AuthState

internal enum class AccountStatus {
    SignedOut,
    SignedIn,
}

internal data class AccountPresentation(
    val status: AccountStatus,
    val displayName: String?,
    val localLibraryAvailable: Boolean,
)

internal fun accountPresentation(authState: AuthState): AccountPresentation =
    when (authState) {
        AuthState.SignedOut ->
            AccountPresentation(
                status = AccountStatus.SignedOut,
                displayName = null,
                localLibraryAvailable = true,
            )

        is AuthState.SignedIn ->
            AccountPresentation(
                status = AccountStatus.SignedIn,
                displayName = authState.username,
                localLibraryAvailable = true,
            )
    }
