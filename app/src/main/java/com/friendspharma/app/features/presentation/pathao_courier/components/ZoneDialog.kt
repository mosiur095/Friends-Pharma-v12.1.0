package com.friendspharma.app.features.presentation.pathao_courier.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.features.data.remote.model.ZoneDtoItem

@Composable
fun ZoneDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelected: (ZoneDtoItem) -> Unit,
    zones: List<ZoneDtoItem>,
    selectedZone: ZoneDtoItem
) {

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Companion.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.W500
            )
            Spacer(modifier = Modifier.height(20.dp))

            repeat(zones.size) { index ->
                val zone = zones[index]
                Card(modifier = Modifier
                    .clickable {
                        onSelected(zone)
                    }
                    .fillMaxWidth(),
                    colors = CardDefaults.cardColors(if (zone.zone_id == selectedZone.zone_id) BackGroundDark else TextFieldBackGround)) {
                    Text(text = zone.zone_name ?: "", modifier = Modifier.padding(15.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}