package com.example.purrytify.data.service

import android.util.Log
import kotlinx.coroutines.*
import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.remote.AuthService
import com.example.purrytify.domain.repository.AuthRepository
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshService @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authService: AuthService,
    private val authRepository: AuthRepository
) {

    companion object {
        private const val TAG = "TokenRefreshService"
        private val TOKEN_EXPIRY_CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(5)
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    fun start() {
        if (isRunning) {
            Log.d(TAG, "Token refresh service sudah berjalan.")
            return
        }
        isRunning = true
        Log.d(TAG, "Memulai layanan refresh token menggunakan Coroutine.")
        serviceScope.launch {
            while (isActive) {
                delay(TOKEN_EXPIRY_CHECK_INTERVAL)
                verifyAndRefreshToken()
            }
        }
    }

    fun stop() {
        Log.d(TAG, "Menghentikan layanan refresh token.")
        serviceScope.cancel()
        isRunning = false
    }

    private suspend fun verifyAndRefreshToken() {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()

        if (accessToken.isNullOrBlank()) {
            Log.d(TAG, "Access token tidak ditemukan. Menghentikan pengecekan.")
            stop()
            return
        }

        try {
            val response = authService.verifyToken("Bearer $accessToken")
            if (response.isSuccessful) {
                Log.d(TAG, "Access token masih valid.")
            } else if (response.code() == 403) {
                Log.d(TAG, "Access token expired. Attempting to refresh.")
                refreshToken(refreshToken)
            } else {
                Log.w(TAG, "Gagal memverifikasi token. Response code: ${response.code()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error saat memverifikasi token (HTTP): ${e.message()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat memverifikasi token: ${e.message}")
        }
    }

    private suspend fun refreshToken(oldRefreshToken: String?) {
        if (oldRefreshToken.isNullOrBlank()) {
            Log.w(TAG, "Refresh token tidak ditemukan. Mungkin perlu logout.")
            authRepository.logout()
            stop()
            return
        }

        try {
            val refreshResponse =
                authService.refreshToken(mapOf("refreshToken" to oldRefreshToken))
            if (refreshResponse.isSuccessful) {
                val responseBody = refreshResponse.body()
                val newAccessToken = responseBody?.accessToken
                val newRefreshToken = responseBody?.refreshToken

                if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                    Log.i(TAG, "Access token and refresh token refreshed successfully.")
                    tokenStorage.saveAccessToken(newAccessToken)
                    tokenStorage.saveRefreshToken(newRefreshToken)
                } else if (!newAccessToken.isNullOrBlank()) {
                    Log.i(TAG, "Access token refreshed successfully, refresh token not updated.")
                    tokenStorage.saveAccessToken(newAccessToken)
                } else {
                    Log.e(TAG, "Gagal mendapatkan access token baru dari refresh token.")
                    authRepository.logout()
                    stop()
                }
            } else {
                Log.e(
                    TAG,
                    "Gagal melakukan refresh token. Response code: ${refreshResponse.code()}"
                )
                authRepository.logout()
                stop()
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error saat melakukan refresh token (HTTP): ${e.message()}")
            authRepository.logout()
            stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat melakukan refresh token: ${e.message}")
            authRepository.logout()
            stop()
        }
    }
}