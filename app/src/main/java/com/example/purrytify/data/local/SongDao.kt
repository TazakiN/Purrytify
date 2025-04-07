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
}
