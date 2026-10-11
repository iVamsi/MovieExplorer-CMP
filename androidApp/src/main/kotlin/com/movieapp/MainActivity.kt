package com.movieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.movieapp.app.App
import com.movieapp.app.DeepLinks
import com.movieapp.platform.AndroidActivityHolder

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidActivityHolder.attach(this)
        enableEdgeToEdge()
        intent?.data?.let { DeepLinks.emit(it.toString()) }
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { DeepLinks.emit(it.toString()) }
    }

    override fun onDestroy() {
        AndroidActivityHolder.detach(this)
        super.onDestroy()
    }
}
