package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalCategoryItem

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category")
    suspend fun getAll(): List<LocalCategoryItem>

    @Query("SELECT * FROM category WHERE PID_CATEGORY = :id")
    suspend fun getById(id: String): LocalCategoryItem?

    @Upsert
    suspend fun upsert(track: LocalCategoryItem)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalCategoryItem>)

    @Query("DELETE FROM category WHERE PID_CATEGORY = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM category")
    suspend fun deleteAll()

}