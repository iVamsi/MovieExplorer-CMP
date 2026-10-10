package com.movieapp.core.common.di

import com.movieapp.core.common.network.NetworkConnectivity
import com.movieapp.core.common.network.NetworkConnectivityObserver
import com.movieapp.core.common.preferences.PREFERENCES_FILE
import com.movieapp.core.common.preferences.createPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val commonPlatformModule: Module = module {
    single { createPreferencesDataStore(androidContext().filesDir.resolve(PREFERENCES_FILE).absolutePath) }
    single<NetworkConnectivity> { NetworkConnectivityObserver(androidContext()) }
}
