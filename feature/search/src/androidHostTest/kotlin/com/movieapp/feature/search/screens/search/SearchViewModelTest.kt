package com.movieapp.feature.search.screens.search

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.network.NetworkConnectivity
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.WatchProviderRepository
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("FunctionName", "TooManyFunctions")
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val seriesRepository = mockk<SeriesRepository>(relaxed = true)
    private val actorRepository = mockk<ActorRepository>(relaxed = true)
    private val watchProviderRepository = mockk<WatchProviderRepository>(relaxed = true)
    private val preferencesRepository =
        mockk<PreferencesRepository>().also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
            every { it.recentSearchQueries } returns flowOf(emptyList())
            coEvery { it.addRecentSearchQuery(any()) } returns Unit
            coEvery { it.clearRecentSearchQueries() } returns Unit
        }

    private val networkConnectivity =
        mockk<NetworkConnectivity>().also {
            every { it.isOnline } returns MutableStateFlow(true).asStateFlow()
        }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { movieRepository.getTrendingMovies() } returns
            flowOf(Result.Success(emptyList<Movie>()))
        every { seriesRepository.getTrendingTvSeriesPage() } returns
            flowOf(Result.Success(emptyList<TvSeries>()))
        every { watchProviderRepository.getWatchProviderCatalog() } returns
            flowOf(Result.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SearchViewModel(
            movieRepository = movieRepository,
            seriesRepository = seriesRepository,
            actorRepository = actorRepository,
            watchProviderRepository = watchProviderRepository,
            preferencesRepository = preferencesRepository,
            networkConnectivity = networkConnectivity,
        )

    @Test
    fun `year validation - accepts blank or positive four digit years only`() {
        assertThat(isValidFilterYear("")).isTrue()
        assertThat(isValidFilterYear("2026")).isTrue()
        assertThat(isValidFilterYear("026")).isFalse()
        assertThat(isValidFilterYear("0000")).isFalse()
        assertThat(isValidFilterYear("20a6")).isFalse()
    }

    @Test
    fun `remove filter - clears only requested applied field`() {
        val filter =
            SearchFilter(year = 2024, ratingGte = 7.0, genreId = 28, watchProviderIds = setOf(8))

        assertThat(filter.without(SearchFilterField.YEAR)).isEqualTo(filter.copy(year = null))
        assertThat(filter.without(SearchFilterField.PROVIDERS))
            .isEqualTo(filter.copy(watchProviderIds = emptySet()))
    }

    @Test
    fun `initial state - query is empty and discover lists are empty`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.query).isEmpty()
            assertThat(state.movies).isEmpty()
            assertThat(state.tvSeries).isEmpty()
            assertThat(state.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQueryChange - updates query in state immediately`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.QueryChanged("Batman"))

        assertThat(viewModel.uiState.value.query).isEqualTo("Batman")
    }

    @Test
    fun `onQueryChange after debounce - requests paged movie search`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.searchMoviesPaged("Batman", "en-US", "US") } returns
            flowOf(PagingData.empty())

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch { viewModel.movieSearchPaged.collect {} }

        viewModel.onEvent(SearchUiEvent.QueryChanged("Batman"))
        advanceTimeBy(400)
        advanceUntilIdle()

        verify(atLeast = 1) { movieRepository.searchMoviesPaged("Batman", "en-US", "US") }
        collectJob.cancel()
    }

    @Test
    fun `onQueryChange after debounce - requests multi search for All tab flow`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.multiSearchPaged("Batman", "en-US", "US") } returns
            flowOf(PagingData.empty())

        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch { viewModel.multiSearchPaged.collect {} }

        viewModel.onEvent(SearchUiEvent.QueryChanged("Batman"))
        advanceTimeBy(400)
        advanceUntilIdle()

        verify(atLeast = 1) { movieRepository.multiSearchPaged("Batman", "en-US", "US") }
        collectJob.cancel()
    }

    @Test
    fun `blank query after debounce - clears snapshot lists used for discover`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.QueryChanged("Batman"))
        advanceTimeBy(400)
        viewModel.onEvent(SearchUiEvent.QueryChanged(""))
        advanceTimeBy(400)

        assertThat(viewModel.uiState.value.movies).isEmpty()
        assertThat(viewModel.uiState.value.tvSeries).isEmpty()
    }

    @Test
    fun `onSearchCommitted - persists trimmed query to preferences`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.QueryChanged("  Interstellar  "))
        viewModel.onEvent(SearchUiEvent.SearchCommitted)
        advanceUntilIdle()

        coVerify { preferencesRepository.addRecentSearchQuery("Interstellar") }
    }

    @Test
    fun `discover genres merge - updates state from movie and tv genre APIs`() = runTest {
        val g1 = Genre(1, "Action")
        val g2 = Genre(2, "Drama")
        every { movieRepository.getMovieGenresList("en-US") } returns
            flowOf(Result.Success(listOf(g1)))
        every { seriesRepository.getTvGenresList("en-US") } returns
            flowOf(Result.Success(listOf(g2)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.discoverGenres.map { it.id }).containsExactly(1, 2)
    }

    @Test
    fun `watch provider catalog load - updates state from repository`() = runTest {
        val provider =
            WatchProvider(
                displayPriority = 0,
                logoPath = "/netflix.png",
                providerId = 8,
                providerName = "Netflix",
            )
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { watchProviderRepository.getWatchProviderCatalog() } returns
            flowOf(Result.Success(listOf(provider)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.watchProviders.map { it.providerName })
            .containsExactly("Netflix")
    }

    @Test
    fun `filter changed with watch providers - discovers movies and series with provider ids`() =
        runTest {
            every { movieRepository.getMovieGenresList(any()) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getTvGenresList(any()) } returns
                flowOf(Result.Success(emptyList()))
            every {
                movieRepository.discoverMovies(
                    year = null,
                    ratingGte = null,
                    genreIds = null,
                    watchProviderIds = listOf(8, 337),
                    watchRegion = "US",
                )
            } returns flowOf(Result.Success(emptyList()))
            every {
                seriesRepository.discoverSeries(
                    year = null,
                    ratingGte = null,
                    genreIds = null,
                    watchProviderIds = listOf(8, 337),
                    watchRegion = "US",
                )
            } returns flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()
            viewModel.onEvent(
                SearchUiEvent.FilterChanged(SearchFilter(watchProviderIds = setOf(8, 337)))
            )
            advanceUntilIdle()

            verify {
                movieRepository.discoverMovies(
                    year = null,
                    ratingGte = null,
                    genreIds = null,
                    watchProviderIds = listOf(8, 337),
                    watchRegion = "US",
                )
            }
            verify {
                seriesRepository.discoverSeries(
                    year = null,
                    ratingGte = null,
                    genreIds = null,
                    watchProviderIds = listOf(8, 337),
                    watchRegion = "US",
                )
            }
        }

    @Test
    fun `trending spotlight - blank query - populates trending movies and series`() = runTest {
        val trendingMovie = Movie(id = 11, title = "Trending Movie")
        val trendingSeries = TvSeries(id = 22, name = "Trending Series")
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.getTrendingMovies() } returns
            flowOf(Result.Success(listOf(trendingMovie)))
        every { seriesRepository.getTrendingTvSeriesPage() } returns
            flowOf(Result.Success(listOf(trendingSeries)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.trendingMovies).containsExactly(trendingMovie)
        assertThat(state.trendingTvSeries).containsExactly(trendingSeries)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `trending spotlight - repository cancellation - collector job remains canceled`() =
        runTest {
            val completionCause = CompletableDeferred<Throwable?>()
            every { movieRepository.getMovieGenresList(any()) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getTvGenresList(any()) } returns
                flowOf(Result.Success(emptyList()))
            every { movieRepository.getTrendingMovies() } returns
                flow {
                    currentCoroutineContext().job.invokeOnCompletion { cause ->
                        completionCause.complete(cause)
                    }
                    throw CancellationException("cancelled")
                }

            createViewModel()
            advanceUntilIdle()

            assertThat(completionCause.await()).isInstanceOf(CancellationException::class.java)
        }

    @Test
    fun `trending spotlight - both sources fail - sets error from movies failure`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.getTrendingMovies() } returns
            flowOf(Result.Error(RuntimeException("network down")))
        every { seriesRepository.getTrendingTvSeriesPage() } returns
            flowOf(Result.Error(RuntimeException("tv down")))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.trendingMovies).isEmpty()
        assertThat(state.trendingTvSeries).isEmpty()
        assertThat(state.error).isEqualTo("network down")
    }

    @Test
    fun `trending spotlight - only movies fail - keeps series and no error`() = runTest {
        val trendingSeries = TvSeries(id = 22, name = "Trending Series")
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.getTrendingMovies() } returns
            flowOf(Result.Error(RuntimeException("network down")))
        every { seriesRepository.getTrendingTvSeriesPage() } returns
            flowOf(Result.Success(listOf(trendingSeries)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.trendingMovies).isEmpty()
        assertThat(state.trendingTvSeries).containsExactly(trendingSeries)
        assertThat(state.error).isNull()
    }

    @Test
    fun `watch provider catalog load failure - sets watchProvidersError`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { watchProviderRepository.getWatchProviderCatalog() } returns
            flowOf(Result.Error(RuntimeException("catalog unavailable")))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.watchProviders).isEmpty()
        assertThat(state.isWatchProvidersLoading).isFalse()
        assertThat(state.watchProvidersError).isEqualTo("catalog unavailable")
    }

    @Test
    fun `discover content - repository cancellation - collector job remains canceled`() = runTest {
        val completionCause = CompletableDeferred<Throwable?>()
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { movieRepository.discoverMovies(any(), any(), any(), any(), any()) } returns
            flow {
                currentCoroutineContext().job.invokeOnCompletion { cause ->
                    completionCause.complete(cause)
                }
                throw CancellationException("cancelled")
            }
        every { seriesRepository.discoverSeries(any(), any(), any(), any(), any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.FilterChanged(SearchFilter(year = 2026)))
        advanceUntilIdle()

        assertThat(completionCause.await()).isInstanceOf(CancellationException::class.java)
    }

    @Test
    fun `movie clicked - sends NavigateToMovieDetail side effect`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(SearchUiEvent.MovieClicked(42))
            assertThat(awaitItem()).isEqualTo(SearchSideEffect.NavigateToMovieDetail(42))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `series clicked - sends NavigateToSeriesDetail side effect`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(SearchUiEvent.SeriesClicked(7))
            assertThat(awaitItem()).isEqualTo(SearchSideEffect.NavigateToSeriesDetail(7))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `actor clicked - sends NavigateToActorDetail side effect`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(SearchUiEvent.ActorClicked(99))
            assertThat(awaitItem()).isEqualTo(SearchSideEffect.NavigateToActorDetail(99))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear recent searches - delegates to preferences repository`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.ClearRecentSearches)
        advanceUntilIdle()

        coVerify { preferencesRepository.clearRecentSearchQueries() }
    }

    @Test
    fun `recent search click - updates query and persists it`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(SearchUiEvent.RecentSearchClick("Inception"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("Inception")
        coVerify { preferencesRepository.addRecentSearchQuery("Inception") }
    }

    @Test
    fun `recent search queries from preferences - reflected in state`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { preferencesRepository.recentSearchQueries } returns flowOf(listOf("batman", "dune"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.recentSearchQueries)
            .containsExactly("batman", "dune")
            .inOrder()
    }

    @Test
    fun `network offline - sets isOffline in state`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { networkConnectivity.isOnline } returns MutableStateFlow(false).asStateFlow()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun `filter sheet visibility changed - updates state immediately`() = runTest {
        every { movieRepository.getMovieGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getTvGenresList(any()) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        viewModel.onEvent(SearchUiEvent.FilterSheetVisibilityChanged(true))
        assertThat(viewModel.uiState.value.isFilterSheetVisible).isTrue()

        viewModel.onEvent(SearchUiEvent.FilterSheetVisibilityChanged(false))
        assertThat(viewModel.uiState.value.isFilterSheetVisible).isFalse()
    }
}
