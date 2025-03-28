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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.purrytify.presentation.screen.HomeScreen
import com.example.purrytify.presentation.screen.LoginScreen
import com.example.purrytify.presentation.theme.PurrytifyTheme
import com.example.purrytify.worker.TokenExpiryWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<SplahViewModel>()

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
                val navController = rememberNavController()
                val isReady by viewModel.isReady.collectAsStateWithLifecycle()
                val isLoggedIn = false // TODO: Implement logic to check if user is logged in

                LaunchedEffect(isReady) {
//                    if (isReady) {
//                        if (isLoggedIn) {
//                            navController.navigate(Screen.Home.route) {
//                                popUpTo(Screen.Splash.route) { inclusive = true }
//                            }
//                        } else {
//                            navController.navigate(Screen.Login.route) {
//                                popUpTo(Screen.Splash.route) { inclusive = true }
//                            }
//                        }
//                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(
                        navController = navController, startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(onLoginSuccess = {
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
        scheduleTokenExpiryCheck()
    }

    private fun scheduleTokenExpiryCheck() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<TokenExpiryWorker>(
            repeatInterval = 1, // Interval pengecekan 4 menit
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TokenExpiryCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}