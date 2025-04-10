package com.example.purrytify.presentation.screen

import android.view.LayoutInflater
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
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.adapter.SongVerticalAdapter
import com.example.purrytify.presentation.viewmodel.HomeScreenViewModel
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val context = LocalContext.current
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val newSongs by viewModel.newSongs.collectAsState()

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_home, null, false)

            val rvNew = view.findViewById<RecyclerView>(R.id.recyclerViewNewSongs)
            val rvRecently = view.findViewById<RecyclerView>(R.id.recyclerViewRecentlyPlayed)

            rvNew.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            rvRecently.layoutManager = LinearLayoutManager(ctx)

            val playSong: (song: com.example.purrytify.domain.model.Song) -> Unit = { song ->
                try {
                    // Use MusicPlayerViewModel instead of creating a new MediaPlayer
                    musicPlayerViewModel.playSong(song)
                    Toast.makeText(ctx, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
                    libraryViewModel.updateLastPlayed(song.id)
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Failed to play song: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
            }

            // Store adapters to tags for later access in update
            val adapterRecently = SongAdapter(recentlyPlayed, playSong)
            val adapterNew = SongVerticalAdapter(newSongs, playSong)

            rvRecently.adapter = adapterRecently
            rvNew.adapter = adapterNew

            rvRecently.setTag(R.id.recyclerViewRecentlyPlayed, adapterRecently)
            rvNew.setTag(R.id.recyclerViewNewSongs, adapterNew)

            view
        },
        update = { view ->
            val rvRecently = view.findViewById<RecyclerView>(R.id.recyclerViewRecentlyPlayed)
            val rvNew = view.findViewById<RecyclerView>(R.id.recyclerViewNewSongs)

            val adapterRecently = rvRecently.getTag(R.id.recyclerViewRecentlyPlayed) as? SongAdapter
            val adapterNew = rvNew.getTag(R.id.recyclerViewNewSongs) as? SongVerticalAdapter

            adapterRecently?.updateSongs(recentlyPlayed)
            adapterNew?.updateSongs(newSongs)
        }
    )
}