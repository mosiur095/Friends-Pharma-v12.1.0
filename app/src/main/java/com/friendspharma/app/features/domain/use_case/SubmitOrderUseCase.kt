package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.SubmitOrder
import com.friendspharma.app.features.data.remote.model.SubmitOrderDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class SubmitOrderUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(area: String, submitOrder: SubmitOrder): Flow<Async<SubmitOrderDto>> = flow {
        try {
            emit(Async.Loading())
            val submit = apiRepo.submitOrder(area, submitOrder)
            emit(Async.Success(submit))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}