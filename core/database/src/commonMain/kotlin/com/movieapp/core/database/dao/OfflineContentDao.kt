package com.movieapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.movieapp.core.database.entity.OfflineContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineContentDao {

    @Query("SELECT * FROM offline_content ORDER BY prefetchedAt DESC")
    fun getOfflineContent(): Flow<List<OfflineContentEntity>>

    @Query(
        """
        SELECT * FROM offline_content
        WHERE contentId = :contentId AND contentType = :contentType
        """
    )
    fun observeOfflineContent(contentId: Int, contentType: String): Flow<OfflineContentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflineContent(entity: OfflineContentEntity)

    @Query(
        "DELETE FROM offline_content WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun deleteOfflineContent(contentId: Int, contentType: String)

    @Query("DELETE FROM offline_content") suspend fun clearOfflineContent()
}
