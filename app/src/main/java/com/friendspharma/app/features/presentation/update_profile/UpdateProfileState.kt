package com.friendspharma.app.features.presentation.update_profile

import com.friendspharma.app.features.data.remote.model.DistrictData
import com.friendspharma.app.features.data.remote.model.DivisionData
import com.friendspharma.app.features.data.remote.model.ThanaData

data class UpdateProfileState(
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
    val post: String = "",      // thana name — sent to API
    val district: String = "", // district name — sent to API
    val division: String = "", // division name — sent to API
    val userType: String = "",
    val drugno: String = "",

    // Division dropdown — mirrors SignUpState
    val divisions: List<DivisionData> = emptyList(),
    val selectedDivision: DivisionData? = null,

    // District dropdown — mirrors SignUpState
    val districts: List<DistrictData> = emptyList(),
    val selectedDistrict: DistrictData? = null,

    // Thana dropdown — mirrors SignUpState
    val thanas: List<ThanaData> = emptyList(),
    val selectedThana: ThanaData? = null,
)