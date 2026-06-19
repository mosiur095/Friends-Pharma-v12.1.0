package com.friendspharma.app.features.data.remote

import com.friendspharma.app.features.data.remote.entity.SteadFastOrder
import com.friendspharma.app.features.data.remote.model.SteadFastOrderDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private const val API_KEY = "lnjyd9e7n05x88n0rrztqjvvuwjc1g2b"
private const val SECRET_KEY = "g8taeul4ferdjfzsfohbg30s"

interface SteadFastApis {

    @Headers("Content-Type: application/json", "Api-Key: $API_KEY", "Secret-Key: $SECRET_KEY")
    @POST("create_order")
    suspend fun createOrder(@Body steadFastOrder: SteadFastOrder): SteadFastOrderDto
}