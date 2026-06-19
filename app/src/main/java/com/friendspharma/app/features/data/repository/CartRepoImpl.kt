package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.CartDao
import com.friendspharma.app.features.data.local.dao.toExternal
import com.friendspharma.app.features.data.local.dao.toLocal
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem
import com.friendspharma.app.features.domain.repository.CartRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CartRepoImpl @Inject constructor(
    private val cartDao: CartDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
): CartRepo {
    override suspend fun getProducts(): List<CartInfoDtoItem> {
        return withContext(dispatcher) {
            cartDao.getAll().toExternal()
        }
    }

    override suspend fun getProduct(id: String): CartInfoDtoItem? {
        return withContext(dispatcher) {
            cartDao.getById(id)?.toExternal()
        }
    }

    override suspend fun createProduct(product: CartInfoDtoItem): Int? {
        withContext(dispatcher) {
            cartDao.upsert(product.toLocal())
        }
        return product.PID_PRODUCT
    }

    override suspend fun createProducts(products: List<CartInfoDtoItem>) {
        withContext(dispatcher) {
            cartDao.upsertAll(products.toLocal())
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(dispatcher) {
            cartDao.deleteById(id)
        }
    }

    override suspend fun deleteAllProducts() {
        withContext(dispatcher) {
            cartDao.deleteAll()
        }
    }

}