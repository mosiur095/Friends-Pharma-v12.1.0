package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.UpdateInvoiceRequest
import com.friendspharma.app.features.data.remote.model.UpdateInvoiceDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UpdateInvoiceUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    operator fun invoke(request: UpdateInvoiceRequest): Flow<Async<UpdateInvoiceDto>> =
        flow {
            try {
                emit(Async.Loading())

                val result = apiRepo.updateInvoice(request = request)
                emit(Async.Success(result))

            } catch (e: HttpException) {
                // Non-2xx response (e.g. 404 while endpoint not yet registered, 500, etc.)
                emit(Async.Error(e.localizedMessage ?: "Server error"))
            } catch (e: IOException) {
                emit(Async.Error("Couldn't reach server. Check your internet connection."))
            } catch (e: Exception) {
                // Safety net: serialization issues, unexpected errors — never crash
                emit(Async.Error(e.localizedMessage ?: "Something went wrong"))
            }
        }
}