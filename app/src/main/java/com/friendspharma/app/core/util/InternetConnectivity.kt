package com.friendspharma.app.core.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InternetConnectivity @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun init(function: (() -> Unit?)? = null) {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (function != null) {
                    function()
                }
            }

            override fun onLost(network: Network) {
                println("Lost")
            }
        })
    }
}