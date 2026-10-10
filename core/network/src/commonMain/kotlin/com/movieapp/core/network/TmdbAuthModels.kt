package com.movieapp.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestTokenResponse(
    val success: Boolean,
    @SerialName("request_token") val requestToken: String,
)

@Serializable data class CreateSessionBody(@SerialName("request_token") val requestToken: String)

@Serializable
data class SessionResponse(val success: Boolean, @SerialName("session_id") val sessionId: String)

@Serializable data class DeleteSessionBody(@SerialName("session_id") val sessionId: String)

@Serializable data class TmdbPostSuccess(val success: Boolean)

@Serializable
data class TmdbAccountDto(
    val id: Int,
    val username: String,
    val name: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
)

@Serializable
data class AccountFavoriteBody(
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_id") val mediaId: Int,
    val favorite: Boolean,
)

@Serializable
data class AccountWatchlistBody(
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_id") val mediaId: Int,
    @SerialName("watchlist") val watchlist: Boolean,
)

@Serializable data class MovieTvRatingBody(val value: Float)
