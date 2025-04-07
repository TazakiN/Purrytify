package com.example.purrytify.domain.repository

import com.example.purrytify.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun insertSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSong(song: Song)
    fun getAllSongs(): Flow<List<Song>>
    fun getLikedSongs(): Flow<List<Song>>
}
