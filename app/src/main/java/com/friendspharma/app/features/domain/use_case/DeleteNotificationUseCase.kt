package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.features.domain.repository.NotificationRepo
import javax.inject.Inject

/** Delete one via invoke(id), or wipe the table via clearAll(). */
class DeleteNotificationUseCase @Inject constructor(
    private val repo: NotificationRepo,
) {
    suspend fun invoke(id: String) = repo.delete(id)
    suspend fun clearAll() = repo.clearAll()
}
