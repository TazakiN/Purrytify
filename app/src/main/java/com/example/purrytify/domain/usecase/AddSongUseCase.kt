package com.example.purrytify.domain.usecase

import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.repository.SongRepository
import javax.inject.Inject

class AddSongUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(song: Song) {
        repository.insertSong(song)
    }
}
