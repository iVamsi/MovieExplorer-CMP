package com.movieapp.core.ui.utils

import com.movieapp.core.common.Constants
import com.movieapp.core.common.formatRating as commonFormatRating
import co.touchlab.kermit.Logger

fun String?.toImageUrl(size: String = Constants.ImageSize.POSTER_MEDIUM): String? {
    return this?.let { path ->
        if (path.isEmpty()) {
            Logger.withTag("ImageUtils").w { "Empty poster path provided" }
            null
        } else if (path.startsWith("http")) {
            path
        } else {
            val normalizedPath = if (path.startsWith("/")) path else "/$path"
            val imageUrl = "${Constants.Api.IMAGE_BASE_URL}$size$normalizedPath"
            Logger.withTag("ImageUtils").d { "Generated image URL: $imageUrl" }
            imageUrl
        }
    }
}

/** TMDB release date string → display year, or "Unknown" if null/invalid. */
fun String?.formatReleaseYear(): String {
    return this?.let { date ->
        try {
            if (date.length >= 4) {
                date.substring(0, 4)
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    } ?: "Unknown"
}

fun Double?.formatRating(): String {
    return this?.commonFormatRating() ?: "N/A"
}
