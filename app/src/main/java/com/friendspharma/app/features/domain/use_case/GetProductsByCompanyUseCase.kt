package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.repository.ApiRepo
import com.friendspharma.app.features.domain.repository.ProductRepo
import com.friendspharma.app.features.domain.repository.RetailProductRepo
import com.friendspharma.app.features.domain.repository.SpecialProductRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetProductsByCompanyUseCase @Inject constructor(
    private val productRepo: ProductRepo,
    private val retailProductRepo: RetailProductRepo,
    private val specialProductRepo: SpecialProductRepo,
    private val apiRepo: ApiRepo
) {
    
    operator fun invoke(id: Int): Flow<Async<List<ProductsDtoItem>>> = flow {
        try {
            emit(Async.Loading())

            val localProducts =
                if (MainActivity.userType.value == "2") productRepo.getProductsByCompany(id)
                    .sortedBy { it.PRODUCT_NAME }
                else if (MainActivity.userType.value == "3") specialProductRepo.getProductsByCompany(
                    id
                ).sortedBy { it.PRODUCT_NAME }
                else retailProductRepo.getProductsByCompany(id).sortedBy { it.PRODUCT_NAME }

            emit(Async.Success(localProducts))

//            val products = apiRepo.getProductsByCompany(id = id.toString()).data?.sortedBy { it.PRODUCT_NAME }
//            emit(Async.Success(products ?: emptyList()))

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (_: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}