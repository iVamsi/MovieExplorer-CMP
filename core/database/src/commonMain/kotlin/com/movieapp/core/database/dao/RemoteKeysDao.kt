package com.movieapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.movieapp.core.database.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE id = :id AND contentType = :contentType")
    suspend fun getRemoteKeyById(id: Int, contentType: String): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys WHERE contentType = :contentType")
    suspend fun clearByContentType(contentType: String)

    @Query("DELETE FROM remote_keys") suspend fun clearRemoteKeys()
}
