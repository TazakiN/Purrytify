package com.example.purrytify.data.local

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TokenStorage"

@Singleton
class TokenStorage @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKeyAlias: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: GeneralSecurityException) {
        Log.e(TAG, "Error initializing EncryptedSharedPreferences (Security):", e)
        null
    } catch (e: IOException) {
        Log.e(TAG, "Error initializing EncryptedSharedPreferences (IO):", e)
        null
    }

    // Refresh token management
    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences?.edit(commit = false) {
            putString("refresh_token", refreshToken)
        }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences?.getString("refresh_token", null)
    }

    fun deleteRefreshToken() {
        sharedPreferences?.edit(commit = false) {
            remove("refresh_token")
        }
    }

    // Access token management
    private var accessToken: String? = null

    fun saveAccessToken(token: String) {
        accessToken = token
    }

    fun getAccessToken(): String? {
        return accessToken
    }

    fun deleteAccessToken() {
        accessToken = null
    }

}