package com.friendspharma.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.GrayLight

@OptIn(ExperimentalMaterial3Api::class) @Composable
fun CustomDropdownField( modifier: Modifier = Modifier, value: String, items: List<String>, label: Int, onValueChange: (String) -> Unit, focusRequester: FocusRequester, onTap: (() -> Unit)? = null, onFocus: (() -> Unit)? = null, enabled: Boolean = true, height: Dp = 60.dp, fontSize: Int = 18, borderColor: Color = BackGroundDark, cornerRadius: Int = 10, containerColor: Color = Color.White, placeHolderFontSize: Int = 16, leadingIcon: @Composable (() -> Unit)? = null, error: String = "" ) { val expanded = remember { mutableStateOf(false) }



    Column(
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = {
                expanded.value = it
                if (it && onTap != null) onTap()
            },
            modifier = Modifier
                .fillMaxWidth() // ← add horizontal margin
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused && onFocus != null) onFocus()
                }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { /* readOnly - selection via menu */ },
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),

                textStyle = androidx.compose.ui.text.TextStyle(fontSize = fontSize.sp),
                label = {
                    Text(
                        text = stringResource(id = label),
                        fontSize = placeHolderFontSize.sp,
                        color = GrayLight,
                        modifier = Modifier.padding(start = 0.dp)  // ← title left padding
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded.value,
                    )
                },
                leadingIcon = leadingIcon,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    focusedIndicatorColor = borderColor,
                    unfocusedIndicatorColor = borderColor,
                    disabledIndicatorColor = borderColor,
                    disabledLeadingIconColor = GrayLight
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                containerColor = Color.White,
                matchTextFieldWidth = false,
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(cornerRadius.dp))
                    .background(Color.White, RoundedCornerShape(cornerRadius.dp)),
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(text = item, fontSize = fontSize.sp)

                        },
                        onClick = {
                            onValueChange(item)
                            expanded.value = false
                        },
                        leadingIcon = null
                    )
                }
            }
        }
    }


    if (error.isNotEmpty()) {
        Text(text = error, fontSize = 12.sp, color = Color.Red)
    }
}