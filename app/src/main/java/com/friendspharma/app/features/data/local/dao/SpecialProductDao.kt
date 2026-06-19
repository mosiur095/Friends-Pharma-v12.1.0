package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalSpecialProduct

@Dao
interface SpecialProductDao {

    @Query("SELECT * FROM special_product")
    suspend fun getAll(): List<LocalSpecialProduct>

    @Query("SELECT * FROM special_product WHERE PID_PRODUCT = :id")
    suspend fun getById(id: String): LocalSpecialProduct?

    @Upsert
    suspend fun upsert(track: LocalSpecialProduct)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalSpecialProduct>)

    @Query("DELETE FROM special_product WHERE PID_PRODUCT = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM special_product")
    suspend fun deleteAll()

    @Query("SELECT * FROM special_product WHERE PID_COMPANY = :id")
    suspend fun getProductsByCompany(id: Int): List<LocalSpecialProduct>

    @Query("SELECT * FROM special_product WHERE PID_CATEGORY = :id")
    suspend fun getProductsByCategory(id: Int): List<LocalSpecialProduct>

    @Query("SELECT * FROM special_product ORDER BY COMPANY_NAME, PRODUCT_NAME LIMIT :limit")
    suspend fun getFirstN(limit: Int): List<LocalSpecialProduct>
}