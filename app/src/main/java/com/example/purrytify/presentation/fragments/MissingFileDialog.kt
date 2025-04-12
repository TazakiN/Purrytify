package com.example.purrytify.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.purrytify.R
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel

class MissingFileDialog(private val song: Song) : DialogFragment() {

    private val viewModel: MusicPlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_missing_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.textMessage).text =
            "The audio file for \"${song.title}\" is no longer on your device. Click OK to delete the song data."

        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            viewModel.deleteSong(song)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}