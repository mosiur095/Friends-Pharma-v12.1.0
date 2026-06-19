package com.friendspharma.app.features.presentation.reset_password.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight

@Composable
fun SuccessDialogue(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackGroundColor, shape = RoundedCornerShape(20.dp))
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Close, contentDescription = null,
                    modifier = Modifier.clip(shape = CircleShape).clickable { onDismiss() })
            }
            Icon(
                Icons.Filled.Check, contentDescription = null,
                modifier = Modifier
                    .background(color = BackGroundDark, shape = CircleShape)
                    .padding(12.dp)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.congrats),
                fontSize = 20.sp,
                color = Gray,
                modifier = Modifier.padding(top = 20.dp, bottom = 5.dp)
            )
            Text(text = stringResource(id = R.string.reset_password_success), color = GrayLight)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(25.dp))
                    .clip(shape = RoundedCornerShape(25.dp))
                    .clickable {
                        onDismiss()
                    }
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}