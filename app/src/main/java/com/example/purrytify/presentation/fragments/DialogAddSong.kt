package com.example.purrytify.presentation.fragments

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.purrytify.R
import com.example.purrytify.databinding.DialogAddSongBinding
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DialogAddSong : BottomSheetDialogFragment() {

    private var _binding: DialogAddSongBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels(ownerProducer = { requireActivity() })

    private var songUri: Uri? = null
    private var duration: Long = 0L
    private var selectedArtworkUri: Uri? = null

    private val pickAudio = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            songUri = it

            val fileName = getFileNameFromUri(it)
            binding.txtSelectedFileName.text = "File: $fileName"

            lifecycleScope.launch {
                delay(200)
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(requireContext(), it)

                    val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                    duration = durationStr?.toLongOrNull() ?: 0
                    binding.inputTitle.setText(title ?: "")
                    binding.inputArtist.setText(artist ?: "")
                    binding.txtDuration.text = "Durasi: ${duration / 1000} detik"

                    mmr.release()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membaca metadata lagu", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedArtworkUri = it
            binding.imgArtworkPreview.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imgArtworkPreview.setImageResource(R.drawable.ic_artwork_placeholder)

        binding.btnChooseSong.setOnClickListener {
            pickAudio.launch(arrayOf("audio/*"))
        }

        binding.btnChooseArtwork.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val artist = binding.inputArtist.text.toString().trim()
            val uri = songUri?.toString()
            val artwork = selectedArtworkUri?.toString()

            if (title.isNotEmpty() && artist.isNotEmpty() && uri != null && duration > 0) {
                viewModel.addSong(
                    Song(
                        id = 0,
                        title = title,
                        artist = artist,
                        artworkUri = artwork,
                        songUri = uri,
                        duration = duration,
                        isLiked = false,
                        username = "You"
                    )
                )
                Toast.makeText(context, "Lagu berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Lengkapi semua data terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) result = it.getString(index)
                }
            }
        }
        return result ?: uri.lastPathSegment ?: "Unknown"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}