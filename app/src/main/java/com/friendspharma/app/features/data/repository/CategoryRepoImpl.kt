package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.CartDao
import com.friendspharma.app.features.data.local.dao.CategoryDao
import com.friendspharma.app.features.data.local.dao.toExternal
import com.friendspharma.app.features.data.local.dao.toLocal
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem
import com.friendspharma.app.features.domain.repository.CategoryRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepoImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
) : CategoryRepo {
    override suspend fun getCategories(): List<AllCategoryDtoItem> {
        return withContext(dispatcher) {
            categoryDao.getAll().toExternal()
        }
    }

    override suspend fun getCategory(id: String): AllCategoryDtoItem? {
        return withContext(dispatcher) {
            categoryDao.getById(id)?.toExternal()
        }
    }

    override suspend fun createCategory(product: AllCategoryDtoItem): Int? {
        withContext(dispatcher) {
            categoryDao.upsert(product.toLocal())
        }
        return product.PID_CATEGORY
    }

    override suspend fun createCategories(products: List<AllCategoryDtoItem>) {
        withContext(dispatcher) {
            categoryDao.upsertAll(products.toLocal())
        }
    }

    override suspend fun deleteCategory(id: String) {
        withContext(dispatcher) {
            categoryDao.deleteById(id)
        }
    }

    override suspend fun deleteAllCategories() {
        withContext(dispatcher) {
            categoryDao.deleteAll()
        }
    }
}