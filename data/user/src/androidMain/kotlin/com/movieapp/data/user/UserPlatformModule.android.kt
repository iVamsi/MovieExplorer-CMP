package com.movieapp.data.user

import com.movieapp.data.user.auth.AndroidSecureSessionStore
import com.movieapp.data.user.auth.SecureSessionStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val userPlatformModule: Module = module {
    single<SecureSessionStore> { AndroidSecureSessionStore(androidContext()) }
}
