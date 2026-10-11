package com.movieapp.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.database.converter.ListConverter
import com.movieapp.core.database.converter.MapConverter
import com.movieapp.core.database.converter.ModelConverters
import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.OfflineContentDao
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.database.entity.CollectionItemEntity
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.GenreEntity
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.database.entity.OfflineContentEntity
import com.movieapp.core.database.entity.RemoteKeysEntity
import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.database.entity.UserCollectionEntity
import com.movieapp.core.database.entity.UserRatingEntity
import com.movieapp.core.database.entity.WatchlistItemEntity

@Database(
    entities =
        [
            MovieEntity::class,
            TvSeriesEntity::class,
            ActorEntity::class,
            GenreEntity::class,
            UserCollectionEntity::class,
            CollectionItemEntity::class,
            WatchlistItemEntity::class,
            FavoriteItemEntity::class,
            UserRatingEntity::class,
            OfflineContentEntity::class,
            RemoteKeysEntity::class,
        ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ListConverter::class, MapConverter::class, ModelConverters::class)
@ConstructedBy(MovieAppDatabaseConstructor::class)
abstract class MovieAppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    abstract fun tvSeriesDao(): TvSeriesDao

    abstract fun actorDao(): ActorDao

    abstract fun userDao(): UserDao

    abstract fun remoteKeysDao(): RemoteKeysDao

    abstract fun offlineContentDao(): OfflineContentDao
}

@Suppress("KotlinNoActualForExpect") // Room's KSP processor generates the actuals
expect object MovieAppDatabaseConstructor : RoomDatabaseConstructor<MovieAppDatabase> {
    override fun initialize(): MovieAppDatabase
}

internal const val DATABASE_FILE = "movie_app.db"

internal fun RoomDatabase.Builder<MovieAppDatabase>.buildMovieAppDatabase(dispatchers: AppDispatchers): MovieAppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatchers.io)
        .build()
