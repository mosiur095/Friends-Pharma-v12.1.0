package com.friendspharma.app.features.domain.use_case

import android.net.http.HttpException
import android.os.Build
import android.util.Log

import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.DistrictListDto
import com.friendspharma.app.features.data.remote.model.DivisionListDto
import com.friendspharma.app.features.data.remote.model.UserDetailsDto
import com.friendspharma.app.features.domain.repository.ApiRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetDistrictUseCase @Inject constructor(private val apiRepo: ApiRepo) {

    
    operator fun invoke(divisionId: String): Flow<Async<DistrictListDto>> = flow {
        try {
            emit(Async.Loading())
            val districts = apiRepo.getDistrict(divisionId)
            Log.d("Districts ::::", "invoke: Districts: $districts")
            emit(Async.Success(districts))
        } catch (e: HttpException) {
            emit(Async.Error(e.localizedMessage ?: "An error occurred"))
        } catch (e: IOException) {
            emit(Async.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}