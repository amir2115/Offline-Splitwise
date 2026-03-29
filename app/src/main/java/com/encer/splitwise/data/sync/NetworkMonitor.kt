package com.encer.splitwise.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.encer.splitwise.data.preferences.HealthStatusRepository
import com.encer.splitwise.data.remote.network.ApiClient
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkStatus(
    val hasInternet: Boolean = false,
    val isApiReachable: Boolean = false,
)

class NetworkMonitor @Inject constructor(
    context: Context,
    private val apiClient: ApiClient,
    private val healthStatusRepository: HealthStatusRepository,
    private val scope: CoroutineScope,
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val status = MutableStateFlow(NetworkStatus(hasInternet = hasInternetConnection()))

    init {
        scope.launch {
            observeConnectivity().collect { hasInternet ->
                status.update { current ->
                    current.copy(
                        hasInternet = hasInternet,
                        isApiReachable = if (hasInternet) current.isApiReachable else false,
                    )
                }
                if (hasInternet) {
                    refreshApiReachability()
                }
            }
        }
        scope.launch { refreshApiReachability() }
    }

    fun observeStatus(): StateFlow<NetworkStatus> = status.asStateFlow()

    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        trySend(hasInternetConnection())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(hasInternetConnection())
            }

            override fun onLost(network: Network) {
                trySend(hasInternetConnection())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                )
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    fun hasInternetConnection(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun isOnline(): Boolean = status.value.hasInternet

    fun isApiAvailable(): Boolean = status.value.isApiReachable

    suspend fun refreshApiReachability(forceRequest: Boolean = false): Boolean {
        val hasInternet = hasInternetConnection()
        if (!hasInternet && !forceRequest) {
            status.value = NetworkStatus(hasInternet = false, isApiReachable = false)
            return false
        }
        val reachable = runCatching { apiClient.health() }
            .onSuccess { healthStatusRepository.recordSuccess(it) }
            .onFailure { healthStatusRepository.recordFailure(it.message) }
            .isSuccess
        status.value = NetworkStatus(hasInternet = hasInternet, isApiReachable = reachable)
        return reachable
    }
}
