package com.friendspharma.app.features.presentation.update_profile

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.CustomDropdownField
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import kotlinx.coroutines.CoroutineScope

@SuppressLint("ContextCastToActivity", "RememberInComposition", "ConfigurationScreenWidthHeight")

@Composable
fun UpdateProfileScreen(
    viewModel: UpdateProfileViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scope: CoroutineScope = rememberCoroutineScope()
) {

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.update_profile),
                navAction = navAction
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val width = LocalConfiguration.current.screenWidthDp
        val usernameFocusRequester = FocusRequester()
        val mobileFocusRequester = FocusRequester()
        val addressFocusRequester = FocusRequester()
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
                        .padding(bottom = 20.dp, top = 20.dp)
                        .size((width / 4).dp)
                )

                Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                    // ── User Name ─────────────────────────────────────────────
                    TextFieldK(
                        value = state.userName,
                        onValueChange = { viewModel.userNameChanged(it) },
                        focusRequester = usernameFocusRequester,
                        label = R.string.user_name,
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        keyboardType = KeyboardType.Text,
                        error = if (state.isValidate && state.userName.isEmpty())
                            stringResource(id = R.string.enter_username) else "",
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // ── Mobile Number (read-only) ──────────────────────────────
                    TextFieldK(
                        value = state.mobile,
                        onValueChange = { viewModel.mobileChanged(it) },
                        focusRequester = mobileFocusRequester,
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
                        label = R.string.mobile_number,
                        keyboardType = KeyboardType.Phone,
                        error = if (state.isValidate && !Common.isValidMobile(state.mobile))
                            stringResource(id = R.string.enter_valid_mobile_number) else "",
                        modifier = Modifier.padding(vertical = 6.dp),
                        enabled = false
                    )

                    // ── Address ───────────────────────────────────────────────
                    TextFieldK(
                        value = state.address,
                        onValueChange = { viewModel.addressChanged(it) },
                        focusRequester = addressFocusRequester,
                        label = R.string.address,
                        leadingIcon = { Icon(Icons.Filled.AddLocation, contentDescription = null) },
                        keyboardType = KeyboardType.Text,
                        error = if (state.isValidate && state.address.isEmpty())
                            stringResource(id = R.string.enter_address) else "",
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // ── Division Dropdown ─────────────────────────────────────
                    // Exact same CustomDropdownField as SignUpScreen
                    CustomDropdownField(
                        value = state.selectedDivision?.divisionName ?: "",
                        items = state.divisions.map { it.divisionName ?: "" },
                        onValueChange = { selectedName ->
                            val division = state.divisions.find { it.divisionName == selectedName }
                            division?.let { viewModel.onDivisionSelected(it) }
                        },
                        label = R.string.division,
                        leadingIcon = {
                            Icon(Icons.Filled.AddLocation, contentDescription = null)
                        },
                        error = if (state.isValidate && state.division.isEmpty())
                            stringResource(id = R.string.select_division) else "",
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .padding(top = 5.dp),
                        focusRequester = FocusRequester(),
                    )

                    // ── District Dropdown ─────────────────────────────────────
                    CustomDropdownField(
                        value = state.selectedDistrict?.districtName ?: "",
                        items = state.districts.map { it.districtName ?: "" },
                        onValueChange = { selectedName ->
                            val district = state.districts.find { it.districtName == selectedName }
                            district?.let { viewModel.onDistrictSelected(it) }
                        },
                        label = R.string.district,
                        leadingIcon = {
                            Icon(Icons.Filled.AddLocation, contentDescription = null)
                        },
                        error = if (state.isValidate && state.district.isEmpty())
                            stringResource(id = R.string.select_district) else "",
                        modifier = Modifier.padding(vertical = 6.dp),
                        focusRequester = FocusRequester(),
                    )

                    // ── Thana Dropdown ────────────────────────────────────────
                    CustomDropdownField(
                        value = state.selectedThana?.thanaName ?: "",
                        items = state.thanas.map { it.thanaName ?: "" },
                        onValueChange = { selectedName ->
                            val thana = state.thanas.find { it.thanaName == selectedName }
                            thana?.let { viewModel.onThanaSelected(it) }
                        },
                        label = R.string.thana,
                        leadingIcon = {
                            Icon(Icons.Filled.AddLocation, contentDescription = null)
                        },
                        error = if (state.isValidate && state.post.isEmpty())
                            stringResource(id = R.string.enter_thana) else "",
                        modifier = Modifier.padding(vertical = 6.dp),
                        focusRequester = FocusRequester(),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Update Button ─────────────────────────────────────────
                    Box {
                        ButtonK(
                            text = R.string.update,
                            isLoading = state.isLoading,
                            isValid = state.valid
                        ) {
                            viewModel.updateProfile(
                                usernameFocusRequester = usernameFocusRequester,
                                mobileFocusRequester = mobileFocusRequester,
                                addressFocusRequester = addressFocusRequester,
                                navAction = navAction,
                                context = context
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                AppName()
            }
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)
    }
}