package com.example.purrytify.presentation.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.theme.Black
import android.util.Log

@Composable
fun SongContextMenu(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onEdit: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    Log.d("QueueDebug", "SongContextMenu opened for song: ${song.title}")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = Black
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Play option
                ContextMenuItem(
                    icon = Icons.Default.PlayArrow,
                    label = "Play",
                    onClick = {
                        Log.d("QueueDebug", "Play clicked for song: ${song.title}")
                        onPlay(song)
                        onDismiss()
                    }
                )

                // Add to queue option
                ContextMenuItem(
                    icon = Icons.Default.QueueMusic,
                    label = "Add to Queue",
                    onClick = {
                        Log.d("QueueDebug", "Add to Queue clicked for song: ${song.title}")
                        onAddToQueue(song)
                        onDismiss()
                    }
                )

                // Like/Unlike option
                ContextMenuItem(
                    icon = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (song.isLiked) "Remove from Liked Songs" else "Add to Liked Songs",
                    onClick = {
                        Log.d("QueueDebug", "Toggle favorite clicked for song: ${song.title}")
                        onToggleFavorite(song)
                        onDismiss()
                    }
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Edit option
                ContextMenuItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Song",
                    onClick = {
                        Log.d("QueueDebug", "Edit clicked for song: ${song.title}")
                        onEdit(song)
                        onDismiss()
                    }
                )

                // Delete option
                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    label = "Delete Song",
                    onClick = {
                        Log.d("QueueDebug", "Delete clicked for song: ${song.title}")
                        onDelete(song)
                        onDismiss()
                    },
                    textColor = Color.Red
                )
            }
        }
    }
}

@Composable
fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = label,
            color = textColor
        )
    }
}