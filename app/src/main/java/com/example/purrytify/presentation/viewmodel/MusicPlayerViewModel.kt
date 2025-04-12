package com.example.purrytify.presentation.viewmodel

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.domain.model.Song
import com.example.purrytify.domain.repository.SongRepository
import com.example.purrytify.domain.usecase.UpdateLastPlayedUseCase
import com.example.purrytify.domain.usecase.UpdateSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val updateSongUseCase: UpdateSongUseCase,
    private val updateLastPlayedUseCase: UpdateLastPlayedUseCase,
    private val songRepository: SongRepository,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var updateProgressJob: Job? = null

    // StateFlow for current song
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    // StateFlow for all songs in order
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())

    // StateFlow for player state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // StateFlow for progress
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    // StateFlow for duration
    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration

    // StateFlow for showing full player
    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer: StateFlow<Boolean> = _showFullPlayer

    // StateFlow for showing options dialog (titik tiga yg di atas)
    private val _showOptionsDialog = MutableStateFlow(false)
    val showOptionsDialog: StateFlow<Boolean> = _showOptionsDialog

    // New StateFlow to indicate that a file is missing
    private val _missingFileSong = MutableStateFlow<Song?>(null)
    val missingFileSong: StateFlow<Song?> = _missingFileSong

    init {
        // Load all songs to have an ordered list for next/previous functionality
        viewModelScope.launch {
            songRepository.getAllSongs().collectLatest { songs ->
                _allSongs.value = songs

                // If a song is currently playing, update its data if it exists in the new list
                _currentSong.value?.let { currentSong ->
                    val updatedSong = songs.find { it.id == currentSong.id }
                    if (updatedSong != null && updatedSong != currentSong) {
                        _currentSong.value = updatedSong
                    }
                }
            }
        }
    }

    fun playSong(song: Song) {
        try {
            // Release any existing MediaPlayer
            releaseMediaPlayer()

            // First, check if the file exists and is accessible
            val uri = Uri.parse(song.songUri)
            try {
                // Try to open the file to see if it's accessible
                val afd = contentResolver.openAssetFileDescriptor(uri, "r")

                if (afd != null) {
                    // File exists, proceed with playback
                    afd.close()

                    // Create new MediaPlayer
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(contentResolver.openAssetFileDescriptor(uri, "r")?.fileDescriptor)
                        prepare()

                        // Update song info
                        _currentSong.value = song
                        _totalDuration.value = duration / 1000 // Convert to seconds

                        // Start playing and update state
                        start()
                        _isPlaying.value = true

                        // Start progress tracking
                        startProgressTracking()

                        // Next song otomatis (autoplay)
                        setOnCompletionListener {
                            playNextSong()
                        }

                        // Update last played timestamp in the database
                        viewModelScope.launch {
                            updateLastPlayedUseCase(song.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // File is missing or inaccessible
                _missingFileSong.value = song

                // Reset these for safety
                _currentSong.value = null
                _isPlaying.value = false

                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle other errors
        }
    }

    fun resetMissingFileState() {
        _missingFileSong.value = null
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                stopProgressTracking()
            } else {
                it.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun seekTo(seconds: Int) {
        mediaPlayer?.seekTo(seconds * 1000) // Convert to milliseconds
        _currentPosition.value = seconds
    }

    fun toggleFavorite() {
        val song = _currentSong.value ?: return
        val updatedSong = song.copy(isLiked = !song.isLiked)

        viewModelScope.launch {
            updateSongUseCase(updatedSong)
            _currentSong.value = updatedSong
        }
    }

    fun togglePlayerView() {
        _showFullPlayer.value = !_showFullPlayer.value
    }

    fun toggleOptionsDialog() {
        _showOptionsDialog.value = !_showOptionsDialog.value
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            try {
                // If this is the currently playing song, stop playback first
                if (_currentSong.value?.id == song.id) {
                    releaseMediaPlayer()
                    _currentSong.value = null
                }

                // Delete the song from the repository
                songRepository.deleteSong(song)

                // Close dialog after deletion
                _showOptionsDialog.value = false

                // Reset missing file state if this was the missing file
                if (_missingFileSong.value?.id == song.id) {
                    _missingFileSong.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    fun updateCurrentSongIfMatches(updatedSong: Song) {
        if (_currentSong.value?.id == updatedSong.id) {
            _currentSong.value = updatedSong
        }
    }

    fun playNextSong() {
        val currentSong = _currentSong.value ?: return
        val currentSongIndex = _allSongs.value.indexOfFirst { it.id == currentSong.id }

        if (currentSongIndex != -1 && currentSongIndex < _allSongs.value.size - 1) {
            // There is a next song, play it
            val nextSong = _allSongs.value[currentSongIndex + 1]
            playSong(nextSong)
        } else if (currentSongIndex != -1 && _allSongs.value.isNotEmpty()) {
            // We're at the end, loop back to the first song
            val firstSong = _allSongs.value[0]
            playSong(firstSong)
        }
    }

    fun playPreviousSong() {
        val currentSong = _currentSong.value ?: return
        val currentSongIndex = _allSongs.value.indexOfFirst { it.id == currentSong.id }

        if (currentSongIndex > 0) {
            // There is a previous song, play it
            val previousSong = _allSongs.value[currentSongIndex - 1]
            playSong(previousSong)
        } else if (_allSongs.value.isNotEmpty()) {
            // We're at the beginning, loop back to the last song
            val lastSong = _allSongs.value.last()
            playSong(lastSong)
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        updateProgressJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    mediaPlayer?.let {
                        _currentPosition.value = it.currentPosition / 1000 // Convert to seconds
                    }
                    delay(1000) // Update every second
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun stopProgressTracking() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        stopProgressTracking()
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }
}