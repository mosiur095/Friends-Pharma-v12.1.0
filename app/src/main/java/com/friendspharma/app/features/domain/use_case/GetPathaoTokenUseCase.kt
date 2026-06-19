package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.PathaoToken
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.domain.repository.PathaoApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetPathaoTokenUseCase @Inject constructor(private val pathaoApiRepo: PathaoApiRepo) {

    
    operator fun invoke(body: PathaoToken): Flow<Async<PathaoTokenDto>> = flow {
        try {
            emit(Async.Loading())
            val token = pathaoApiRepo.getToken(body)
            emit(Async.Success(token))

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }

}