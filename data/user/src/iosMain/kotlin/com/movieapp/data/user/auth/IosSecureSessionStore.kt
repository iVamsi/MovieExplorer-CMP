package com.movieapp.data.user.auth

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import kotlinx.serialization.json.Json

/** Keeps the session state as one JSON entry in the iOS Keychain. */
@OptIn(ExperimentalSettingsImplementation::class)
class IosSecureSessionStore(private val json: Json) : SecureSessionStore {

    private val settings = KeychainSettings(service = "com.movieapp.cmp.session")

    override suspend fun read(): TmdbSessionState =
        settings.getStringOrNull(KEY_SESSION)?.let { json.decodeFromString<TmdbSessionState>(it) }
            ?: TmdbSessionState()

    override suspend fun write(state: TmdbSessionState) {
        settings.putString(KEY_SESSION, json.encodeToString(state))
    }

    private companion object {
        const val KEY_SESSION = "session"
    }
}
