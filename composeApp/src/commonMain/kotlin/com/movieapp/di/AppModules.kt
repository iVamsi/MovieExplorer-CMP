package com.movieapp.di

import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.movieapp.app.AppViewModel
import com.movieapp.config.AppConfig
import com.movieapp.core.common.di.commonModule
import com.movieapp.core.database.databaseModule
import com.movieapp.core.domain.di.domainModule
import com.movieapp.core.network.NetworkConfig
import com.movieapp.core.network.networkModule
import com.movieapp.data.actors.actorsDataModule
import com.movieapp.data.movies.moviesDataModule
import com.movieapp.data.series.seriesDataModule
import com.movieapp.data.user.userDataModule
import com.movieapp.feature.actors.di.actorsFeatureModule
import com.movieapp.feature.collections.di.collectionsFeatureModule
import com.movieapp.feature.movies.di.moviesFeatureModule
import com.movieapp.feature.profile.di.profileFeatureModule
import com.movieapp.feature.search.di.searchFeatureModule
import com.movieapp.feature.series.di.seriesFeatureModule
import com.movieapp.feature.settings.di.settingsFeatureModule
import com.movieapp.ui.gallery.ImageGalleryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModules(): List<Module> =
    listOf(
        module {
            single {
                NetworkConfig(
                    apiKey = AppConfig.TMDB_API_KEY,
                    baseUrl = AppConfig.TMDB_BASE_URL,
                    debugLogging = AppConfig.NETWORK_LOGGING,
                )
            }
            single { SingletonImageLoader.get(get<PlatformContext>()) }
            viewModelOf(::AppViewModel)
            viewModel { parameters ->
                ImageGalleryViewModel(
                    kind = parameters.get(),
                    contentId = parameters.get(),
                    movieRepository = get(),
                    seriesRepository = get(),
                    actorRepository = get(),
                    preferencesRepository = get(),
                )
            }
        },
        appPlatformModule,
        commonModule,
        networkModule,
        databaseModule,
        domainModule,
        actorsDataModule,
        moviesDataModule,
        seriesDataModule,
        userDataModule,
        actorsFeatureModule,
        collectionsFeatureModule,
        moviesFeatureModule,
        profileFeatureModule,
        searchFeatureModule,
        seriesFeatureModule,
        settingsFeatureModule,
    )

internal expect val appPlatformModule: Module
