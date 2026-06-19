package com.friendspharma.app.features.presentation.forgot_password

import android.app.Application
import android.os.Build

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.features.data.remote.entity.Otp
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.RequestOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    application: Application,
    private val getUserUseCase: GetUserUseCase,
    private val requestOtpUseCase: RequestOtpUseCase
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()


    fun mobileNumberChanged(mobile: String) {
        _state.value = state.value.copy(mobile = mobile)
        validate()
    }

    private fun validate() {
        if (Common.isValidMobile(state.value.mobile)
        ) {
            _state.value = state.value.copy(valid = true)
        } else {
            _state.value = state.value.copy(valid = false)
        }
    }

    
    fun checkPhoneNumber(navToOtp: (String) -> Unit) {
        _state.update { it.copy(isLoading = true) }

        getUserUseCase.invoke("88" + state.value.mobile).onEach { result ->
            println(result.data)
            when (result) {
                is Async.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    if (!result.data?.data.isNullOrEmpty()) {
                        sendOtp(navToOtp)
                    } else {
                        _state.update { it.copy(message = "Invalid User") }
                    }
                }

                is Async.Error<*> -> {
                    _state.update { it.copy(isLoading = false) }
                }

                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)


    }

    private fun sendOtp(navToOtp: (String) -> Unit) {
        _state.value = state.value.copy(isValidate = true)
        if (Common.isValidMobile(state.value.mobile) && !state.value.isLoading) {

            requestOtpUseCase.invoke(otp = Otp(msisdn = "88" + state.value.mobile))
                .onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            _state.value = state.value.copy(
                                isLoading = false,
                                showSnackBar = true,
                                message = result.data?.response ?: ""
                            )
                            navToOtp(state.value.mobile)
                        }

                        is Async.Error -> {
                            _state.value = state.value.copy(
                                isLoading = false,
                                showSnackBar = true,
                                message = "Failed"
                            )
                        }

                        is Async.Loading -> {
                            _state.value = state.value.copy(isLoading = true)
                        }
                    }

                }.launchIn(viewModelScope)

        }
    }

    fun closeMessage() {
        _state.value = state.value.copy(message = "")
    }
}