package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build
import android.util.Log

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.DivisionListDto
import com.friendspharma.app.features.data.remote.model.UserDetailsDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetDivisionUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(): Flow<Async<DivisionListDto>> = flow {
        try {
            emit(Async.Loading())
            val division = apiRepo.getDivision()
            Log.d("Division ::::", "invoke: division: $division")
            emit(Async.Success(division))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}