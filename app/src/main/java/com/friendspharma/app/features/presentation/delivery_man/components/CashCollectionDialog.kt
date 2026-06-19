package com.friendspharma.app.features.presentation.delivery_man.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem

@Composable
fun CashCollectionDialog(item: PendignDeliveryDtoItem, confirm: (PendignDeliveryDtoItem) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            InfoItem("Invoice no: ", item.INVOICE_NO ?: "")

            InfoItem(
                "Date: ",
                item.TRANSACTION_DATE?.replace("T", "  ")
                    ?.replace("Z", "")
                    ?: ""
            )

            InfoItem(
                "Order to: ",
                item.ADDRESS ?: ""
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    InfoItem("Total Amount: ", item.SALES_AMOUNT.toString())
                    InfoItem(
                        "Discount: (-) ",
                        ((item.TOTAL_AMOUNT ?: 0.0) - (item.SALES_AMOUNT ?: 0.0)).toString()
                    )
                    InfoItem("Grand Total: ", item.TOTAL_AMOUNT.toString())
                }

            }

            Spacer(Modifier.height(20.dp))

            Row {
                ButtonK(modifier = Modifier.weight(1f), text = R.string.cash_collection) {
                    confirm(item)
                }
            }

            Spacer(Modifier.height(20.dp))

        }
    }
}