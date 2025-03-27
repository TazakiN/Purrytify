package com.example.purrytify.data.repository

import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.remote.UserRemoteDataSource
import com.example.purrytify.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val tokenStorage: TokenStorage
) : UserRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponseDTO> {
        return try {
            val result = userRemoteDataSource.login(email, password)
            result.onSuccess { response ->
                response.refreshToken?.let {
                    tokenStorage.saveRefreshToken(it)
                }
            }
            result
        } catch (e :Exception) {
            Result.failure(e)
        }
    }
}