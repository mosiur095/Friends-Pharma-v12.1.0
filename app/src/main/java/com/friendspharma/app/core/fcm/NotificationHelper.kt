package com.friendspharma.app.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.features.data.remote.model.NotificationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun show(dto: NotificationDto) {
        createChannel()
        val manager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_ID, dto.id)
            putExtra(EXTRA_TITLE, dto.title)
            putExtra(EXTRA_BODY, dto.body)
            putExtra(EXTRA_TYPE, dto.type)
            putExtra(EXTRA_ROUTE, dto.route)
            putExtra(EXTRA_PRODUCT_ID, dto.productId)
        }
        val pending = PendingIntent.getActivity(
            context, dto.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(dto.title ?: "Friends Pharma")
            .setContentText(dto.body.orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(dto.body.orEmpty()))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        manager.notify(dto.id.hashCode(), notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "General", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Order updates, offers and stock alerts" }
        )
    }

    companion object {
        const val CHANNEL_ID = "pharma_general"
        const val EXTRA_FROM_NOTIFICATION = "from_notification"
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_TYPE = "type"
        const val EXTRA_ROUTE = "route"
        const val EXTRA_PRODUCT_ID = "productId"
    }
}