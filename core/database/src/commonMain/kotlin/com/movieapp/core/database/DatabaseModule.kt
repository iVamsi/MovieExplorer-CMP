package com.movieapp.core.database

import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule = module {
    single { get<RoomDatabase.Builder<MovieAppDatabase>>().buildMovieAppDatabase(get()) }
    single<TransactionRunner> { RoomTransactionRunner(get()) }
    single { get<MovieAppDatabase>().movieDao() }
    single { get<MovieAppDatabase>().tvSeriesDao() }
    single { get<MovieAppDatabase>().actorDao() }
    single { get<MovieAppDatabase>().userDao() }
    single { get<MovieAppDatabase>().remoteKeysDao() }
    single { get<MovieAppDatabase>().offlineContentDao() }
    includes(databasePlatformModule)
}

internal expect val databasePlatformModule: Module
