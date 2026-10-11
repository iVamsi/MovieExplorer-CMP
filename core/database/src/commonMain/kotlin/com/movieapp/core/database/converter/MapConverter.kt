package com.movieapp.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MapConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let {
            try {
                json.decodeFromString<Map<String, String>>(it)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringIntMap(value: String?): Map<String, Int>? {
        return value?.let {
            try {
                json.decodeFromString<Map<String, Int>>(it)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    @TypeConverter
    fun fromStringDoubleMap(value: Map<String, Double>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringDoubleMap(value: String?): Map<String, Double>? {
        return value?.let {
            try {
                json.decodeFromString<Map<String, Double>>(it)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
}
