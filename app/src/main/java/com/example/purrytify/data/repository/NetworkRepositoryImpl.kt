package com.example.purrytify.data.repository

import com.example.purrytify.data.network.NetworkMonitor
import com.example.purrytify.domain.model.NetworkStatus
import com.example.purrytify.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(
    private val monitor: NetworkMonitor
) : NetworkRepository {

    override val networkStatus: Flow<NetworkStatus> = monitor.networkStatus
}