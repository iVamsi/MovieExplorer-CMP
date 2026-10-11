package com.movieapp.feature.actors.di

import com.movieapp.feature.actors.screens.actor.ActorDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val actorsFeatureModule = module {
    viewModel { params ->
        ActorDetailViewModel(
            actorId = params.get(),
            actorRepository = get(),
            userRepository = get(),
            toggleFavoriteActorUseCase = get(),
        )
    }
}
