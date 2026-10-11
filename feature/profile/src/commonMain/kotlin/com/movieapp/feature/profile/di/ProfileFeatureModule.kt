package com.movieapp.feature.profile.di

import com.movieapp.feature.profile.screens.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileFeatureModule = module {
    viewModelOf(::ProfileViewModel)
}
