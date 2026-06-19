package com.friendspharma.app.features.data.local.dao

import com.friendspharma.app.features.data.local.entities.LocalNotificationItem
import com.friendspharma.app.features.data.remote.model.NotificationDto

// Local entity <-> DTO, same pattern as ProductMapping.kt.

fun LocalNotificationItem.toDto(): NotificationDto = NotificationDto(
    id = id,
    title = title,
    body = body,
    type = type,
    isRead = isRead,
    timestamp = timestamp,
    route = route,
    productId = productId,
    imageUrl = imageUrl,
)

fun NotificationDto.toLocal(): LocalNotificationItem = LocalNotificationItem(
    id = id,
    title = title.orEmpty(),
    body = body.orEmpty(),
    type = type.orEmpty(),
    isRead = isRead,
    timestamp = timestamp,
    route = route,
    productId = productId,
    imageUrl = imageUrl,
)