package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build
import android.util.Log

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.TokenDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetTokenUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(): Flow<Async<TokenDto>> = flow {
        try {
            emit(Async.Loading())
            val token = apiRepo.getToken()
            Log.d("Token", "Token received: ${token.data}")
            emit(Async.Success(token))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }

}