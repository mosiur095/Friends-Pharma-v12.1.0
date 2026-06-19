package com.friendspharma.app.core.fcm

import android.util.Log
import com.friendspharma.app.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor() {

    fun onNewToken(token: String) {
        // TODO: send token + userType to backend
    }

    // Called after successful login AND when app starts with saved session
    fun syncOnLogin() {
        val userType = MainActivity.userType.value
        if (userType.isEmpty()) {
            Log.w("FCM", "syncOnLogin: empty userType — skipping")
            return
        }
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            onNewToken(token)
            syncTopics(userType)
        }
    }

    fun syncTopics(userType: String) {
        val fm = FirebaseMessaging.getInstance()

        // Always subscribe all_users — everyone gets generic promotions
        fm.subscribeToTopic("all_users")
            .addOnSuccessListener { Log.d("FCM", "Subscribed: all_users") }

        when (userType) {
            "2" -> {
                // Wholesale — all_users + wholesale topic
                fm.unsubscribeFromTopic("special")
                fm.subscribeToTopic("wholesale")
                    .addOnSuccessListener { Log.d("FCM", "Subscribed: wholesale") }
            }
            "3" -> {
                // Special — all_users + special topic
                fm.unsubscribeFromTopic("wholesale")
                fm.subscribeToTopic("special")
                    .addOnSuccessListener { Log.d("FCM", "Subscribed: special") }
            }
            else -> {
                // Retail (type 1) or any other — all_users only
                // Same as guest, no extra topic needed
                fm.unsubscribeFromTopic("wholesale")
                fm.unsubscribeFromTopic("special")
                Log.d("FCM", "Retail user: all_users only")
            }
        }
    }

    // Called on logout — remove user-type topics, keep all_users
    fun clearTopics() {
        val fm = FirebaseMessaging.getInstance()
        fm.unsubscribeFromTopic("wholesale")
            .addOnSuccessListener { Log.d("FCM", "Unsubscribed: wholesale") }
        fm.unsubscribeFromTopic("special")
            .addOnSuccessListener { Log.d("FCM", "Unsubscribed: special") }
        // all_users stays — guest users still get generic notifications
    }
}