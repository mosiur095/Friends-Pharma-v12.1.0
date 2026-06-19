package com.friendspharma.app.features.presentation.update_password

import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.entity.ChangePassword
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val preferenceHelper: SharedPreferenceHelper
) :
    ViewModel() {

    private val _state = MutableStateFlow(UpdatePasswordState())
    val state: StateFlow<UpdatePasswordState> = _state.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() {
        _state.update { it.copy(user = preferenceHelper.getUser()) }
    }

    private fun validate() {
        if (state.value.oldPassword == state.value.user.PASSWORD || state.value.password.isNotEmpty() && state.value.confirmPassword.isNotEmpty() && state.value.password.length > 2 && state.value.password.length < 7 &&
            state.value.confirmPassword.length > 2 && state.value.confirmPassword.length < 7 && state.value.password == state.value.confirmPassword) {
            _state.update { it.copy(valid = true) }
        } else {
            _state.update { it.copy(valid = false) }
        }
    }

    fun signUp(
        passwordFocusRequester: FocusRequester,
        confirmPasswordFocusRequester: FocusRequester,
        oldPasswordFocusRequester: FocusRequester,
        navAction: NavigationActions,
        context: Context
    ) {
        _state.update { it.copy(isValidate = true) }

        if (state.value.oldPassword != state.value.user.PASSWORD) {
            oldPasswordFocusRequester.requestFocus()
        } else if (state.value.password.length < 3 || state.value.password.length > 6) {
            passwordFocusRequester.requestFocus()
        } else if (state.value.confirmPassword != state.value.confirmPassword) {
            confirmPasswordFocusRequester.requestFocus()
        } else {
            changePasswordUseCase.invoke(
                changePassword = ChangePassword(
                    userId = state.value.user.USER_ID.toString(),
                    password = state.value.password
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        Toast.makeText(
                            context,
                            result.data?.message ?: "Success",
                            Toast.LENGTH_LONG
                        ).show()
                        navAction.pop()
                    }

                    is Async.Error -> _state.update { it.copy(isLoading = false) }
                    is Async.Loading -> _state.update { it.copy(isLoading = true) }
                }

            }.launchIn(viewModelScope)
        }
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

    fun oldPasswordChanged(pass: String) {
        _state.update { it.copy(oldPassword = pass) }
        validate()
    }


}