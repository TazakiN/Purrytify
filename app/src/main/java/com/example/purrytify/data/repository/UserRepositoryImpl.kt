package com.example.purrytify.data.repository

import com.example.purrytify.data.remote.RetrofitClient
import com.example.purrytify.data.remote.UserService
import com.example.purrytify.domain.model.User
import com.example.purrytify.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val songRepository: SongRepositoryImpl) : UserRepository {
    override suspend fun getUserProfile(): Result<User> {

        return try {
            val songsCount = songRepository.getAllSongsCount().firstOrNull() ?: 0
            val likedCount = songRepository.getLikedSongsCount().firstOrNull() ?: 0
            val listenedCount = songRepository.getPlayedSongsCount().firstOrNull() ?: 0

            val response = userService.getProfile()
            if (response.isSuccessful) {
                val profileResponse = response.body()!!
                val user = User(
                    id = profileResponse.id.toString(),
                    username = profileResponse.username,
                    email = profileResponse.email,
                    location = profileResponse.location,
                    profilePhoto = RetrofitClient.profilePictureUrlBuilder(profileResponse.profilePhoto),
                    createdAt = profileResponse.createdAt,
                    updatedAt = profileResponse.updatedAt,
                    songsCount = songsCount,
                    likedCount = likedCount,
                    listenedCount = listenedCount
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