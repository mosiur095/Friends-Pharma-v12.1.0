package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.CompanyDao
import com.friendspharma.app.features.data.local.dao.toExternal
import com.friendspharma.app.features.data.local.dao.toLocal
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import com.friendspharma.app.features.domain.repository.CompanyRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CompanyRepoImpl @Inject constructor(
    private val companyDao: CompanyDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
) : CompanyRepo {
    override suspend fun getCompanies(): List<AllCompanyDtoItem> {
        return withContext(dispatcher) {
            companyDao.getAll().toExternal()
        }
    }

    override suspend fun getCompany(id: String): AllCompanyDtoItem? {
        return withContext(dispatcher) {
            companyDao.getById(id)?.toExternal()
        }
    }

    override suspend fun createCompany(product: AllCompanyDtoItem): Int? {
        withContext(dispatcher) {
            companyDao.upsert(product.toLocal())
        }
        return product.PID_COMPANY
    }

    override suspend fun createCompanies(products: List<AllCompanyDtoItem>) {
        withContext(dispatcher) {
            companyDao.upsertAll(products.toLocal())
        }
    }

    override suspend fun deleteCompany(id: String) {
        withContext(dispatcher) {
            companyDao.deleteById(id)
        }
    }

    override suspend fun deleteAllCompanies() {
        withContext(dispatcher) {
            companyDao.deleteAll()
        }
    }
}