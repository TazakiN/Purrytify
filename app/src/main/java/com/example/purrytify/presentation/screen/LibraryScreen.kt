package com.example.purrytify.presentation.screen

import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val songs by viewModel.allSongs.collectAsState()

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_library, null, false)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val addButton = view.findViewById<Button>(R.id.btnAddSong)

            recyclerView.layoutManager = LinearLayoutManager(ctx)
            val adapter = SongAdapter(songs)
            recyclerView.adapter = adapter

            addButton.setOnClickListener {
                val dummySong = Song(
                    title = "Judul Lagu Baru",
                    artist = "Artist Baru",
                    artworkUri = null,
                    songUri = "file:///android_asset/sample.mp3",
                    duration = 200_000L,
                    isLiked = false,
                    username = "test"
                )
                viewModel.addSong(dummySong)
                Toast.makeText(ctx, "Lagu ditambahkan!", Toast.LENGTH_SHORT).show()
            }

            view
        },
        update = { view ->
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val adapter = recyclerView.adapter as? SongAdapter
            adapter?.updateSongs(songs)
        }
    )
}
