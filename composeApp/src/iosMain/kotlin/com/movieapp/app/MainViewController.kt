package com.movieapp.app

import androidx.compose.ui.window.ComposeUIViewController
import com.movieapp.di.appModules
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun initKoin() {
    startKoin { modules(appModules()) }
}

fun handleDeepLink(url: String) {
    DeepLinks.emit(url)
}
