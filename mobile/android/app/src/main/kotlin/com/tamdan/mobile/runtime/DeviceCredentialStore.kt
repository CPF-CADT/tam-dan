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

    fun saveChildPairing(
        credential: String,
        deviceId: Long,
        childId: Long,
        displayName: String,
        platform: String,
    ) {
        require(credential.isNotBlank() && deviceId > 0 && childId > 0)
        val value = listOf(
            encodePart(credential),
            deviceId.toString(),
            childId.toString(),
            encodePart(displayName),
            encodePart(platform),
        ).joinToString("\n")
        writeEncrypted(CHILD_CIPHERTEXT, CHILD_NONCE, value)
    }

    fun readChildPairing(): Map<String, Any>? = try {
        val value = readEncrypted(CHILD_CIPHERTEXT, CHILD_NONCE) ?: return null
        val parts = value.split('\n')
        require(parts.size == 5)
        mapOf(
            "credential" to decodePart(parts[0]),
            "deviceId" to parts[1].toInt(),
            "childId" to parts[2].toInt(),
            "displayName" to decodePart(parts[3]),
            "platform" to decodePart(parts[4]),
        )
    } catch (_: Exception) {
        preferences.edit().remove(CHILD_CIPHERTEXT).remove(CHILD_NONCE).apply()
        null
    }

    fun saveChildState(value: String) = writeEncrypted(STATE_CIPHERTEXT, STATE_NONCE, value)

    fun readChildState(): String? = try {
        readEncrypted(STATE_CIPHERTEXT, STATE_NONCE)
    } catch (_: Exception) {
        preferences.edit().remove(STATE_CIPHERTEXT).remove(STATE_NONCE).apply()
        null
    }

    fun saveSession(accessToken: String, refreshToken: String) {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        writeEncrypted(SESSION_CIPHERTEXT, SESSION_NONCE, listOf(
            encodePart(accessToken),
            encodePart(refreshToken),
        ).joinToString("\n"))
    }

    fun readSession(): Map<String, String>? = try {
        val value = readEncrypted(SESSION_CIPHERTEXT, SESSION_NONCE) ?: return null
        val parts = value.split('\n')
        require(parts.size == 2)
        mapOf("accessToken" to decodePart(parts[0]), "refreshToken" to decodePart(parts[1]))
    } catch (_: Exception) {
        preferences.edit().remove(SESSION_CIPHERTEXT).remove(SESSION_NONCE).apply()
        null
    }

    fun clearSession() {
        preferences.edit().remove(SESSION_CIPHERTEXT).remove(SESSION_NONCE).apply()
    }

    fun clearChildDeviceContext() {
        preferences.edit()
            .remove(CHILD_CIPHERTEXT)
            .remove(CHILD_NONCE)
            .remove(STATE_CIPHERTEXT)
            .remove(STATE_NONCE)
            .apply()
    }

    fun saveValue(name: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        preferences.edit()
            .putString(valueKey(name), Base64.encodeToString(cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP))
            .putString(nonceKey(name), Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun readValue(name: String): String? = try {
        val ciphertext = preferences.getString(valueKey(name), null) ?: return null
        val nonce = preferences.getString(nonceKey(name), null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, Base64.decode(nonce, Base64.NO_WRAP)))
        }
        String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)), StandardCharsets.UTF_8)
    } catch (_: Exception) {
        clearValue(name)
        null
    }

    fun clearValue(name: String) {
        preferences.edit().remove(valueKey(name)).remove(nonceKey(name)).apply()
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

    private fun writeEncrypted(ciphertextKey: String, nonceKey: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        preferences.edit()
            .putString(ciphertextKey, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(nonceKey, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    private fun readEncrypted(ciphertextKey: String, nonceKey: String): String? {
        val ciphertext = preferences.getString(ciphertextKey, null)
        val nonce = preferences.getString(nonceKey, null)
        if (ciphertext == null && nonce == null) return null
        require(ciphertext != null && nonce != null)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, Base64.decode(nonce, Base64.NO_WRAP)))
        }
        return String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)), StandardCharsets.UTF_8)
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

    private fun valueKey(name: String) = "value.$name"
    private fun nonceKey(name: String) = "nonce.$name"

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tamdan.device.credentials.v1"
        private const val PREFERENCES = "tamdan_device_credentials"
        private const val CIPHERTEXT = "ciphertext"
        private const val NONCE = "nonce"
        private const val CHILD_CIPHERTEXT = "child_ciphertext"
        private const val CHILD_NONCE = "child_nonce"
        private const val STATE_CIPHERTEXT = "child_state_ciphertext"
        private const val STATE_NONCE = "child_state_nonce"
        private const val SESSION_CIPHERTEXT = "session_ciphertext"
        private const val SESSION_NONCE = "session_nonce"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_BITS = 128
    }
}
