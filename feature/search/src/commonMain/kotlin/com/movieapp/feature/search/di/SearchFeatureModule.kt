package com.movieapp.feature.search.di

import com.movieapp.feature.search.screens.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchFeatureModule = module {
    viewModelOf(::SearchViewModel)
}
