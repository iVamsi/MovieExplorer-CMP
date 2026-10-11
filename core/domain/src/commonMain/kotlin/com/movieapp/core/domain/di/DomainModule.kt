package com.movieapp.core.domain.di

import com.movieapp.core.domain.usecase.ClearUserContentRatingUseCase
import com.movieapp.core.domain.usecase.GetFavoriteMoviesUseCase
import com.movieapp.core.domain.usecase.GetFavoriteTvSeriesUseCase
import com.movieapp.core.domain.usecase.GetUserRatingForContentUseCase
import com.movieapp.core.domain.usecase.GetUserWatchStatsUseCase
import com.movieapp.core.domain.usecase.PrefetchContentForOfflineUseCase
import com.movieapp.core.domain.usecase.SetUserContentRatingUseCase
import com.movieapp.core.domain.usecase.ToggleFavoriteActorUseCase
import com.movieapp.core.domain.usecase.ToggleFavoriteMovieUseCase
import com.movieapp.core.domain.usecase.ToggleWatchlistMovieUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ClearUserContentRatingUseCase)
    factoryOf(::GetFavoriteMoviesUseCase)
    factoryOf(::GetFavoriteTvSeriesUseCase)
    factoryOf(::GetUserRatingForContentUseCase)
    factoryOf(::GetUserWatchStatsUseCase)
    factoryOf(::PrefetchContentForOfflineUseCase)
    factoryOf(::SetUserContentRatingUseCase)
    factoryOf(::ToggleFavoriteActorUseCase)
    factoryOf(::ToggleFavoriteMovieUseCase)
    factoryOf(::ToggleWatchlistMovieUseCase)
}
