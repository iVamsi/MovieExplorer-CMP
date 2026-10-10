package com.movieapp.core.common

/**
 * Values shared across network, data, and UI. Keep in sync with TMDB / app wiring (see BuildConfig
 * for API base URL).
 */
object Constants {

    object Api {
        /**
         * TMDB image CDN base; path segment is size (e.g. [ImageSize.POSTER_MEDIUM]) + file path.
         */
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val API_KEY_HEADER = "Authorization"
        const val API_KEY_PREFIX = "Bearer "
        const val TIMEOUT_SECONDS = 30L
    }

    object Database {
        const val CACHE_DURATION_HOURS = 24L
    }

    object Pagination {
        const val DEFAULT_PAGE_SIZE = 20
    }

    /**
     * TMDB image size path segments (see https://developer.themoviedb.org/docs/image-basics).
     *
     * **In active use:** [POSTER_MEDIUM], [BACKDROP_MEDIUM], [PROFILE_MEDIUM], [PROFILE_LARGE]
     * (cards, gallery, image URLs). Other values are valid TMDB segments kept for API parity /
     * future features (e.g. higher-res heroes, small avatars).
     */
    object ImageSize {
        const val POSTER_SMALL = "w185"
        const val POSTER_MEDIUM = "w342"
        const val POSTER_LARGE = "w500"
        const val POSTER_ORIGINAL = "original"
        const val BACKDROP_SMALL = "w300"
        const val BACKDROP_MEDIUM = "w780"
        const val BACKDROP_LARGE = "w1280"
        const val BACKDROP_ORIGINAL = "original"
        const val PROFILE_SMALL = "w45"
        const val PROFILE_MEDIUM = "w185"
        const val PROFILE_LARGE = "w632"
        const val PROFILE_ORIGINAL = "original"
    }

    /**
     * TMDB trending time window query values. [DAY] is used by movie/TV trending; [WEEK] reserved
     * for weekly trending.
     */
    object TimeWindow {
        const val DAY = "day"
        const val WEEK = "week"
    }
}
