package com.friendspharma.app.features.data.repository

import com.friendspharma.app.features.data.local.dao.NotificationDao
import com.friendspharma.app.features.data.local.dao.toDto
import com.friendspharma.app.features.data.local.dao.toLocal
import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.repository.NotificationRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepoImpl @Inject constructor(
    private val dao: NotificationDao,
) : NotificationRepo {

    override fun getNotifications(): Flow<List<NotificationDto>> =
        dao.observeAll().map { rows -> rows.map { it.toDto() } }

    override fun getUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun insert(notification: NotificationDto) =
        dao.insert(notification.toLocal())

    override suspend fun markAsRead(id: String) = dao.markAsRead(id)

    override suspend fun markAllAsRead() = dao.markAllAsRead()

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun clearAll() = dao.clearAll()
}
