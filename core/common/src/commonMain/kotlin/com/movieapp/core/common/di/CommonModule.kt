package com.movieapp.core.common.di

import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.common.preferences.PreferencesRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single { AppDispatchers(io = Dispatchers.IO, default = Dispatchers.Default, main = Dispatchers.Main) }
    single<Clock> { Clock.System }
    single(createdAtStart = false) { CoroutineScope(SupervisorJob() + get<AppDispatchers>().io) } // application scope
    single { PreferencesRepository(get()) }
    includes(commonPlatformModule)
}

internal expect val commonPlatformModule: Module
