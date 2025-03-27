package com.example.purrytify.data.repository

import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.remote.UserRemoteDataSource
import com.example.purrytify.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponseDTO> {
        return userRemoteDataSource.login(email, password)
    }
}