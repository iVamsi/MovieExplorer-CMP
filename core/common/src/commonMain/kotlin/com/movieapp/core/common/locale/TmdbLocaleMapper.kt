package com.movieapp.core.common.locale

/**
 * Maps app UI language codes (ISO 639-1) to TMDB `language` query values (`language-REGION`). See
 * TMDB supported locales; unknown codes fall back to [FALLBACK_TMDB_LANGUAGE].
 */
object TmdbLocaleMapper {

    const val FALLBACK_TMDB_LANGUAGE = "en-US"

    /**
     * When [PreferencesRepository.contentLanguage] is still the default, derive TMDB locale from
     * app language.
     */
    fun fromAppLanguageCode(appLanguageCode: String): String {
        val code =
            appLanguageCode.lowercase().trim().take(2).ifBlank {
                return FALLBACK_TMDB_LANGUAGE
            }
        return when (code) {
            "en" -> "en-US"
            "es" -> "es-ES"
            "pt" -> "pt-BR"
            "fr" -> "fr-FR"
            "de" -> "de-DE"
            "it" -> "it-IT"
            "ja" -> "ja-JP"
            "ko" -> "ko-KR"
            "zh" -> "zh-CN"
            "hi" -> "hi-IN"
            "ru" -> "ru-RU"
            "pl" -> "pl-PL"
            "tr" -> "tr-TR"
            "nl" -> "nl-NL"
            "sv" -> "sv-SE"
            "ar" -> "ar-SA"
            "id" -> "id-ID"
            "th" -> "th-TH"
            "vi" -> "vi-VN"
            else -> FALLBACK_TMDB_LANGUAGE
        }
    }
}
