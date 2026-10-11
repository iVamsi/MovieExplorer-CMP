package com.movieapp.core.database.converter

import androidx.room.TypeConverter
import com.movieapp.core.model.Genre
import com.movieapp.core.model.VideoResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ModelConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromGenreList(value: List<Genre>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toGenreList(value: String?): List<Genre>? {
        return value?.let {
            try {
                json.decodeFromString<List<Genre>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromVideoResponse(value: VideoResponse?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toVideoResponse(value: String?): VideoResponse? {
        return value?.let {
            try {
                json.decodeFromString<VideoResponse>(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}
