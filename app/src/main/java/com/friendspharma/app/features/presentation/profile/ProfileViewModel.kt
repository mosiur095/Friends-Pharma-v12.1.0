package com.friendspharma.app.features.presentation.profile

import android.app.Application
import android.os.Build

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.fcm.FcmTokenManager
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.UserDetailsDtoData
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val preferenceHelper: SharedPreferenceHelper,
    private val getUserUseCase: GetUserUseCase,
    private val fcmTokenManager: FcmTokenManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun getUser() {
        getUserUseCase.invoke(preferenceHelper.getUser().MOBILE_NO ?: "").onEach { result ->
            when (result) {
                is Async.Success -> {
                    if (!result.data?.data.isNullOrEmpty())
                        _state.update {
                            it.copy(
                                user = result.data?.data?.get(0) ?: UserDetailsDtoData()
                            )
                        }
                }
                is Async.Error<*>   -> {}
                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun logOut(navAction: NavigationActions) {
        preferenceHelper.deleteAll()

        // Unsubscribe from wholesale/special topics — keep all_users for generic notifications
        fcmTokenManager.clearTopics()

        MainActivity.userType.value   = ""
        MainActivity.isLoggedIn.value = false

        viewModelScope.launch {
            delay(200)
            navAction.navToMain()
        }
    }
}