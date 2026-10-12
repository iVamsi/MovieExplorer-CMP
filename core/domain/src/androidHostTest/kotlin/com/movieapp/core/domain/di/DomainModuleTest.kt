package com.movieapp.core.domain.di

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.domain.repository.UserRepository
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
import com.movieapp.core.testing.FakeMovieRepository
import com.movieapp.core.testing.FakeOfflineContentRepository
import com.movieapp.core.testing.FakeSeriesRepository
import com.movieapp.core.testing.FakeStatsRepository
import com.movieapp.core.testing.FakeUserRepository
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class DomainModuleTest {

    @Test
    fun `domain module - wires every use case`() {
        val koin = startKoin {
            modules(
                module {
                    single<MovieRepository> { FakeMovieRepository() }
                    single<SeriesRepository> { FakeSeriesRepository() }
                    single<UserRepository> { FakeUserRepository() }
                    single<StatsRepository> { FakeStatsRepository() }
                    single<OfflineContentRepository> { FakeOfflineContentRepository() }
                },
                domainModule,
            )
        }.koin
        try {
            val resolved: List<Any> = listOf(
                koin.get<ClearUserContentRatingUseCase>(),
                koin.get<GetFavoriteMoviesUseCase>(),
                koin.get<GetFavoriteTvSeriesUseCase>(),
                koin.get<GetUserRatingForContentUseCase>(),
                koin.get<GetUserWatchStatsUseCase>(),
                koin.get<PrefetchContentForOfflineUseCase>(),
                koin.get<SetUserContentRatingUseCase>(),
                koin.get<ToggleFavoriteActorUseCase>(),
                koin.get<ToggleFavoriteMovieUseCase>(),
                koin.get<ToggleWatchlistMovieUseCase>(),
            )
            assertThat(resolved).hasSize(10)
        } finally {
            stopKoin()
        }
    }
}
