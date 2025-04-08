package com.example.purrytify.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.purrytify.R
import com.example.purrytify.domain.model.Song

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit
) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artworkImage: ImageView = itemView.findViewById(R.id.imageArtwork)
        val titleText: TextView = itemView.findViewById(R.id.textTitle)
        val artistText: TextView = itemView.findViewById(R.id.textArtist)
        val durationText: TextView = itemView.findViewById(R.id.textDuration)
        val likedText: TextView = itemView.findViewById(R.id.textLiked)
        val usernameText: TextView = itemView.findViewById(R.id.textUsername)
        val songUriText: TextView = itemView.findViewById(R.id.textSongUri)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        // Load artwork image
        Glide.with(holder.itemView)
            .load(song.artworkUri)
            .placeholder(R.drawable.ic_artwork_placeholder) // pastikan ini ada di drawable
            .into(holder.artworkImage)

        holder.titleText.text = song.title
        holder.artistText.text = "Artist: ${song.artist}"
        holder.durationText.text = "Duration: ${song.duration / 1000}s"
        holder.likedText.text = if (song.isLiked) "❤️ Liked" else "♡ Not Liked"
        holder.usernameText.text = "Added by: ${song.username}"
        holder.songUriText.text = "URI: ${song.songUri}"

        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}