package com.movieapp.core.common.auth

/** OAuth-style redirect target for TMDB `redirect_to` (must match MainActivity intent-filter). */
object AuthDeepLink {
    const val SCHEME = "movieapp"
    const val HOST = "auth"
    const val PATH_CALLBACK = "/callback"
    const val QUERY_STATE = "state"
    const val CALLBACK_URI = "movieapp://auth/callback"

    fun callbackUri(state: String): String = "$CALLBACK_URI?$QUERY_STATE=$state"
}
