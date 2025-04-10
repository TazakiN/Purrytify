package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.domain.model.User
import com.example.purrytify.domain.usecase.GetProfileUseCase
import com.example.purrytify.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val result = getProfileUseCase()
            result.onSuccess { user ->
                _userData.value = user
                _errorMessage.value = null
            }
            result.onFailure { error ->
                _userData.value = null
                _errorMessage.value = error.message ?: "Failed to load profile data"
            }
        }
    }


    suspend fun logout() {
        logoutUseCase()
        _userData.value = null
    }
}