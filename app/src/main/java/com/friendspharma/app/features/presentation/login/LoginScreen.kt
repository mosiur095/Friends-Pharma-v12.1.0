package com.friendspharma.app.features.presentation.login

import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.presentation.login.components.RestrictedAccountDialog
import com.friendspharma.app.features.presentation.sign_up.components.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.sign_in),
                navAction = navAction
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val width = LocalConfiguration.current.screenWidthDp
        val usernameFocusRequester = FocusRequester()
        val passwordFocusRequester = FocusRequester()
        val context = LocalContext.current

        KeyboardUnFocusHandler()

        // ✅ Show snackbar ONLY for wrong mobile/password errors
        if (state.message.isNotEmpty() && !state.success) {
            UserMessage(state.message) {
                scope.launch {
                    viewModel.closeSnackBar()
                }
            }
        }

        // 🚫 Blocked by admin dialog
        if (state.restrictionType == RestrictionType.BLOCKED) {
            RestrictedAccountDialog(
                title = "Account Blocked",
                message = "You are not authorized to place orders. " +
                        "Please contact support to resolve this issue.",
                onDismiss = { viewModel.dismissRestrictionDialog() }
            )
        }

        // ⏳ Pending approval dialog
        if (state.restrictionType == RestrictionType.PENDING_APPROVAL) {
            RestrictedAccountDialog(
                title = "Account Not Approved",
                message = "Your account registration is under review and has not been " +
                        "approved yet. You are not authorized to place orders until " +
                        "admin approval. Please contact support for assistance.",
                onDismiss = { viewModel.dismissRestrictionDialog() }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 10.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_small),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(bottom = 20.dp, top = 20.dp)
                        .size((width / 4).dp)
                )

                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    TextFieldK(
                        value = state.mobile,
                        onValueChange = { viewModel.mobileChanged(it) },
                        focusRequester = usernameFocusRequester,
                        label = R.string.mobile_number,
                        keyboardType = KeyboardType.Phone,
                        error = if (state.isValidate && !Common.isValidMobile(state.mobile))
                            stringResource(id = R.string.enter_valid_mobile_number) +
                                    " ${state.mobile.length}/11"
                        else "",
                        leadingIcon = {
                            Row(
                                modifier = Modifier.padding(start = 15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "88 ", fontSize = 20.sp)
                            }
                        },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextFieldK(
                        value = state.password,
                        label = R.string.enter_password,
                        focusRequester = passwordFocusRequester,
                        onValueChange = { viewModel.passwordChanged(it) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                        },
                        error = if (state.isValidate && state.password.isEmpty())
                            stringResource(id = R.string.enter_password)
                        else if (state.isValidate &&
                            (state.password.length < 3 || state.password.length > 6))
                            stringResource(id = R.string.password_should_be)
                        else "",
                        visualTransformation = if (state.showPassword)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.padding(vertical = 6.dp),
                        suffixIcon = {
                            Icon(
                                if (state.showPassword) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    viewModel.showPassword()
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                    ButtonK(
                        text = R.string.sign_in,
                        isLoading = state.isLoading,
                        isValid = state.valid
                    ) {
                        viewModel.login(
                            mobileFocusRequester = usernameFocusRequester,
                            passwordFocusRequester = passwordFocusRequester,
                            navAction = navAction,
                            context = context
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(id = R.string.is_forgot_password),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { navAction.navToForgotPassword() }
                            .padding(end = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.dont_have_account),
                        color = GrayLight,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(id = R.string.create_account),
                        color = Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.clickable {
                            navAction.navToSignUp("1")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)
    }
}