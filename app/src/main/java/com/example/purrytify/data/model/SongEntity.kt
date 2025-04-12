package com.example.purrytify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(
    tableName = "songs",
    indices = [Index(value = ["username", "title", "artist"], unique = true)]
)
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
    val createdAt: Long = System.currentTimeMillis()
)
