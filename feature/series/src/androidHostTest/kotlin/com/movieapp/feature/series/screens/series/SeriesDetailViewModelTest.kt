package com.movieapp.feature.series.screens.series

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ClearUserContentRatingUseCase
import com.movieapp.core.domain.usecase.PrefetchContentForOfflineUseCase
import com.movieapp.core.domain.usecase.SetUserContentRatingUseCase
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.OfflineContent
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.model.WatchProviderCountry
import com.movieapp.core.model.WatchProviders
import com.movieapp.core.testing.FakeData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val seriesRepository = mockk<SeriesRepository>()
    private val userRepository = mockk<UserRepository>()
    private val setUserContentRatingUseCase = mockk<SetUserContentRatingUseCase>(relaxed = true)
    private val clearUserContentRatingUseCase = mockk<ClearUserContentRatingUseCase>(relaxed = true)
    private val prefetchContentForOfflineUseCase = mockk<PrefetchContentForOfflineUseCase>()
    private val offlineContentRepository = mockk<OfflineContentRepository>()
    private val preferencesRepository =
        mockk<PreferencesRepository>().also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }

    private val testSeries = FakeData.fakeTvSeries.first()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        seriesId: Int = testSeries.id,
        userRating: Float? = null,
        watchProviders: WatchProviders = WatchProviders(emptyMap()),
        savedOffline: Boolean = false,
    ): SeriesDetailViewModel {
        coEvery { seriesRepository.getTvSeriesCredits(any(), any()) } returns
            Result.Success(Credits())
        coEvery { seriesRepository.getTvSeriesKeywords(any()) } returns Result.Success(emptyList())
        coEvery { seriesRepository.getTvSeriesExternalIds(any()) } returns
            Result.Success(ExternalIds())
        every { seriesRepository.getSimilarSeries(any(), any()) } returns flowOf(PagingData.empty())
        every { seriesRepository.getRecommendedSeries(any(), any()) } returns
            flowOf(PagingData.empty())
        every { seriesRepository.getTvSeriesReviewsPaged(any(), any()) } returns
            flowOf(PagingData.empty())
        every { seriesRepository.getTvWatchProviders(any()) } returns
            flow { emit(Result.Success(watchProviders)) }
        coEvery { userRepository.getUserRating(any(), any()) } returns Result.Success(userRating)
        every { offlineContentRepository.observeOfflineContent(any(), any()) } returns
            flowOf(
                if (savedOffline) {
                    OfflineContent(contentId = seriesId, contentType = ContentType.TV_SERIES)
                } else {
                    null
                }
            )

        return SeriesDetailViewModel(
            seriesId = seriesId,
            seriesRepository = seriesRepository,
            userRepository = userRepository,
            setUserContentRatingUseCase = setUserContentRatingUseCase,
            clearUserContentRatingUseCase = clearUserContentRatingUseCase,
            prefetchContentForOfflineUseCase = prefetchContentForOfflineUseCase,
            offlineContentRepository = offlineContentRepository,
            preferencesRepository = preferencesRepository,
        )
    }

    @Test
    fun `loadWatchProviders - selected region absent - does not show foreign providers`() = runTest {
        val providers = WatchProviders(
            results = mapOf(
                "GB" to WatchProviderCountry(
                    link = "https://tmdb/watch/gb",
                    flatrate = listOf(WatchProvider(
                        displayPriority = 0,
                        logoPath = "/n.png",
                        providerId = 8,
                        providerName = "Netflix",
                    )),
                )
            )
        )
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel(watchProviders = providers)

        assertThat(viewModel.uiState.value.watchProvidersForRegion).isEmpty()
        assertThat(viewModel.uiState.value.watchProviderLink).isNull()
        assertThat(viewModel.uiState.value.watchProvidersLoading).isFalse()
    }

    @Test
    fun `loadSeriesDetails - when flow returns success - state shows series`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow {
                emit(Result.Loading)
                emit(Result.Success(testSeries))
            }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.series).isEqualTo(testSeries)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `loadSeriesDetails - when flow returns error - state shows error`() = runTest {
        val errorMessage = "Failed to load series"
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Error(Exception(errorMessage))) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { seriesRepository.getSeriesById(testSeries.id) } returns
            Result.Error(Exception("no cache"))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.series).isNull()
        assertThat(viewModel.uiState.value.error).isEqualTo(errorMessage)
    }

    @Test
    fun `checkUserStatus - when series is in watchlist but not favorite - state reflects correctly`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                true

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.isFavorite).isFalse()
            assertThat(viewModel.uiState.value.isWatchlisted).isTrue()
        }

    @Test
    fun `onEvent ToggleFavorite - when not favorite - calls addToFavorites and flips flag`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.addToFavorites(testSeries.id, ContentType.TV_SERIES) } returns
                Result.Success(Unit)

            val viewModel = createViewModel()
            viewModel.sideEffect.test {
                viewModel.onEvent(SeriesDetailUiEvent.ToggleFavorite)
                assertThat(awaitItem())
                    .isEqualTo(SeriesDetailSideEffect.FavoriteUpdated(isFavorite = true))
            }

            assertThat(viewModel.uiState.value.isFavorite).isTrue()
            coVerify { userRepository.addToFavorites(testSeries.id, ContentType.TV_SERIES) }
        }

    @Test
    fun `onEvent ToggleFavorite - when is favorite - calls removeFromFavorites and flips flag`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns true
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery {
                userRepository.removeFromFavorites(testSeries.id, ContentType.TV_SERIES)
            } returns Result.Success(Unit)

            val viewModel = createViewModel()
            viewModel.sideEffect.test {
                viewModel.onEvent(SeriesDetailUiEvent.ToggleFavorite)
                assertThat(awaitItem())
                    .isEqualTo(SeriesDetailSideEffect.FavoriteUpdated(isFavorite = false))
            }

            assertThat(viewModel.uiState.value.isFavorite).isFalse()
            coVerify { userRepository.removeFromFavorites(testSeries.id, ContentType.TV_SERIES) }
        }

    @Test
    fun `onEvent ToggleFavorite - when save fails - emits typed failure`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.addToFavorites(testSeries.id, ContentType.TV_SERIES) } returns
            Result.Error(Exception("disk full"))
        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(SeriesDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem()).isEqualTo(SeriesDetailSideEffect.FavoriteUpdateFailed)
        }
    }

    @Test
    fun `onEvent ToggleWatchlist - when not watchlisted - calls addToWatchlist and flips flag`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.addToWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                Result.Success(Unit)

            val viewModel = createViewModel()
            viewModel.sideEffect.test {
                viewModel.onEvent(SeriesDetailUiEvent.ToggleWatchlist)
                assertThat(awaitItem())
                    .isEqualTo(SeriesDetailSideEffect.WatchlistUpdated(isWatchlisted = true))
            }

            assertThat(viewModel.uiState.value.isWatchlisted).isTrue()
            coVerify { userRepository.addToWatchlist(testSeries.id, ContentType.TV_SERIES) }
        }

    @Test
    fun `onEvent ToggleWatchlist - when save fails - keeps state and reports failure`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.addToWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
            Result.Error(Exception("disk full"))

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(SeriesDetailUiEvent.ToggleWatchlist)
            assertThat(awaitItem()).isEqualTo(SeriesDetailSideEffect.WatchlistUpdateFailed)
        }
        assertThat(viewModel.uiState.value.isWatchlisted).isFalse()
        assertThat(viewModel.uiState.value.isWatchlistMutationPending).isFalse()
    }

    @Test
    fun `onEvent Retry - reloads series details`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel()
        viewModel.onEvent(SeriesDetailUiEvent.Retry)

        coVerify(exactly = 2) { seriesRepository.getSeriesByIdFlow(testSeries.id) }
    }

    @Test
    fun `onEvent SaveForOffline - when series is not saved - prefetches series and marks saved`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false
            val offlineContent =
                OfflineContent(
                    contentId = testSeries.id,
                    contentType = ContentType.TV_SERIES,
                    prefetchedAt = 123L,
                    posterCached = true,
                    backdropCached = true,
                )
            every {
                prefetchContentForOfflineUseCase(testSeries.id, ContentType.TV_SERIES)
            } returns
                flow {
                    emit(Result.Loading)
                    emit(Result.Success(offlineContent))
                }

            val viewModel = createViewModel()
            viewModel.onEvent(SeriesDetailUiEvent.SaveForOffline)

            assertThat(viewModel.uiState.value.isOfflineSaving).isFalse()
            assertThat(viewModel.uiState.value.isSavedOffline).isTrue()
        }

    @Test
    fun `loadSeriesDetails - when flow fails but remote lookup succeeds - shows cached series`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Error(Exception("offline"))) }
            coEvery { seriesRepository.getSeriesById(testSeries.id) } returns
                Result.Success(testSeries)
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.series).isEqualTo(testSeries)
            assertThat(viewModel.uiState.value.fromCacheOnly).isTrue()
            assertThat(viewModel.uiState.value.error).isNull()
        }

    @Test
    fun `checkUserStatus - maps stored rating out of ten to stars out of five`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel(userRating = 8f)

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(4)
    }

    @Test
    fun `onEvent SetUserRating - when use case succeeds - updates stars`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel()
        coEvery {
            setUserContentRatingUseCase(testSeries.id, ContentType.TV_SERIES, 8f, null)
        } returns Result.Success(Unit)
        viewModel.onEvent(SeriesDetailUiEvent.SetUserRating(4))

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(4)
        coVerify { setUserContentRatingUseCase(testSeries.id, ContentType.TV_SERIES, 8f, null) }
    }

    @Test
    fun `onEvent SetUserRating - when use case fails - emits ShowMessage and keeps stars`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false

            val viewModel = createViewModel()
            coEvery {
                setUserContentRatingUseCase(testSeries.id, ContentType.TV_SERIES, 6f, null)
            } returns Result.Error(Exception("db full"))

            viewModel.sideEffect.test {
                viewModel.onEvent(SeriesDetailUiEvent.SetUserRating(3))
                assertThat(awaitItem())
                    .isEqualTo(SeriesDetailSideEffect.ShowMessage("Could not save rating"))
            }
            assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(0)
        }

    @Test
    fun `onEvent ClearUserRating - resets stars and calls use case`() = runTest {
        coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
            flow { emit(Result.Success(testSeries)) }
        coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns false
        coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns false

        val viewModel = createViewModel(userRating = 8f)
        coEvery { clearUserContentRatingUseCase(testSeries.id, ContentType.TV_SERIES) } returns
            Result.Success(Unit)
        viewModel.onEvent(SeriesDetailUiEvent.ClearUserRating)

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(0)
        coVerify { clearUserContentRatingUseCase(testSeries.id, ContentType.TV_SERIES) }
    }

    @Test
    fun `loadWatchProviders - merges flatrate rent and buy for region without duplicates`() =
        runTest {
            val netflix =
                WatchProvider(
                    displayPriority = 0,
                    logoPath = "/n.png",
                    providerId = 8,
                    providerName = "Netflix",
                )
            val apple =
                WatchProvider(
                    displayPriority = 1,
                    logoPath = "/a.png",
                    providerId = 2,
                    providerName = "Apple TV",
                )
            val providers =
                WatchProviders(
                    results =
                        mapOf(
                            "US" to
                                WatchProviderCountry(
                                    link = "https://tmdb/watch/us",
                                    flatrate = listOf(netflix),
                                    rent = listOf(apple),
                                    buy = listOf(apple),
                                )
                        )
                )
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false

            val viewModel = createViewModel(watchProviders = providers)

            assertThat(viewModel.uiState.value.watchProvidersForRegion)
                .containsExactly(netflix, apple)
            assertThat(viewModel.uiState.value.watchProviderLink).isEqualTo("https://tmdb/watch/us")
            assertThat(viewModel.uiState.value.watchProvidersLoading).isFalse()
        }

    @Test
    fun `onEvent SaveForOffline - when prefetch fails - emits ShowMessage and clears flag`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false

            val viewModel = createViewModel()
            every { prefetchContentForOfflineUseCase(testSeries.id, ContentType.TV_SERIES) } returns
                flowOf(Result.Loading, Result.Error(Exception("no space")))

            viewModel.sideEffect.test {
                viewModel.onEvent(SeriesDetailUiEvent.SaveForOffline)
                assertThat(awaitItem())
                    .isEqualTo(SeriesDetailSideEffect.ShowMessage("Could not save for offline"))
            }
            assertThat(viewModel.uiState.value.isOfflineSaving).isFalse()
            assertThat(viewModel.uiState.value.isSavedOffline).isFalse()
        }

    @Test
    fun `onEvent SaveForOffline - when already saved offline - does not prefetch again`() =
        runTest {
            coEvery { seriesRepository.getSeriesByIdFlow(testSeries.id) } returns
                flow { emit(Result.Success(testSeries)) }
            coEvery { userRepository.isFavorite(testSeries.id, ContentType.TV_SERIES) } returns
                false
            coEvery { userRepository.isInWatchlist(testSeries.id, ContentType.TV_SERIES) } returns
                false

            val viewModel = createViewModel(savedOffline = true)
            viewModel.onEvent(SeriesDetailUiEvent.SaveForOffline)

            verify(exactly = 0) {
                prefetchContentForOfflineUseCase(testSeries.id, ContentType.TV_SERIES)
            }
        }
}
