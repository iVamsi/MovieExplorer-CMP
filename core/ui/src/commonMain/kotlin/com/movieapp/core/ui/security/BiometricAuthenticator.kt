package com.movieapp.core.ui.security

interface BiometricAuthenticator {
    fun isAvailable(): Boolean

    suspend fun authenticate(title: String, subtitle: String): AppLockAuthResult
}

enum class AppLockAuthResult {
    Succeeded,
    Unavailable,
    Canceled,
    Failed,
}
