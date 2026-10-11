package com.movieapp.core.ui.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class MovieAppMotionTest {

    @Test
    fun `scaledMotionDurationMillis - animations disabled - returns zero`() {
        assertEquals(0, scaledMotionDurationMillis(durationMillis = 180, animatorDurationScale = 0f))
    }

    @Test
    fun `scaledMotionDurationMillis - nonzero reduced scale - preserves base duration`() {
        assertEquals(180, scaledMotionDurationMillis(durationMillis = 180, animatorDurationScale = 0.5f))
    }

    @Test
    fun `scaledMotionDurationMillis - default speed - preserves duration`() {
        assertEquals(180, scaledMotionDurationMillis(durationMillis = 180, animatorDurationScale = 1f))
    }

    @Test
    fun `scaledMotionDurationMillis - extended scale - preserves base duration`() {
        assertEquals(180, scaledMotionDurationMillis(durationMillis = 180, animatorDurationScale = 10f))
    }
}
