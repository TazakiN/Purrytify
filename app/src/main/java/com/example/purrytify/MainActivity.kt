package com.example.purrytify

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.purrytify.data.service.TokenRefreshService
import com.example.purrytify.presentation.screen.HomeScreen
import com.example.purrytify.presentation.screen.LibraryScreen
import com.example.purrytify.presentation.screen.LoginScreen
import com.example.purrytify.presentation.screen.ProfileScreen
import com.example.purrytify.presentation.theme.PurrytifyTheme
import com.example.purrytify.presentation.viewmodel.SplashViewModel
import com.example.purrytify.presentation.viewmodel.StartupLoginState
import com.example.purrytify.presentation.fragments.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen(val route: String, val title: String, val icon: Int) {
    data object Login : Screen("login", "Login", R.drawable.ic_login)
    data object Home : Screen("home", "Home", R.drawable.ic_home)
    data object Library : Screen("library", "Your Library", R.drawable.ic_library)
    data object Profile : Screen("profile", "Profile", R.drawable.ic_profile)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplashViewModel>()

    @Inject
    lateinit var tokenRefreshService: TokenRefreshService

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }
        enableEdgeToEdge()
        setContent {
            PurrytifyTheme {
                navController = rememberNavController()
                val isReady by viewModel.isReady.collectAsStateWithLifecycle()
                val startupLoginState by viewModel.startupLoginState.collectAsStateWithLifecycle(initialValue = StartupLoginState.Loading)

                LaunchedEffect(isReady) {
                    if (isReady) {
                        when (startupLoginState) {
                            StartupLoginState.LoggedIn -> {
                                tokenRefreshService.start()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            StartupLoginState.LoggedOut -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            StartupLoginState.Loading -> {
                                // TODO: Handle loading state
                            }
                        }
                    }
                }

                val bottomNavItems = listOf(Screen.Home, Screen.Library, Screen.Profile)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute != Screen.Login.route) {
                            BottomAppBar {
                                BottomNavigationBar(navController = navController, items = bottomNavItems)
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (startupLoginState == StartupLoginState.LoggedIn && isReady) Screen.Home.route else Screen.Login.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(onLoginSuccess = {
                                    tokenRefreshService.start()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                })
                            }
                            composable(Screen.Home.route) {
                                HomeScreen()
                            }
                            composable(Screen.Library.route) {
                                LibraryScreen()
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(onLogoutSuccess = {
                                    tokenRefreshService.stop()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                })
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