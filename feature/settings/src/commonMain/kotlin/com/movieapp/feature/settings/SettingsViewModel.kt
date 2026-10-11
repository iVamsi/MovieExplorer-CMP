package com.movieapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import com.movieapp.core.common.locale.AppLocaleApplier
import com.movieapp.core.common.locale.TmdbLocaleMapper
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiEvent {
    data class ThemeModeChanged(val mode: String) : SettingsUiEvent

    data class WallpaperColorsChanged(val enabled: Boolean) : SettingsUiEvent

    data class AppLanguageChanged(val languageCode: String) : SettingsUiEvent

    data object ClearImageCache : SettingsUiEvent

    data class DeleteOfflineContent(val contentId: Int, val contentType: ContentType) :
        SettingsUiEvent

    data object ClearOfflineContent : SettingsUiEvent

    data class ContentLanguageChanged(val languageCode: String) : SettingsUiEvent

    data class WatchRegionChanged(val regionCode: String) : SettingsUiEvent

    data class AdultContentChanged(val enabled: Boolean) : SettingsUiEvent
}

data class SettingsUiState(
    val language: String = "en",
    val themeMode: String = "system",
    val useWallpaperColors: Boolean = false,
    val contentLanguage: String = TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE,
    val watchRegion: String = "US",
    val adultContent: Boolean = false,
    val cacheSizeMb: Long = 0,
    val offlineContent: List<OfflineContent> = emptyList(),
)

sealed interface SettingsSideEffect {
    data object ImageCacheCleared : SettingsSideEffect
}

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val imageLoader: ImageLoader,
    private val appLocaleApplier: AppLocaleApplier,
    private val offlineContentRepository: OfflineContentRepository,
) : ViewModel() {

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ThemeModeChanged -> setThemeMode(event.mode)
            is SettingsUiEvent.WallpaperColorsChanged ->
                viewModelScope.launch { preferencesRepository.setUseWallpaperColors(event.enabled) }
            is SettingsUiEvent.AppLanguageChanged -> setAppLanguage(event.languageCode)
            SettingsUiEvent.ClearImageCache -> clearCache()
            is SettingsUiEvent.DeleteOfflineContent ->
                deleteOfflineContent(event.contentId, event.contentType)
            SettingsUiEvent.ClearOfflineContent -> clearOfflineContent()
            is SettingsUiEvent.ContentLanguageChanged -> setContentLanguage(event.languageCode)
            is SettingsUiEvent.WatchRegionChanged -> setWatchRegion(event.regionCode)
            is SettingsUiEvent.AdultContentChanged -> setAdultContent(event.enabled)
        }
    }

    private val _cacheSizeMb = MutableStateFlow(0L)
    private val _offlineContent = MutableStateFlow<List<OfflineContent>>(emptyList())
    private val _sideEffects = Channel<SettingsSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<SettingsSideEffect> = _sideEffects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> =
        preferencesRepository.language
            .combine(preferencesRepository.themeMode) { language, themeMode ->
                SettingsUiState(language = language, themeMode = themeMode)
            }
            .combine(preferencesRepository.useWallpaperColors) { state, enabled ->
                state.copy(useWallpaperColors = enabled)
            }
            .combine(preferencesRepository.contentLanguage) { state, language ->
                state.copy(contentLanguage = language)
            }
            .combine(preferencesRepository.watchRegion) { state, region ->
                state.copy(watchRegion = region)
            }
            .combine(preferencesRepository.adultContent) { state, enabled ->
                state.copy(adultContent = enabled)
            }
            .combine(_cacheSizeMb) { state, cacheSizeMb ->
                state.copy(cacheSizeMb = cacheSizeMb)
            }
            .combine(_offlineContent) { state, offlineContent ->
                state.copy(offlineContent = offlineContent)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState(),
            )

    init {
        refreshCacheSize()
        observeOfflineContent()
    }

    private fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    private fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(languageCode)
            appLocaleApplier.applyLanguage(languageCode)
        }
    }

    private fun setContentLanguage(languageCode: String) {
        viewModelScope.launch { preferencesRepository.setContentLanguage(languageCode) }
    }

    private fun setWatchRegion(regionCode: String) {
        viewModelScope.launch {
            val letters = regionCode.trim().uppercase().filter { it.isLetter() }
            if (letters.length < 2) return@launch
            preferencesRepository.setWatchRegion(letters.take(2))
        }
    }

    private fun setAdultContent(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAdultContent(enabled) }
    }

    private fun observeOfflineContent() {
        viewModelScope.launch {
            offlineContentRepository.getOfflineContent().collect { content ->
                _offlineContent.value = content
            }
        }
    }

    private fun deleteOfflineContent(contentId: Int, contentType: ContentType) {
        viewModelScope.launch {
            offlineContentRepository.deleteOfflineContent(
                contentId = contentId,
                contentType = contentType,
            )
        }
    }

    private fun clearOfflineContent() {
        viewModelScope.launch { offlineContentRepository.clearOfflineContent() }
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun clearCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
        refreshCacheSize()
        _sideEffects.trySend(SettingsSideEffect.ImageCacheCleared)
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun refreshCacheSize() {
        val diskBytes = imageLoader.diskCache?.size ?: 0L
        _cacheSizeMb.value = diskBytes / (1024 * 1024)
    }
}
