package com.movieapp.feature.collections.di

import com.movieapp.feature.collections.screens.collections.CollectionDetailViewModel
import com.movieapp.feature.collections.screens.collections.CollectionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val collectionsFeatureModule = module {
    viewModelOf(::CollectionsViewModel)
    viewModel { params ->
        CollectionDetailViewModel(
            collectionId = params.get(),
            userRepository = get(),
            movieRepository = get(),
            seriesRepository = get(),
        )
    }
}
