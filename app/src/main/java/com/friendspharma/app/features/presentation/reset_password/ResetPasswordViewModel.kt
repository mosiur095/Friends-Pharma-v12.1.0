package com.friendspharma.app.features.presentation.reset_password

import android.app.Application
import android.os.Build

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.ChangePassword
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject



@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    application: Application,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val getUserUseCase: GetUserUseCase,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    private val mobile: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])

    init {
        _state.value = state.value.copy(mobile = mobile)
        getUser()
    }

    
    fun getUser() {
        _state.update { it.copy(isLoading = true) }

        getUserUseCase.invoke("88" + state.value.mobile).onEach { result ->
            when (result) {
                is Async.Success -> {
                    if (!result.data?.data.isNullOrEmpty()) {
                        _state.update {
                            it.copy(
                                userId = (result.data?.data?.get(0)?.USER_ID ?: "").toString()
                            )
                        }
                    } else {
                        _state.update { it.copy(message = "Invalid User") }
                    }
                    _state.update { it.copy(isLoading = false) }
                }

                is Async.Error<*> -> {
                    _state.update { it.copy(isLoading = false) }
                }

                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)


    }

    fun passwordChanged(password: String) {
        _state.value = state.value.copy(password = password)
        validate()
    }

    fun confirmPasswordChanged(password: String) {
        _state.value = state.value.copy(confirmPassword = password)
        validate()
    }

    private fun validate() {
        if (state.value.password.isNotEmpty() && state.value.confirmPassword.isNotEmpty() && state.value.password.length > 2 && state.value.password.length < 7 &&
            state.value.confirmPassword.length > 2 && state.value.confirmPassword.length < 7 && state.value.password == state.value.confirmPassword) {
            _state.update { it.copy(valid = true) }
        } else {
            _state.value = state.value.copy(valid = false)
        }
    }

    fun submit() {
        _state.value = state.value.copy(isValidate = true)
        if (state.value.password.length > 2 && state.value.password.length < 7) {
            resetPasswordUseCase.invoke(
                ChangePassword(userId = state.value.userId, password = state.value.password)
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                            showDialogue = true,
                            message = "Success"
                        )
                        //navToLogin()
                    }

                    is Async.Error -> {
                        _state.value = state.value.copy(
                            isLoading = false,
                            showError = true,
                            message = result.message ?: "Unknown error"
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
        _state.value = state.value.copy(showError = false)
    }
}