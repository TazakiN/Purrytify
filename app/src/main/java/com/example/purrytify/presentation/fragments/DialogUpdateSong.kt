package com.example.purrytify.presentation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.purrytify.databinding.DialogAddSongBinding
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.viewmodel.LibraryViewModel
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogUpdateSong(private val song: Song) : BottomSheetDialogFragment() {

    private var _binding: DialogAddSongBinding? = null
    private val binding get() = _binding!!

    private val libraryViewModel: LibraryViewModel by viewModels(ownerProducer = { requireActivity() })
    private val musicPlayerViewModel: MusicPlayerViewModel by activityViewModels()

    private var selectedArtworkUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Prefill data
        binding.inputTitle.setText(song.title)
        binding.inputArtist.setText(song.artist)
        selectedArtworkUri = song.artworkUri?.let { Uri.parse(it) }
        selectedArtworkUri?.let { binding.imgArtworkPreview.setImageURI(it) }

        binding.txtSelectedFileName.text = "Tidak bisa mengubah audio"
        binding.boxSong.isEnabled = false
        binding.boxSong.alpha = 0.5f

        // Bisa ubah artwork
        binding.boxArtwork.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        // Save perubahan
        binding.btnSave.text = "Save Changes"
        binding.btnSave.setOnClickListener {
            val newTitle = binding.inputTitle.text.toString().trim()
            val newArtist = binding.inputArtist.text.toString().trim()
            val artwork = selectedArtworkUri?.toString()

            if (newTitle.isNotEmpty() && newArtist.isNotEmpty()) {
                val updatedSong = song.copy(
                    title = newTitle,
                    artist = newArtist,
                    artworkUri = artwork
                )

                // Update the song in the database
                libraryViewModel.updateSong(updatedSong)

                // Directly update the current song in MusicPlayerViewModel
                musicPlayerViewModel.updateCurrentSongIfMatches(updatedSong)

                Toast.makeText(context, "Lagu berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Judul dan artis tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedArtworkUri = it
            binding.imgArtworkPreview.setImageURI(it)
            binding.txtArtworkLabel.text = "Photo Selected"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}