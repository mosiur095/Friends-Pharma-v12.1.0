package com.friendspharma.app.core.fcm

import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.use_case.InsertNotificationUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PharmaMessagingService : FirebaseMessagingService() {

    @Inject lateinit var insertNotification: InsertNotificationUseCase
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var tokenManager: FcmTokenManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        tokenManager.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val dto = NotificationDto(
            id        = data["id"] ?: message.messageId ?: System.currentTimeMillis().toString(),
            title     = message.notification?.title ?: data["title"],
            body      = message.notification?.body  ?: data["body"],
            type      = data["type"],
            route     = data["route"],
            productId = data["productId"],
            imageUrl  = data["imageUrl"],
        )

        scope.launch { runCatching { insertNotification.invoke(dto) } }
        notificationHelper.show(dto)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}