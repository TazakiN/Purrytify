package com.example.purrytify.data.local

import androidx.room.*
import com.example.purrytify.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("SELECT * FROM songs WHERE username = :userName")
    fun getAllSongs(userName: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE username = :userName and isLiked = 1")
    fun getLikedSongs(userName: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE username = :userName ORDER BY createdAt DESC LIMIT 10")
    fun getNewSongs(userName: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE username = :userName and lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT 10")
    fun getRecentlyPlayed(userName: String): Flow<List<SongEntity>>

    @Query("UPDATE songs SET lastPlayed = :timestamp WHERE id = :songId")
    suspend fun updateLastPlayed(songId: Int, timestamp: Long)
}
