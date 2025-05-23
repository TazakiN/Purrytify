package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.domain.usecase.LoginUseCase
import com.example.purrytify.data.service.TokenRefreshService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenRefreshService: TokenRefreshService
) : ViewModel() {
    private val _loginResult = MutableStateFlow<Result<LoginResponseDTO>?>(null)
    val loginResult: StateFlow<Result<LoginResponseDTO>?> = _loginResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            return
        }

        if (password.isBlank()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(email, password)
            _loginResult.value = result
            _isLoading.value = false

            if (result.isSuccess) {
                tokenRefreshService.start()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}