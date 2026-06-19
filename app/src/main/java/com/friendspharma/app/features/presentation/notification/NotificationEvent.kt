package com.friendspharma.app.features.presentation.notification

sealed interface NotificationEvent {
    data object Refresh : NotificationEvent
    data object MarkAllAsRead : NotificationEvent
    data object ClearAll : NotificationEvent
    data class MarkAsRead(val id: String) : NotificationEvent
    data class DeleteNotification(val notification: NotificationUi) : NotificationEvent
    data class NotificationClicked(val notification: NotificationUi) : NotificationEvent
}