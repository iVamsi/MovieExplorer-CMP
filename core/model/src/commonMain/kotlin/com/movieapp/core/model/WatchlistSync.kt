package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchlistSyncItem(
    val id: Int,
    val title: String,
    val posterUrl: String? = null,
)

@Serializable
data class WatchlistSyncSnapshot(
    val items: List<WatchlistSyncItem> = emptyList(),
    val updatedAtMillis: Long = 0L,
)
