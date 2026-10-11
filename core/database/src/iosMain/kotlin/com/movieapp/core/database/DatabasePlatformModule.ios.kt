package com.movieapp.core.database

import androidx.room.Room
import com.movieapp.core.common.platform.documentsPath
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val databasePlatformModule: Module = module {
    single { Room.databaseBuilder<MovieAppDatabase>(name = "${documentsPath()}/$DATABASE_FILE") }
}
