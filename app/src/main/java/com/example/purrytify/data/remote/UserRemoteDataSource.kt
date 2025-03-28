package com.example.purrytify.data.remote

import com.example.purrytify.data.model.LoginRequestDTO
import com.example.purrytify.data.model.LoginResponseDTO
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val authService: AuthService
) {
    suspend fun login(email: String, password: String): Result<LoginResponseDTO> {
    return try {
        val request = LoginRequestDTO(email, password)
        val response = authService.login(request)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            throw Exception("Login failed with code: ${response.code()}")
        }
    } catch (e: Exception) {
        throw Exception("Network error: ${e.message}")
    }
}}
