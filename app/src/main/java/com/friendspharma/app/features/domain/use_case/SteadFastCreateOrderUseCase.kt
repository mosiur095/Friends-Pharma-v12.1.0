package com.friendspharma.app.features.domain.use_case

import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.SteadFastOrder
import com.friendspharma.app.features.data.remote.model.SteadFastOrderDto
import com.friendspharma.app.features.domain.repository.SteadFastApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SteadFastCreateOrderUseCase @Inject constructor(private val steadFastApiRepo: SteadFastApiRepo) {

    
    operator fun invoke(body: SteadFastOrder): Flow<Async<SteadFastOrderDto>> = flow {
        try {
            emit(Async.Loading())
            val token = steadFastApiRepo.createOrder(body)
            emit(Async.Success(token))

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An unexpected error occurred"))

        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}