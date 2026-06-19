package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.data.remote.model.ErrorDto
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.domain.repository.SmsApiRepo
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RequestOtpUseCase @Inject constructor(private val smsApiRepo: SmsApiRepo) {

    operator fun invoke(otp: Otp): Flow<Async<OtpDto>> = flow {
        try {
            emit(Async.Loading())
            val otpDto = smsApiRepo.requestOtp(otp)
            emit(Async.Success(otpDto))
        } catch (e: HttpException) {
            val error: ErrorDto = Gson().fromJson(
                e.response()?.errorBody()?.charStream(),
                ErrorDto::class.java
            )
            emit(Async.Error(error.message ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}