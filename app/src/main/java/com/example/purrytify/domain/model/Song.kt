package com.example.purrytify.domain.model

data class Song(
    val id: Int = 0,
    val title: String,
    val artist: String,
    val artworkUri: String?,
    val songUri: String,
    val duration: Long,
    val isLiked: Boolean = false,
    val username: String
)
