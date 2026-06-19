package com.friendspharma.app.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions

@Composable
fun Drawer(
    closeDrawer: () -> Unit,
    navAction: NavigationActions,
    productByBox: (Boolean) -> Unit,
    sortByPharmaceutical: () -> Unit,
    sortByCategory: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(BackGroundColor)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = null,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable {
                        closeDrawer()
                    },
                tint = Primary
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_small),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 5.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            icon = R.drawable.baseline_checklist_24,
            title = stringResource(id = R.string.my_orders)
        ) {
            navAction.navToMyOrders()
        }

//        DrawerItem(
//            icon = R.drawable.baseline_list_alt_24,
//            title = stringResource(id = R.string.products_by_box)
//        ) {
//            productByBox(true)
//        }
//
//        DrawerItem(
//            icon = R.drawable.baseline_list_24,
//            title = stringResource(id = R.string.products_by_leaf)
//        ) {
//            productByBox(false)
//        }
//        DrawerItem(
//            icon = R.drawable.baseline_category_24,
//            title = stringResource(id = R.string.sort_by_category)
//        ) {
//            sortByCategory()
//        }
//        DrawerItem(
//            icon = R.drawable.baseline_warehouse_24,
//            title = stringResource(id = R.string.sort_by_company)
//        ) {
//            sortByPharmaceutical()
//        }

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.contact),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(5.dp)
        )
    }
}

@Composable
fun DrawerItem(icon: Int, title: String, press: () -> Unit) {
    Box(modifier = Modifier.clickable { press() }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 5.dp)
        ) {
            Image(
                painter = painterResource(id = icon), contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 16.sp, color = Gray)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
