package com.example.purrytify.presentation.screen

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.fragments.DialogAddSong
import com.example.purrytify.presentation.fragments.DialogUpdateSong
import com.example.purrytify.presentation.fragments.SongContextMenu
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch

@SuppressLint("InflateParams")
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs.collectAsState()
    val queueSize by musicPlayerViewModel.queue.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showLikedOnly by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = allSongs
        .filter { if (showLikedOnly) it.isLiked else true }
        .filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }

    // Show context menu when a song is selected
    if (showContextMenu && selectedSong != null) {
        SongContextMenu(
            song = selectedSong!!,
            onDismiss = { showContextMenu = false },
            onPlay = { song ->
                musicPlayerViewModel.playSong(song)
                viewModel.updateLastPlayed(song.id)
                Toast.makeText(context, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddToQueue = { song ->
                coroutineScope.launch {
                    // Debug before adding to queue
                    Log.d("QueueDebug", "Before adding to queue: Queue size = ${queueSize.size}")

                    // Add to queue
                    musicPlayerViewModel.addToQueue(song)

                    // Show toast notification
                    Toast.makeText(context, "Added to queue: ${song.title}", Toast.LENGTH_SHORT).show()

                    // Debug after adding to queue
                    Log.d("QueueDebug", "After adding to queue: Expected size = ${queueSize.size + 1}")

                }
            },
            onEdit = { song ->
                (context as? AppCompatActivity)?.let {
                    DialogUpdateSong(song).show(it.supportFragmentManager, "UpdateSongDialog")
                }
            },
            onDelete = { song ->
                if (musicPlayerViewModel.currentSong.value?.id == song.id) {
                    musicPlayerViewModel.deleteSong(song)
                } else {
                    musicPlayerViewModel.deleteSong(song)
                }
                Toast.makeText(context, "Deleted: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onToggleFavorite = { song ->
                val updatedSong = song.copy(isLiked = !song.isLiked)
                viewModel.updateSong(updatedSong)
                musicPlayerViewModel.updateCurrentSongIfMatches(updatedSong)
            }
        )
    }

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_library, null, false)

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val addButton = view.findViewById<ImageButton>(R.id.btnAddSong)
            val btnAll = view.findViewById<Button>(R.id.btnAllSongs)
            val btnLiked = view.findViewById<Button>(R.id.btnLikedSongs)
            val searchView = view.findViewById<SearchView>(R.id.searchView)

            recyclerView.layoutManager = LinearLayoutManager(ctx)

            val adapter = SongAdapter(
                songs = filteredSongs,
                onItemClick = { song ->
                    try {
                        musicPlayerViewModel.playSong(song)
                        Toast.makeText(ctx, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
                        viewModel.updateLastPlayed(song.id)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Failed to play song: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                },
                onOptionsClick = { song, _ ->
                    // Show context menu for the song
                    selectedSong = song
                    showContextMenu = true

                    // Debug
                    Log.d("QueueDebug", "Context menu opened for song: ${song.title}")
                }
            )

            recyclerView.adapter = adapter

            fun updateButtonStyles() {
                if (showLikedOnly) {
                    btnLiked.setBackgroundColor(0xFF1DB954.toInt())
                    btnLiked.setTextColor(0xFF000000.toInt())
                    btnAll.setBackgroundColor(0xFF212121.toInt())
                    btnAll.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    btnAll.setBackgroundColor(0xFF1DB954.toInt())
                    btnAll.setTextColor(0xFF000000.toInt())
                    btnLiked.setBackgroundColor(0xFF212121.toInt())
                    btnLiked.setTextColor(0xFFFFFFFF.toInt())
                }
            }

            btnAll.setOnClickListener {
                showLikedOnly = false
                adapter.updateSongs(filteredSongs)
                updateButtonStyles()
            }

            btnLiked.setOnClickListener {
                showLikedOnly = true
                adapter.updateSongs(filteredSongs)
                updateButtonStyles()
            }

            searchView.queryHint = "Search by title or artist"
            searchView.setIconifiedByDefault(false)

            val searchText = searchView.findViewById<android.widget.EditText>(androidx.appcompat.R.id.search_src_text)
            searchText.setTextColor(Color.WHITE)
            searchText.setHintTextColor(Color.GRAY)

            val magIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
            magIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)

            val closeIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            closeIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)


            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    searchQuery = newText.orEmpty()
                    showLikedOnly = false  // Force switch to "All Songs" when searching
                    adapter.updateSongs(filteredSongs)

                    return true
                }
            })

            addButton.setOnClickListener {
                (ctx as? AppCompatActivity)?.let {
                    DialogAddSong().show(it.supportFragmentManager, "AddSongDialog")
                }
            }

            updateButtonStyles()
            view
        },
        update = { view ->
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val adapter = recyclerView.adapter as? SongAdapter
            adapter?.updateSongs(filteredSongs)

            val btnAll = view.findViewById<Button>(R.id.btnAllSongs)
            val btnLiked = view.findViewById<Button>(R.id.btnLikedSongs)

            if (showLikedOnly) {
                btnLiked.setBackgroundColor(0xFF1DB954.toInt())
                btnLiked.setTextColor(0xFF000000.toInt())
                btnAll.setBackgroundColor(0xFF212121.toInt())
                btnAll.setTextColor(0xFFFFFFFF.toInt())
            } else {
                btnAll.setBackgroundColor(0xFF1DB954.toInt())
                btnAll.setTextColor(0xFF000000.toInt())
                btnLiked.setBackgroundColor(0xFF212121.toInt())
                btnLiked.setTextColor(0xFFFFFFFF.toInt())
            }

            val searchView = view.findViewById<SearchView>(R.id.searchView)
            if (searchView.query.toString() != searchQuery) {
                searchView.setQuery(searchQuery, false)
            }
        }
    )
}
