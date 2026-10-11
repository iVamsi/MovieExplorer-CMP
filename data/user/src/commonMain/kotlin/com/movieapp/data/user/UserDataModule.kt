package com.movieapp.data.user

import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.data.user.auth.AuthSessionInvalidator
import com.movieapp.data.user.auth.TmdbSessionStorage
import com.movieapp.data.user.repository.AuthRepositoryImpl
import com.movieapp.data.user.repository.OfflineContentRepositoryImpl
import com.movieapp.data.user.repository.StatsRepositoryImpl
import com.movieapp.data.user.repository.UserRepositoryImpl
import com.movieapp.data.user.sync.TmdbUserLibrarySync
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

val userDataModule = module {
    includes(userPlatformModule)
    single { TmdbSessionStorage(store = get(), dispatchers = get(), appScope = get()) }
    single {
        TmdbUserLibrarySync(
            api = get(),
            sessionStorage = get(),
            userDao = get(),
            movieDao = get(),
            tvSeriesDao = get(),
            actorDao = get(),
            clock = get(),
            sessionInvalidator = lazy { get<AuthSessionInvalidator>() },
        )
    }
    single {
        AuthRepositoryImpl(
            api = get(),
            sessionStorage = get(),
            tmdbUserLibrarySync = lazy { get<TmdbUserLibrarySync>() },
        )
    } binds arrayOf(AuthRepository::class, AuthSessionInvalidator::class)
    single<UserRepository> {
        UserRepositoryImpl(userDao = get(), clock = get(), tmdbUserLibrarySync = get())
    }
    single<StatsRepository> {
        StatsRepositoryImpl(userDao = get(), movieDao = get(), tvSeriesDao = get(), dispatchers = get())
    }
    single<OfflineContentRepository> {
        OfflineContentRepositoryImpl(
            offlineContentDao = get(),
            movieDao = get(),
            tvSeriesDao = get(),
            movieRepository = get(),
            seriesRepository = get(),
            imageLoader = get(),
            platformContext = get(),
            clock = get(),
            dispatchers = get(),
        )
    }
}

internal expect val userPlatformModule: Module
