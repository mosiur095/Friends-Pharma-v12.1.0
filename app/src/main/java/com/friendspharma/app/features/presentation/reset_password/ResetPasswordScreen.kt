package com.friendspharma.app.features.presentation.reset_password

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavAction
import androidx.navigation.NavHostController
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.presentation.reset_password.components.SuccessDialogue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ResetPasswordScreen(
    navAction: NavigationActions,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    navToLogin: (String) -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scope: CoroutineScope = rememberCoroutineScope()
    ) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.reset_password),
                navAction = navAction
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state = viewModel.state.collectAsStateWithLifecycle()
        val passwordFocusRequester = FocusRequester()
        val confirmPasswordFocusRequester = FocusRequester()

        KeyboardUnFocusHandler()

        if (state.value.showDialogue) {
            SuccessDialogue(onDismiss = {
                navToLogin(state.value.mobile)
            })
        }

        if(state.value.showError){
            scope.launch {
                viewModel.closeSnackBar()
                snackBarHostState.showSnackbar(state.value.message)
            }
        }

        BackHandler {
            navToLogin(state.value.mobile)
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = BackGroundColor)
                .verticalScroll(rememberScrollState())
        ) {


            Column(Modifier.padding(start = 20.dp, end = 20.dp)) {

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .background(color = BackGroundDark, shape = RoundedCornerShape(15.dp))
                ) {
                    Icon(
                        Icons.Filled.Lock, contentDescription = null,
                        modifier = Modifier
                            .padding(15.dp)
                            .size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = stringResource(id = R.string.reset_password),
                    fontSize = 20.sp, color = Gray
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(id = R.string.reset_password_description),
                    color = GrayLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextFieldK(
                    value = state.value.password,
                    label = R.string.enter_password,
                    focusRequester = passwordFocusRequester,
                    onValueChange = { viewModel.passwordChanged(it) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    error = if (state.value.isValidate && state.value.password.isEmpty()) stringResource(
                        id = R.string.enter_password
                    ) else if (state.value.isValidate && (state.value.password.length < 3 || state.value.password.length > 6)) stringResource(
                        id = R.string.password_should_be
                    ) else "",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                TextFieldK(
                    value = state.value.confirmPassword,
                    label = R.string.re_enter_password,
                    focusRequester = confirmPasswordFocusRequester,
                    onValueChange = { viewModel.confirmPasswordChanged(it) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    error = if (state.value.isValidate && state.value.confirmPassword.isEmpty()) stringResource(
                        id = R.string.re_enter_password
                    ) else if (state.value.isValidate && (state.value.confirmPassword.length < 3 || state.value.confirmPassword.length > 6)) stringResource(
                        id = R.string.re_enter_password_error
                    )
                    else if (state.value.isValidate && state.value.password != state.value.confirmPassword) stringResource(
                        id = R.string.password_didnt_match
                    )
                    else "",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(vertical = 6.dp)
                )



                Spacer(modifier = Modifier.height(20.dp))

                ButtonK(
                    text = R.string.reset_password,
                    isValid = state.value.valid,
                    isLoading = state.value.isLoading
                ) {
                    viewModel.submit()
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

        }
    }
}
