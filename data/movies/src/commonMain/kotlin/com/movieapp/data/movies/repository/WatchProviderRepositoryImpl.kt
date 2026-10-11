package com.movieapp.data.movies.repository

import com.movieapp.core.common.Result
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.common.safeResultFlow
import com.movieapp.core.domain.repository.WatchProviderRepository
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.network.TmdbWatchProviderApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn

class WatchProviderRepositoryImpl(
    private val api: TmdbWatchProviderApi,
    private val preferencesRepository: PreferencesRepository,
    private val dispatchers: AppDispatchers,
) : WatchProviderRepository {

    override fun getWatchProviderCatalog(): Flow<Result<List<WatchProvider>>> =
        safeResultFlow(
                fetch = {
                    val language = preferencesRepository.tmdbLanguage.first()
                    val region = preferencesRepository.watchRegion.first()
                    val movieProviders =
                        api.getMovieWatchProviderCatalog(
                                language = language,
                                watchRegion = region,
                            )
                            .results
                    val tvProviders =
                        api.getTvWatchProviderCatalog(language = language, watchRegion = region)
                            .results
                    (movieProviders + tvProviders)
                        .distinctBy { it.providerId }
                        .sortedWith(
                            compareBy<WatchProvider> { it.displayPriority }
                                .thenBy { it.providerName }
                        )
                }
            )
            .flowOn(dispatchers.io)
}
