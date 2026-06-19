package com.friendspharma.app.features.presentation.login

import android.content.Context
import android.os.Build

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.model.LoginDto
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.friendspharma.app.core.fcm.FcmTokenManager


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val preferenceHelper: SharedPreferenceHelper,
    private val fcmTokenManager: FcmTokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mobile: String = checkNotNull(savedStateHandle[ScreenArgs.DATA] ?: "")

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    val scope = CoroutineScope(Dispatchers.Main)

    init {
        _state.value = state.value.copy(mobile = mobile)
    }

    fun mobileChanged(mobile: String) {
        _state.update { it.copy(mobile = mobile) }
        validate()
    }

    fun passwordChanged(pass: String) {
        _state.update { it.copy(password = pass) }
        validate()
    }

    fun showPassword() {
        _state.update { it.copy(showPassword = !state.value.showPassword) }
    }

    fun closeSnackBar() {
        scope.cancel()
        _state.update { it.copy(message = "") }
    }

    fun dismissRestrictionDialog() {
        _state.update { it.copy(restrictionType = RestrictionType.NONE) }
    }

    private fun validate() {
        val isValid = Common.isValidMobile(state.value.mobile) &&
                state.value.password.isNotEmpty() &&
                state.value.password.length > 2 &&
                state.value.password.length < 7
        _state.update { it.copy(valid = isValid) }
    }

    fun login(
        mobileFocusRequester: FocusRequester,
        navAction: NavigationActions,
        context: Context,
        passwordFocusRequester: FocusRequester,
    ) {
        _state.update { it.copy(isValidate = true) }

        when {
            !Common.isValidMobile(state.value.mobile) -> {
                mobileFocusRequester.requestFocus()
            }
            state.value.password.isEmpty() ||
                    state.value.password.length < 3 ||
                    state.value.password.length > 6 -> {
                passwordFocusRequester.requestFocus()
            }
            else -> callLoginApi(navAction)
        }
    }

    private fun callLoginApi(navAction: NavigationActions) {
        loginUseCase.invoke(
            userName = "88" + state.value.mobile,
            password = state.value.password
        ).onEach { result ->
            when (result) {
                is Async.Loading -> _state.update { it.copy(isLoading = true) }
                is Async.Error -> _state.update { it.copy(isLoading = false) }
                is Async.Success -> handleLoginSuccess(result, navAction)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleLoginSuccess(
        result: Async.Success<LoginDto>,
        navAction: NavigationActions
    ) {
        _state.update { it.copy(isLoading = false) }

        val user = result.data?.data?.get(0)

        if (result.data?.status == 200 && user != null) {
            when {
                user.APPROVAL_STATUS?.equals("Approved", ignoreCase = true) != true -> {
                    _state.update { it.copy(restrictionType = RestrictionType.PENDING_APPROVAL) }
                }
                user.ACTIVE_FLAG?.equals("Active", ignoreCase = true) != true -> {
                    _state.update { it.copy(restrictionType = RestrictionType.BLOCKED) }
                }
                else -> {
                    preferenceHelper.saveUser(
                        user.copy(PASSWORD = state.value.password)
                    )
                    val userType = user.USER_TYPE ?: "1"
                    MainActivity.userType.value   = userType
                    MainActivity.isLoggedIn.value = true

                    // Sync FCM token + subscribe to correct topic for this user type
                    fcmTokenManager.syncOnLogin()

                    if (userType == "4") {
                        navAction.navToDeliveryMan()
                    } else {
                        navAction.pop()
                    }
                }
            }
        } else {
            _state.update { it.copy(message = result.data?.message ?: "Login failed") }
        }
    }
}