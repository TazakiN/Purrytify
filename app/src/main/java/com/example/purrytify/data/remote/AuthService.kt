package com.example.purrytify.data.remote

import com.example.purrytify.data.model.LoginRequestDTO
import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.data.model.RefreshTokenResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginResponseDTO>

    @POST("/api/refresh-token")
    suspend fun refreshToken(@Body request: Map<String, String>): Response<RefreshTokenResponseDTO>

    @GET("/api/verify-token")
    suspend fun verifyToken(@Header("Authorization") authToken: String): Response<Unit>
}