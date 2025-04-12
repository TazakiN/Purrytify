package com.example.purrytify.presentation.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream

class DialogAddSong : BottomSheetDialogFragment() {

    private var _binding: DialogAddSongBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels(ownerProducer = { requireActivity() })

    private var songUri: Uri? = null
    private var duration: Long = 0L
    private var selectedArtworkUri: Uri? = null
    private var artworkBitmap: Bitmap? = null

    private val pickAudio = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            songUri = it

            val fileName = getFileNameFromUri(it)
            binding.txtSelectedFileName.text = fileName

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

                    // Extract embedded album art
                    val artBytes = mmr.embeddedPicture
                    val decodedBitmap = artBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

                    if (decodedBitmap != null) {
                        artworkBitmap = decodedBitmap
                        binding.imgArtworkPreview.setImageBitmap(decodedBitmap)
                        binding.txtArtworkLabel.text = "Embedded Cover Art"
                    } else {
                        artworkBitmap = null
                        binding.imgArtworkPreview.setImageResource(R.drawable.ic_artwork_placeholder)
                        binding.txtArtworkLabel.text = "No Artwork or corrupted"
                    }

                    mmr.release()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read song metadata", Toast.LENGTH_SHORT).show()
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
            binding.txtArtworkLabel.text = "Photo Selected"
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

        binding.boxArtwork.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        binding.boxSong.setOnClickListener {
            pickAudio.launch(arrayOf("audio/*"))
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val artist = binding.inputArtist.text.toString().trim()
            val uri = songUri?.toString()
            val artwork = selectedArtworkUri?.toString()

            Log.d("ArtworkURI", "Saved artwork URI: $artwork")

            if (title.isNotEmpty() && artist.isNotEmpty() && uri != null && duration > 0) {
                // Save the artwork bitmap only when the "Save" button is clicked
                val artworkUriToSave = saveArtworkBitmapIfNeeded(artworkBitmap, title)

                viewModel.addSong(
                    Song(
                        id = 0,
                        title = title,
                        artist = artist,
                        artworkUri = artworkUriToSave,
                        songUri = uri,
                        duration = duration,
                        isLiked = false,
                        username = "You"
                    )
                ) { result ->
                    result.onSuccess {
                        Toast.makeText(context, "Song successfully added!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }.onFailure { exception ->
                        if (exception is IllegalArgumentException) {
                            Toast.makeText(context, exception.message ?: "A song with the same title and artist already exists!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to add the song!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Please complete all the fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveArtworkBitmapIfNeeded(bitmap: Bitmap?, title: String): String? {
        if (bitmap != null) {
            // Save the artwork bitmap to internal storage if needed
            return saveBitmapToInternalStorage(bitmap, title)
        }
        return null
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

    private fun saveBitmapToInternalStorage(bitmap: Bitmap, title: String): String? {
        return try {
            val context = requireContext()

            // Create a unique filename using the current time in milliseconds
            val uniqueFileName = "${System.currentTimeMillis()}_${title.takeIf { it.isNotEmpty() } ?: "artwork"}.jpg"

            val file = File(context.filesDir, uniqueFileName)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()

            file.absolutePath // Return the unique file path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}