package com.example.purrytify.data.repository

import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.model.RefreshTokenResponseDTO
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
}