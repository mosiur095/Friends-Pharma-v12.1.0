package com.friendspharma.app.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.Primary

@Composable
fun NoInternet(refresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_internet), contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.no_internet),
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            color = Gray
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = stringResource(id = R.string.check_internet), color = GrayLight)
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .border(color = Primary, width = 1.dp, shape = RoundedCornerShape(25.dp))
                .clickable {
                    refresh()
                }, contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Primary)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.try_again),
                    fontWeight = FontWeight.W500,
                    color = Primary
                )
            }
        }
    }
}