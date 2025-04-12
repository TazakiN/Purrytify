package com.example.purrytify.presentation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.purrytify.databinding.DialogAddSongBinding
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogUpdateSong(private val song: Song) : BottomSheetDialogFragment() {

    private var _binding: DialogAddSongBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MusicPlayerViewModel by viewModels(ownerProducer = { requireActivity() })

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

        binding.txtSelectedFileName.text = "Audio file cannot be changed"
        binding.boxSong.isEnabled = false
        binding.boxSong.alpha = 0.5f

        binding.boxArtwork.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

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
                viewModel.updateSong(updatedSong)
                viewModel.refreshCurrentSong(updatedSong)

                Toast.makeText(context, "Song updated successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Title and artist must not be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val inputStream = requireContext().contentResolver.openInputStream(it)
                val decodedBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (decodedBitmap != null) {
                    selectedArtworkUri = it
                    binding.imgArtworkPreview.setImageBitmap(decodedBitmap)
                    binding.txtArtworkLabel.text = "Photo Selected"
                } else {
                    Toast.makeText(context, "Selected image is corrupted or unsupported.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load selected image.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}