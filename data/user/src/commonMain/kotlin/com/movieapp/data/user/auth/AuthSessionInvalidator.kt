package com.movieapp.data.user.auth

/** Called when TMDB returns 401 for session-authenticated calls. */
fun interface AuthSessionInvalidator {
    suspend fun onSessionInvalidated()
}
