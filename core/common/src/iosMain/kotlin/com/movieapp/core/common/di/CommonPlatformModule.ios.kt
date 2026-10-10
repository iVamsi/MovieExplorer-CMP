package com.movieapp.core.common.di

import com.movieapp.core.common.network.IosNetworkConnectivity
import com.movieapp.core.common.network.NetworkConnectivity
import com.movieapp.core.common.platform.documentsPath
import com.movieapp.core.common.preferences.PREFERENCES_FILE
import com.movieapp.core.common.preferences.createPreferencesDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val commonPlatformModule: Module = module {
    single { createPreferencesDataStore("${documentsPath()}/$PREFERENCES_FILE") }
    single<NetworkConnectivity> { IosNetworkConnectivity() }
}
