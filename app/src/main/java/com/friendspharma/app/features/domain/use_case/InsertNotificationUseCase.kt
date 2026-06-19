package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.repository.NotificationRepo
import javax.inject.Inject

/** Persists an incoming push. Call from PharmaMessagingService on message receipt. */
class InsertNotificationUseCase @Inject constructor(
    private val repo: NotificationRepo,
) {
    suspend fun invoke(notification: NotificationDto) = repo.insert(notification)
}
