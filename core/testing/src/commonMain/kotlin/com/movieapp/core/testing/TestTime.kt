package com.movieapp.core.testing

import com.movieapp.core.common.dispatchers.AppDispatchers
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.TestDispatcher

class FixedClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}

fun testDispatchers(dispatcher: TestDispatcher) = AppDispatchers(dispatcher, dispatcher, dispatcher)
