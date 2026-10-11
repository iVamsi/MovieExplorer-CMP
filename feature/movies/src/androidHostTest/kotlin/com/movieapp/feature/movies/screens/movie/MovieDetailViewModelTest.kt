package com.movieapp.feature.movies.screens.movie

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ClearUserContentRatingUseCase
import com.movieapp.core.domain.usecase.PrefetchContentForOfflineUseCase
import com.movieapp.core.domain.usecase.SetUserContentRatingUseCase
import com.movieapp.core.domain.usecase.ToggleFavoriteMovieUseCase
import com.movieapp.core.domain.usecase.ToggleWatchlistMovieUseCase
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
abstract class MovieDetailViewModelTestFixture {

    protected val testDispatcher = UnconfinedTestDispatcher()

    protected val movieRepository = mockk<MovieRepository>()
    protected val userRepository = mockk<UserRepository>()
    protected val toggleFavoriteMovieUseCase = mockk<ToggleFavoriteMovieUseCase>()
    protected val toggleWatchlistMovieUseCase = mockk<ToggleWatchlistMovieUseCase>()
    protected val setUserContentRatingUseCase = mockk<SetUserContentRatingUseCase>(relaxed = true)
    protected val clearUserContentRatingUseCase = mockk<ClearUserContentRatingUseCase>(relaxed = true)
    protected val prefetchContentForOfflineUseCase =
        mockk<PrefetchContentForOfflineUseCase>(relaxed = true)
    protected val offlineContentRepository = mockk<OfflineContentRepository>()
    protected val preferencesRepository =
        mockk<PreferencesRepository>().also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }

    protected val testMovie = FakeData.fakeMovies.first()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun createViewModel(
        movieId: Int = testMovie.id,
        userRating: Float? = null,
        watchProviders: WatchProviders = WatchProviders(emptyMap()),
        savedOffline: Boolean = false,
    ): MovieDetailViewModel {
        coEvery { movieRepository.getMovieCredits(any(), any()) } returns Result.Success(Credits())
        coEvery { movieRepository.getMovieKeywords(any()) } returns Result.Success(emptyList())
        coEvery { movieRepository.getMovieExternalIds(any()) } returns Result.Success(ExternalIds())
        every { movieRepository.getSimilarMoviesPaged(any(), any()) } returns
            flowOf(PagingData.empty())
        every { movieRepository.getRecommendedMoviesPaged(any(), any()) } returns
            flowOf(PagingData.empty())
        every { movieRepository.getMovieReviewsPaged(any(), any()) } returns
            flowOf(PagingData.empty())
        every { movieRepository.getMovieWatchProviders(any()) } returns
            flow { emit(Result.Success(watchProviders)) }
        coEvery { userRepository.getUserRating(any(), any()) } returns Result.Success(userRating)
        every { offlineContentRepository.observeOfflineContent(any(), any()) } returns
            flowOf(
                if (savedOffline) {
                    OfflineContent(contentId = movieId, contentType = ContentType.MOVIE)
                } else {
                    null
                }
            )
        every { prefetchContentForOfflineUseCase(any(), any()) } returns
            flowOf(
                Result.Success(OfflineContent(contentId = movieId, contentType = ContentType.MOVIE))
            )

        return MovieDetailViewModel(
            movieRepository = movieRepository,
            userRepository = userRepository,
            toggleFavoriteMovieUseCase = toggleFavoriteMovieUseCase,
            toggleWatchlistMovieUseCase = toggleWatchlistMovieUseCase,
            setUserContentRatingUseCase = setUserContentRatingUseCase,
            clearUserContentRatingUseCase = clearUserContentRatingUseCase,
            prefetchContentForOfflineUseCase = prefetchContentForOfflineUseCase,
            offlineContentRepository = offlineContentRepository,
            preferencesRepository = preferencesRepository,
            movieId = movieId,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest : MovieDetailViewModelTestFixture() {
    @Test
    fun loadMovieDetailswhenapireturnssuccessstateshowsmovie() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow {
                emit(Result.Loading)
                emit(Result.Success(testMovie))
            }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.movie).isEqualTo(testMovie)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun loadMovieDetailswhenapireturnserrorstateshowserror() = runTest {
        val errorMessage = "Network error"
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow {
                emit(Result.Loading)
                emit(Result.Error(Exception(errorMessage)))
            }
        coEvery { movieRepository.getMovieById(testMovie.id) } returns
            flow { emit(Result.Error(Exception("no cache"))) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.movie).isNull()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isEqualTo(errorMessage)
    }

    @Test
    fun checkUserStatuswhenmovieisfavoriteandwatchlistedstatereflectsboth() =
        runTest {
            coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
                flow { emit(Result.Success(testMovie)) }
            coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns true
            coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns true

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.isFavorite).isTrue()
            assertThat(viewModel.uiState.value.isWatchlisted).isTrue()
        }

    @Test
    fun onEventToggleFavoritewhennotfavoritetogglestofavorite() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { toggleFavoriteMovieUseCase(testMovie.id, false) } returns Result.Success(Unit)

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem())
                .isEqualTo(MovieDetailSideEffect.FavoriteUpdated(isFavorite = true))
        }

        assertThat(viewModel.uiState.value.isFavorite).isTrue()
        coVerify { toggleFavoriteMovieUseCase(testMovie.id, false) }
    }

    @Test
    fun onEventToggleFavoritewhenisfavoritetogglestonotfavorite() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns true
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { toggleFavoriteMovieUseCase(testMovie.id, true) } returns Result.Success(Unit)

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem())
                .isEqualTo(MovieDetailSideEffect.FavoriteUpdated(isFavorite = false))
        }

        assertThat(viewModel.uiState.value.isFavorite).isFalse()
        coVerify { toggleFavoriteMovieUseCase(testMovie.id, true) }
    }

    @Test
    fun onEventToggleFavoritewhensavefailsemitstypedfailure() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { toggleFavoriteMovieUseCase(testMovie.id, false) } returns
            Result.Error(Exception("disk full"))
        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem()).isEqualTo(MovieDetailSideEffect.FavoriteUpdateFailed)
        }
    }

    @Test
    fun onEventToggleWatchlistwhennotwatchlistedtogglestowatchlisted() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { toggleWatchlistMovieUseCase(testMovie.id, false) } returns Result.Success(Unit)

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.ToggleWatchlist)
            assertThat(awaitItem())
                .isEqualTo(MovieDetailSideEffect.WatchlistUpdated(isWatchlisted = true))
        }

        assertThat(viewModel.uiState.value.isWatchlisted).isTrue()
        coVerify { toggleWatchlistMovieUseCase(testMovie.id, false) }
    }

    @Test
    fun onEventToggleWatchlistwhensavefailskeepsstateandreportsfailure() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { toggleWatchlistMovieUseCase(testMovie.id, false) } returns
            Result.Error(Exception("disk full"))

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.ToggleWatchlist)
            assertThat(awaitItem()).isEqualTo(MovieDetailSideEffect.WatchlistUpdateFailed)
        }
        assertThat(viewModel.uiState.value.isWatchlisted).isFalse()
        assertThat(viewModel.uiState.value.isWatchlistMutationPending).isFalse()
    }

    @Test
    fun onEventRetryreloadsmoviedetails() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel()
        viewModel.onEvent(MovieDetailUiEvent.Retry)

        coVerify(exactly = 2) { movieRepository.getMovieDetails(testMovie.id) }
    }

    @Test
    fun onEventSaveForOfflinewhenprefetchsucceedsmarksmoviesavedoffline() = runTest {
        val offlineContent =
            OfflineContent(
                contentId = testMovie.id,
                contentType = ContentType.MOVIE,
                title = testMovie.title,
                posterPath = testMovie.posterPath,
                prefetchedAt = 100L,
                posterCached = true,
                backdropCached = true,
            )
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        every { prefetchContentForOfflineUseCase(testMovie.id, ContentType.MOVIE) } returns
            flowOf(Result.Loading, Result.Success(offlineContent))

        val viewModel = createViewModel()
        viewModel.onEvent(MovieDetailUiEvent.SaveForOffline)

        assertThat(viewModel.uiState.value.isOfflineSaving).isFalse()
        assertThat(viewModel.uiState.value.isSavedOffline).isTrue()
    }

    @Test
    fun loadMovieDetailswhenapifailsbutcachehasmovieshowscachedmovie() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Error(Exception("offline"))) }
        every { movieRepository.getMovieById(testMovie.id) } returns
            flow {
                emit(Result.Loading)
                emit(Result.Success(testMovie))
            }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.movie).isEqualTo(testMovie)
        assertThat(viewModel.uiState.value.fromCacheOnly).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun checkUserStatusmapsstoredratingoutoftentostarsoutoffive() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel(userRating = 8f)

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(4)
    }

    @Test
    fun onEventSetUserRatingwhenusecasesucceedsupdatesstars() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel()
        coEvery { setUserContentRatingUseCase(testMovie.id, ContentType.MOVIE, 8f, null) } returns
            Result.Success(Unit)
        viewModel.onEvent(MovieDetailUiEvent.SetUserRating(4))

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(4)
        coVerify { setUserContentRatingUseCase(testMovie.id, ContentType.MOVIE, 8f, null) }
    }

    @Test
    fun onEventSetUserRatingwhenusecasefailsemitsShowMessageandkeepsstars() =
        runTest {
            coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
                flow { emit(Result.Success(testMovie)) }
            coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
            coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

            val viewModel = createViewModel()
            coEvery {
                setUserContentRatingUseCase(testMovie.id, ContentType.MOVIE, 6f, null)
            } returns Result.Error(Exception("db full"))

            viewModel.sideEffect.test {
                viewModel.onEvent(MovieDetailUiEvent.SetUserRating(3))
                assertThat(awaitItem())
                    .isEqualTo(MovieDetailSideEffect.ShowMessage("Could not save rating"))
            }
            assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(0)
        }

    @Test
    fun onEventClearUserRatingresetsstarsandcallsusecase() = runTest {
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel(userRating = 8f)
        coEvery { clearUserContentRatingUseCase(testMovie.id, ContentType.MOVIE) } returns
            Result.Success(Unit)
        viewModel.onEvent(MovieDetailUiEvent.ClearUserRating)

        assertThat(viewModel.uiState.value.userRatingOutOfFive).isEqualTo(0)
        coVerify { clearUserContentRatingUseCase(testMovie.id, ContentType.MOVIE) }
    }

    @Test
    fun loadWatchProvidersmergesflatraterentandbuyforregionwithoutduplicates() =
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
            coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
                flow { emit(Result.Success(testMovie)) }
            coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
            coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

            val viewModel = createViewModel(watchProviders = providers)

            assertThat(viewModel.uiState.value.watchProvidersForRegion)
                .containsExactly(netflix, apple)
            assertThat(viewModel.uiState.value.watchProviderLink).isEqualTo("https://tmdb/watch/us")
            assertThat(viewModel.uiState.value.watchProvidersLoading).isFalse()
        }

    @Test
    fun loadWatchProvidersselectedregionabsentleavesprovidersempty() = runTest {
        val foreignProvider =
            WatchProvider(
                displayPriority = 0,
                logoPath = "/n.png",
                providerId = 8,
                providerName = "Netflix",
            )
        val providers =
            WatchProviders(
                mapOf(
                    "GB" to
                        WatchProviderCountry(
                            link = "https://tmdb/watch/gb",
                            flatrate = listOf(foreignProvider),
                        )
                )
            )
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flowOf(Result.Success(testMovie))
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

        val viewModel = createViewModel(watchProviders = providers)

        assertThat(viewModel.uiState.value.watchProvidersForRegion).isEmpty()
        assertThat(viewModel.uiState.value.watchProviderLink).isNull()
    }

    @Test
    fun onEventSaveForOfflinewhenprefetchfailsemitsShowMessageandclearssavingflag() =
        runTest {
            coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
                flow { emit(Result.Success(testMovie)) }
            coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
            coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

            val viewModel = createViewModel()
            every { prefetchContentForOfflineUseCase(testMovie.id, ContentType.MOVIE) } returns
                flowOf(Result.Loading, Result.Error(Exception("no space")))

            viewModel.sideEffect.test {
                viewModel.onEvent(MovieDetailUiEvent.SaveForOffline)
                assertThat(awaitItem())
                    .isEqualTo(MovieDetailSideEffect.ShowMessage("Could not save for offline"))
            }
            assertThat(viewModel.uiState.value.isOfflineSaving).isFalse()
            assertThat(viewModel.uiState.value.isSavedOffline).isFalse()
        }

    @Test
    fun onEventSaveForOfflinewhenalreadysavedofflinedoesnotprefetchagain() =
        runTest {
            coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
                flow { emit(Result.Success(testMovie)) }
            coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
            coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false

            val viewModel = createViewModel(savedOffline = true)
            viewModel.onEvent(MovieDetailUiEvent.SaveForOffline)

            verify(exactly = 0) {
                prefetchContentForOfflineUseCase(testMovie.id, ContentType.MOVIE)
            }
        }
}

class MovieDetailCollectionViewModelTest : MovieDetailViewModelTestFixture() {
    @Test
    fun onEventAddToCollectionsuccessclosessheetandnotifies() = runTest {
        val collection = FakeData.fakeUserCollections.first()
        coEvery { movieRepository.getMovieDetails(testMovie.id) } returns
            flow { emit(Result.Success(testMovie)) }
        coEvery { userRepository.isFavorite(testMovie.id, ContentType.MOVIE) } returns false
        coEvery { userRepository.isInWatchlist(testMovie.id, ContentType.MOVIE) } returns false
        every { userRepository.getAllCollections() } returns flowOf(listOf(collection))
        every { userRepository.getCollectionItems(collection.id) } returns flowOf(emptyList())
        coEvery {
            userRepository.addToCollection(collection.id, testMovie.id, ContentType.MOVIE)
        } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(MovieDetailUiEvent.OpenAddToCollection)
        assertThat(viewModel.uiState.value.showAddToCollection).isTrue()

        viewModel.sideEffect.test {
            viewModel.onEvent(MovieDetailUiEvent.AddToCollection(collection.id))
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(MovieDetailSideEffect.ShowMessage::class.java)
            assertThat((effect as MovieDetailSideEffect.ShowMessage).message)
                .contains(collection.name)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(viewModel.uiState.value.showAddToCollection).isFalse()
    }
}
