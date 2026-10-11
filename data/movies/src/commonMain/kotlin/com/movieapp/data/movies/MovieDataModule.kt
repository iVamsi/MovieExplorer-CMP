package com.movieapp.data.movies

import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.WatchProviderRepository
import com.movieapp.data.movies.mapper.MovieMapper
import com.movieapp.data.movies.repository.MovieRepositoryImpl
import com.movieapp.data.movies.repository.WatchProviderRepositoryImpl
import org.koin.dsl.module

val moviesDataModule = module {
    factory { MovieMapper(clock = get()) }
    single<MovieRepository> {
        MovieRepositoryImpl(
            apiService = get(),
            movieDao = get(),
            transactionRunner = get(),
            remoteKeysDao = get(),
            mapper = get(),
            clock = get(),
            preferencesRepository = get(),
            dispatchers = get(),
        )
    }
    single<WatchProviderRepository> {
        WatchProviderRepositoryImpl(api = get(), preferencesRepository = get(), dispatchers = get())
    }
}
