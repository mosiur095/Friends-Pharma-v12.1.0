package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem

interface CompanyRepo {

    suspend fun getCompanies(): List<AllCompanyDtoItem>

    suspend fun getCompany(id: String): AllCompanyDtoItem?

    suspend fun createCompany(product: AllCompanyDtoItem): Int?

    suspend fun createCompanies(products: List<AllCompanyDtoItem>)

    suspend fun deleteCompany(id: String)

    suspend fun deleteAllCompanies()
}