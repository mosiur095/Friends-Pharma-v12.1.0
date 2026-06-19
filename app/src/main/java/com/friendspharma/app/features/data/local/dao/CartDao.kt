package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalCartItem

@Dao
interface CartDao {

    @Query("SELECT * FROM cart")
    suspend fun getAll(): List<LocalCartItem>

    @Query("SELECT * FROM cart WHERE PID_PRODUCT = :id")
    suspend fun getById(id: String): LocalCartItem?

    @Upsert
    suspend fun upsert(track: LocalCartItem)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalCartItem>)

    @Query("DELETE FROM cart WHERE PID_PRODUCT = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM cart")
    suspend fun deleteAll()
}