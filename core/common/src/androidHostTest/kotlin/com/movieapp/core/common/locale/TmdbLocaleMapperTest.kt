package com.movieapp.core.common.locale

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TmdbLocaleMapperTest {

    @Test
    fun `fromAppLanguageCode - en maps to en-US`() {
        assertThat(TmdbLocaleMapper.fromAppLanguageCode("en")).isEqualTo("en-US")
    }

    @Test
    fun `fromAppLanguageCode - es maps to es-ES`() {
        assertThat(TmdbLocaleMapper.fromAppLanguageCode("es")).isEqualTo("es-ES")
    }

    @Test
    fun `fromAppLanguageCode - blank falls back`() {
        assertThat(TmdbLocaleMapper.fromAppLanguageCode(""))
            .isEqualTo(TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE)
    }

    @Test
    fun `fromAppLanguageCode - unknown falls back`() {
        assertThat(TmdbLocaleMapper.fromAppLanguageCode("xx"))
            .isEqualTo(TmdbLocaleMapper.FALLBACK_TMDB_LANGUAGE)
    }
}
