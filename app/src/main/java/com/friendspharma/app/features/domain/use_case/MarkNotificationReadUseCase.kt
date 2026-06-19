package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.features.domain.repository.NotificationRepo
import javax.inject.Inject

/** Mark one read via invoke(id), or all via markAll(). */
class MarkNotificationReadUseCase @Inject constructor(
    private val repo: NotificationRepo,
) {
    suspend fun invoke(id: String) = repo.markAsRead(id)
    suspend fun markAll() = repo.markAllAsRead()
}
