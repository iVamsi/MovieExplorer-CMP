package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseReminder(
    val contentId: Int,
    val contentType: ContentType,
    val title: String,
    val releaseAtMillis: Long,
    val scheduledAtMillis: Long,
    val workRequestId: String,
)
