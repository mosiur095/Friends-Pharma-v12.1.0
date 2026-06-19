package com.friendspharma.app.features.presentation.pathao_courier

import android.os.Build
import android.widget.Toast

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
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.TypeSpecimen
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.friendspharma.app.features.presentation.pathao_courier.components.AreaDialog
import com.friendspharma.app.features.presentation.pathao_courier.components.CityDialog
import com.friendspharma.app.features.presentation.pathao_courier.components.DeliveryTypeDialog
import com.friendspharma.app.features.presentation.pathao_courier.components.ItemTypeDialog
import com.friendspharma.app.features.presentation.pathao_courier.components.ZoneDialog


@Composable
fun PathaoCourierScreen(
    viewModel: PathaoCourierViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyListState = rememberLazyListState()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.pathao),
                navAction = navAction,
                icon = R.drawable.baseline_commute_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val merchantOrderIdFocusRequester = FocusRequester()
        val recipientNameFocusRequester = FocusRequester()
        val recipientPhoneFocusRequester = FocusRequester()
        val recipientAddressFocusRequester = FocusRequester()
        val recipientCityFocusRequester = FocusRequester()
        val recipientZoneFocusRequester = FocusRequester()
        val recipientAreaFocusRequester = FocusRequester()
        val deliveryTypeFocusRequester = FocusRequester()
        val itemTypeFocusRequester = FocusRequester()
        val specialInstructionFocusRequester = FocusRequester()
        val itemQuantityFocusRequester = FocusRequester()
        val itemWeightFocusRequester = FocusRequester()
        val amountToCollectFocusRequester = FocusRequester()
        val itemDescriptionFocusRequester = FocusRequester()
        val openCityDialog = remember { mutableStateOf(false) }
        val openZoneDialog = remember { mutableStateOf(false) }
        val openAreaDialog = remember { mutableStateOf(false) }
        val openDeliveryTypeDialog = remember { mutableStateOf(false) }
        val openItemTypeDialog = remember { mutableStateOf(false) }


        KeyboardUnFocusHandler()

        when {
            openCityDialog.value ->
                CityDialog(
                    title = stringResource(id = R.string.select_city),
                    onDismiss = {
                        openCityDialog.value = false
                    },
                    onSelected = {
                        viewModel.cityChanged(it)
                        openCityDialog.value = false
                    },
                    cities = state.cities.data?.data ?: emptyList(),
                    selectedCity = state.recipientCity
                )
        }

        when {
            openZoneDialog.value ->
                ZoneDialog(
                    title = stringResource(id = R.string.select_zone),
                    onDismiss = {
                        openZoneDialog.value = false
                    },
                    onSelected = {
                        viewModel.zoneChanged(it)
                        openZoneDialog.value = false
                    },
                    zones = state.zones.data?.data ?: emptyList(),
                    selectedZone = state.recipientZone
                )
        }

        when {
            openAreaDialog.value ->
                AreaDialog(
                    title = stringResource(id = R.string.select_area),
                    onDismiss = {
                        openAreaDialog.value = false
                    },
                    onSelected = {
                        viewModel.areaChanged(it)
                        openAreaDialog.value = false
                    },
                    areas = state.areas.data?.data ?: emptyList(),
                    selectedArea = state.recipientArea
                )
        }

        when {
            openDeliveryTypeDialog.value ->
                DeliveryTypeDialog(
                    title = stringResource(id = R.string.select_delivery_type),
                    onDismiss = {
                        openDeliveryTypeDialog.value = false
                    },
                    onSelected = {
                        viewModel.deliveryTypeChanged(it)
                        openDeliveryTypeDialog.value = false
                    },
                    selectedType = state.deliveryType
                )
        }

        when {
            openItemTypeDialog.value ->
                ItemTypeDialog(
                    title = stringResource(id = R.string.select_item_type),
                    onDismiss = {
                        openItemTypeDialog.value = false
                    },
                    onSelected = {
                        viewModel.itemTypeChanged(it)
                        openItemTypeDialog.value = false
                    },
                    selectedType = state.itemType
                )
        }

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
                            value = state.merchantOrderId,
                            onValueChange = { viewModel.merchantOrderIdChanged(it) },
                            focusRequester = merchantOrderIdFocusRequester,
                            label = R.string.merchant_id,
                            leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientName,
                            onValueChange = { viewModel.recipientNameChanged(it) },
                            focusRequester = recipientNameFocusRequester,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            label = R.string.recipient_name,
                            keyboardType = KeyboardType.Text,
                            error = if (!state.pathaoOrderError.recipient_name.isNullOrEmpty()) (state.pathaoOrderError.recipient_name?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && (state.recipientName.isEmpty())) stringResource(
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
                            error = if (!state.pathaoOrderError.recipient_phone.isNullOrEmpty()) (state.pathaoOrderError.recipient_phone?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && !Common.isValidMobile(state.recipientPhone)) stringResource(
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
                            error = if (!state.pathaoOrderError.recipient_address.isNullOrEmpty()) (state.pathaoOrderError.recipient_address?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.recipientAddress.isEmpty()) stringResource(
                                id = R.string.enter_recipient_address
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientCity.city_name ?: "",
                            focusRequester = recipientCityFocusRequester,
                            onValueChange = {},
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            label = R.string.recipent_city,
                            error = if (!state.pathaoOrderError.recipient_city.isNullOrEmpty()) (state.pathaoOrderError.recipient_city?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.recipientCity.city_name.isNullOrEmpty() == true) stringResource(
                                id = R.string.select_recipent_city
                            ) else "",
                            onTap = { openCityDialog.value = true },
                            enabled = false,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientZone.zone_name ?: "",
                            focusRequester = recipientZoneFocusRequester,
                            onValueChange = {},
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            label = R.string.recipent_zone,
                            error = if (!state.pathaoOrderError.recipient_zone.isNullOrEmpty()) (state.pathaoOrderError.recipient_zone?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.recipientZone.zone_name.isNullOrEmpty() == true) stringResource(
                                id = R.string.select_recipent_zone
                            ) else "",
                            onTap = {
                                if (state.recipientCity.city_name.isNullOrEmpty()) {
                                    Toast.makeText(context, "Select City First", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    openZoneDialog.value = true
                                }
                            },
                            enabled = false,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.recipientArea.area_name ?: "",
                            focusRequester = recipientAreaFocusRequester,
                            onValueChange = {},
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            label = R.string.recipent_area,
                            onTap = {
                                if (state.recipientZone.zone_name.isNullOrEmpty()) {
                                    Toast.makeText(context, "Select Zone First", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    openAreaDialog.value = true
                                }
                            },
                            enabled = false,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.deliveryType.name ?: "",
                            focusRequester = deliveryTypeFocusRequester,
                            onValueChange = {},
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.TypeSpecimen,
                                    contentDescription = null
                                )
                            },
                            label = R.string.delivery_type,
                            error = if (!state.pathaoOrderError.delivery_type.isNullOrEmpty()) (state.pathaoOrderError.delivery_type?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.deliveryType.name.isNullOrEmpty()) stringResource(
                                id = R.string.enter_delivery_type
                            ) else "",
                            enabled = false,
                            onTap = { openDeliveryTypeDialog.value = true },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.itemType.name ?: "",
                            focusRequester = itemTypeFocusRequester,
                            onValueChange = {},
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Category,
                                    contentDescription = null
                                )
                            },
                            label = R.string.item_type,
                            error = if (!state.pathaoOrderError.item_type.isNullOrEmpty()) (state.pathaoOrderError.item_type?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.itemType.name.isNullOrEmpty()) stringResource(
                                id = R.string.enter_item_type
                            ) else "",
                            enabled = false,
                            onTap = { openItemTypeDialog.value = true },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.specialInstruction,
                            focusRequester = specialInstructionFocusRequester,
                            onValueChange = { viewModel.specialInstructionChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.MedicalInformation,
                                    contentDescription = null
                                )
                            },
                            label = R.string.special_instruction,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.itemQuantity,
                            focusRequester = itemQuantityFocusRequester,
                            onValueChange = { viewModel.itemQuantityChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.ProductionQuantityLimits,
                                    contentDescription = null
                                )
                            },
                            label = R.string.item_quantity,
                            keyboardType = KeyboardType.Number,
                            error = if (!state.pathaoOrderError.item_quantity.isNullOrEmpty()) (state.pathaoOrderError.item_quantity?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.itemQuantity.isEmpty()) stringResource(
                                id = R.string.enter_item_quantity
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.itemWeight,
                            focusRequester = itemWeightFocusRequester,
                            onValueChange = { viewModel.itemWeightChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Balance,
                                    contentDescription = null
                                )
                            },
                            label = R.string.item_weight,
                            keyboardType = KeyboardType.Number,
                            error = if (!state.pathaoOrderError.item_weight.isNullOrEmpty()) (state.pathaoOrderError.item_weight?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.itemWeight.isEmpty()) stringResource(
                                id = R.string.enter_item_weight
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.amountToCollect,
                            focusRequester = amountToCollectFocusRequester,
                            onValueChange = { viewModel.amountToCollectChanged(it) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null
                                )
                            },
                            label = R.string.amount_to_collect,
                            keyboardType = KeyboardType.Number,
                            error = if (!state.pathaoOrderError.amount_to_collect.isNullOrEmpty()) (state.pathaoOrderError.amount_to_collect?.get(
                                0
                            )
                                ?: "") else if (state.isValidate && state.amountToCollect.isEmpty()) stringResource(
                                id = R.string.enter_amount_to_collect
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = state.itemDescription,
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
                                recipientCityFocusRequester,
                                recipientZoneFocusRequester,
                                deliveryTypeFocusRequester,
                                itemTypeFocusRequester,
                                itemQuantityFocusRequester,
                                itemWeightFocusRequester,
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