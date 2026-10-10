package com.movieapp.core.network

import com.movieapp.core.model.WatchProviderCatalog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TmdbWatchProviderApi(private val client: HttpClient) {
    suspend fun getMovieWatchProviderCatalog(
        language: String = "en-US",
        watchRegion: String? = null,
    ): WatchProviderCatalog =
        client.get("watch/providers/movie") {
            parameter("language", language)
            parameter("watch_region", watchRegion)
        }.body()

    suspend fun getTvWatchProviderCatalog(
        language: String = "en-US",
        watchRegion: String? = null,
    ): WatchProviderCatalog =
        client.get("watch/providers/tv") {
            parameter("language", language)
            parameter("watch_region", watchRegion)
        }.body()
}
