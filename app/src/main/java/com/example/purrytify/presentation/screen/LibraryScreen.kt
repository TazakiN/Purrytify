package com.example.purrytify.presentation.screen

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = allSongs
        .filter { if (showLikedOnly) it.isLiked else true }
        .filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
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

            val adapter = SongAdapter(filteredSongs) { song ->
                try {
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
