package com.friendspharma.app.features.presentation.return_products

import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDtoItem


data class ReturnMedicineState(
    val isLoading: Boolean = true,
    val allProduct: ReturnProductDto = ReturnProductDto(),
    val allSearchedProduct: ReturnProductDto = ReturnProductDto(),
    val search: String = "",
    val cartInfo: ReturnCartInfoDto = ReturnCartInfoDto(),
    val cartItemQuantity: Int = 0,
    val cartIds: Set<Int> = HashSet(),
    val currentItem: ReturnProductDtoItem = ReturnProductDtoItem(),
    val addToCartLoading: String = ""
)
