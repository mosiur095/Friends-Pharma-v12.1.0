package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalRetailProduct

@Dao
interface RetailProductDao {

    @Query("SELECT * FROM retail_product")
    suspend fun getAll(): List<LocalRetailProduct>

    @Query("SELECT * FROM retail_product WHERE PID_PRODUCT = :id")
    suspend fun getById(id: String): LocalRetailProduct?

    @Upsert
    suspend fun upsert(track: LocalRetailProduct)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalRetailProduct>)

    @Query("DELETE FROM retail_product WHERE PID_PRODUCT = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM retail_product")
    suspend fun deleteAll()

    @Query("SELECT * FROM retail_product WHERE PID_COMPANY = :id")
    suspend fun getProductsByCompany(id: Int): List<LocalRetailProduct>

    @Query("SELECT * FROM retail_product WHERE PID_CATEGORY = :id")
    suspend fun getProductsByCategory(id: Int): List<LocalRetailProduct>

    @Query("SELECT * FROM retail_product ORDER BY COMPANY_NAME, PRODUCT_NAME LIMIT :limit")
    suspend fun getFirstN(limit: Int): List<LocalRetailProduct>
}