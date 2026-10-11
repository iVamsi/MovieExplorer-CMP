package com.movieapp.security

import com.movieapp.core.ui.security.AppLockAuthResult
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLockPolicyTest {
    private val policy = AppLockPolicy(backgroundThresholdMs = 30_000)

    @Test
    fun `onStart - lock disabled - remains unlocked`() {
        assertEquals(AppLockState.Unlocked, policy.onStart(false, 0, 50_000))
    }

    @Test
    fun `onStart - first locked launch or elapsed threshold - locks`() {
        assertEquals(AppLockState.Locked, policy.onStart(true, 0, 50_000))
        assertEquals(AppLockState.Locked, policy.onStart(true, 10_000, 40_001))
    }

    @Test
    fun `onStart - returned before threshold - remains unlocked`() {
        assertEquals(AppLockState.Unlocked, policy.onStart(true, 10_000, 40_000))
    }

    @Test
    fun `afterAuthentication - only success unlocks`() {
        assertEquals(
            AppLockState.Unlocked,
            policy.afterAuthentication(AppLockAuthResult.Succeeded),
        )
        assertEquals(AppLockState.Locked, policy.afterAuthentication(AppLockAuthResult.Failed))
        assertEquals(AppLockState.Locked, policy.afterAuthentication(AppLockAuthResult.Canceled))
        assertEquals(AppLockState.Locked, policy.afterAuthentication(AppLockAuthResult.Unavailable))
    }
}
