package com.friendspharma.app.features.data.remote

import com.friendspharma.app.features.data.remote.entity.PathaoOrder
import com.friendspharma.app.features.data.remote.entity.PathaoToken
import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.data.remote.model.CityDto
import com.friendspharma.app.features.data.remote.model.PathaoOrderDto
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.data.remote.model.ZoneDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface PathaoApis {

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("issue-token")
    suspend fun getToken(@Body body: PathaoToken): PathaoTokenDto

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("orders")
    suspend fun pathaoOrder(
        @Header("Authorization") token: String,
        @Body body: PathaoOrder
    ): PathaoOrderDto

    @Headers("Content-Type: application/json", "accept: application/json")
    @GET("city-list")
    suspend fun getCities(@Header("Authorization") token: String): CityDto

    @Headers("Content-Type: application/json", "accept: application/json")
    @GET("cities/{city_id}/zone-list")
    suspend fun getZoneByCity(@Header("Authorization") token: String, @Path("city_id") city: String): ZoneDto

    @Headers("Content-Type: application/json", "accept: application/json")
    @GET("zones/{zone_id}/area-list")
    suspend fun getAreaByZone(@Header("Authorization") token: String, @Path("zone_id") zone: String): AreaDto

}