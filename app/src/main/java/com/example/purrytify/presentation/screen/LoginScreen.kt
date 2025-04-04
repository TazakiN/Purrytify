package com.example.purrytify.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.purrytify.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginResult = viewModel.loginResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        loginResult.value?.let { result ->
            when {
                result.isSuccess -> {
                    Text("Login Successful!", color = MaterialTheme.colorScheme.primary)
                    LaunchedEffect(Unit) {
                        onLoginSuccess()
                    }
                }

                result.isFailure -> {
                    Text(
                        "Login Failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    if (viewModel.isLoading.collectAsState().value) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}