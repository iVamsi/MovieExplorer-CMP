package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchStats(
    val genreBreakdown: List<GenreCount> = emptyList(),
    val totalWatchTimeMinutes: Long = 0,
    val decadeBreakdown: List<DecadeCount> = emptyList(),
    val topPeople: List<PersonCount> = emptyList(),
    val ratingHistogram: Map<Int, Int> = emptyMap(),
)

@Serializable data class GenreCount(val genreName: String, val count: Int)

@Serializable data class DecadeCount(val decade: String, val count: Int)

@Serializable
data class PersonCount(
    val id: Int,
    val name: String,
    val role: String,
    val profilePath: String? = null,
    val count: Int,
)
