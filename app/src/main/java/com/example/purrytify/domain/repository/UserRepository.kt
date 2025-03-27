package com.example.purrytify.domain.repository

import com.example.purrytify.data.model.LoginResponseDTO

interface UserRepository {
    suspend fun login(email: String, password: String): Result<LoginResponseDTO>
//    suspend fun logout(): Result<Unit>
//    suspend fun getUserDetails(): Result<UserDetails>
//    suspend fun updateUserDetails(userDetails: UserDetails): Result<Unit>
//    suspend fun deleteUserAccount(): Result<Unit>
}