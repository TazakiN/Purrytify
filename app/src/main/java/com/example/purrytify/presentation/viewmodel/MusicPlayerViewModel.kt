package com.example.purrytify.presentation.viewmodel

import android.content.ContentResolver
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
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
    private var shuffledSongs: List<Song> = emptyList()
    private var currentShuffledIndex: Int = -1

    // StateFlow for current song
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    // StateFlow for all songs in order
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())

    // StateFlow for queue
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue

    // StateFlow to show if queue UI is visible
    private val _showQueue = MutableStateFlow(false)
    val showQueue: StateFlow<Boolean> = _showQueue

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

    // StateFlow for shuffle state
    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled

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

                // Update any queued songs too
                if (_queue.value.isNotEmpty()) {
                    val updatedQueue = _queue.value.map { queuedSong ->
                        songs.find { it.id == queuedSong.id } ?: queuedSong
                    }
                    _queue.value = updatedQueue
                }

                // If shuffle is enabled, regenerate the shuffled list
                if (_isShuffled.value && songs.isNotEmpty()) {
                    regenerateShuffledList(songs, _currentSong.value)
                }
            }
        }
    }

    private fun regenerateShuffledList(songs: List<Song>, currentSong: Song?) {
        shuffledSongs = songs.filter { it.id != currentSong?.id }.shuffled()
        currentShuffledIndex = -1 // Reset index
        if (currentSong != null) {
            shuffledSongs = listOf(currentSong) + shuffledSongs
            currentShuffledIndex = 0
        }
    }

    fun addToQueue(song: Song) {
        viewModelScope.launch {
            try {
                val currentQueue = _queue.value.toMutableList()
                // Check if the song is already in the queue to avoid duplicates
                if (!currentQueue.any { it.id == song.id }) {
                    currentQueue.add(song)
                    _queue.value = currentQueue
                }
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error adding song to queue: ${e.message}", e)
            }
        }
    }

    fun removeFromQueue(song: Song) {
        viewModelScope.launch {
            try {
                val currentQueue = _queue.value.toMutableList()
                currentQueue.removeAll { it.id == song.id }
                _queue.value = currentQueue
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error removing song from queue: ${e.message}", e)
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            try {
                _queue.value = emptyList()
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error clearing queue: ${e.message}", e)
            }
        }
    }

    fun reorderQueue(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            try {
                if (fromPosition < 0 || toPosition < 0 ||
                    fromPosition >= _queue.value.size ||
                    toPosition >= _queue.value.size) {
                    return@launch
                }

                val currentQueue = _queue.value.toMutableList()
                val movedSong = currentQueue.removeAt(fromPosition)
                currentQueue.add(toPosition, movedSong)
                _queue.value = currentQueue
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error reordering queue: ${e.message}", e)
            }
        }
    }

    fun toggleQueueVisibility() {
        _showQueue.value = !_showQueue.value
    }

    fun playSong(song: Song) {
        try {
            // Release any existing MediaPlayer
            releaseMediaPlayer()

            val uri = song.songUri.toUri()
            try {
                val afd = contentResolver.openAssetFileDescriptor(uri, "r")

                if (afd != null) {
                    afd.close()

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(contentResolver.openAssetFileDescriptor(uri, "r")?.fileDescriptor)
                        prepare()

                        // Update song info
                        val updatedSong = song.copy(lastPlayed = System.currentTimeMillis())
                        _currentSong.value = updatedSong
                        _totalDuration.value = duration / 1000 // Convert to seconds

                        start()
                        _isPlaying.value = true

                        startProgressTracking()

                        setOnCompletionListener {
                            playNextSong()
                        }

                        viewModelScope.launch {
                            updateLastPlayedUseCase(song.id)
                        }
                    }
                }
            } catch (e: Exception) {
                _missingFileSong.value = song
                _currentSong.value = null
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
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
        val updatedSong = song.copy(
            isLiked = !song.isLiked,
            lastPlayed = song.lastPlayed
        )

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
                if (_currentSong.value?.id == song.id) {
                    releaseMediaPlayer()
                    _currentSong.value = null
                }

                val initialQueueSize = _queue.value.size
                val newQueue = _queue.value.filter { it.id != song.id }
                if (newQueue.size != initialQueueSize) {
                    _queue.value = newQueue
                }

                songRepository.deleteSong(song)

                _showOptionsDialog.value = false

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

        // Also update in queue if present
        if (_queue.value.any { it.id == updatedSong.id }) {
            val updatedQueue = _queue.value.map {
                if (it.id == updatedSong.id) updatedSong else it
            }
            _queue.value = updatedQueue
        }
    }

    fun playNextSong() {
        if (_queue.value.isNotEmpty()) {
            // Shuffle the current queue, excluding the currently playing song
            val currentSongInQueue = _currentSong.value?.let { _queue.value.find { queueSong -> queueSong.id == it.id } }
            val songsToShuffle = if (currentSongInQueue != null) {
                _queue.value.filter { it.id != currentSongInQueue.id }.shuffled()
            } else {
                _queue.value.shuffled()
            }

            if (songsToShuffle.isNotEmpty()) {
                val nextSong = songsToShuffle.first()
                val updatedQueue = _queue.value.toMutableList()
                updatedQueue.remove(nextSong) // Remove the played song
                _queue.value = updatedQueue
                playSong(nextSong)
                return
            } else if (_currentSong.value != null) {
                releaseMediaPlayer()
                _isPlaying.value = false
                if (_allSongs.value.isNotEmpty()) {
                    playSong(_allSongs.value.first()) // main lagu pertama kalo abis
                }
                return
            }
        }

        if (_isShuffled.value && _allSongs.value.isNotEmpty()) {
            val currentSongId = _currentSong.value?.id
            val nextShuffledSong = _allSongs.value.filter { it.id != currentSongId }.randomOrNull()
            if (nextShuffledSong != null) {
                playSong(nextShuffledSong)
                return
            }
        } else {
            _currentSong.value?.let { currentSong ->
                val currentSongIndex = _allSongs.value.indexOfFirst { it.id == currentSong.id }
                if (currentSongIndex != -1 && currentSongIndex < _allSongs.value.size - 1) {
                    val nextSong = _allSongs.value[currentSongIndex + 1]
                    playSong(nextSong)
                } else if (_allSongs.value.isNotEmpty()) {
                    val firstSong = _allSongs.value[0]
                    playSong(firstSong)
                }
            }
        }
    }

    fun playPreviousSong() {

        if (_isShuffled.value && _allSongs.value.isNotEmpty()) {
            val currentSong = _currentSong.value
            if (currentSong != null) {
                val shuffledList = _allSongs.value.shuffled()
                val currentIndex = shuffledList.indexOf(currentSong)
                if (currentIndex > 0) {
                    playSong(shuffledList[currentIndex - 1])
                    return
                } else if (shuffledList.isNotEmpty()) {
                    playSong(shuffledList.last())
                    return
                }
            }
        } else {
            _currentSong.value?.let { currentSong ->
                val currentSongIndex = _allSongs.value.indexOfFirst { it.id == currentSong.id }
                if (currentSongIndex > 0) {
                    val previousSong = _allSongs.value[currentSongIndex - 1]
                    playSong(previousSong)
                } else if (_allSongs.value.isNotEmpty()) {
                    val lastSong = _allSongs.value.last()
                    playSong(lastSong)
                }
            }
        }
    }

    fun toggleShuffle() {
        _isShuffled.value = !_isShuffled.value
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
            try {
                if (isPlaying) {
                    stop()
                }
                release()
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error releasing MediaPlayer: ${e.message}", e)
            }
        }
        mediaPlayer = null
        stopProgressTracking()
    }

    fun resetMissingFileState() {
        _missingFileSong.value = null
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

    fun refreshCurrentSong(updatedSong: Song) {
        val current = _currentSong.value
        if (current?.id == updatedSong.id) {
            _currentSong.value = updatedSong
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }
}