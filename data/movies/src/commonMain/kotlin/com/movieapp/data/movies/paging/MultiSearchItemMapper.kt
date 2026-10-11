package com.movieapp.data.movies.paging

import com.movieapp.core.model.Actor
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchItem
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.TvSeries

internal fun MultiSearchItem.toMultiSearchListRow(): MultiSearchListRow? =
    when (mediaType.lowercase()) {
        "movie" -> MultiSearchListRow.MovieRow(toMovie())
        "tv" -> MultiSearchListRow.TvRow(toTvSeries())
        "person" -> MultiSearchListRow.PersonRow(toActor())
        else -> null
    }

private fun MultiSearchItem.toMovie(): Movie =
    Movie(
        id = id,
        title = title ?: name ?: "",
        overview = overview ?: "",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        originalLanguage = originalLanguage,
        originalTitle = originalTitle ?: title ?: "",
        genreIds = genreIds,
    )

private fun MultiSearchItem.toTvSeries(): TvSeries =
    TvSeries(
        id = id,
        name = name ?: title ?: "",
        overview = overview ?: "",
        posterPath = posterPath,
        backdropPath = backdropPath,
        firstAirDate = firstAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        originalLanguage = originalLanguage,
        originalName = originalName ?: name ?: "",
        genreIds = genreIds,
        originCountry = originCountry,
    )

private fun MultiSearchItem.toActor(): Actor =
    Actor(
        id = id,
        name = name ?: title ?: "",
        profilePath = profilePath,
        knownForDepartment = knownForDepartment ?: "",
        gender = gender ?: 0,
        popularity = popularity,
        originalName = originalName ?: name ?: "",
    )
