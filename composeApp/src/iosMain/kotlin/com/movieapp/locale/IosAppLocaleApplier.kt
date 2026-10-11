package com.movieapp.locale

import com.movieapp.core.common.locale.AppLocaleApplier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosAppLocaleApplier : AppLocaleApplier {
    override fun applyLanguage(languageCode: String) {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url = url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
