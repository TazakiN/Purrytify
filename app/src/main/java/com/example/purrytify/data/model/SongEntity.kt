package com.example.purrytify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val artworkUri: String?,
    val songUri: String,
    val duration: Long,
    val isLiked: Boolean = false,
    val username: String,
    val lastPlayed: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var timesPlayed: Int = 0
)
