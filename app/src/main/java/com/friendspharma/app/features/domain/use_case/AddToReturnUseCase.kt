package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.AddReturn
import com.friendspharma.app.features.data.remote.model.AddReturnDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AddToReturnUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    operator fun invoke(addReturn: AddReturn): Flow<Async<AddReturnDto>> =
        flow {
            try {
                emit(Async.Loading())

                val result = apiRepo.addToReturn(addReturn = addReturn)
                emit(Async.Success(result))

            } catch (e: HttpException) {
                emit(Async.Error(e.localizedMessage ?: "An error occurred"))
            } catch (e: IOException) {
                emit(Async.Error("Couldn't reach server. Check your internet connection."))
            } catch (e: Exception) {
                emit(Async.Error(e.localizedMessage ?: "Something went wrong"))
            }
        }
}