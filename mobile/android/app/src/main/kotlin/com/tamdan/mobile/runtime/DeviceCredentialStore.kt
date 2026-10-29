package com.tamdan.mobile.runtime

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DeviceCredentialStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun save(context: DeviceContext) {
        require(context.deviceId > 0 && context.childId > 0) { "invalid device context" }
        require(context.accessToken.isNotBlank() && context.refreshToken.isNotBlank()) { "credentials are required" }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(serialize(context).toByteArray(StandardCharsets.UTF_8))
        preferences.edit()
            .putString(CIPHERTEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(NONCE, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun read(): DeviceContext? = try {
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
        deserialize(String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)), StandardCharsets.UTF_8))
    } catch (_: Exception) {
        clear()
        null
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun key(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build())
        return generator.generateKey()
    }

    private fun serialize(value: DeviceContext) = listOf(
        value.deviceId.toString(),
        value.childId.toString(),
        encodePart(value.accessToken),
        encodePart(value.refreshToken),
    ).joinToString("\n")

    private fun deserialize(value: String): DeviceContext {
        val parts = value.split('\n')
        require(parts.size == 4)
        return DeviceContext(parts[0].toLong(), parts[1].toLong(), decodePart(parts[2]), decodePart(parts[3]))
    }

    private fun encodePart(value: String) = Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

    private fun decodePart(value: String) = String(Base64.decode(value, Base64.NO_WRAP), StandardCharsets.UTF_8)

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tamdan.device.credentials.v1"
        private const val PREFERENCES = "tamdan_device_credentials"
        private const val CIPHERTEXT = "ciphertext"
        private const val NONCE = "nonce"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_BITS = 128
    }
}
