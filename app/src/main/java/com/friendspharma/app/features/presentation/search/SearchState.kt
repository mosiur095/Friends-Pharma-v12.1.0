package com.friendspharma.app.features.presentation.search

import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem


data class SearchState(
    val isLoading: Boolean = false,
    val currentItem: ProductsDtoItem = ProductsDtoItem(),
    val products: ProductsDto = ProductsDto(),
    val searchedProduct: List<ProductsDtoItem> = emptyList(),
    val search: String = "",
    val cartInfo: CartInfoDto = CartInfoDto(),
    val cartItemQuantity: Int = 0,
    val cartIds: Set<Int> = HashSet(),
    val isBox: Boolean = true,
    val addToCartLoading: String = "",
    val refreshLoading: Boolean = false
)
