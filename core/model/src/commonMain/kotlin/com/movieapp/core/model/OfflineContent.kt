package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class OfflineContent(
    val contentId: Int,
    val contentType: ContentType,
    val title: String = "",
    val posterPath: String? = null,
    val prefetchedAt: Long = 0L,
    val posterCached: Boolean = false,
    val backdropCached: Boolean = false,
)
