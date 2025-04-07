package com.example.purrytify.presentation.fragments

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun NetworkStatusSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState)
}
