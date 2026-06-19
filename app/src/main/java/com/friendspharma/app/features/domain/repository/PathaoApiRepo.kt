package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.entity.PathaoOrder
import com.friendspharma.app.features.data.remote.entity.PathaoToken
import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.data.remote.model.CityDto
import com.friendspharma.app.features.data.remote.model.PathaoOrderDto
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.data.remote.model.ZoneDto

interface PathaoApiRepo {

    suspend fun getToken(body: PathaoToken): PathaoTokenDto
    suspend fun pathaoOrder(body: PathaoOrder, token: String): PathaoOrderDto
    suspend fun getCities(token: String): CityDto
    suspend fun getZoneByCity(token: String, city: String): ZoneDto
    suspend fun getAreaByZone(token: String, zone: String): AreaDto

}