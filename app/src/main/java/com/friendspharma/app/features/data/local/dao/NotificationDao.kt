package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.friendspharma.app.features.data.local.entities.LocalNotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notification ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LocalNotificationItem>>

    @Query("SELECT COUNT(*) FROM notification WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LocalNotificationItem)

    @Query("UPDATE notification SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notification SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM notification")
    suspend fun clearAll()
}
