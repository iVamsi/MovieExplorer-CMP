package com.movieapp.core.common.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AppearancePreferencesTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test
    fun `appearance - existing dark choice - wallpaper colors remain opt in`() = runTest {
        val store =
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                temporaryFolder.newFolder().resolve("appearance.preferences_pb")
            }
        val repository = PreferencesRepository(store)
        repository.setThemeMode("dark")

        assertThat(repository.useWallpaperColors.first()).isFalse()
        assertThat(repository.themeMode.first()).isEqualTo("dark")
    }

    @Test
    fun `setUseWallpaperColors - toggled - persists independently of theme mode`() = runTest {
        val store =
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                temporaryFolder.newFolder().resolve("appearance.preferences_pb")
            }
        val repository = PreferencesRepository(store)
        repository.setThemeMode("light")
        repository.setUseWallpaperColors(true)

        val restored = PreferencesRepository(store)
        assertThat(restored.useWallpaperColors.first()).isTrue()
        assertThat(restored.themeMode.first()).isEqualTo("light")
        restored.setUseWallpaperColors(false)
        assertThat(repository.useWallpaperColors.first()).isFalse()
    }
}
