package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.purrytify.domain.model.User
import com.example.purrytify.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    init {
        loadDummyUserData()
    }


    private fun loadDummyUserData() {
        val dummyUser = User( // data dummy
            username = "13522032",
            country = "Indonesia",
            songsCount = 135,
            likedCount = 32,
            listenedCount = 50,
            profileImageUrl = "http://34.101.226.132:3000/uploads/profile-picture/dummy.png"
        )
        _userData.value = dummyUser
    }

    suspend fun logout() {
        logoutUseCase()
        _userData.value = null
    }
}