package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.usecase.LoadRecentlyPlayedUseCase
import com.example.purrytify.domain.usecase.LoadNewSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    loadRecentlyPlayedUseCase: LoadRecentlyPlayedUseCase,
    loadNewSongsUseCase: LoadNewSongsUseCase
) : ViewModel() {

    val recentlyPlayed: StateFlow<List<Song>> = loadRecentlyPlayedUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val newSongs: StateFlow<List<Song>> = loadNewSongsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
