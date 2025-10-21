package com.capstone.cropcare.data.local.preferences

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ALIAS = "token_key"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12 // 96 bits for GCM
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Obtiene o crea la clave AES en el Android Keystore
    private fun getSecretKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        // Si ya existe, se reutiliza
        keyStore.getKey(KEY_ALIAS, null)?.let {
            return it as SecretKey
        }

        // Si no, la creamos
        val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        val parameterSpec = android.security.keystore.KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    // Encripta texto con AES/GCM
    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray())
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    // Desencripta texto con AES/GCM
    private fun decrypt(encryptedText: String): String {
        val decoded = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = decoded.copyOfRange(0, IV_SIZE)
        val encryptedBytes = decoded.copyOfRange(IV_SIZE, decoded.size)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted)
    }

    // Guarda los tokens cifrados
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(refreshToken))
            .apply()
    }

    // Obtiene los tokens desencriptados
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        return decrypt(token)
    }

    fun getRefreshToken(): String? {
        val token = sharedPreferences.getString(KEY_REFRESH_TOKEN, null) ?: return null
        return decrypt(token)
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    fun hasValidTokens(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}
