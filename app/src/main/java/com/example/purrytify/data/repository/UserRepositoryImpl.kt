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
                    profilePhoto = sanitizeProfilePhoto(profileResponse.profilePhoto),
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

    private fun sanitizeProfilePhoto(photoPath: String?): String {
        if (photoPath.isNullOrBlank()) return ""

        // Remove "..", "/", "\", and allow only safe characters
        val sanitizedPath = photoPath
            .replace(Regex("""[.]{2,}"""), "")        // remove ".."
            // temporary comment takutnya error karena dari server
            // .replace(Regex("""[\\/]+"""), "")         // remove slashes
            // .replace(Regex("""[^a-zA-Z0-9_.-]"""), "") // allow only safe characters

        return RetrofitClient.profilePictureUrlBuilder(sanitizedPath)
    }
}