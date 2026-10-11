package com.movieapp.core.database.entity

import androidx.room.Entity

@Entity(tableName = "offline_content", primaryKeys = ["contentId", "contentType"])
data class OfflineContentEntity(
    val contentId: Int,
    val contentType: String,
    val prefetchedAt: Long,
    val metadataVersion: String? = null,
    val posterCached: Boolean,
    val backdropCached: Boolean,
)
