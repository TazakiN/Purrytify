package com.example.purrytify.data.repository

import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.remote.AuthRemoteDataSource
import com.example.purrytify.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponseDTO> {
        return try {
            val result = authRemoteDataSource.login(email, password)
            result.onSuccess { response ->
                response.refreshToken?.let {
                    tokenStorage.saveRefreshToken(it)
                }

                response.accessToken?.let {
                    tokenStorage.saveAccessToken(it)
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenStorage.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUsername(): String? {
        val token = tokenStorage.getAccessToken() ?: tokenStorage.getRefreshToken() ?: return null
        return decodeJwtUsername(token)
    }

    private fun decodeJwtUsername(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = String(
                android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
            )
            val json = org.json.JSONObject(payload)
            json.optString("username", null.toString()) // atau "email" kalau itu yang dipakai
        } catch (e: Exception) {
            null
        }
    }
}