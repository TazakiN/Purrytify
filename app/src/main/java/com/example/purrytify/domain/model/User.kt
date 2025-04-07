package com.example.purrytify.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val location: String,
    val profilePhoto: String,
    val createdAt: String,
    val updatedAt: String,
    val songsCount: Int,
    val likedCount: Int,
    val listenedCount: Int,
)