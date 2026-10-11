package com.movieapp.data.actors

import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.data.actors.mapper.ActorMapper
import com.movieapp.data.actors.repository.ActorRepositoryImpl
import org.koin.dsl.module

val actorsDataModule = module {
    factory { ActorMapper(clock = get()) }
    single<ActorRepository> {
        ActorRepositoryImpl(
            apiService = get(),
            actorDao = get(),
            transactionRunner = get(),
            remoteKeysDao = get(),
            actorMapper = get(),
            preferencesRepository = get(),
            dispatchers = get(),
        )
    }
}
