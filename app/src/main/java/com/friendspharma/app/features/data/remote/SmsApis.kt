package com.friendspharma.app.features.data.remote

import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.data.remote.entity.SMSRegistration
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.data.remote.model.SMSRegistrationResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SmsApis {
    @Headers("Content-Type: application/json")
    @POST("otpreq")
    suspend fun requestOtp(@Body otp: Otp): OtpDto

    @Headers("Content-Type: application/json")
    @POST("otpcheck")
    suspend fun checkOtp(@Body checkOtp: CheckOtp): OtpDto

    @Headers("Content-Type: application/json")
    @POST("smsregistration")
    suspend fun smsRegistration(@Body body: SMSRegistration): SMSRegistrationResponseDto
}