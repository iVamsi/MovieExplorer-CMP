package com.movieapp.core.ui.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidBiometricAuthenticator(
    private val context: Context,
    private val activityProvider: () -> FragmentActivity?,
) : BiometricAuthenticator {

    override fun isAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun authenticate(title: String, subtitle: String): AppLockAuthResult {
        val activity = activityProvider() ?: return AppLockAuthResult.Unavailable
        if (!isAvailable()) return AppLockAuthResult.Unavailable

        return suspendCancellableCoroutine { continuation ->
            val prompt =
                BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(context),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(AppLockAuthResult.Succeeded)
                            }
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    if (
                                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                    ) {
                                        AppLockAuthResult.Canceled
                                    } else {
                                        AppLockAuthResult.Failed
                                    }
                                )
                            }
                        }

                        override fun onAuthenticationFailed() = Unit
                    },
                )
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                    .build()
            )
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        }
    }

    private companion object {
        const val ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
