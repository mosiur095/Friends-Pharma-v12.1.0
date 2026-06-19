package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.repository.NotificationRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Single source of truth for the notification list. Local reads come straight
 * from Room as a Flow, so no Async wrapper is needed here (unlike API-backed
 * use cases like GetProductsUseCase).
 */
class GetNotificationsUseCase @Inject constructor(
    private val repo: NotificationRepo,
) {
    fun invoke(): Flow<List<NotificationDto>> = repo.getNotifications()
}
