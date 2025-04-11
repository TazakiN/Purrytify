package com.example.purrytify.data.remote

import com.example.purrytify.data.model.LoginRequestDTO
import com.example.purrytify.data.model.LoginResponseDTO
import com.google.gson.Gson
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authService: AuthService
) {
    suspend fun login(email: String, password: String): Result<LoginResponseDTO> {
        return try {
            val request = LoginRequestDTO(email, password)
            val response = authService.login(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    Gson().fromJson(errorBody, Map::class.java) as Map<String, String>
                } catch (e: Exception) {
                    null
                }
                val errorMessage = errorResponse?.get("error") ?: "Login failed with code: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }
}
