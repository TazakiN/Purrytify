package com.example.purrytify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purrytify.domain.model.NetworkStatus
import com.example.purrytify.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    repository: NetworkRepository
) : ViewModel() {
    val networkStatus = repository.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.Available)
}
