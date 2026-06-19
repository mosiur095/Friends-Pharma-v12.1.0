package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.DeliveredDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class ConfirmCashCollectionUseCase @Inject constructor(private val apiRepo: ApiRepo) {
    
    operator fun invoke(id: String, user: String): Flow<Async<DeliveredDto>> = flow {
        try {
            emit(Async.Loading())
            val deliver = apiRepo.confirmCashCollection(id, user)
            emit(Async.Success(deliver))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}