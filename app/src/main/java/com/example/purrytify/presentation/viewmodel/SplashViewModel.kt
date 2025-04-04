package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.data.local.TokenStorage
import com.example.purrytify.data.remote.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authService: AuthService
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val _startupLoginState = MutableStateFlow<StartupLoginState>(StartupLoginState.Loading)
    val startupLoginState: StateFlow<StartupLoginState> = _startupLoginState

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            checkAndRefreshToken()
            _isReady.value = true
        }
    }

    private fun checkAndRefreshToken() {
        viewModelScope.launch {
            val refreshToken = tokenStorage.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                try {
                    val refreshResponse = authService.refreshToken(mapOf("refreshToken" to refreshToken))
                    if (refreshResponse.isSuccessful) {
                        val newAccessToken = refreshResponse.body()?.accessToken
                        if (!newAccessToken.isNullOrBlank()) {
                            tokenStorage.saveAccessToken(newAccessToken)
                            _startupLoginState.value = StartupLoginState.LoggedIn
                        } else {
                            _startupLoginState.value = StartupLoginState.LoggedOut
                        }
                    } else {
                        _startupLoginState.value = StartupLoginState.LoggedOut
                    }
                } catch (e: Exception) {
                    _startupLoginState.value = StartupLoginState.LoggedOut
                }
            } else {
                _startupLoginState.value = StartupLoginState.LoggedOut
            }
        }
    }
}

sealed class StartupLoginState {
    data object Loading : StartupLoginState()
    data object LoggedIn : StartupLoginState()
    data object LoggedOut : StartupLoginState()
}