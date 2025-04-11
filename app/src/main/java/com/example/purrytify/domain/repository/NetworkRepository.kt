package com.example.purrytify.domain.repository

import com.example.purrytify.domain.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    val networkStatus: Flow<NetworkStatus>
}