package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.domain.repository.SmsApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CheckOtpUseCase @Inject constructor(private val smsApiRepo: SmsApiRepo) {

    operator fun invoke(checkOtp: CheckOtp): Flow<Async<OtpDto>> = flow {
        try {
            emit(Async.Loading())
            val check = smsApiRepo.checkOtp(checkOtp)
            emit(Async.Success(check))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}