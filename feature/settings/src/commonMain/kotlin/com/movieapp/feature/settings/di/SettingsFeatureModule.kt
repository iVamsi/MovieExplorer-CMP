package com.movieapp.feature.settings.di

import com.movieapp.feature.settings.AccountViewModel
import com.movieapp.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AccountViewModel)
}
