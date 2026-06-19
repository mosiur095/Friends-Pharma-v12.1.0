package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import com.friendspharma.app.features.domain.repository.CompanyRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetAllCompanyUseCase @Inject constructor(private val apiRepo: ApiRepo, private val companyRepo: CompanyRepo) {
    
    operator fun invoke(): Flow<Async<AllCompanyDto>> = flow {
        try {
            emit(Async.Loading())

            val localCompanies = companyRepo.getCompanies().sortedBy { it.COMPANY_NAME }

            if (localCompanies.isNotEmpty()) {
                emit(Async.Success(AllCompanyDto(data = localCompanies)))
            }

            val companies = apiRepo.getAllCompany()
            emit(Async.Success(companies.copy(data = companies.data?.sortedBy { it.COMPANY_NAME })))

            companyRepo.deleteAllCompanies()
            companyRepo.createCompanies(companies.data ?: emptyList())

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}