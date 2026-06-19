package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.SignUpSeller
import com.friendspharma.app.features.data.remote.model.LoginDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.IOException
import javax.inject.Inject

class SignUpWholeSellerUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(signUp: SignUpSeller, image: File?): Flow<Async<LoginDto>> = flow {
        try {
            emit(Async.Loading())
            val token = apiRepo.signUpWholeSeller(signUp = signUp, image)
            emit(Async.Success(token))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}