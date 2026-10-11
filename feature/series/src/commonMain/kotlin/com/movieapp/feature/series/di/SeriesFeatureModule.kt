package com.movieapp.feature.series.di

import com.movieapp.feature.series.screens.series.SeriesDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val seriesFeatureModule = module {
    viewModel { params ->
        SeriesDetailViewModel(
            seriesId = params.get(),
            seriesRepository = get(),
            userRepository = get(),
            setUserContentRatingUseCase = get(),
            clearUserContentRatingUseCase = get(),
            prefetchContentForOfflineUseCase = get(),
            offlineContentRepository = get(),
            preferencesRepository = get(),
        )
    }
}
