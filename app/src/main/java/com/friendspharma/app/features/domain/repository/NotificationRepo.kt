package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.model.NotificationDto
import kotlinx.coroutines.flow.Flow

/**
 * Implemented by data/repository/NotificationRepoImpl, bound via Hilt.
 * Reads are Room Flows mapped to DTOs, so background FCM inserts push updates
 * to collectors automatically (DB-centric, local-only — no GET endpoint).
 */
interface NotificationRepo {
    fun getNotifications(): Flow<List<NotificationDto>>
    fun getUnreadCount(): Flow<Int>
    suspend fun insert(notification: NotificationDto)
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun delete(id: String)
    suspend fun clearAll()
}
