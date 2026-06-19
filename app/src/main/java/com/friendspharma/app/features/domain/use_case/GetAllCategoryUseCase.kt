package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.AddressDto
import com.friendspharma.app.features.data.remote.model.AllCategoryDto
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import com.friendspharma.app.features.domain.repository.CategoryRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(private val apiRepo: ApiRepo, private val categoryRepo: CategoryRepo) {
    
    operator fun invoke(): Flow<Async<AllCategoryDto>> = flow {
        try {
            emit(Async.Loading())

            val localCategories = categoryRepo.getCategories().sortedBy { it.CATEGORY_NAME }

            if (localCategories.isNotEmpty()) {
                emit(Async.Success(AllCategoryDto(data = localCategories)))
            }

            val categories = apiRepo.getAllCategory()
            emit(Async.Success(categories.copy(data = categories.data?.sortedBy { it.CATEGORY_NAME })))

            categoryRepo.deleteAllCategories()
            categoryRepo.createCategories(categories.data ?: emptyList())

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}