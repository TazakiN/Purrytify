package com.example.purrytify.domain.repository

import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.model.RefreshTokenResponseDTO

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponseDTO>
    suspend fun logout(): Result<Unit>
}