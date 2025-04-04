package com.example.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.purrytify.presentation.screen.HomeScreen
import com.example.purrytify.presentation.screen.LoginScreen
import com.example.purrytify.presentation.theme.PurrytifyTheme
import com.example.purrytify.data.service.TokenRefreshService
import com.example.purrytify.presentation.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.purrytify.presentation.viewmodel.StartupLoginState

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                                // TODO: Handle loading state (optional)
                            }
                        }
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
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