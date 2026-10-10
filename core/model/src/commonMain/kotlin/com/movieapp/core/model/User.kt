package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarPath: String? = null,
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L,
    val preferences: UserPreferences = UserPreferences(),
)

@Serializable
data class UserPreferences(
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val adultContent: Boolean = false,
    val notifications: NotificationPreferences = NotificationPreferences(),
)

@Serializable
data class NotificationPreferences(
    val newReleases: Boolean = true,
    val recommendations: Boolean = true,
    val watchlistUpdates: Boolean = true,
    val marketing: Boolean = false,
)

@Serializable
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}

@Serializable
data class UserCollection(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val isPublic: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val itemCount: Int = 0,
    val items: List<CollectionItem> = emptyList(),
)

@Serializable
data class CollectionItem(
    val id: String = "",
    val collectionId: String = "",
    val contentId: Int = 0,
    val contentType: ContentType = ContentType.MOVIE,
    val addedAt: Long = 0L,
    val userRating: Int? = null,
    val userNotes: String? = null,
    val isWatched: Boolean = false,
    val watchedAt: Long? = null,
)

@Serializable
enum class ContentType {
    MOVIE,
    TV_SERIES,
    ACTOR,
}

@Serializable
data class WatchlistItem(
    val id: String = "",
    val contentId: Int = 0,
    val contentType: ContentType = ContentType.MOVIE,
    val addedAt: Long = 0L,
    val priority: Int = 0,
)

@Serializable
data class FavoriteItem(
    val id: String = "",
    val contentId: Int = 0,
    val contentType: ContentType = ContentType.MOVIE,
    val addedAt: Long = 0L,
)

@Serializable
data class UserRating(
    val id: String = "",
    val contentId: Int = 0,
    val contentType: ContentType = ContentType.MOVIE,
    val rating: Int = 0,
    val ratedAt: Long = 0L,
    val notes: String? = null,
)
