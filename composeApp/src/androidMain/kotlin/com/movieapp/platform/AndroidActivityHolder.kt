package com.movieapp.platform

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object AndroidActivityHolder {
    private var reference = WeakReference<FragmentActivity>(null)

    fun attach(activity: FragmentActivity) {
        reference = WeakReference(activity)
    }

    fun detach(activity: FragmentActivity) {
        if (reference.get() === activity) reference.clear()
    }

    fun current(): FragmentActivity? = reference.get()
}
