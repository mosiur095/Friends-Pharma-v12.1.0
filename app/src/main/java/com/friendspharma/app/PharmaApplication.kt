package com.friendspharma.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PharmaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Subscribe all_users on every app start — covers guest users
        // User-type topics (wholesale/special) are managed by
        // FcmTokenManager.syncOnLogin() called from MainActivity.checkLogin()
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pharma_general",
                "General",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Order updates, offers and stock alerts"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}