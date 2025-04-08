package com.example.purrytify.presentation.screen

import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.fragments.DialogAddSong
import com.example.purrytify.presentation.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs.collectAsState()

    // Filter mode state
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
            val addButton = view.findViewById<Button>(R.id.btnAddSong)

            val btnAll = view.findViewById<Button>(R.id.btnAllSongs)
            val btnLiked = view.findViewById<Button>(R.id.btnLikedSongs)

            recyclerView.layoutManager = LinearLayoutManager(ctx)

            val adapter = SongAdapter(filteredSongs) { song ->
                try {
                    val uri = Uri.parse(song.songUri)
                    val afd = ctx.contentResolver.openAssetFileDescriptor(uri, "r")
                    if (afd != null) {
                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.setDataSource(afd.fileDescriptor)
                        afd.close()
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        Toast.makeText(ctx, "Memutar: ${song.title}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "Gagal membuka file audio", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Gagal memutar lagu: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            recyclerView.adapter = adapter

            btnAll.setOnClickListener {
                showLikedOnly = false
                adapter.updateSongs(allSongs)
            }

            btnLiked.setOnClickListener {
                showLikedOnly = true
                adapter.updateSongs(allSongs.filter { it.isLiked })
            }

            addButton.setOnClickListener {
                (ctx as? AppCompatActivity)?.let {
                    DialogAddSong().show(it.supportFragmentManager, "AddSongDialog")
                }
            }

            view
        },
        update = { view ->
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val adapter = recyclerView.adapter as? SongAdapter
            adapter?.updateSongs(filteredSongs)
        }
    )
}