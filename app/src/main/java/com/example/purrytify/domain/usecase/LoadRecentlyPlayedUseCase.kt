package com.example.purrytify.domain.usecase

import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadRecentlyPlayedUseCase @Inject constructor(
    private val repository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getRecentlyPlayed()
    }
}
