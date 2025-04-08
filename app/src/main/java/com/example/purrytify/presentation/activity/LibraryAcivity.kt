package com.example.purrytify.presentation.activity

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrytify.R
import com.example.purrytify.presentation.adapter.SongAdapter
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryActivity : AppCompatActivity() {

    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        recyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SongAdapter(emptyList()) { song ->
            try {
                mediaPlayer?.release() // stop previous if any
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@LibraryActivity, Uri.parse(song.songUri))
                    prepare()
                    start()
                }
                Toast.makeText(this, "Memutar: ${song.title}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memutar lagu", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.allSongs.collect { songs ->
                adapter.updateSongs(songs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}