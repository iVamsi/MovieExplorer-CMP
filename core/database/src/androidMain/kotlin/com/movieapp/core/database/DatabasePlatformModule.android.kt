package com.movieapp.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val databasePlatformModule: Module = module {
    single {
        val context = androidContext()
        Room.databaseBuilder<MovieAppDatabase>(context = context, name = context.getDatabasePath(DATABASE_FILE).absolutePath)
    }
}
