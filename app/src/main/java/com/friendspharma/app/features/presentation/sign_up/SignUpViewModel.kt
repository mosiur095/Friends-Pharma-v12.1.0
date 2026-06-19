package com.friendspharma.app.features.presentation.sign_up

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.IntentSenderRequest

import androidx.compose.ui.focus.FocusRequester
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.core.util.customer
import com.friendspharma.app.core.util.wholeSeller
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.entity.InsertAddress
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.data.remote.entity.SignUp
import com.friendspharma.app.features.data.remote.entity.SignUpSeller
import com.friendspharma.app.features.data.remote.model.AllCategoryDto
import com.friendspharma.app.features.data.remote.model.DistrictData
import com.friendspharma.app.features.data.remote.model.DivisionData
import com.friendspharma.app.features.data.remote.model.DivisionListDto
import com.friendspharma.app.features.data.remote.model.LoginDto
import com.friendspharma.app.features.data.remote.model.OtpDto
import com.friendspharma.app.features.data.remote.model.ThanaData
import com.friendspharma.app.features.data.remote.model.UserDto
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.CheckOtpUseCase
import com.friendspharma.app.features.domain.use_case.GetDistrictUseCase
import com.friendspharma.app.features.domain.use_case.GetDivisionUseCase
import com.friendspharma.app.features.domain.use_case.GetThanaUseCase
import com.friendspharma.app.features.domain.use_case.InsertAddressUseCase
import com.friendspharma.app.features.domain.use_case.RequestOtpUseCase
import com.friendspharma.app.features.domain.use_case.SignUpWholeSellerUseCase
import com.friendspharma.app.features.domain.use_case.SingUpUseCase
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.invoke

// ✅ Removed @RequiresExtension from @HiltViewModel class
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SingUpUseCase,
    application: android.app.Application,
    private val signUpWholeSellerUseCase: SignUpWholeSellerUseCase,
    private val preferenceHelper: SharedPreferenceHelper,
    private val requestOtpUseCase: RequestOtpUseCase,
    private val checkOtpUseCase: CheckOtpUseCase,
    private val insertAddressUseCase: InsertAddressUseCase,
    private val divisionUseCase: GetDivisionUseCase,
    private val districtUseCase: GetDistrictUseCase,
    private val thanaUseCase: GetThanaUseCase,

    savedStateHandle: SavedStateHandle
) :
    AndroidViewModel(application) {

    private val id: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])

    private var fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private var geocoder: Geocoder = Geocoder(application)
    private var addresses: List<Address>? = listOf()


    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    val scope = CoroutineScope(Dispatchers.Main)

    init {
        _state.update { it.copy(userType = if (id == "2") wholeSeller else customer) }
        getDivision()

    }

    fun dismissOTPDialog() {
        _state.update { it.copy(otpResponse = OtpDto()) }
    }

    fun pinNumberChanged(pin: String) {
        _state.update { it.copy(pin = pin) }
    }

    fun checkLocationSetting(
        context: Context,
        activity: Activity,
        onDisabled: (IntentSenderRequest) -> Unit,
        onEnabled: () -> Unit
    ) {

        // ✅ LocationRequest.create() deprecated → use LocationRequest.Builder()
        val locationRequest = LocationRequest.Builder(1000)
            .setMinUpdateIntervalMillis(1000)
            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
            .Builder()
            .addLocationRequest(locationRequest)

        val gpsSettingTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(builder.build())

        gpsSettingTask.addOnSuccessListener { getLocation(context, activity) }
        gpsSettingTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution)
                        .build()
                    onDisabled(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // ignore here
                }
            }
        }

    }

    fun getLocation(context: Context, activity: Activity) {

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        val task = fusedLocationProviderClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        task.addOnSuccessListener {
            if (it != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(it.latitude, it.longitude, 1) { addrs ->
                            addresses = addrs
                            var address = ""
                            var district = ""
                            var thana = ""
                            if (addrs.isNotEmpty()) {
                                address  = addrs[0].getAddressLine(0) ?: ""
                                district = addrs[0].subAdminArea ?: ""
                                thana    = addrs[0].subLocality ?: ""
                            }
                            if (address.isNotEmpty()) {
                                _state.update { it.copy(address = address, district = district, thana = thana) }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        var address = ""
                        var district = ""
                        var subDistrict = ""
                        var thana = ""
                        if (!addresses.isNullOrEmpty()) {
                            address  = addresses?.get(0)?.getAddressLine(0) ?: ""
                            district = addresses?.get(0)?.subAdminArea ?: ""
                            thana    = addresses?.get(0)?.subLocality ?: ""
                        }
                        if (address.isNotEmpty()) {
                            _state.update { it.copy(address = address, district = district, thana = thana) }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }


    fun userNameChanged(username: String) {
        _state.update { it.copy(userName = username) }
        validate()
    }

    fun closeSnackBar() {
        scope.cancel()
        _state.update { it.copy(message = "") }
    }

    private fun validate() {
        if (state.value.userName.isEmpty() || !Common.isValidMobile(state.value.mobile) || state.value.address.isEmpty() || state.value.thana.isEmpty() || state.value.district.isEmpty()
            || state.value.password.isEmpty() || state.value.password.length < 3 || state.value.password.length > 6 || state.value.confirmPassword.isEmpty() || state.value.confirmPassword.length < 2 ||
            state.value.confirmPassword.length > 6 || state.value.password != state.value.confirmPassword || state.value.userType.isEmpty() || (state.value.userType == wholeSeller && state.value.drugno.isEmpty())
        ) {
            _state.update { it.copy(valid = false) }
        } else {
            _state.update { it.copy(valid = true) }
        }
    }

    fun checkPageValidations(
        mobileFocusRequester: FocusRequester,
        usernameFocusRequester: FocusRequester,
        passwordFocusRequester: FocusRequester,
        confirmPasswordFocusRequester: FocusRequester,
        addressFocusRequester: FocusRequester,
        districtFocusRequester: FocusRequester,
        subDistrictFocusRequester: FocusRequester,
        userTypeFocusRequester: FocusRequester,
        drugnoFocusRequester: FocusRequester,
        postFocusRequester: FocusRequester,
        navAction: NavigationActions,
        context: Context
    ) {
        _state.update { it.copy(isValidate = true) }
        if (state.value.userName.isEmpty()) {
            usernameFocusRequester.requestFocus()
        } else if (!Common.isValidMobile(state.value.mobile)) {
            mobileFocusRequester.requestFocus()
        } else if (state.value.address.isEmpty()) {
            addressFocusRequester.requestFocus()
        } else if (state.value.thana.isEmpty()) {
            postFocusRequester.requestFocus()
        } else if (state.value.district.isEmpty()) {
            districtFocusRequester.requestFocus()
        } else if (state.value.userType.isEmpty()) {
            userTypeFocusRequester.requestFocus()
        } else if (state.value.userType == wholeSeller && state.value.drugno.isEmpty()) {
            drugnoFocusRequester.requestFocus()
        } else if (state.value.password.length < 3 || state.value.password.length > 6) {
            passwordFocusRequester.requestFocus()
        } else if (state.value.password != state.value.confirmPassword) {
            confirmPasswordFocusRequester.requestFocus()
        } else {
            sendOtp()
        }
    }

    private fun signUp(navAction: NavigationActions) {
        if (state.value.userType == wholeSeller) {
            signUpWholeSellerUseCase.invoke(
                SignUpSeller(
                    userName = state.value.userName,
                    mobileNo = "88" + state.value.mobile,
                    email = state.value.email,
                    passWordNo = state.value.password,
                    address = state.value.address + ", " + state.value.thana.replace(
                        ",",
                        ""
                    ) + ", " + state.value.district.replace(",", ""),
                    usertype = if (state.value.userType == customer) "1" else if (state.value.userType == wholeSeller) "2" else "3",
                    drugno = state.value.drugno
                ),
                image = state.value.imageFile
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        if (result.data?.status == 200 && !result.data?.data.isNullOrEmpty()) {
                            insertAddress(navAction, result.data)
                        } else {
                            _state.update {
                                it.copy(
                                    message = result.data?.message ?: "Failed",
                                    isLoading = false
                                )
                            }
                        }
                    }

                    is Async.Error -> _state.update {
                        it.copy(
                            isLoading = false,
                            message = result.message ?: "Sign Up Failed"
                        )
                    }

                    is Async.Loading -> _state.update { it.copy(isLoading = true) }
                }

            }.launchIn(viewModelScope)
        } else {
            signUpUseCase.invoke(
                SignUp(
                    userName = state.value.userName,
                    mobileNo = "88" + state.value.mobile,
                    email = state.value.email,
                    passWordNo = state.value.password,
                    address = state.value.address + ", " + state.value.thana.replace(
                        ",",
                        ""
                    ) + ", " + state.value.district.replace(",", "")
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        if (result.data?.status == 200 && !result.data?.data.isNullOrEmpty()) {
                            insertAddress(navAction, result.data)
                        } else {
                            _state.update {
                                it.copy(
                                    message = result.data?.message ?: "Failed",
                                    isLoading = false
                                )
                            }
                        }
                    }

                    is Async.Error -> _state.update {
                        it.copy(
                            isLoading = false,
                            message = result.message ?: "Sign Up Failed"
                        )
                    }

                    is Async.Loading -> _state.update { it.copy(isLoading = true) }
                }

            }.launchIn(viewModelScope)
        }
    }

    private fun getDivision() {
        divisionUseCase.invoke()
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("getDivision", "data: ${result.data}")
                        val tempDivisions = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                divisions = tempDivisions
                            )
                        }
                        if (tempDivisions.isNotEmpty()){
                            onDivisionSelected(tempDivisions[0])
                        }
                    }

                    is Async.Error -> {
                        Log.e("getDivision", "error: ${result.message}")
                    }
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }
    fun onDivisionSelected(division: DivisionData) {
        _state.update {
            it.copy(
                selectedDivision = division,
                division = division.divisionName ?: "",
                isDropdownExpanded = false,
                district = "",
                thana = ""
            )
        }

        getDistrict(division.divisionId.toString())
    }

    private fun getDistrict(divisionId: String) {
        districtUseCase.invoke(divisionId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("getDistrict", "data: ${result.data}")
                        val tempDistricts = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                districts = tempDistricts
                            )
                        }

                        if (tempDistricts.isNotEmpty()){
                            onDistrictSelected(tempDistricts[0])
                        }

                    }

                    is Async.Error -> {
                        Log.e("getDistrict", "error: ${result.message}")
                    }
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }
    fun onDistrictSelected(district: DistrictData) {
        _state.update {
            it.copy(
                selectedDistrict = district,
                district = district.districtName ?: "",
                isDropdownExpanded = false,
                thana = ""
            )
        }

        getThana(district.districtId.toString())
    }


    private fun getThana(districtId: String) {
        thanaUseCase.invoke(districtId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Log.d("getThana", "data: ${result.data}")
                        val tempThanas = result.data?.data?.filterNotNull() ?: emptyList()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                thanas = tempThanas
                            )
                        }

                        if (tempThanas.isNotEmpty()){
                            onThanaSelected(tempThanas[0])
                        }

                    }

                    is Async.Error -> {
                        Log.e("getThana", "error: ${result.message}")
                    }
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }
    fun onThanaSelected(thana: ThanaData) {
        _state.update {
            it.copy(
                selectedThana = thana,
                thana = thana.thanaName ?: "",
                isDropdownExpanded = false,
            )
        }
    }

    fun delayAndBack(navAction: NavigationActions) {
        scope.launch {
            delay(3000)
            navAction.pop()
        }
    }


    fun mobileChanged(mobile: String) {
        _state.update { it.copy(mobile = mobile) }
        validate()
    }

    fun emailChanged(email: String) {
        _state.update { it.copy(email = email) }
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

    fun districtChanged(string: String) {
        _state.update { it.copy(district = string) }
        validate()
    }

    fun postChanged(string: String) {
        _state.update { it.copy(thana = string) }
        validate()
    }

    fun divisionChanged(value: String) {
        _state.update { it.copy(division = value, district = "", thana = "") }
    }


    fun thanaChanged(value: String) {
        _state.update { it.copy(thana = value) }
    }



    fun sendOtp() {
        _state.value = state.value.copy(isValidate = false)

        if (Common.isValidMobile(state.value.mobile) && !state.value.isLoading) {

            requestOtpUseCase.invoke(otp = Otp(msisdn = "88" + state.value.mobile))
                .onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            if (result.data?.response == "success") {
                                _state.value = state.value.copy(
                                    isLoading = false,
                                    otpResponse = result.data
                                )
                            } else {
                                _state.value = state.value.copy(
                                    isLoading = false,
                                    message = result.data?.response ?: "Failed"
                                )
                            }
                        }

                        is Async.Error -> {
                            _state.value = state.value.copy(
                                isLoading = false,
                                message = result.message ?: "Failed"
                            )
                        }

                        is Async.Loading -> {
                            _state.value = state.value.copy(isLoading = true)
                        }
                    }

                }.launchIn(viewModelScope)

        }
    }

    fun verifyOtp(navAction: NavigationActions) {
        _state.value = state.value.copy(isValidate = true)

        if (state.value.pin.length == 6) {
            _state.update { it.copy(isLoading = true) }
            checkOtpUseCase.invoke(
                CheckOtp(
                    msisdn = "88" + state.value.mobile,
                    password = state.value.pin
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                        )
                        if (result.data?.response == "true") {
                            _state.update { it.copy(otpResponse = OtpDto()) }
                            signUp(navAction)
                        } else {
                            _state.value = state.value.copy(
                                message = "Invalid OTP"
                            )
                        }
                    }

                    is Async.Error -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                            message = "Invalid OTP"
                        )
                    }

                    is Async.Loading -> {
                        _state.value = state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)

        }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    fun uploadImage(context: Context, image: Uri) {
        _state.update { it.copy(image = image, imageFile = uriToFile(context, image)) }
    }

    fun insertAddress(navAction: NavigationActions, data: LoginDto) {
        insertAddressUseCase.invoke(
            InsertAddress(
                userId = data.data[0].USER_ID.toString(),
                address = state.value.address,
                addrType = "Home"
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    preferenceHelper.saveUser(
                        data?.data?.get(0)?.copy(PASSWORD = state.value.password)
                            ?: UserDto()
                    )
                    MainActivity.isLoggedIn.value = true
                    MainActivity.userType.value =
                        preferenceHelper.getUser().USER_TYPE ?: "1"

                    if (data.data?.get(0)?.APPROVAL_STATUS == "Approved") {
                        _state.update {
                            it.copy(
                                message = result.data?.message ?: "Success",
                                success = true
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                message = "Approval Status : ${
                                    data?.data?.get(
                                        0
                                    )?.APPROVAL_STATUS
                                }",
                                success = true
                            )
                        }
                    }
                    delayAndBack(navAction)
                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }




}