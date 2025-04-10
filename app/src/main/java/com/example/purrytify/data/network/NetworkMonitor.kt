package com.example.purrytify.data.network

import android.content.Context
import android.net.*
import com.example.purrytify.domain.model.NetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkStatus = MutableStateFlow(NetworkStatus.Available)
    val networkStatus = _networkStatus.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkStatus.value = NetworkStatus.Available
        }

        override fun onLost(network: Network) {
            _networkStatus.value = NetworkStatus.Lost
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            _networkStatus.value = NetworkStatus.Losing
        }

        override fun onUnavailable() {
            _networkStatus.value = NetworkStatus.Unavailable
        }
    }

    init {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}