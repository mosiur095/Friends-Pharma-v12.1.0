package com.friendspharma.app.features.presentation.sign_up

import android.net.Uri
import com.friendspharma.app.features.data.remote.model.DistrictData
import com.friendspharma.app.features.data.remote.model.DivisionData
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.data.remote.model.ThanaData
import java.io.File

data class SignUpState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val message: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val rememberMe: Boolean = false,
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val thana: String = "",
    val division: String = "",
    val district: String = "",
    val userType: String = "",
    val drugno: String = "",
    val success: Boolean = false,
    val otpResponse: OtpDto = OtpDto(),
    val pin: String = "",
    val image: Uri? = null,
    val imageFile: File? = null,

    val divisions: List<DivisionData> = emptyList(),
    val selectedDivision: DivisionData? = null,
    val isDropdownExpanded: Boolean = false,

    val districts: List<DistrictData> = emptyList(),
    val selectedDistrict: DistrictData? = null,

    val thanas: List<ThanaData> = emptyList(),
    val selectedThana: ThanaData? = null,

    )
