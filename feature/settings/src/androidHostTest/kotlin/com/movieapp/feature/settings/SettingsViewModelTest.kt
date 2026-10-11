package com.movieapp.feature.settings

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.locale.AppLocaleApplier
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val imageLoader = mockk<ImageLoader>(relaxed = true)
    private val diskCache = mockk<DiskCache>(relaxed = true)
    private val memoryCache = mockk<MemoryCache>(relaxed = true)
    private val appLocaleApplier = mockk<AppLocaleApplier>(relaxed = true)
    private val offlineContentRepository = mockk<OfflineContentRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesRepository.language } returns flowOf("en")
        every { preferencesRepository.themeMode } returns flowOf("system")
        every { preferencesRepository.useWallpaperColors } returns flowOf(false)
        every { preferencesRepository.contentLanguage } returns flowOf("en-US")
        every { preferencesRepository.watchRegion } returns flowOf("US")
        every { preferencesRepository.adultContent } returns flowOf(false)
        every { imageLoader.diskCache } returns diskCache
        every { imageLoader.memoryCache } returns memoryCache
        every { diskCache.size } returns 5L * 1024 * 1024
        every { offlineContentRepository.getOfflineContent() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SettingsViewModel(
            preferencesRepository,
            imageLoader,
            appLocaleApplier,
            offlineContentRepository,
        )

    @Test
    fun `init - reads disk cache size and converts bytes to MB`() = runTest {
        val viewModel = createViewModel()

        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        assertThat(viewModel.uiState.value.cacheSizeMb).isEqualTo(5)
        job.cancel()
    }

    @Test
    fun `onEvent ThemeModeChanged - persists mode via preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.ThemeModeChanged("dark"))

        coVerify { preferencesRepository.setThemeMode("dark") }
    }

    @Test
    fun `onEvent AppLanguageChanged - persists language and applies locale`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.AppLanguageChanged("es"))

        coVerify { preferencesRepository.setLanguage("es") }
        coVerify { appLocaleApplier.applyLanguage("es") }
    }

    @Test
    fun `onEvent WatchRegionChanged - normalises to upper case two-letter code`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.WatchRegionChanged("  gb "))

        coVerify { preferencesRepository.setWatchRegion("GB") }
    }

    @Test
    fun `onEvent WatchRegionChanged - rejects codes with fewer than 2 letters`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.WatchRegionChanged("1"))

        coVerify(exactly = 0) { preferencesRepository.setWatchRegion(any()) }
    }

    @Test
    fun `onEvent ClearImageCache - clears coil caches and refreshes size`() = runTest {
        val viewModel = createViewModel()
        coJustRun { memoryCache.clear() }

        viewModel.onEvent(SettingsUiEvent.ClearImageCache)

        verify { memoryCache.clear() }
        verify { diskCache.clear() }
    }

    @Test
    fun `init - loads offline library`() = runTest {
        val offlineContent =
            OfflineContent(
                contentId = 1,
                contentType = ContentType.MOVIE,
                title = "The Matrix",
                posterPath = "/poster.jpg",
                prefetchedAt = 100L,
                posterCached = true,
                backdropCached = true,
            )
        every { offlineContentRepository.getOfflineContent() } returns
            flowOf(listOf(offlineContent))

        val viewModel = createViewModel()

        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        assertThat(viewModel.uiState.value.offlineContent).containsExactly(offlineContent)
        job.cancel()
    }

    @Test
    fun `onEvent DeleteOfflineContent - removes offline row`() = runTest {
        coEvery { offlineContentRepository.deleteOfflineContent(1, ContentType.MOVIE) } returns
            Result.Success(Unit)
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.DeleteOfflineContent(1, ContentType.MOVIE))

        coVerify { offlineContentRepository.deleteOfflineContent(1, ContentType.MOVIE) }
    }

    @Test
    fun `onEvent AdultContentChanged - delegates to preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(SettingsUiEvent.AdultContentChanged(true))

        coVerify { preferencesRepository.setAdultContent(true) }
    }

    @Test
    fun `themeMode - flows default until preference collected`() = runTest {
        val viewModel = createViewModel()

        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        assertThat(viewModel.uiState.value.themeMode).isEqualTo("system")
        job.cancel()
    }

    @Test
    fun `uiState - combines preferences cache and offline content`() = runTest {
        val offlineContent =
            OfflineContent(
                contentId = 7,
                contentType = ContentType.TV_SERIES,
                title = "Severance",
                posterPath = "/severance.jpg",
                prefetchedAt = 200L,
                posterCached = true,
                backdropCached = false,
            )
        every { preferencesRepository.themeMode } returns flowOf("dark")
        every { preferencesRepository.useWallpaperColors } returns flowOf(true)
        every { offlineContentRepository.getOfflineContent() } returns
            flowOf(listOf(offlineContent))
        val viewModel = createViewModel()

        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        assertThat(viewModel.uiState.value.themeMode).isEqualTo("dark")
        assertThat(viewModel.uiState.value.useWallpaperColors).isTrue()
        assertThat(viewModel.uiState.value.cacheSizeMb).isEqualTo(5)
        assertThat(viewModel.uiState.value.offlineContent).containsExactly(offlineContent)
        job.cancel()
    }

    @Test
    fun `onEvent ClearImageCache - emits cache cleared side effect`() = runTest {
        val viewModel = createViewModel()
        val effect = backgroundScope.async { viewModel.sideEffects.first() }

        viewModel.onEvent(SettingsUiEvent.ClearImageCache)

        assertThat(effect.await()).isEqualTo(SettingsSideEffect.ImageCacheCleared)
    }
}
