package com.example.purrytify.domain.usecase

import com.example.purrytify.domain.repository.SongRepository
import javax.inject.Inject

class UpdateLastPlayedUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(songId: Int, timestamp: Long = System.currentTimeMillis()) {
        repository.updateLastPlayed(songId, timestamp)
    }
}
