package com.tamdan.mobile.runtime

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class DeviceIdentity(val identifier: String, val platform: String = PLATFORM_ANDROID) {
    init {
        require(DeviceIdentityRules.isValidIdentifier(identifier)) { "invalid device identity" }
        require(platform == PLATFORM_ANDROID) { "unsupported device platform" }
    }

    companion object {
        const val PLATFORM_ANDROID = "ANDROID"
    }
}

object DeviceIdentityRules {
    private val uuidPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")

    fun isValidIdentifier(value: String): Boolean = uuidPattern.matches(value)
}

/**
 * Stores an app-generated identity rather than a hardware or system identifier.
 * The value is stable across process restarts and reboots and is encrypted at rest.
 */
class DeviceIdentityStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun readOrCreate(): DeviceIdentity {
        val existing = read()
        if (existing != null) return existing

        val identity = DeviceIdentity(UUID.randomUUID().toString())
        save(identity)
        return identity
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun read(): DeviceIdentity? = try {
        val ciphertext = preferences.getString(CIPHERTEXT, null)
        val nonce = preferences.getString(NONCE, null)
        if (ciphertext == null && nonce == null) return null
        if (ciphertext == null || nonce == null) {
            clear()
            return null
        }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, Base64.decode(nonce, Base64.NO_WRAP)))
        }
        val identifier = String(
            cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)),
            StandardCharsets.UTF_8,
        )
        DeviceIdentity(identifier)
    } catch (_: Exception) {
        clear()
        null
    }

    private fun save(identity: DeviceIdentity) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(identity.identifier.toByteArray(StandardCharsets.UTF_8))
        preferences.edit()
            .putString(CIPHERTEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(NONCE, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    private fun key(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tamdan.device.identity.v1"
        private const val PREFERENCES = "tamdan_device_identity"
        private const val CIPHERTEXT = "ciphertext"
        private const val NONCE = "nonce"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_BITS = 128
    }
}
