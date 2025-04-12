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

    init {
        // Load all songs to have an ordered list for next/previous functionality
        viewModelScope.launch {
            songRepository.getAllSongs().collectLatest { songs ->
                _allSongs.value = songs
                Log.d("QueueDebug", "All songs loaded. Count: ${songs.size}")

                // If a song is currently playing, update its data if it exists in the new list
                _currentSong.value?.let { currentSong ->
                    val updatedSong = songs.find { it.id == currentSong.id }
                    if (updatedSong != null && updatedSong != currentSong) {
                        _currentSong.value = updatedSong
                    }
                }

                // Update any queued songs too
                if (_queue.value.isNotEmpty()) {
                    Log.d("QueueDebug", "Updating queue from loaded songs.")
                    val updatedQueue = _queue.value.map { queuedSong ->
                        songs.find { it.id == queuedSong.id } ?: queuedSong
                    }
                    _queue.value = updatedQueue
                }
            }
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
                    Log.d("QueueDebug", "Added song to queue: ${song.title}. Queue size now: ${currentQueue.size}")
                } else {
                    Log.d("QueueDebug", "Song already in queue, not adding again: ${song.title}")
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
                val initialSize = currentQueue.size
                currentQueue.removeAll { it.id == song.id }
                _queue.value = currentQueue

                // Add more detailed logging
                Log.d("QueueDebug", "Removed song from queue: ${song.title}. Queue size: $initialSize -> ${currentQueue.size}")
                if (currentQueue.isEmpty() && _showQueue.value) {
                    // Optionally auto-close empty queue view
                    Log.d("QueueDebug", "Queue is now empty, consider closing queue view")
                }
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error removing song from queue: ${e.message}", e)
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            try {
                _queue.value = emptyList()
                Log.d("QueueDebug", "Queue cleared. Size now: 0")
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
                    Log.w("QueueDebug", "Invalid reorder positions. From: $fromPosition, To: $toPosition, Size: ${_queue.value.size}")
                    return@launch
                }

                val currentQueue = _queue.value.toMutableList()
                val movedSong = currentQueue.removeAt(fromPosition)
                currentQueue.add(toPosition, movedSong)
                _queue.value = currentQueue
                Log.d("QueueDebug", "Reordered queue. Moved song: ${movedSong.title} from $fromPosition to $toPosition")
            } catch (e: Exception) {
                Log.e("QueueDebug", "Error reordering queue: ${e.message}", e)
            }
        }
    }

    fun toggleQueueVisibility() {
        Log.d("QueueDebug", "Before toggle - Queue size: ${_queue.value.size}, Visibility: ${_showQueue.value}")

        // Toggle visibility state
        _showQueue.value = !_showQueue.value

        Log.d("QueueDebug", "After toggle - Queue size: ${_queue.value.size}, Visibility: ${_showQueue.value}")

        if (_queue.value.isNotEmpty()) {
            _queue.value.forEachIndexed { index, song ->
                Log.d("QueueDebug", "Queue item $index: ${song.title} (ID: ${song.id})")
            }
        }
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
                            Log.d("QueueDebug", "Started playing song: ${song.title}")
                        }
                    }
                }
            } catch (e: Exception) {
                _missingFileSong.value = song
                Log.e("QueueDebug", "File missing or inaccessible for song: ${song.title}", e)

                // Reset these for safety
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
                    Log.d("QueueDebug", "Currently playing song deleted and playback stopped")
                }

                val initialQueueSize = _queue.value.size
                val newQueue = _queue.value.filter { it.id != song.id }
                if (newQueue.size != initialQueueSize) {
                    _queue.value = newQueue
                    Log.d("QueueDebug", "Deleted song removed from queue. Queue size: $initialQueueSize -> ${newQueue.size}")
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
            Log.d("QueueDebug", "Updated current song: ${updatedSong.title}")
        }

        // Also update in queue if present
        if (_queue.value.any { it.id == updatedSong.id }) {
            val updatedQueue = _queue.value.map {
                if (it.id == updatedSong.id) updatedSong else it
            }
            _queue.value = updatedQueue
            Log.d("QueueDebug", "Updated song in queue: ${updatedSong.title}")
        }
    }

    fun playNextSong() {
        // First check if there's anything in the queue
        if (_queue.value.isNotEmpty()) {
            // Play the first song in the queue
            val nextSong = _queue.value.first()
            Log.d("QueueDebug", "Playing next song from queue: ${nextSong.title}")

            // Remove the played song from the queue before playing it
            // to avoid potential race conditions
            val updatedQueue = _queue.value.toMutableList()
            updatedQueue.removeAt(0)
            _queue.value = updatedQueue

            playSong(nextSong)
            return
        }

        // If no queue, fallback to the default sequential behavior
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
            try {
                if (isPlaying) {
                    stop()
                }
                release()
                Log.d("QueueDebug", "MediaPlayer released")
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

    // Debug helper function to log the current queue state
    fun debugQueueState() {
        Log.d("QueueDebug", "==== QUEUE STATE DEBUG ====")
        Log.d("QueueDebug", "Current queue size: ${_queue.value.size}")
        Log.d("QueueDebug", "Queue contents: ${_queue.value.map { it.title }}")
        Log.d("QueueDebug", "Queue visibility: ${_showQueue.value}")
        Log.d("QueueDebug", "Current song: ${_currentSong.value?.title}")
        Log.d("QueueDebug", "Is playing: ${_isPlaying.value}")
        Log.d("QueueDebug", "==========================")
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
        Log.d("QueueDebug", "MusicPlayerViewModel cleared")
    }
}