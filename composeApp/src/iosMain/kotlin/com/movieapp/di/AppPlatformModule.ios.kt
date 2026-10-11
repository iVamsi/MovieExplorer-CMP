package com.movieapp.di

import coil3.PlatformContext
import com.movieapp.core.common.locale.AppLocaleApplier
import com.movieapp.core.ui.security.BiometricAuthenticator
import com.movieapp.core.ui.security.IosBiometricAuthenticator
import com.movieapp.locale.IosAppLocaleApplier
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val appPlatformModule: Module = module {
    single<PlatformContext> { PlatformContext.INSTANCE }
    single<AppLocaleApplier> { IosAppLocaleApplier() }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
}
