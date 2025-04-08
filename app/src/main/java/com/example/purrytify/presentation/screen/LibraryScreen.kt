package com.example.purrytify.presentation.screen

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.fragments.DialogAddSong
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs.collectAsState()

    var showLikedOnly by remember { mutableStateOf(false) }

    val filteredSongs = if (showLikedOnly) {
        allSongs.filter { it.isLiked }
    } else {
        allSongs
    }

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_library, null, false)

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val addButton = view.findViewById<ImageButton>(R.id.btnAddSong)
            val btnAll = view.findViewById<Button>(R.id.btnAllSongs)
            val btnLiked = view.findViewById<Button>(R.id.btnLikedSongs)

            recyclerView.layoutManager = LinearLayoutManager(ctx)

            val adapter = SongAdapter(filteredSongs) { song ->
                try {
                    // Use MusicPlayerViewModel instead of creating a new MediaPlayer
                    musicPlayerViewModel.playSong(song)
                    Toast.makeText(ctx, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
                    viewModel.updateLastPlayed(song.id)
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Failed to play song: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

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
                adapter.updateSongs(allSongs)
                updateButtonStyles()
            }

            btnLiked.setOnClickListener {
                showLikedOnly = true
                adapter.updateSongs(allSongs.filter { it.isLiked })
                updateButtonStyles()
            }

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
        }
    )
}