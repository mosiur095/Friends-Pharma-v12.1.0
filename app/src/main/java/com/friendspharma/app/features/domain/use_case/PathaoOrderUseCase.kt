package com.friendspharma.app.features.domain.use_case

import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.PathaoOrder
import com.friendspharma.app.features.data.remote.model.PathaoOrderDto
import com.friendspharma.app.features.data.remote.model.PathaoOrderErrorDto
import com.friendspharma.app.features.domain.repository.PathaoApiRepo
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PathaoOrderUseCase @Inject constructor(private val pathaoApiRepo: PathaoApiRepo) {

    
    operator fun invoke(body: PathaoOrder, token: String): Flow<Async<PathaoOrderDto>> = flow {
        try {
            emit(Async.Loading())
            val token = pathaoApiRepo.pathaoOrder(body, token = token)
            emit(Async.Success(token))

        } catch (e: HttpException) {

            val error: PathaoOrderErrorDto = Gson().fromJson(
                e.response()?.errorBody()?.charStream(),
                PathaoOrderErrorDto::class.java
            )
            emit(Async.Error(Gson().toJson(error.errors)))

        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}