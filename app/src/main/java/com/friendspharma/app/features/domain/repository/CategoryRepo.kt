package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem

interface CategoryRepo {

    suspend fun getCategories(): List<AllCategoryDtoItem>

    suspend fun getCategory(id: String): AllCategoryDtoItem?

    suspend fun createCategory(product: AllCategoryDtoItem): Int?

    suspend fun createCategories(products: List<AllCategoryDtoItem>)

    suspend fun deleteCategory(id: String)

    suspend fun deleteAllCategories()
}