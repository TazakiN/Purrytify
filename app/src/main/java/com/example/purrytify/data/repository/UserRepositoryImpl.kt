package com.example.purrytify.data.repository

import com.example.purrytify.data.remote.RetrofitClient
import com.example.purrytify.data.remote.UserService
import com.example.purrytify.domain.model.User
import com.example.purrytify.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userService: UserService) : UserRepository {
    override suspend fun getUserProfile(): Result<User> {
        return try {
            val response = userService.getProfile()
            if (response.isSuccessful) {
                val profileResponse = response.body()!!
                val user: User = User(
                    id = profileResponse.id.toString(),
                    username = profileResponse.username,
                    email = profileResponse.email,
                    location = profileResponse.location,
                    profilePhoto = RetrofitClient.profilePictureUrlBuilder(profileResponse.profilePhoto),
                    createdAt = profileResponse.createdAt,
                    updatedAt = profileResponse.updatedAt,
                    songsCount = 135, // placeholder for testing
                    likedCount = 52,
                    listenedCount = 100
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch user profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}