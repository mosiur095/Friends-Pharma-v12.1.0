package com.friendspharma.app.features.presentation.return_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.core.theme.GrayExtraLight
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.features.ScreenRoute
import com.friendspharma.app.features.data.remote.model.ReturnList
import com.friendspharma.app.features.data.remote.model.ReturnListDtoData
import com.friendspharma.app.features.presentation.delivery_man.components.InfoItem

@Composable
fun ReturnDetailsDialog(item: ReturnList, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            InfoItem("Return invoice: ", item.invoice ?: "")

            InfoItem(
                "Customer name: ",
                item.customerName ?: ""
            )
            InfoItem(
                "Mobile Number: ",
                item.mobile ?: ""
            )
            InfoItem(
                "Status: ",
                item.status ?: ""
            )
            Spacer(Modifier.height(10.dp))
            Row {
                Text(
                    "Product name",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    "Quantity",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    "Total price",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(
                color = GrayExtraLight,
                modifier = Modifier.padding(top = 2.dp, bottom = 5.dp)
            )
            item.data?.forEach { data ->
                Row {
                    Text(
                        data.productName ?: "",
                        modifier = Modifier.weight(2f)
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        data.quantity.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "৳ ${data.totalPrice}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


    }

}