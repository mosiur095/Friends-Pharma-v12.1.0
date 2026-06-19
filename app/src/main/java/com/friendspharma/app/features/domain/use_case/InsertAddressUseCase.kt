package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.InsertAddress
import com.friendspharma.app.features.data.remote.model.DefaultDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class InsertAddressUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(
        body: InsertAddress
    ): Flow<Async<DefaultDto>> = flow {
        try {
            emit(Async.Loading())

            val address =
                apiRepo.insertAddress(body = body)
            emit(Async.Success(address))

        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}