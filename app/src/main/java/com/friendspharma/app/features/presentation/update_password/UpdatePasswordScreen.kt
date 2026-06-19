package com.friendspharma.app.features.presentation.update_password

import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions


@Composable
fun UpdatePasswordScreen(
    viewModel: UpdatePasswordViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions
) {

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.change_password),
                navAction = navAction
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val width = LocalConfiguration.current.screenWidthDp
        val oldPasswordFocusRequester = FocusRequester()
        val passwordFocusRequester = FocusRequester()
        val confirmPassFocusRequester = FocusRequester()
        val context = LocalContext.current

        KeyboardUnFocusHandler()

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
                        .padding(bottom = 40.dp, top = 20.dp)
                        .size((width / 4).dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = state.user.MOBILE_NO ?: "",
                        fontSize = 18.sp,
                        color = Primary,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextFieldK(
                        value = state.oldPassword,
                        label = R.string.enter_old_password,
                        focusRequester = oldPasswordFocusRequester,
                        onValueChange = { viewModel.oldPasswordChanged(it) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        error = if (state.isValidate && state.oldPassword.isEmpty()) stringResource(
                            id = R.string.enter_password
                        ) else if (state.isValidate && (state.oldPassword != state.user.PASSWORD)) stringResource(
                            id = R.string.invalid_old_password
                        ) else "",
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                    TextFieldK(
                        value = state.password,
                        label = R.string.enter_password,
                        focusRequester = passwordFocusRequester,
                        onValueChange = { viewModel.passwordChanged(it) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        error = if (state.isValidate && state.password.isEmpty()) stringResource(
                            id = R.string.enter_password
                        ) else if (state.isValidate && (state.password.length < 3 || state.password.length > 6)) stringResource(
                            id = R.string.password_should_be
                        ) else "",
                        visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.padding(vertical = 6.dp),
                        suffixIcon = {
                            Icon(if (state.showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.showPassword()
                                    })
                        }
                    )

                    TextFieldK(
                        value = state.confirmPassword,
                        label = R.string.re_enter_password,
                        focusRequester = confirmPassFocusRequester,
                        onValueChange = { viewModel.confirmPasswordChanged(it) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        error = if (state.isValidate && state.confirmPassword.isEmpty()) stringResource(
                            id = R.string.re_enter_password
                        ) else if (state.isValidate && (state.confirmPassword.length < 3 || state.confirmPassword.length > 6)) stringResource(
                            id = R.string.re_enter_password_error
                        )
                        else if (state.isValidate && state.password != state.confirmPassword) stringResource(
                            id = R.string.password_didnt_match
                        )
                        else "",
                        visualTransformation = if (state.showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.padding(vertical = 6.dp),
                        suffixIcon = {
                            Icon(if (state.showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.showConPassword()
                                    })
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                    ButtonK(
                        text = R.string.save,
                        isLoading = state.isLoading,
                        isValid = state.valid
                    ) {
                        viewModel.signUp(
                            passwordFocusRequester = passwordFocusRequester,
                            confirmPasswordFocusRequester = confirmPassFocusRequester,
                            oldPasswordFocusRequester = oldPasswordFocusRequester,
                            navAction = navAction,
                            context = context
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

            }
            AppName()
        }


        if (state.isLoading)
            Loader(paddingValues = paddingValues)

    }
}