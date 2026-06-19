package com.friendspharma.app.features.data.repository

import com.friendspharma.app.features.data.remote.SmsApis
import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.data.remote.entity.SMSRegistration
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.data.remote.model.SMSRegistrationResponseDto
import com.friendspharma.app.features.domain.repository.SmsApiRepo
import javax.inject.Inject

class SmsApiRepoImpl @Inject constructor(private val smsApis: SmsApis): SmsApiRepo {
    override suspend fun requestOtp(otp: Otp): OtpDto {
        return smsApis.requestOtp(otp = otp)
    }

    override suspend fun checkOtp(checkOtp: CheckOtp): OtpDto {
        return smsApis.checkOtp(checkOtp = checkOtp)
    }

    override suspend fun smsRegistration(body: SMSRegistration): SMSRegistrationResponseDto {
        return smsApis.smsRegistration(body)
    }
}