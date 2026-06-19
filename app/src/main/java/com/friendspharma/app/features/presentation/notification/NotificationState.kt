package com.friendspharma.app.features.presentation.notification

data class NotificationState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationUi> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
) {
    val isEmpty get() = !isLoading && error == null && notifications.isEmpty()
}

data class NotificationUi(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val timestamp: Long = 0L,
    val route: String? = null,
    val productId: String? = null,
    val imageUrl: String? = null,   // promotional banner image URL
)

enum class NotificationType {
    ORDER_STATUS, BACK_IN_STOCK, OFFER, PRICE_CHANGE, GENERAL;

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw?.uppercase() } ?: GENERAL
    }
}