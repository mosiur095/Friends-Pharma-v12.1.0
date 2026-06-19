package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.domain.repository.PathaoApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetAreasByZoneUseCase @Inject constructor(private val pathaoApiRepo: PathaoApiRepo) {

    
    operator fun invoke(token: String, zone: String): Flow<Async<AreaDto>> = flow {
        try {
            emit(Async.Loading())
            val order = pathaoApiRepo.getAreaByZone(token, zone)
            emit(Async.Success(order))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}