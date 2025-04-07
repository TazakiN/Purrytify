package com.example.purrytify.domain.repository

import com.example.purrytify.domain.model.User

interface UserRepository {
    suspend fun getUserProfile(): Result<User>
}