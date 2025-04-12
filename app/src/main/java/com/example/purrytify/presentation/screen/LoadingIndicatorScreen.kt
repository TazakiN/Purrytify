package com.example.purrytify.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.purrytify.presentation.theme.Green
import com.example.purrytify.presentation.theme.PurrytifyTheme

@Composable
fun LoadingIndicatorScreen(
    modifier: Modifier = Modifier,
    loadingText: String? = "Memuat..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Green,
            strokeWidth = 4.dp
        )
        if (loadingText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorScreenPreview() {
    PurrytifyTheme {
        LoadingIndicatorScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorScreenWithoutTextPreview() {
    PurrytifyTheme {
        LoadingIndicatorScreen(loadingText = null)
    }
}