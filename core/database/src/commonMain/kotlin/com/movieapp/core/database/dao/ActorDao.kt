package com.movieapp.core.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.movieapp.core.database.entity.ActorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActorDao {

    @Query("SELECT * FROM actors WHERE id = :actorId")
    suspend fun getActorById(actorId: Int): ActorEntity?

    @Query("SELECT * FROM actors WHERE id = :actorId")
    fun getActorByIdFlow(actorId: Int): Flow<ActorEntity?>

    @Query("SELECT * FROM actors ORDER BY popularity DESC")
    fun getAllActors(): Flow<List<ActorEntity>>

    @Query("SELECT * FROM actors ORDER BY popularity DESC")
    fun getActorsPagingSource(): PagingSource<Int, ActorEntity>

    @Query("SELECT * FROM actors WHERE name LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchActors(query: String): Flow<List<ActorEntity>>

    @Query("SELECT * FROM actors WHERE name LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchActorsPagingSource(query: String): PagingSource<Int, ActorEntity>

    @Query("SELECT * FROM actors WHERE id IN (:actorIds) ORDER BY popularity DESC")
    fun getActorsByIds(actorIds: List<Int>): Flow<List<ActorEntity>>

    @Query("SELECT * FROM actors WHERE knownForDepartment = :department ORDER BY popularity DESC")
    fun getActorsByDepartment(department: String): Flow<List<ActorEntity>>

    @Query("SELECT * FROM actors WHERE knownForDepartment = :department ORDER BY popularity DESC")
    fun getActorsByDepartmentPagingSource(department: String): PagingSource<Int, ActorEntity>

    @Query("SELECT * FROM actors WHERE cachedAt > :timestamp ORDER BY popularity DESC")
    fun getCachedActors(timestamp: Long): Flow<List<ActorEntity>>

    @Query("SELECT COUNT(*) FROM actors") suspend fun getActorCount(): Int

    @Query("SELECT * FROM actors ORDER BY cachedAt DESC LIMIT :limit")
    fun getRecentActors(limit: Int): Flow<List<ActorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertActor(actor: ActorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActors(actors: List<ActorEntity>)

    @Update suspend fun updateActor(actor: ActorEntity)

    @Delete suspend fun deleteActor(actor: ActorEntity)

    @Query("DELETE FROM actors WHERE id = :actorId") suspend fun deleteActorById(actorId: Int)

    @Query("DELETE FROM actors WHERE cachedAt < :timestamp")
    suspend fun deleteOldActors(timestamp: Long)

    @Query("DELETE FROM actors") suspend fun deleteAllActors()

    @Query(
        "SELECT * FROM actors WHERE id IN (SELECT contentId FROM watchlist_items WHERE contentType = 'ACTOR') ORDER BY popularity DESC"
    )
    fun getWatchlistActors(): Flow<List<ActorEntity>>

    @Query(
        "SELECT * FROM actors WHERE id IN (SELECT contentId FROM favorite_items WHERE contentType = 'ACTOR') ORDER BY popularity DESC"
    )
    fun getFavoriteActors(): Flow<List<ActorEntity>>

    @Query(
        "SELECT * FROM actors WHERE id IN (SELECT contentId FROM collection_items WHERE collectionId = :collectionId AND contentType = 'ACTOR') ORDER BY popularity DESC"
    )
    fun getActorsInCollection(collectionId: String): Flow<List<ActorEntity>>
}
