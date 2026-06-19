package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import com.friendspharma.app.features.domain.repository.CartRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetCartInfoUseCase @Inject constructor(
    private val apiRepo: ApiRepo,
    private val cartRepo: CartRepo
) {

    
    operator fun invoke(mobile: String): Flow<Async<CartInfoDto>> = flow {
        try {
            emit(Async.Loading())

//            val cartItems = cartRepo.getProducts()
//            if (cartItems.isNotEmpty()) {
//                emit(Async.Success(CartInfoDto(data = cartItems)))
//            }

            val productInfo = apiRepo.getCartInfo(mobile)
            emit(Async.Success(productInfo))

            //cartRepo.createProducts(productInfo.data ?: emptyList())
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }

}