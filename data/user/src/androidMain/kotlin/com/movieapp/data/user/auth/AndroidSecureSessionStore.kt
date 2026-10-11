package com.movieapp.data.user.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath

/** Preferences DataStore whose values are encrypted with an Android Keystore AES-GCM key. */
class AndroidSecureSessionStore(context: Context) : SecureSessionStore {

    private val dataStore =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { context.filesDir.resolve(DATASTORE_FILE).absolutePath.toPath() }
        )

    override suspend fun read(): TmdbSessionState {
        val prefs = dataStore.data.first()
        return TmdbSessionState(
            sessionId = prefs.decryptString(KEY_SESSION),
            accountId = prefs.decryptString(KEY_ACCOUNT)?.toIntOrNull(),
            username = prefs.decryptString(KEY_USERNAME),
            name = prefs.decryptString(KEY_NAME),
            avatarPath = prefs.decryptString(KEY_AVATAR),
            pendingRequestToken = prefs.decryptString(KEY_PENDING_REQUEST_TOKEN),
            pendingLoginState = prefs.decryptString(KEY_PENDING_STATE),
        )
    }

    override suspend fun write(state: TmdbSessionState) {
        dataStore.edit { prefs ->
            prefs.encryptString(KEY_SESSION, state.sessionId)
            prefs.encryptString(KEY_ACCOUNT, state.accountId?.toString())
            prefs.encryptString(KEY_USERNAME, state.username)
            prefs.encryptString(KEY_NAME, state.name)
            prefs.encryptString(KEY_AVATAR, state.avatarPath)
            prefs.encryptString(KEY_PENDING_REQUEST_TOKEN, state.pendingRequestToken)
            prefs.encryptString(KEY_PENDING_STATE, state.pendingLoginState)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        return "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(ciphertext, Base64.NO_WRAP)}"
    }

    private fun decrypt(encoded: String): String? =
        try {
            val colonIdx = encoded.indexOf(':')
            if (colonIdx < 0) return null
            val iv = Base64.decode(encoded.substring(0, colonIdx), Base64.NO_WRAP)
            val ciphertext = Base64.decode(encoded.substring(colonIdx + 1), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            Logger.withTag(TAG).w(e) { "Decryption failed for a session field" }
            null
        }

    private fun Preferences.decryptString(key: String): String? {
        val raw = this[stringPreferencesKey(key)] ?: return null
        return decrypt(raw)
    }

    private fun MutablePreferences.encryptString(key: String, value: String?) {
        if (value == null) {
            remove(stringPreferencesKey(key))
        } else {
            this[stringPreferencesKey(key)] = encrypt(value)
        }
    }

    private companion object {
        const val TAG = "AndroidSecureSessionStore"
        const val DATASTORE_FILE = "session.preferences_pb"
        const val KEY_ALIAS = "movieapp_session_key"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"

        const val KEY_SESSION = "session_id"
        const val KEY_ACCOUNT = "account_id"
        const val KEY_USERNAME = "username"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar_path"
        const val KEY_PENDING_REQUEST_TOKEN = "pending_request_token"
        const val KEY_PENDING_STATE = "pending_state"
    }
}
