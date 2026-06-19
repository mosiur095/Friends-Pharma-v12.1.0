package com.friendspharma.app.features.presentation.order_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.AmberColor
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.data.remote.model.TrackOrderDtoData

val statusList =
    listOf("Submitted", "Confirmed", "In Transit", "Delivered", "Cash Collection")

@Composable
fun TrackOrderDialog(track: TrackOrderDtoData, onDismiss: () -> Unit) {

    val currentIndex = statusList.indexOf(track.STATUS)

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp), // Rounded corners
            color = Color.White,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                repeat(statusList.size) {
                    StatusItem(
                        index = it,
                        currentIndex = currentIndex
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(36.dp)
                            .width(100.dp)
                            .background(
                                color = Color(0xFFDB5A3C),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .clickable {
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = stringResource(id = R.string.close),
                            color = Color.White,
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(36.dp)
                            .width(100.dp)
                            .background(color = Primary, shape = RoundedCornerShape(25.dp))
                            .clickable {
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = stringResource(id = R.string.ok),
                            color = Color.White,
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusItem(modifier: Modifier = Modifier, index: Int, currentIndex: Int) {
    val isDone = remember { index < currentIndex }
    val color =
        remember { if (isDone) Color.Green else if (index == currentIndex) AmberColor else GrayLight }
    Column(modifier = modifier) {
        if (index != 0)
            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(start = 8.dp),
                color = color
            )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.border(2.dp, color, shape = CircleShape)) {
                if (index < currentIndex) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                } else
                    Icon(
                        Icons.Default.Circle, contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .padding(2.dp)
                            .size(12.dp)
                    )
            }
            Spacer(Modifier.width(10.dp))
            Text(statusList[index], color = Gray)
        }
    }
}
