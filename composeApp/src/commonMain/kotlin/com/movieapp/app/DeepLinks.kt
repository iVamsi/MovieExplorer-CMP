package com.movieapp.app

import com.movieapp.core.common.auth.AuthDeepLink
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object DeepLinks {
    private val channel = Channel<String>(Channel.BUFFERED)
    val urls: Flow<String> = channel.receiveAsFlow()

    fun emit(url: String) {
        channel.trySend(url)
    }
}

sealed interface DeepLink {
    data class AuthCallback(
        val approved: Boolean,
        val requestToken: String?,
        val state: String?,
    ) : DeepLink

    data class Movie(val id: Int) : DeepLink

    data class Series(val id: Int) : DeepLink

    data class Actor(val id: Int) : DeepLink

    data class Collection(val id: String) : DeepLink

    data object Search : DeepLink

    data object Library : DeepLink

    data object Upcoming : DeepLink
}

/** Parses `movieapp://<host>/<id>?<query>` and rejects malformed or unsupported URLs. */
fun parseDeepLink(url: String): DeepLink? {
    val schemeEnd = url.indexOf("://")
    if (schemeEnd <= 0 || url.substring(0, schemeEnd) != AuthDeepLink.SCHEME) return null

    val remainder = url.substring(schemeEnd + 3)
    val queryStart = remainder.indexOf('?')
    val pathPart = if (queryStart >= 0) remainder.substring(0, queryStart) else remainder
    val queryPart = if (queryStart >= 0) remainder.substring(queryStart + 1) else ""
    val segments = pathPart.split('/').filter { it.isNotBlank() }
    val host = segments.firstOrNull() ?: return null
    val id = segments.getOrNull(1)
    val query =
        queryPart.split('&').mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) null else part.substring(0, separator) to part.substring(separator + 1)
        }.toMap()

    return when (host) {
        AuthDeepLink.HOST -> {
            if (id != AuthDeepLink.PATH_CALLBACK.removePrefix("/")) return null
            DeepLink.AuthCallback(
                approved = query["approved"] != "false",
                requestToken = query["request_token"],
                state = query[AuthDeepLink.QUERY_STATE],
            )
        }

        "movie" -> id?.toIntOrNull()?.let(DeepLink::Movie)
        "series" -> id?.toIntOrNull()?.let(DeepLink::Series)
        "actor" -> id?.toIntOrNull()?.let(DeepLink::Actor)
        "collection" -> id?.takeIf { it.isNotBlank() }?.let(DeepLink::Collection)
        "search" -> DeepLink.Search
        "collections" -> DeepLink.Library
        "upcoming" -> DeepLink.Upcoming
        else -> null
    }
}
