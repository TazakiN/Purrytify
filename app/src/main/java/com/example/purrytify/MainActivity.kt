package com.example.purrytify

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrytify.data.service.TokenRefreshService
import com.example.purrytify.domain.model.NetworkStatus
import com.example.purrytify.presentation.fragments.BottomNavigationBar
import com.example.purrytify.presentation.fragments.MiniPlayer
import com.example.purrytify.presentation.fragments.MissingFileDialog
import com.example.purrytify.presentation.screen.*
import com.example.purrytify.presentation.theme.PurrytifyTheme
import com.example.purrytify.presentation.viewmodel.MusicPlayerViewModel
import com.example.purrytify.presentation.viewmodel.NetworkViewModel
import com.example.purrytify.presentation.viewmodel.SplashViewModel
import com.example.purrytify.presentation.viewmodel.StartupLoginState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen(val route: String, val title: String, val icon: Int) {
    data object Login : Screen("login", "Login", R.drawable.ic_login)
    data object Home : Screen("home", "Home", R.drawable.ic_home)
    data object Library : Screen("library", "Your Library", R.drawable.ic_library)
    data object Profile : Screen("profile", "Profile", R.drawable.ic_profile)
    data object Player : Screen("player", "Music Player", 0)
    data object Queue : Screen("queue", "Play Queue", 0)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val splashViewModel by viewModels<SplashViewModel>()
    private val musicPlayerViewModel by viewModels<MusicPlayerViewModel>()
    private val networkViewModel by viewModels<NetworkViewModel>()

    @Inject
    lateinit var tokenRefreshService: TokenRefreshService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { !splashViewModel.isReady.value }
        }

        enableEdgeToEdge()

        setContent {
            PurrytifyTheme {
                val navController = rememberNavController()

                val isReady by splashViewModel.isReady.collectAsStateWithLifecycle()
                val startupLoginState by splashViewModel.startupLoginState.collectAsStateWithLifecycle(
                    initialValue = StartupLoginState.Loading
                )

                val currentSong by musicPlayerViewModel.currentSong.collectAsStateWithLifecycle()
                val showFullPlayer by musicPlayerViewModel.showFullPlayer.collectAsStateWithLifecycle()
                val showQueue by musicPlayerViewModel.showQueue.collectAsStateWithLifecycle()
                val queueSize by musicPlayerViewModel.queue.collectAsStateWithLifecycle()
                val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()

                // For debugging queue functionality
                LaunchedEffect(queueSize.size) {
                    Log.d("QueueDebug", "Queue size changed in MainActivity: ${queueSize.size}")
                    if (queueSize.isNotEmpty()) {
                        queueSize.forEachIndexed { index, song ->
                            Log.d("QueueDebug", "Queue item $index: ${song.title}")
                        }
                    }
                }

                // Add state for missing file song
                val missingFileSong by musicPlayerViewModel.missingFileSong.collectAsStateWithLifecycle()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute != Screen.Login.route &&
                        currentRoute != Screen.Player.route &&
                        currentRoute != Screen.Queue.route
                val showMiniPlayer = showBottomBar && currentSong != null

                // Navigation logic after splash and login status are known
                LaunchedEffect(isReady, startupLoginState) {
                    if (isReady) {
                        when (startupLoginState) {
                            StartupLoginState.LoggedIn -> {
                                tokenRefreshService.start()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            StartupLoginState.LoggedOut -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            else -> {} // loading state
                        }
                    }
                }

                // Show full music player when requested
                LaunchedEffect(showFullPlayer) {
                    if (showFullPlayer && currentSong != null) {
                        navController.navigate(Screen.Player.route)
                    }
                }

                // Show queue screen when requested - improved with better navigation logic
                LaunchedEffect(showQueue) {
                    if (showQueue) {
                        if (currentRoute != Screen.Queue.route) {
                            Log.d("QueueDebug", "Navigating to Queue screen. Current queue size: ${queueSize.size}")
                            navController.navigate(Screen.Queue.route)
                        }
                    } else {
                        if (currentRoute == Screen.Queue.route) {
                            Log.d("QueueDebug", "Queue visibility turned off, navigating back")
                            navController.popBackStack()
                        }
                    }
                }

                LaunchedEffect(networkStatus) {
                    if (networkStatus != NetworkStatus.Available) {
                        val message = when (networkStatus) {
                            NetworkStatus.Unavailable -> "Tidak ada koneksi internet."
                            NetworkStatus.Lost -> "Koneksi internet terputus."
                            NetworkStatus.Losing -> "Koneksi internet tidak stabil."
                            else -> "Unknown network error."
                        }
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(missingFileSong) {
                    missingFileSong?.let { song ->
                        val dialogFragment = MissingFileDialog(song)
                        dialogFragment.show(supportFragmentManager, "MissingFileDialog")

                        musicPlayerViewModel.resetMissingFileState()
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            Column {
                                if (showMiniPlayer) {
                                    MiniPlayer(
                                        viewModel = musicPlayerViewModel,
                                        onPlayerClick = {
                                            musicPlayerViewModel.togglePlayerView()
                                        }
                                    )
                                }
                                BottomNavigationBar(
                                    navController = navController,
                                    items = listOf(Screen.Home, Screen.Library, Screen.Profile)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onLoginSuccess = {
                                        tokenRefreshService.start()
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Home.route) {
                                HomeScreen(musicPlayerViewModel = musicPlayerViewModel)
                            }
                            composable(Screen.Library.route) {
                                LibraryScreen(musicPlayerViewModel = musicPlayerViewModel)
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(onLogoutSuccess = {
                                    tokenRefreshService.stop()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                })
                            }
                            composable(Screen.Player.route) {
                                MusicPlayerScreen(
                                    viewModel = musicPlayerViewModel,
                                    onBackPressed = {
                                        musicPlayerViewModel.togglePlayerView()
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(Screen.Queue.route) {
                                QueueScreen(
                                    onBackPressed = {
                                        navController.popBackStack()
                                    },
                                    viewModel = musicPlayerViewModel  // Pass the existing ViewModel instance
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tokenRefreshService.stop()
    }
}