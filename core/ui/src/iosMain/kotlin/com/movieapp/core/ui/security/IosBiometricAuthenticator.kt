package com.movieapp.core.ui.security

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

@OptIn(ExperimentalForeignApi::class)
class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun isAvailable(): Boolean =
        LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)

    override suspend fun authenticate(title: String, subtitle: String): AppLockAuthResult =
        suspendCancellableCoroutine { continuation ->
            LAContext().evaluatePolicy(
                policy = LAPolicyDeviceOwnerAuthentication,
                localizedReason = subtitle,
            ) { success, error ->
                if (!continuation.isActive) return@evaluatePolicy
                continuation.resume(
                    when {
                        success -> AppLockAuthResult.Succeeded
                        error?.code == LAErrorUserCancel -> AppLockAuthResult.Canceled
                        else -> AppLockAuthResult.Failed
                    }
                )
            }
        }
}
