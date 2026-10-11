package com.movieapp.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.movieapp.core.common.locale.AppLocaleApplier

class AndroidAppLocaleApplier : AppLocaleApplier {
    override fun applyLanguage(languageCode: String) {
        val tag = languageCode.trim().ifBlank { "en" }.replace('_', '-')
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
