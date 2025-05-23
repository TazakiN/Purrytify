package com.example.purrytify.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.purrytify.R
import com.example.purrytify.presentation.fragments.Button
import com.example.purrytify.presentation.fragments.CustomInputField
import com.example.purrytify.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginResult by viewModel.loginResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isLoginEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(email, password) {
        isLoginEnabled = email.isNotBlank() && password.isNotBlank()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.loginbg_erased),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.asset_3),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Millions of Songs.",
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "Only on Purritify.",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomInputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )

                CustomInputField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    text = "Log In",
                    onClick = {
                        if (isLoginEnabled) {
                            viewModel.login(email, password)
                        }
                    },
                    enabled = isLoginEnabled // Disable the button if fields are empty
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                }

                loginResult?.let { result ->
                    if (result.isFailure) {
                        Text(
                            result.exceptionOrNull()?.message ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (result.isSuccess) {
                        LaunchedEffect(Unit) {
                            onLoginSuccess()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}