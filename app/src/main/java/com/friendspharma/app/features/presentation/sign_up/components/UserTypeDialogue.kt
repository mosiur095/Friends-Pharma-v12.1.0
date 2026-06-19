package com.friendspharma.app.features.presentation.sign_up.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.util.customer
import com.friendspharma.app.core.util.wholeSeller


@Composable
fun UserTypeDialogue(
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    val types = remember { mutableListOf(customer, wholeSeller) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Companion.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {

            Text(
                text = stringResource(id = R.string.select_user_type),
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.W500
            )
            Spacer(modifier = Modifier.height(20.dp))

            repeat(types.size){ index ->
                Card(modifier = Modifier
                    .clickable {
                        onSelected(types[index])
                    }
                    .fillMaxWidth()) {
                    Text(text = types[index], modifier = Modifier.padding(15.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}