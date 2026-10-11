package com.movieapp.security

import com.movieapp.core.ui.security.AppLockAuthResult

internal class AppLockPolicy(private val backgroundThresholdMs: Long) {

    fun onStart(lockEnabled: Boolean, lastBackgroundedAt: Long, now: Long): AppLockState {
        if (!lockEnabled) return AppLockState.Unlocked
        if (lastBackgroundedAt == 0L) return AppLockState.Locked
        return if (now - lastBackgroundedAt > backgroundThresholdMs) {
            AppLockState.Locked
        } else {
            AppLockState.Unlocked
        }
    }

    fun afterAuthentication(result: AppLockAuthResult): AppLockState =
        if (result == AppLockAuthResult.Succeeded) {
            AppLockState.Unlocked
        } else {
            AppLockState.Locked
        }
}

internal enum class AppLockState {
    Locked,
    Unlocked,
}


