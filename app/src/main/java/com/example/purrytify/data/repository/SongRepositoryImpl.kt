package com.example.purrytify.data.repository

import com.example.purrytify.data.local.SongDao
import com.example.purrytify.data.mapper.toDomain
import com.example.purrytify.data.mapper.toEntity
import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.repository.AuthRepository
import com.example.purrytify.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val dao: SongDao,
    private val authRepository: AuthRepository
) : SongRepository {

    private fun getCurrentUsername(): String? {
        return authRepository.getUsername()
    }

    override suspend fun insertSong(song: Song) {
        val username = getCurrentUsername()
        if (username != null) {
            dao.insertSong(song.copy(username = username).toEntity())
        } else {
            throw IllegalStateException("User must be logged in to add a song.")
        }
    }

    override suspend fun updateSong(song: Song) {
        dao.updateSong(song.toEntity())
    }

    override suspend fun deleteSong(song: Song) {
        dao.deleteSong(song.toEntity())
    }

    override fun getAllSongs(): Flow<List<Song>> {
        return getCurrentUsername()?.let { username ->
            dao.getAllSongs(username).map { list -> list.map { it.toDomain() } }
        } ?: emptyFlow()
    }

    override fun getLikedSongs(): Flow<List<Song>> {
        return getCurrentUsername()?.let { username ->
            dao.getLikedSongs(username).map { list -> list.map { it.toDomain() } }
        } ?: emptyFlow()
    }

    override fun getNewSongs(): Flow<List<Song>> {
        return getCurrentUsername()?.let { username ->
            dao.getNewSongs(username).map { list -> list.map { it.toDomain() } }
        } ?: emptyFlow()
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return getCurrentUsername()?.let { username ->
            dao.getRecentlyPlayed(username).map { list -> list.map { it.toDomain() } }
        } ?: emptyFlow()
    }

    override suspend fun updateLastPlayed(songId: Int, timestamp: Long) {
        dao.updateLastPlayed(songId, timestamp)
    }
}