package com.friendspharma.app.features.presentation.pharma

import com.friendspharma.app.features.data.remote.model.AllCompanyDto

data class PharmaState(
    val isLoading: Boolean = true,
    val cartItemQuantity: Int = 0,
    val search: String = "",
    val companies: AllCompanyDto = AllCompanyDto(),
    val allSearchedCompanies: AllCompanyDto = AllCompanyDto()
)