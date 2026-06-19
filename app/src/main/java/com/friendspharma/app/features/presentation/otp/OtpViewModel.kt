package com.friendspharma.app.features.presentation.otp

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.CheckOtp
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.domain.use_case.CheckOtpUseCase
import com.friendspharma.app.features.domain.use_case.RequestOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class OtpViewModel @Inject constructor(
    application: Application,
    private val checkOtpUseCase: CheckOtpUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(OtpState())
    val state: StateFlow<OtpState> = _state.asStateFlow()

    private val phone: String = checkNotNull(savedStateHandle[ScreenArgs.DATA] ?: "")

    private val timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            _state.value = state.value.copy(time = (millisUntilFinished / 1000).toString())
        }

        override fun onFinish() {
            _state.value = state.value.copy(time = "")
        }

    }

    init {
        _state.value = state.value.copy(phone = phone)
        timer.start()
    }

    fun mobileNumberChanged(mobile: String) {
        _state.value = state.value.copy(otp = mobile)
        validate()
    }

    private fun validate() {
        if (state.value.otp.length == 6
        ) {
            _state.value = state.value.copy(valid = true)
        } else {
            _state.value = state.value.copy(valid = false)
        }
    }

    fun submit(navToResetPassword: (String) -> Unit) {
        _state.value = state.value.copy(isValidate = true)
        if (state.value.otp.length == 6) {

            checkOtpUseCase.invoke(
                CheckOtp(
                    msisdn = "88" + state.value.phone,
                    password = state.value.otp
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                            showSnackBar = true,
                            message = result.data?.response ?: ""
                        )
                        if (result.data?.response == "true") {
                            navToResetPassword(state.value.phone)
                        } else {
                            _state.value = state.value.copy(
                                showSnackBar = true,
                                message = "Invalid OTP"
                            )
                        }
                    }

                    is Async.Error -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                            showSnackBar = true,
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

    fun closeSnackBar() {
        _state.value = state.value.copy(showSnackBar = false)
    }

    fun resend() {
        if (state.value.time.isEmpty()) {
            timer.start()
            requestOtpUseCase.invoke(otp = Otp(msisdn = "88" + state.value.phone))
                .onEach { result ->

                    when (result) {
                        is Async.Success -> {
                            _state.value = state.value.copy(
                                isLoading = false,
                                showSnackBar = true,
                                message = result.data?.response ?: ""
                            )
                        }

                        is Async.Error -> {
                            _state.value = state.value.copy(
                                isLoading = false,
                                showSnackBar = true,
                                message = "Failed to request OTP"
                            )
                        }

                        is Async.Loading -> {
                            _state.value = state.value.copy(isLoading = true)
                        }
                    }

                }.launchIn(viewModelScope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}