package com.friendspharma.app.features.presentation.update_profile

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.core.util.customer
import com.friendspharma.app.core.util.wholeSeller
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.UpdateProfile
import com.friendspharma.app.features.data.remote.model.DistrictData
import com.friendspharma.app.features.data.remote.model.DivisionData
import com.friendspharma.app.features.data.remote.model.ThanaData
import com.friendspharma.app.features.data.remote.model.UserDetailsDtoData
import com.friendspharma.app.features.domain.use_case.GetDistrictUseCase
import com.friendspharma.app.features.domain.use_case.GetDivisionUseCase
import com.friendspharma.app.features.domain.use_case.GetThanaUseCase
import com.friendspharma.app.features.domain.use_case.UpdateProfileUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val divisionUseCase: GetDivisionUseCase,
    private val districtUseCase: GetDistrictUseCase,
    private val thanaUseCase: GetThanaUseCase,
    application: android.app.Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val userData: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])
    private val user: UserDetailsDtoData =
        Gson().fromJson(userData, UserDetailsDtoData::class.java)

    private val _state = MutableStateFlow(UpdateProfileState())
    val state: StateFlow<UpdateProfileState> = _state.asStateFlow()

    // Parsed address parts from the stored ADDRESS string ("address, thana, district")
    private var parsedAddress: String = ""
    private var parsedThana: String = ""
    private var parsedDistrict: String = ""

    init {
        parseUserAddress()
        getDivision()
    }

    // ── Parse stored address ──────────────────────────────────────────────────

    private fun parseUserAddress() {
        val parts = user.ADDRESS?.split(",")?.map { it.trim() } ?: emptyList()
        parsedDistrict = if (parts.size >= 1) parts.last() else ""
        parsedThana = if (parts.size >= 2) parts[parts.size - 2] else ""
        parsedAddress = if (parts.size >= 3) parts.subList(0, parts.size - 2).joinToString(", ") else ""

        _state.update {
            it.copy(
                userName = user.USER_NAME ?: "",
                mobile = user.MOBILE_NO?.let { m -> if (m.length > 2) m.substring(2) else m } ?: "",
                email = user.EMAIL ?: "",
                address = parsedAddress,
                post = parsedThana,
                district = parsedDistrict,
                userType = if (user.USER_TYPE == 2) wholeSeller else customer
            )
        }
    }

    // ── Division ──────────────────────────────────────────────────────────────

    private fun getDivision() {
        divisionUseCase.invoke()
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("UpdateProfile", "getDivision: ${result.data}")
                        val divisions = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update { it.copy(divisions = divisions) }

                        // Pre-select the division that matches the user's stored district.
                        // We load districts for each division until we find the match,
                        // or fall back to the first division if no match found.
                        if (divisions.isNotEmpty()) {
                            // Start by loading districts for the first division — getDistrict
                            // will check if the user's district belongs there and cascade.
                            getDistrictForInit(divisions, 0)
                        }
                    }
                    is Async.Error -> Log.e("UpdateProfile", "getDivision error: ${result.message}")
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Called only during init to find which division owns the user's stored district.
     * Iterates through divisions in order; when a match is found, pre-selects it and
     * loads the matching district's thanas. If no match is found after all divisions,
     * falls back to selecting the first division.
     */
    private fun getDistrictForInit(divisions: List<DivisionData>, index: Int) {
        if (index >= divisions.size) {
            // Fallback: no match found — select first division normally
            onDivisionSelected(divisions[0])
            return
        }
        val division = divisions[index]
        districtUseCase.invoke(division.divisionId.toString())
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        val districts = result.data?.data?.filterNotNull() ?: emptyList()
                        val matchedDistrict = districts.firstOrNull {
                            it.districtName?.trim().equals(parsedDistrict.trim(), ignoreCase = true)
                        }
                        if (matchedDistrict != null) {
                            // Found the right division — update state and load thanas
                            _state.update {
                                it.copy(
                                    selectedDivision = division,
                                    division = division.divisionName ?: "",
                                    districts = districts,
                                    selectedDistrict = matchedDistrict,
                                    district = matchedDistrict.districtName ?: ""
                                )
                            }
                            getThanaForInit(matchedDistrict)
                        } else {
                            // Try next division
                            getDistrictForInit(divisions, index + 1)
                        }
                    }
                    is Async.Error -> {
                        Log.e("UpdateProfile", "getDistrictForInit error: ${result.message}")
                        getDistrictForInit(divisions, index + 1)
                    }
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    private fun getThanaForInit(district: DistrictData) {
        thanaUseCase.invoke(district.districtId.toString())
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        val thanas = result.data?.data?.filterNotNull() ?: emptyList()
                        val matchedThana = thanas.firstOrNull {
                            it.thanaName?.trim().equals(parsedThana.trim(), ignoreCase = true)
                        }
                        _state.update {
                            it.copy(
                                thanas = thanas,
                                selectedThana = matchedThana,
                                post = matchedThana?.thanaName ?: parsedThana
                            )
                        }
                        validate()
                    }
                    is Async.Error -> Log.e("UpdateProfile", "getThanaForInit error: ${result.message}")
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    // ── User-triggered dropdown selections (same as SignUpViewModel) ──────────

    fun onDivisionSelected(division: DivisionData) {
        _state.update {
            it.copy(
                selectedDivision = division,
                division = division.divisionName ?: "",
                // Reset district and thana when user changes division
                districts = emptyList(),
                selectedDistrict = null,
                district = "",
                thanas = emptyList(),
                selectedThana = null,
                post = ""
            )
        }
        getDistrict(division.divisionId.toString())
    }

    private fun getDistrict(divisionId: String) {
        districtUseCase.invoke(divisionId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("UpdateProfile", "getDistrict: ${result.data}")
                        val districts = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update { it.copy(districts = districts) }
                    }
                    is Async.Error -> Log.e("UpdateProfile", "getDistrict error: ${result.message}")
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun onDistrictSelected(district: DistrictData) {
        _state.update {
            it.copy(
                selectedDistrict = district,
                district = district.districtName ?: "",
                // Reset thana when user changes district
                thanas = emptyList(),
                selectedThana = null,
                post = ""
            )
        }
        getThana(district.districtId.toString())
    }

    private fun getThana(districtId: String) {
        thanaUseCase.invoke(districtId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("UpdateProfile", "getThana: ${result.data}")
                        val thanas = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update { it.copy(thanas = thanas) }
                    }
                    is Async.Error -> Log.e("UpdateProfile", "getThana error: ${result.message}")
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun onThanaSelected(thana: ThanaData) {
        _state.update {
            it.copy(
                selectedThana = thana,
                post = thana.thanaName ?: ""
            )
        }
        validate()
    }

    // ── Other field handlers ──────────────────────────────────────────────────

    fun userNameChanged(username: String) {
        _state.update { it.copy(userName = username) }
        validate()
    }

    fun mobileChanged(mobile: String) {
        _state.update { it.copy(mobile = mobile) }
        validate()
    }

    fun emailChanged(email: String) {
        _state.update { it.copy(email = email) }
        validate()
    }

    fun addressChanged(address: String) {
        _state.update { it.copy(address = address) }
        validate()
    }

    fun typeChanged(type: String) {
        _state.update { it.copy(userType = type) }
        validate()
    }

    fun drugNoChanged(drugNo: String) {
        _state.update { it.copy(drugno = drugNo) }
        validate()
    }

    fun passwordChanged(pass: String) {
        _state.update { it.copy(password = pass) }
        validate()
    }

    fun showPassword() {
        _state.update { it.copy(showPassword = !state.value.showPassword) }
    }

    fun confirmPasswordChanged(conPass: String) {
        _state.update { it.copy(confirmPassword = conPass) }
        validate()
    }

    fun showConPassword() {
        _state.update { it.copy(showConfirmPassword = !state.value.showConfirmPassword) }
    }

    fun closeSnackBar() {
        _state.update { it.copy(message = "") }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validate() {
        val s = state.value
        val isValid = s.userName.isNotEmpty()
                && Common.isValidMobile(s.mobile)
                && s.address.isNotEmpty()
                && s.division.isNotEmpty()
                && s.district.isNotEmpty()
                && s.post.isNotEmpty()
                && s.userType.isNotEmpty()
                && (s.userType != wholeSeller || s.drugno.isNotEmpty())
        _state.update { it.copy(valid = isValid) }
    }

    // ── Update API call ───────────────────────────────────────────────────────

    fun updateProfile(
        usernameFocusRequester: FocusRequester,
        mobileFocusRequester: FocusRequester,
        addressFocusRequester: FocusRequester,
        navAction: NavigationActions,
        context: Context
    ) {
        _state.update { it.copy(isValidate = true) }
        val s = state.value
        when {
            s.userName.isEmpty() -> usernameFocusRequester.requestFocus()
            !Common.isValidMobile(s.mobile) -> mobileFocusRequester.requestFocus()
            s.address.isEmpty() -> addressFocusRequester.requestFocus()
            s.division.isEmpty() || s.district.isEmpty() || s.post.isEmpty() -> {
                // Validation errors shown in UI via isValidate flag — no focus needed for dropdowns
            }
            else -> {
                updateProfileUseCase.invoke(
                    UpdateProfile(
                        USER_NAME = s.userName,
                        MOBILE_NO = "88" + s.mobile,
                        ADDRESS = "${s.address}, ${s.post.replace(",", "")}, ${s.district.replace(",", "")}",
                        FILE_NAME = "",
                        LICENSE_IMG_NAME = "",
                        USER_ID = user.USER_ID.toString()
                    )
                ).onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            Toast.makeText(context, result.data?.message ?: "Success", Toast.LENGTH_LONG).show()
                            navAction.pop()
                        }
                        is Async.Error -> _state.update { it.copy(isLoading = false) }
                        is Async.Loading -> _state.update { it.copy(isLoading = true) }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
}