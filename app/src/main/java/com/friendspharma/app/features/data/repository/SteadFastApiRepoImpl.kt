package com.friendspharma.app.features.data.repository

import com.friendspharma.app.features.data.remote.SteadFastApis
import com.friendspharma.app.features.data.remote.entity.SteadFastOrder
import com.friendspharma.app.features.data.remote.model.SteadFastOrderDto
import com.friendspharma.app.features.domain.repository.SteadFastApiRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SteadFastApiRepoImpl @Inject constructor(private val steadFastApis: SteadFastApis) :
    SteadFastApiRepo {
    override suspend fun createOrder(steadFastOrder: SteadFastOrder): SteadFastOrderDto {
        return steadFastApis.createOrder(steadFastOrder = steadFastOrder)
    }
}