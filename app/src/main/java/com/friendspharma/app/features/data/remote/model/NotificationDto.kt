package com.friendspharma.app.features.data.remote.model

/**
 * The through-model for notifications, mirroring ProductsDtoItem's role.
 * This is also the shape parsed from the FCM data payload in PharmaMessagingService.
 * `type` stays a raw String (e.g. "BACK_IN_STOCK"); presentation maps it to its
 * NotificationType enum via NotificationType.from(type).
 */
data class NotificationDto(
    val id: String,
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val route: String? = null,
    val productId: String? = null,
    val imageUrl: String? = null,
)