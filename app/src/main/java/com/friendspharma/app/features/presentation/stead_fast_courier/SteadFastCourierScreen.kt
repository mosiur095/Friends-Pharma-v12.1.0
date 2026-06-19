package com.friendspharma.app.features.presentation.stead_fast_courier

import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions


@Composable
fun SteadFastCourierScreen(
    viewModel: SteadFastCourierViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyListState = rememberLazyListState()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.stead_fast),
                navAction = navAction,
                icon = R.drawable.baseline_commute_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val recipientNameFocusRequester = FocusRequester()
        val recipientPhoneFocusRequester = FocusRequester()
        val recipientAddressFocusRequester = FocusRequester()
        val amountToCollectFocusRequester = FocusRequester()
        val itemDescriptionFocusRequester = FocusRequester()

        KeyboardUnFocusHandler()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)

        ) {

            LazyColumn(
                Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f),
                state = scrollSate
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                item {
                    Column {

                        TextFieldK(
                            value = state.recipientName,
                            onValueChange = { viewModel.recipientNameChanged(it) },
                            focusRequester = recipientNameFocusRequester,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            label = R.string.recipient_name,
                            keyboardType = KeyboardType.Text,
                            error = if (state.isValidate && (state.recipientName.isEmpty())) stringResource(
                                id = R.string.enter_recipient_name
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientPhone,
                            onValueChange = { viewModel.recipientPhoneChanged(it) },
                            focusRequester = recipientPhoneFocusRequester,
                            label = R.string.recipient_phone,
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardType = KeyboardType.Phone,
                            error = if (state.isValidate && !Common.isValidMobile(state.recipientPhone)) stringResource(
                                id = R.string.enter_valid_mobile_number
                            ) + " ${state.recipientPhone.length}/11" else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientAddress,
                            onValueChange = { viewModel.recipientAddressChanged(it) },
                            focusRequester = recipientAddressFocusRequester,
                            label = R.string.recipient_address,
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            error = if (state.isValidate && state.recipientAddress.isEmpty()) stringResource(
                                id = R.string.enter_recipient_address
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.codAmount.toString(),
                            focusRequester = amountToCollectFocusRequester,
                            onValueChange = { viewModel.amountToCollectChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null
                                )
                            },
                            label = R.string.cod_ammount,
                            keyboardType = KeyboardType.Number,
                            error = if (state.isValidate && state.codAmount.toDouble() < 0) stringResource(
                                id = R.string.enter_cod_amount
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.note,
                            focusRequester = itemDescriptionFocusRequester,
                            onValueChange = { viewModel.itemDescriptionChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null
                                )
                            },
                            label = R.string.item_description,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ButtonK(R.string.submit) {
                            viewModel.submit(
                                recipientNameFocusRequester,
                                recipientPhoneFocusRequester,
                                recipientAddressFocusRequester,
                                amountToCollectFocusRequester,
                                context
                            )
                        }
                    }
                }
            }
            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)

    }
}