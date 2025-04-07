package com.example.purrytify.domain.model

data class User(
    val username: String,
    val country: String,
    val songsCount: Int,
    val likedCount: Int,
    val listenedCount: Int,
    val profileImageUrl: String? = null
)