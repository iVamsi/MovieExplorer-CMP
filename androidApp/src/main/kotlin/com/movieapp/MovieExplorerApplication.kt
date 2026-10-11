package com.movieapp

import android.app.Application
import com.movieapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MovieExplorerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovieExplorerApplication)
            modules(appModules())
        }
    }
}
