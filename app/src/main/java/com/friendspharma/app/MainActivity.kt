package com.friendspharma.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.friendspharma.app.core.fcm.FcmTokenManager
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavGraph
import com.friendspharma.app.features.ScreenRoute
import com.friendspharma.app.features.domain.services.LocalConstant
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.use_case.AddToCartRestrictUseCase
import com.friendspharma.app.features.domain.use_case.GetCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.GetTokenUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.InsertNotificationUseCase
import com.friendspharma.app.features.domain.use_case.ProductRemoveUseCase
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var token = ""
        val isLoggedIn = mutableStateOf(false)
        val userType = mutableStateOf("")
        val isRestrict = mutableIntStateOf(-1)
        val cartQuantity = mutableIntStateOf(0)
        var shouldReloadProducts = mutableStateOf(false)
        var onForcedLogout: (() -> Unit)? = null
        var pendingForcedLogout: Boolean = false

        // Tune these to control when the update becomes mandatory.
        private const val FORCE_AFTER_STALE_DAYS = 3   // force once update is this old
        private const val HIGH_PRIORITY_THRESHOLD = 4  // updatePriority set in Play Console (0..5)
    }

    @Inject lateinit var preferenceHelper: SharedPreferenceHelper
    @Inject lateinit var getTokenUseCase: GetTokenUseCase
    @Inject lateinit var getRestrictUseCase: AddToCartRestrictUseCase
    @Inject lateinit var getUserUseCase: GetUserUseCase
    @Inject lateinit var getCartInfoUseCase: GetCartInfoUseCase
    @Inject lateinit var productRemoveUseCase: ProductRemoveUseCase
    @Inject lateinit var insertNotificationUseCase: InsertNotificationUseCase
    @Inject lateinit var fcmTokenManager: FcmTokenManager

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isVerifying = false
    private var isFirstLoad = true
    private var statusPollJob: Job? = null
    private lateinit var appUpdateManager: AppUpdateManager

    // Remember whether the last started flow was the mandatory (immediate) one,
    // so we can re-prompt if the user tries to dodge it.
    private var lastFlowWasImmediate = false

    // Listens for the background (flexible) download finishing.
    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            promptCompleteUpdate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateListener)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavGraph(startDestination = ScreenRoute.Splash.route)
            }
        }

        getToken()
        checkLogin()
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    // Handles notification tap from system tray (app closed or background).
    // Firebase puts custom data fields directly in intent extras with their
    // original key names (title, body, type, route, productId).
    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val bundle = intent?.extras ?: return

        val title = bundle.getString("title")
        val body = bundle.getString("body")
        val type = bundle.getString("type") ?: "GENERAL"
        val route = bundle.getString("route")
        val productId = bundle.getString("productId")
        val imageUrl  = bundle.getString("imageUrl")

        // Only save if this intent actually came from a notification tap
        if (title.isNullOrEmpty() && body.isNullOrEmpty()) return

        scope.launch {
            runCatching {
                insertNotificationUseCase.invoke(
                    NotificationDto(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        body = body,
                        type = type,
                        route = route,
                        productId = productId,
                        imageUrl  = imageUrl
                    )
                )
                Log.d("FCM", "Notification saved from intent tap \u2713")
            }.onFailure {
                Log.e("FCM", "Failed to save notification from intent: ${it.message}")
            }
        }
    }

    private fun checkLogin() {
        val savedUser = preferenceHelper.getUser()
        if (savedUser.USER_ID == null) {
            fcmTokenManager.clearTopics()
            isLoggedIn.value = false
            return
        }
        isLoggedIn.value = true
        userType.value = savedUser.USER_TYPE ?: "1"
        isVerifying = true
        fcmTokenManager.syncOnLogin()
        verifyUserStatus(savedUser.MOBILE_NO ?: return)
    }

    private fun verifyUserStatus(mobileNo: String) {
        getUserUseCase.invoke(id = mobileNo).onEach { result ->
            when (result) {
                is Async.Success -> {
                    isVerifying = false
                    val user = result.data?.data?.getOrNull(0)
                    if (user != null) {
                        val isInactive = user.ACTIVE_FLAG
                            ?.equals("Inactive", ignoreCase = true) == true
                        if (isInactive) clearCartThenLogout(mobileNo)
                    }
                }
                is Async.Error   -> { isVerifying = false }
                is Async.Loading -> {}
            }
        }.launchIn(scope)
    }

    fun clearCartThenLogout(mobileNo: String) {
        getCartInfoUseCase.invoke(mobileNo).onEach { cartResult ->
            when (cartResult) {
                is Async.Success -> {
                    val cartItems = cartResult.data?.data ?: emptyList()
                    if (cartItems.isEmpty()) {
                        forceLogout()
                    } else {
                        cartItems.forEach { item ->
                            productRemoveUseCase.invoke(
                                ProductRemove(
                                    mobile_no    = mobileNo,
                                    pid_product  = item.PID_PRODUCT.toString(),
                                    pid_tran_dtl = item.PID_TRAN_DTL.toString(),
                                    salesunit    = item.SALES_UNIT ?: ""
                                )
                            ).launchIn(scope)
                        }
                        forceLogout()
                    }
                }
                is Async.Error   -> { forceLogout() }
                is Async.Loading -> {}
            }
        }.launchIn(scope)
    }

    fun forceLogout() {
        stopStatusPolling()
        preferenceHelper.deleteAll()
        scope.launch {
            withContext(Dispatchers.Main) {
                userType.value        = ""
                cartQuantity.intValue = 0
                fcmTokenManager.clearTopics()
                if (onForcedLogout != null) {
                    onForcedLogout?.invoke()
                } else {
                    pendingForcedLogout = true
                }
                isLoggedIn.value = false
            }
        }
        Log.d("MainActivity", "User is inactive. Logging out.")
    }

    private fun getToken() {
        val token = preferenceHelper.getToken()
        MainActivity.token = token
        getTokenUseCase.invoke().onEach { result ->
            when (result) {
                is Async.Success -> {
                    MainActivity.token = result.data?.data ?: ""
                    Log.d("user token", "${result.data?.data}")
                    preferenceHelper.saveStringData(
                        LocalConstant.token,
                        result.data?.data ?: ""
                    )
                }
                is Async.Error   -> {}
                is Async.Loading -> {}
            }
        }.launchIn(scope)
    }

    override fun onResume() {
        super.onResume()
        checkInAppUpdate()
        if (isFirstLoad) {
            isFirstLoad = false
            return
        }
        if (isLoggedIn.value && !isVerifying) {
            isVerifying = true
            verifyUserStatus(preferenceHelper.getUser().MOBILE_NO ?: return)
        }
        if (isLoggedIn.value) startStatusPolling()
        else stopStatusPolling()
    }

    private fun startStatusPolling() {
        if (!isLoggedIn.value) return
        statusPollJob?.cancel()
        statusPollJob = scope.launch {
            while (true) {
                delay(20_000)
                if (isLoggedIn.value && !isVerifying) {
                    isVerifying = true
                    verifyUserStatus(preferenceHelper.getUser().MOBILE_NO ?: return@launch)
                }
            }
        }
    }

    private fun stopStatusPolling() {
        statusPollJob?.cancel()
    }

    // HYBRID update strategy:
    //   - Fresh release  -> FLEXIBLE: downloads in the background, user keeps working,
    //                       installs on a quiet restart. User may dismiss for now.
    //   - Stale (>= FORCE_AFTER_STALE_DAYS) or high priority -> IMMEDIATE: mandatory,
    //                       blocks the app until updated, so the update WILL happen.
    private fun checkInAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            // A flexible download already finished (e.g. while backgrounded) -> install it.
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                promptCompleteUpdate()
                return@addOnSuccessListener
            }

            if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE
                && info.updateAvailability() != UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) return@addOnSuccessListener

            val staleDays = info.clientVersionStalenessDays() ?: 0
            val highPriority = info.updatePriority() >= HIGH_PRIORITY_THRESHOLD
            val mustForce = (staleDays >= FORCE_AFTER_STALE_DAYS || highPriority) &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            when {
                mustForce -> {
                    lastFlowWasImmediate = true
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    lastFlowWasImmediate = false
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
            }
        }
    }

    // Called once the new version is downloaded (flexible flow). completeUpdate()
    // restarts the app to apply it. Swap the Toast for a Snackbar with a "Restart"
    // action if you'd rather let the user pick the moment.
    private fun promptCompleteUpdate() {
        Toast.makeText(this, "Update ready \u2014 applying\u2026", Toast.LENGTH_LONG).show()
        appUpdateManager.completeUpdate()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                if (lastFlowWasImmediate) {
                    // Mandatory update was dismissed -> re-prompt so it still happens.
                    Log.d("InAppUpdate", "Mandatory update dismissed. Re-prompting\u2026")
                    checkInAppUpdate()
                } else {
                    // Optional (flexible) update declined -> leave the user alone.
                    Log.d("InAppUpdate", "Flexible update declined or failed.")
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateListener)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello \$name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreevingPreview() {
    Greeting("Android")
}