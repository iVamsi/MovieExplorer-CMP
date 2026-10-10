package com.movieapp.core.common.network

import kotlinx.coroutines.flow.StateFlow

/** Observes device network reachability (internet capability on the active network). */
interface NetworkConnectivity {
    val isOnline: StateFlow<Boolean>
}
