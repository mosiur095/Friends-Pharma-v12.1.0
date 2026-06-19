package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.data.remote.entity.SMSRegistration
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.data.remote.model.SMSRegistrationResponseDto

interface SmsApiRepo {
    suspend fun requestOtp(otp: Otp): OtpDto

    suspend fun checkOtp(checkOtp: CheckOtp): OtpDto

    suspend fun smsRegistration(body: SMSRegistration): SMSRegistrationResponseDto
}