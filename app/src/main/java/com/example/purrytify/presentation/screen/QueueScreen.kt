package com.example.purrytify.presentation.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.purrytify.R
import com.example.purrytify.domain.model.Song
import com.example.purrytify.presentation.theme.Black
import com.example.purrytify.presentation.theme.Green
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBackPressed: () -> Unit,
    // Use viewModel passed from parent
    viewModel: MusicPlayerViewModel
) {
    val queue by viewModel.queue.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Log.d("QueueDebug", "QueueScreen composing with queue size: ${queue.size}")
        queue.forEachIndexed { index, song ->
            Log.d("QueueDebug", "Queue item $index: ${song.title} (ID: ${song.id})")
        }
        viewModel.debugQueueState()
    }

    // Setup reorderable state for drag & drop functionality
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            viewModel.reorderQueue(from.index, to.index)
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with back button and clear queue option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed close/back button behavior
                IconButton(
                    onClick = {
                        // First ensure we update the ViewModel's state
                        viewModel.toggleQueueVisibility()
                        // Then trigger navigation callback
                        onBackPressed()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Play Queue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.clearQueue()
                        }
                    },
                    enabled = queue.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Queue",
                        tint = if (queue.isNotEmpty()) Color.White else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queue title with count badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Queue",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Green
                ) {
                    Text(
                        text = "${queue.size}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Either show the queue or an empty state message
            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_library),
                            contentDescription = "Empty Queue",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your queue is empty",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add songs to your queue by selecting 'Add to Queue' from the song options",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                // Display queue with drag-and-drop reordering
                LazyColumn(
                    state = reorderableState.listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .reorderable(reorderableState)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        ReorderableItem(
                            reorderableState = reorderableState,
                            key = song.id
                        ) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp

                            Surface(
                                shadowElevation = elevation,
                                color = if (isDragging) Color(0xFF222222) else Black,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                QueueSongItem(
                                    song = song,
                                    onPlay = {
                                        coroutineScope.launch {
                                            // Play this song and adjust queue
                                            viewModel.playSong(song)

                                            // Update queue to remove this song and all previous ones
                                            if (index < queue.size - 1) {
                                                val newQueue = queue.drop(index + 1)
                                                viewModel.clearQueue()
                                                newQueue.forEach { viewModel.addToQueue(it) }
                                            } else {
                                                viewModel.clearQueue()
                                            }
                                        }
                                    },
                                    onRemove = {
                                        coroutineScope.launch {
                                            viewModel.removeFromQueue(song)
                                        }
                                    },
                                    dragHandle = {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Reorder",
                                            tint = Color.Gray,
                                            modifier = Modifier
                                                .detectReorder(reorderableState)
                                                .padding(horizontal = 8.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueSongItem(
    song: Song,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag handle for reordering
        dragHandle()

        // Album artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            if (song.artworkUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(Uri.parse(song.artworkUri))
                            .build()
                    ),
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_artwork_placeholder),
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Song info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Remove button
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove from Queue",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}