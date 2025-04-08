package com.example.purrytify.presentation.screen

import android.media.MediaPlayer
import android.net.Uri
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

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
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
                    val uri = Uri.parse(song.songUri)
                    val afd = ctx.contentResolver.openAssetFileDescriptor(uri, "r")
                    if (afd != null) {
                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.setDataSource(afd.fileDescriptor)
                        afd.close()
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        Toast.makeText(ctx, "Memutar: ${song.title}", Toast.LENGTH_SHORT).show()

                        libraryViewModel.updateLastPlayed(song.id)
                    } else {
                        Toast.makeText(ctx, "Gagal membuka file audio", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Gagal memutar lagu: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            // Simpan adapter ke dalam tag untuk akses nanti
            val adapterRecently = SongAdapter(recentlyPlayed, playSong)
            val adapterNew = SongVerticalAdapter(newSongs, playSong)

            rvRecently.adapter = adapterRecently
            rvNew.adapter = adapterNew

            // Simpan adapter dalam tag view untuk digunakan kembali di update
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
