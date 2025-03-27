package com.example.purrytify.data.remote

import com.example.purrytify.data.model.LoginRequestDTO
import com.example.purrytify.data.model.LoginResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginResponseDTO>
}