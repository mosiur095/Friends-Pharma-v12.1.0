package com.friendspharma.app.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions

@Composable
fun ReturnDrawer(
    closeDrawer: () -> Unit,
    navAction: NavigationActions
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
            title = stringResource(id = R.string.deliveries)
        ) {
            navAction.navToDeliveryMan()
        }

        DrawerItem(
            icon = R.drawable.outline_assignment_return_24,
            title = stringResource(id = R.string.returns)
        ) {
            navAction.navToReturnList()
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.contact),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(5.dp)
        )
    }
}