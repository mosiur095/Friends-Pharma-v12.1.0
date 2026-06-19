package com.friendspharma.app.features.data.repository

import com.friendspharma.app.features.data.remote.PathaoApis
import com.friendspharma.app.features.data.remote.entity.PathaoOrder
import com.friendspharma.app.features.data.remote.entity.PathaoToken
import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.data.remote.model.CityDto
import com.friendspharma.app.features.data.remote.model.PathaoOrderDto
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.data.remote.model.ZoneDto
import com.friendspharma.app.features.domain.repository.PathaoApiRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathaoApiRepoImpl @Inject constructor(private val pathaoApis: PathaoApis): PathaoApiRepo{
    override suspend fun getToken(body: PathaoToken): PathaoTokenDto {
        return pathaoApis.getToken(body = body)
    }

    override suspend fun pathaoOrder(body: PathaoOrder, token: String): PathaoOrderDto {
        return pathaoApis.pathaoOrder(body = body, token = token)
    }

    override suspend fun getCities(token: String): CityDto {
        return pathaoApis.getCities(token = token)
    }

    override suspend fun getZoneByCity(
        token: String,
        city: String
    ): ZoneDto {
        return pathaoApis.getZoneByCity(token = token, city = city)
    }

    override suspend fun getAreaByZone(
        token: String,
        zone: String
    ): AreaDto {
        return pathaoApis.getAreaByZone(token = token, zone = zone)
    }
}