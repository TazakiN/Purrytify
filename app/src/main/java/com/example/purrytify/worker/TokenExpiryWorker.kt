package com.example.purrytify.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.remote.AuthService
import com.example.purrytify.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class TokenExpiryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenStorage: TokenStorage,
    private val authService: AuthService,
    private val authRepository: AuthRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "TokenExpiryWorker"
    }

    override suspend fun doWork(): Result {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()

        if (accessToken.isNullOrBlank()) {
            Log.d(TAG, "Access token tidak ditemukan.")
            return Result.success() // Tidak ada token untuk dicek
        }

        try {
            val response = authService.verifyToken("Bearer $accessToken")
            if (response.isSuccessful) {
                Log.d(TAG, "Access token masih valid.")
                return Result.success()
            } else if (response.code() == 403) {
                Log.d(TAG, "Access token expired. Attempting to refresh.")
                if (refreshToken.isNullOrBlank()) {
                    Log.w(TAG, "Refresh token tidak ditemukan. Mungkin perlu logout.")
                    // Opsi 1: Lakukan logout (implementasi tergantung aplikasi Anda)
                    authRepository.logout()
                    return Result.failure()
                }

                try {
                    val refreshResponse =
                        authService.refreshToken(mapOf("refreshToken" to refreshToken))
                    if (refreshResponse.isSuccessful) {
                        val newAccessToken = refreshResponse.body()?.accessToken
                        if (!newAccessToken.isNullOrBlank()) {
                            Log.i(TAG, "Access token refreshed successfully.")
                            tokenStorage.saveAccessToken(newAccessToken)
                            return Result.success()
                        } else {
                            Log.e(TAG, "Gagal mendapatkan access token baru dari refresh token.")
                            // Opsi 2: Lakukan logout karena refresh token gagal
                            authRepository.logout()
                            return Result.failure()
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Gagal melakukan refresh token. Response code: ${refreshResponse.code()}"
                        )
                        // Opsi 3: Lakukan logout karena refresh token gagal
                         authRepository.logout()
                        return Result.failure()
                    }
                } catch (e: HttpException) {
                    Log.e(TAG, "Error saat melakukan refresh token (HTTP): ${e.message()}")
                    // Opsi 4: Lakukan logout karena error HTTP saat refresh token
                    authRepository.logout()
                    return Result.failure()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saat melakukan refresh token: ${e.message}")
                    // Opsi 5: Lakukan logout karena error umum saat refresh token
                    authRepository.logout()
                    return Result.failure()
                }
            } else {
                Log.w(TAG, "Gagal memverifikasi token. Response code: ${response.code()}")
                return Result.failure()
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error saat memverifikasi token (HTTP): ${e.message()}")
            return Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat memverifikasi token: ${e.message}")
            return Result.failure()
        }
    }
}