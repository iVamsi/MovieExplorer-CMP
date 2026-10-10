package com.movieapp.core.common.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.movieapp.core.common.locale.TmdbLocaleMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_WALLPAPER_COLORS = booleanPreferencesKey("use_wallpaper_colors")
        val LANGUAGE = stringPreferencesKey("language")
        val CONTENT_LANGUAGE = stringPreferencesKey("content_language")
        val WATCH_REGION = stringPreferencesKey("watch_region")
        val ADULT_CONTENT = booleanPreferencesKey("adult_content")
        val AUTO_PLAY_VIDEOS = booleanPreferencesKey("auto_play_videos")
        val HIGH_QUALITY_IMAGES = booleanPreferencesKey("high_quality_images")
        val CACHE_SIZE_MB = stringPreferencesKey("cache_size_mb")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")

        private val RECENT_SEARCH_QUERIES = stringPreferencesKey("recent_search_queries")
        private const val RECENT_SEARCH_SEPARATOR = "\u001e"
        private const val MAX_RECENT_SEARCHES = 10
        private const val MAX_QUERY_LENGTH = 200

        private fun encodeRecentSearchQueries(list: List<String>): String =
            list.joinToString(RECENT_SEARCH_SEPARATOR)

        private fun decodeRecentSearchQueries(raw: String?): List<String> =
            raw?.split(RECENT_SEARCH_SEPARATOR)
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()
    }

    /** Theme mode: "light", "dark", or "system" */
    val themeMode: Flow<String> =
        dataStore.data.map { preferences -> preferences[THEME_MODE] ?: "system" }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = mode }
    }

    val useWallpaperColors: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[USE_WALLPAPER_COLORS] ?: false }

    suspend fun setUseWallpaperColors(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[USE_WALLPAPER_COLORS] = enabled }
    }

    /** App language */
    val language: Flow<String> = dataStore.data.map { preferences -> preferences[LANGUAGE] ?: "en" }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences -> preferences[LANGUAGE] = languageCode }
    }

    /**
     * User-selected catalog language for TMDB (titles, overviews). Defaults to
     * [TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE].
     */
    val contentLanguage: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[CONTENT_LANGUAGE] ?: TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE
        }

    suspend fun setContentLanguage(languageCode: String) {
        dataStore.edit { preferences -> preferences[CONTENT_LANGUAGE] = languageCode }
    }

    /**
     * Effective TMDB `language` parameter: uses [contentLanguage] when the user has moved off the
     * default catalog locale; otherwise maps [language] (app UI) via [TmdbLocaleMapper] so Spanish
     * UI etc. request matching translations without a separate setting.
     */
    val tmdbLanguage: Flow<String> =
        combine(language, contentLanguage) { app, content ->
            if (
                content == TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE &&
                    app.isNotBlank() &&
                    app != "en"
            ) {
                TmdbLocaleMapper.fromAppLanguageCode(app)
            } else {
                content
            }
        }

    /** ISO 3166-1 region for watch providers and search (e.g. US, GB). */
    val watchRegion: Flow<String> =
        dataStore.data.map { preferences -> preferences[WATCH_REGION] ?: "US" }

    suspend fun setWatchRegion(regionCode: String) {
        dataStore.edit { preferences -> preferences[WATCH_REGION] = regionCode }
    }

    /** Show adult content */
    val adultContent: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[ADULT_CONTENT] ?: false }

    suspend fun setAdultContent(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[ADULT_CONTENT] = enabled }
    }

    /** Auto-play videos */
    val autoPlayVideos: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[AUTO_PLAY_VIDEOS] ?: true }

    suspend fun setAutoPlayVideos(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[AUTO_PLAY_VIDEOS] = enabled }
    }

    /** High quality images */
    val highQualityImages: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[HIGH_QUALITY_IMAGES] ?: true }

    suspend fun setHighQualityImages(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[HIGH_QUALITY_IMAGES] = enabled }
    }

    /** Cache size in MB */
    val cacheSizeMb: Flow<Int> =
        dataStore.data.map { preferences -> preferences[CACHE_SIZE_MB]?.toIntOrNull() ?: 500 }

    suspend fun setCacheSizeMb(sizeMb: Int) {
        dataStore.edit { preferences -> preferences[CACHE_SIZE_MB] = sizeMb.toString() }
    }

    /** Require biometric unlock when the app is opened or resumed after backgrounding. */
    val biometricLockEnabled: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[BIOMETRIC_LOCK_ENABLED] ?: false }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[BIOMETRIC_LOCK_ENABLED] = enabled }
    }

    /** Recent search queries (newest first), newest 10 retained. */
    val recentSearchQueries: Flow<List<String>> =
        dataStore.data.map { preferences ->
            decodeRecentSearchQueries(preferences[RECENT_SEARCH_QUERIES])
        }

    suspend fun addRecentSearchQuery(query: String) {
        val cleaned = query.trim().replace(RECENT_SEARCH_SEPARATOR, " ").take(MAX_QUERY_LENGTH)
        if (cleaned.isBlank()) return
        dataStore.edit { preferences ->
            val current =
                decodeRecentSearchQueries(preferences[RECENT_SEARCH_QUERIES]).toMutableList()
            current.remove(cleaned)
            current.add(0, cleaned)
            while (current.size > MAX_RECENT_SEARCHES) {
                current.removeAt(current.lastIndex)
            }
            preferences[RECENT_SEARCH_QUERIES] = encodeRecentSearchQueries(current)
        }
    }

    suspend fun clearRecentSearchQueries() {
        dataStore.edit { preferences -> preferences.remove(RECENT_SEARCH_QUERIES) }
    }

    /** Clear all preferences */
    suspend fun clearAll() {
        dataStore.edit { preferences -> preferences.clear() }
    }
}
