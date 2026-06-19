package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem

interface CartRepo {

    suspend fun getProducts(): List<CartInfoDtoItem>

    suspend fun getProduct(id: String): CartInfoDtoItem?

    suspend fun createProduct(product: CartInfoDtoItem): Int?

    suspend fun createProducts(products: List<CartInfoDtoItem>)

    suspend fun deleteProduct(id: String)

    suspend fun deleteAllProducts()
}