package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalCompanyItem

@Dao
interface CompanyDao {

    @Query("SELECT * FROM company")
    suspend fun getAll(): List<LocalCompanyItem>

    @Query("SELECT * FROM company WHERE PID_COMPANY = :id")
    suspend fun getById(id: String): LocalCompanyItem?

    @Upsert
    suspend fun upsert(track: LocalCompanyItem)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalCompanyItem>)

    @Query("DELETE FROM company WHERE PID_COMPANY = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM company")
    suspend fun deleteAll()
}