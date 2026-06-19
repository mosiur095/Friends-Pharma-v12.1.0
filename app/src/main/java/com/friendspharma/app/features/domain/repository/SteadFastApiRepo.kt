package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.entity.SteadFastOrder
import com.friendspharma.app.features.data.remote.model.SteadFastOrderDto

interface SteadFastApiRepo {
    suspend fun createOrder(steadFastOrder: SteadFastOrder): SteadFastOrderDto
}