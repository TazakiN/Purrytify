package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.purrytify.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
): ViewModel(){
    suspend fun logout() {
        logoutUseCase()
    }
}