package com.friendspharma.app.features.presentation.categories

import com.friendspharma.app.features.data.remote.model.AllCategoryDto
import com.friendspharma.app.features.data.remote.model.AllCompanyDto

data class CategoriesState(
    val isLoading: Boolean = true,
    val cartItemQuantity: Int = 0,
    val search: String = "",
    val categories: AllCategoryDto = AllCategoryDto(),
    val allSearchedCategories: AllCategoryDto = AllCategoryDto(),
    val companies: AllCompanyDto = AllCompanyDto()
)