package com.example.purrytify.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.purrytify.R
import com.example.purrytify.presentation.theme.Black
import com.example.purrytify.presentation.theme.Teal
import com.example.purrytify.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val userData by viewModel.userData.collectAsState(initial = null)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Teal,
                            Black,
                            Black
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {

                    if (userData?.profileImageUrl.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            tint = Color.White
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data(userData?.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_profile_default),
                            error = painterResource(id = R.drawable.ic_error),
                        )
                    }
                }

                /* kalo mau Implement edit profile */
    //            IconButton(
    //                onClick = {  },
    //                modifier = Modifier
    //                    .size(24.dp)
    //                    .align(Alignment.CenterHorizontally)
    //                    .padding(top = 8.dp)
    //            ) {
    //                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.White)
    //            }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userData?.username ?: "13522xxx",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userData?.country ?: "Indonesia",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = MaterialTheme.shapes.small
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = Color.White)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = userData?.songsCount?.toString() ?: "135", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "SONGS", color = Color.Gray, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = userData?.likedCount?.toString() ?: "32", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "LIKED", color = Color.Gray, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = userData?.listenedCount?.toString() ?: "50", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "LISTENED", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirm Logout", color = Color.White) },
                        text = { Text("Are you sure you want to logout?", color = Color.White) },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                scope.launch {
                                    viewModel.logout()
                                    onLogoutSuccess()
                                }
                            }) {
                                Text("Yes", color = Color(0xFF1DB954))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("No", color = Color.White)
                            }
                        },
                        containerColor = Black
                    )
                }
            }
        }
    }
}