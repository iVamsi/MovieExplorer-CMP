package com.movieapp.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let {
            try {
                json.decodeFromString<List<Int>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? {
        return value?.let {
            try {
                json.decodeFromString<List<Double>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
