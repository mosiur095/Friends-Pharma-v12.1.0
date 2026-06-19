package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.SubmitReturn
import com.friendspharma.app.features.data.remote.model.ErrorDto
import com.friendspharma.app.features.data.remote.model.SubmitReturnDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SubmitReturnUseCase @Inject constructor(private val apiRepo: ApiRepo) {
    operator fun invoke(submitReturn: SubmitReturn): Flow<Async<SubmitReturnDto>> = flow {
        try {
            emit(Async.Loading())
            val resetPassword = apiRepo.submitReturn(submitReturn = submitReturn)
            emit(Async.Success(resetPassword))
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