package com.movieapp.core.database.entity

import androidx.room.Entity

@Entity(tableName = "remote_keys", primaryKeys = ["id", "contentType"])
data class RemoteKeysEntity(
    val id: Int,
    val contentType: String,
    val prevKey: Int?,
    val nextKey: Int?,
)
