package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.usecase.AddSongUseCase
import com.example.purrytify.domain.usecase.UpdateSongUseCase
import com.example.purrytify.domain.usecase.LoadSongUseCase
import com.example.purrytify.domain.usecase.UpdateLastPlayedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    loadSongUseCase: LoadSongUseCase,
    private val addSongUseCase: AddSongUseCase,
    private val updateSongUseCase: UpdateSongUseCase,
    private val updateLastPlayedUseCase: UpdateLastPlayedUseCase
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = loadSongUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSong(song: Song, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                addSongUseCase(song)
                onResult(Result.success(Unit))
            } catch (e: IllegalArgumentException) {
                onResult(Result.failure(e))
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(Result.failure(e))
            }
        }
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            try {
                updateSongUseCase(song)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateLastPlayed(songId: Int) {
        viewModelScope.launch {
            try {
                updateLastPlayedUseCase(songId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
