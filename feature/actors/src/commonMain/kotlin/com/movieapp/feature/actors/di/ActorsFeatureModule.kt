package com.movieapp.feature.actors.di

import com.movieapp.feature.actors.screens.actor.ActorDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val actorsFeatureModule = module {
    viewModelOf(::ActorDetailViewModel)
}
