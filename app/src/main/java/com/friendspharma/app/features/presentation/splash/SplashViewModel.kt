package com.friendspharma.app.features.presentation.splash

import android.os.Build

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.GetAllCompanyUseCase
import com.friendspharma.app.features.domain.use_case.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceHelper: SharedPreferenceHelper,
    private val getProductsUseCase: GetProductsUseCase,
    private val getAllCompanyUseCase: GetAllCompanyUseCase
) : ViewModel() {

    fun isLoggedIn(): Boolean {
        return !preferenceHelper.getUser().MOBILE_NO.isNullOrEmpty()
    }

    // Pre-fetch products during the 2s splash delay so HomeScreen loads instantly
    
    fun preFetch() {
        val user       = preferenceHelper.getUser()
        val userType   = user.USER_TYPE ?: ""
        val isLoggedIn = !user.MOBILE_NO.isNullOrEmpty()

        // Delivery man (type 4) has no product screen — skip
        if (userType == "4") return

        // Set global state so GetProductsUseCase uses the correct API endpoint
        MainActivity.userType.value   = if (isLoggedIn) userType else ""
        MainActivity.isLoggedIn.value = isLoggedIn

        viewModelScope.launch {
            launch { prefetchCompanies() }
            launch { prefetchProducts() }
        }
    }

    
    private suspend fun prefetchProducts() {
        try {
            getProductsUseCase.invoke().collect { /* cache warm-up, silent */ }
        } catch (e: Exception) { /* silent — HomeViewModel will retry */ }
    }

    
    private suspend fun prefetchCompanies() {
        try {
            getAllCompanyUseCase.invoke().collect { /* silent */ }
        } catch (e: Exception) { /* silent */ }
    }

    fun navigate(navAction: NavigationActions) {
        if (preferenceHelper.getUser().USER_TYPE == "4") {
            navAction.navToDeliveryMan()
        } else {
            navAction.navToMain()
        }
    }
}
