package com.friendspharma.app.features.presentation.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.features.data.remote.entity.ChangeAddress
import com.friendspharma.app.features.data.remote.entity.DeleteAddress
import com.friendspharma.app.features.data.remote.model.AddressDto
import com.friendspharma.app.features.data.remote.model.AddressDtoItem

@Composable
fun AddressDialog(
    addresses: AddressDto,
    onDismiss: () -> Unit,
    onSelect: (AddressDtoItem) -> Unit,
    insertAddress: (AddressDtoItem) -> Unit,
    isLoading: Boolean,
    changeAddress: (ChangeAddress) -> Unit,
    deleteAddress: (DeleteAddress) -> Unit,
    selectedAddress: AddressDtoItem,
    mapAddress: String,
    mapPost: String,
    mapDistrict: String,
    requestLocation: () -> Unit
) {

    val address = remember { mutableStateOf("") }
    val post = remember { mutableStateOf("") }
    val district = remember { mutableStateOf("") }
    val addressFocusRequester = remember { FocusRequester() }
    val postFocusRequester = remember { FocusRequester() }
    val districtFocusRequester = remember { FocusRequester() }

    val isHomeExist = remember { mutableStateOf(false) }
    val isOfficeExist = remember { mutableStateOf(false) }
    val addressType = remember { mutableStateOf("Home") }

    val isValidate = remember { mutableStateOf(false) }
    val addNewAddress = remember { mutableStateOf(false) }
    val changeAddress = remember { mutableStateOf(false) }
    val pid = remember { mutableStateOf("") }

    if (mapAddress.isNotEmpty() && address.value.isEmpty()) {
        address.value = mapAddress
        post.value = mapPost
        district.value = mapDistrict
    }

    LaunchedEffect(Unit) {
        if (addresses.data.isNullOrEmpty())
            requestLocation()
        else {
            addresses.data.forEach {
                if (it.ADDR_TYPE == "Home") {
                    isHomeExist.value = true
                    addressType.value = "Office"
                } else {
                    isOfficeExist.value = true
                    addressType.value = ""
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(modifier = Modifier.padding(15.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (addresses.data.isNullOrEmpty() || addNewAddress.value) {
                        Box {
                            if (addNewAddress.value) {
                                Icon(
                                    Icons.Filled.ArrowBackIosNew, contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier
                                        .clickable {
                                            addNewAddress.value = false
                                        })
                            }
                            Text(
                                text = if (changeAddress.value) stringResource(R.string.edit_address) else stringResource(
                                    id = R.string.add_an_address
                                ),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W500,
                                color = Primary,
                                modifier = Modifier
                                    .padding(bottom = 20.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        TextFieldK(
                            value = address.value,
                            onValueChange = { address.value = it },
                            focusRequester = addressFocusRequester,
                            label = R.string.address,
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            keyboardType = KeyboardType.Text,
                            error = if (isValidate.value && address.value.isEmpty()) stringResource(
                                id = R.string.enter_address
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = post.value,
                            onValueChange = { post.value = it },
                            focusRequester = postFocusRequester,
                            label = R.string.thana,
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            keyboardType = KeyboardType.Text,
                            error = if (isValidate.value && post.value.isEmpty()) stringResource(
                                id = R.string.enter_thana
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        TextFieldK(
                            value = district.value,
                            onValueChange = { district.value = it },
                            focusRequester = districtFocusRequester,
                            label = R.string.district,
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AddLocation,
                                    contentDescription = null
                                )
                            },
                            keyboardType = KeyboardType.Text,
                            error = if (isValidate.value && district.value.isEmpty()) stringResource(
                                id = R.string.select_district
                            ) else "",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ButtonK(
                            if (changeAddress.value) R.string.update else R.string.add,
                            backGroundColor = if (isLoading) BackGroundColor else BackGroundDark
                        ) {
                            if (!isLoading) {
                                if (address.value.isNotEmpty() && post.value.isNotEmpty() && district.value.isNotEmpty() && addressType.value.isNotEmpty()) {
                                    if (changeAddress.value) {
                                        changeAddress(
                                            ChangeAddress(
                                                aid = pid.value,
                                                address = "${address.value}, ${post.value}, ${district.value}",
                                                addrType = addressType.value
                                            )
                                        )
                                    } else {
                                        insertAddress(
                                            AddressDtoItem(
                                                ADDRESS = "${address.value}, ${post.value}, ${district.value}",
                                                ADDR_TYPE = addressType.value
                                            )
                                        )
                                    }
                                } else {
                                    isValidate.value = true
                                }
                                addNewAddress.value = false
                                changeAddress.value = false
                            }
                        }

                    } else {
                        Text(
                            text = stringResource(id = R.string.select_address),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W500,
                            color = Primary,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        LazyColumn {
                            items(addresses.data.size) {
                                Box(modifier = Modifier.padding(vertical = 5.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelect(addresses.data[it])
                                                onDismiss()
                                            }
                                            .background(
                                                if (selectedAddress.PID == addresses.data[it].PID) BackGroundDark.copy(
                                                    alpha = .75f
                                                ) else TextFieldBackGround,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Text(
                                                (addresses.data[it].ADDR_TYPE
                                                    ?: "") + ": " + addresses.data[it].ADDRESS,
                                                fontSize = 16.sp,
                                                color = Gray,
                                                modifier = Modifier
                                                    .weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.clickable {
                                                    changeAddress.value = true
                                                    addNewAddress.value = true
                                                    pid.value = addresses.data[it].PID ?: ""
                                                    address.value = addresses.data[it].ADDRESS ?: ""
                                                    try {
                                                        val addressList = address.value.split(",")
                                                        address.value = ""
                                                        for (a in 0..(addressList.size - 3)) {
                                                            address.value += (if (a == 0) "" else ", ") + addressList[a].trim()
                                                        }
                                                        post.value =
                                                            addressList[addressList.size - 2].trim()
                                                        district.value =
                                                            addressList[addressList.size - 1].trim()
                                                    } catch (e: Exception) {
                                                        println(e.message)
                                                    }

                                                    addressType.value =
                                                        if (addresses.data[it].ADDR_TYPE != "Office") "Home" else "Office"
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.clickable {
                                                    if (!isLoading) {
                                                        deleteAddress(
                                                            DeleteAddress(
                                                                addresses.data[it].PID ?: ""
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        if (!isHomeExist.value || !isOfficeExist.value)
                            ButtonK(
                                R.string.add_new_address,
                                backGroundColor = if (isLoading) BackGroundColor else BackGroundDark
                            ) {
                                addNewAddress.value = true
                                address.value = ""
                                post.value = ""
                                district.value = ""
                                isValidate.value = false
                                requestLocation()
                            }

                    }
                }
            }
            Box {
                Icon(
                    Icons.Default.Close, contentDescription = null,
                    modifier = Modifier
                        .background(BackGroundDark, CircleShape)
                        .padding(2.dp)
                        .clickable {
                            onDismiss()
                        }
                        .size(30.dp)
                )
            }
        }
    }
}