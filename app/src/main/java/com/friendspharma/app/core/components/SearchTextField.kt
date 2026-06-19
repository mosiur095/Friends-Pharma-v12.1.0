package com.friendspharma.app.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: Int,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    suffixIcon: @Composable (() -> Unit)? = null,
    error: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester,
    onTap: (() -> Unit)? = null,
    onFocus: (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    height: Dp = 60.dp,
    fontSize: Int = 18,
    borderColor: Color = BackGroundDark,
    cornerRadius: Int = 10,
    containerColor: Color = Color.White,
    placeHolderFontSize: Int = 16
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            // FIX: explicit Color.Black prevents MIUI from rendering invisible text
            textStyle = TextStyle(fontSize = fontSize.sp, color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isFocused.value) Primary else borderColor,
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
                .focusRequester(focusRequester)
                .clip(shape = RoundedCornerShape(cornerRadius.dp))
                .onFocusChanged {
                    if (onFocus != null)
                        onFocus()
                }
                .clickable {
                    if (onTap != null)
                        onTap()
                }
                .height(height),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
            visualTransformation = visualTransformation,
            enabled = enabled,
            singleLine = singleLine,
            interactionSource = interactionSource
        ) { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = 10.dp),
                shape = RoundedCornerShape(cornerRadius.dp),
                colors = TextFieldDefaults.colors(
                    // FIX: explicit text colors prevent MIUI theme override
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = containerColor,
                    disabledLeadingIconColor = GrayLight
                ),
                leadingIcon = leadingIcon,
                suffix = suffixIcon,
                placeholder = {
                    Text(
                        text = stringResource(id = label),
                        fontSize = placeHolderFontSize.sp,
                        color = GrayLight
                    )
                }
            )
        }

        if (error.isNotEmpty()) {
            Text(text = error, fontSize = 12.sp, color = Color.Red)
        }
    }
}