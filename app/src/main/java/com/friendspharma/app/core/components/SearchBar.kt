package com.friendspharma.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TextFieldBackGround

/**
 * Reusable search bar used across:
 * - HomeScreen
 * - CategoriesScreen
 * - PharmaScreen
 * - CategoryMedicineScreen
 * - PharmaMedicineScreen
 *
 * @param value          Current search text
 * @param onValueChange  Called when text changes
 * @param placeholder    Hint text shown when empty
 * @param focusManager   Used to clear focus on search action
 * @param trailingContent Optional slot for extra content (e.g. BoxSwitch)
 */
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TextFieldBackGround),
            textStyle     = TextStyle(fontSize = 14.sp, color = Color.Black),
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            )
        ) { innerTextField ->
            Row(
                modifier          = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Filled.Search,
                    contentDescription = null,
                    tint               = Primary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text     = placeholder,
                            fontSize = 14.sp,
                            color    = GrayLight
                        )
                    }
                    innerTextField()
                }
                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint               = Primary,
                        modifier           = Modifier
                            .size(18.dp)
                            .clickable { onValueChange("") }
                    )
                }
            }
        }

        // Optional trailing slot — used by HomeScreen, CategoryMedicineScreen,
        // PharmaMedicineScreen for the BoxSwitch toggle
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(6.dp))
            trailingContent()
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}