package com.movieapp.di

import coil3.PlatformContext
import com.movieapp.core.common.locale.AppLocaleApplier
import com.movieapp.core.ui.security.AndroidBiometricAuthenticator
import com.movieapp.core.ui.security.BiometricAuthenticator
import com.movieapp.locale.AndroidAppLocaleApplier
import com.movieapp.platform.AndroidActivityHolder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val appPlatformModule: Module = module {
    single<PlatformContext> { androidContext() }
    single<AppLocaleApplier> { AndroidAppLocaleApplier() }
    single<BiometricAuthenticator> {
        AndroidBiometricAuthenticator(androidContext()) { AndroidActivityHolder.current() }
    }
}
