package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build
import android.util.Log

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.DistrictListDto
import com.friendspharma.app.features.data.remote.model.DivisionListDto
import com.friendspharma.app.features.data.remote.model.ThanaListDto
import com.friendspharma.app.features.data.remote.model.UserDetailsDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetThanaUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(districtId: String): Flow<Async<ThanaListDto>> = flow {
        try {
            emit(Async.Loading())
            val thana = apiRepo.getThana(districtId)
            Log.d("Thana ::::", "invoke: Thana: $thana")
            emit(Async.Success(thana))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}