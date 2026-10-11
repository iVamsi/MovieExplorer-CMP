package com.movieapp.feature.movies.di

import com.movieapp.feature.movies.screens.browseall.BrowseAllViewModel
import com.movieapp.feature.movies.screens.favorites.FavoritesViewModel
import com.movieapp.feature.movies.screens.home.HomeViewModel
import com.movieapp.feature.movies.screens.movie.MovieDetailViewModel
import com.movieapp.feature.movies.screens.upcoming.UpcomingCalendarViewModel
import com.movieapp.feature.movies.screens.watchlist.WatchlistViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moviesFeatureModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::UpcomingCalendarViewModel)
    viewModel { params ->
        BrowseAllViewModel(
            category = params.get(),
            movieRepository = get(),
            seriesRepository = get(),
        )
    }
    viewModel { params ->
        MovieDetailViewModel(
            movieId = params.get(),
            movieRepository = get(),
            userRepository = get(),
            toggleFavoriteMovieUseCase = get(),
            toggleWatchlistMovieUseCase = get(),
            setUserContentRatingUseCase = get(),
            clearUserContentRatingUseCase = get(),
            prefetchContentForOfflineUseCase = get(),
            offlineContentRepository = get(),
            preferencesRepository = get(),
        )
    }
}
