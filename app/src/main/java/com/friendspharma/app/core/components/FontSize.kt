package com.friendspharma.app.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundDark

@Composable
fun FontSize(
    increase: () -> Unit,
    decrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(BackGroundDark)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(id = R.drawable.decrease),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        decrease()
                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "|")
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painterResource(id = R.drawable.increase),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        increase()
                    }
            )
        }
    }
}