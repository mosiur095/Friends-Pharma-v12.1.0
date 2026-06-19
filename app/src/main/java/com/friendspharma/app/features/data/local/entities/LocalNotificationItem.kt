package com.friendspharma.app.features.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room table for notifications. `id` is the FCM message id (or a generated UUID)
 * so re-delivered pushes REPLACE rather than duplicate.
 */
@Entity(tableName = "notification")
data class LocalNotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val route: String? = null,
    val productId: String? = null,
    val imageUrl: String? = null,
)