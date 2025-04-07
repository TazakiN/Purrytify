package com.example.purrytify.presentation.screen

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
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
            // Inflate layout that contains RecyclerView
            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_library, null, false)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            recyclerView.layoutManager = LinearLayoutManager(ctx)
            recyclerView.adapter = SongAdapter(songs)
            view
        },
        update = { view ->
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
            recyclerView.adapter = SongAdapter(songs) // Update when data changes
        }
    )
}