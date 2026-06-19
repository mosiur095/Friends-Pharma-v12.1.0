package com.friendspharma.app.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions

data class ActionItem(val icon: ImageVector? = null, val image: Int? = null, val action: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    titleColor: Color = Gray,
    icon: Int? = null,
    image: String? = null,
    navAction: NavigationActions,
    actions: List<ActionItem> = listOf(),
    onBackPressed: (() -> Unit)? = null,
    isBack: Boolean = true,
    openDrawer: (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 5.dp,
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.White),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (isBack)
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .clickable {
                                    if (onBackPressed != null) onBackPressed()
                                    else navAction.pop()
                                },
                            tint = Primary
                        )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (onBackPressed != null) onBackPressed()
                                else if (openDrawer != null) openDrawer()
                                else navAction.pop()
                            }
                            .weight(1f)
                    ) {
                        if (title == stringResource(id = R.string.app_name))
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                                tint = Primary
                            )

                        if (icon != null) {
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .fillMaxHeight()
                                    .size(24.dp)
                            )
                        } else if (image != null) {
                            AsyncImage(
                                model = image,
                                contentDescription = null,
                                modifier = Modifier.height(24.dp).width(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = title,
                            color = titleColor,
                            fontWeight = FontWeight.W700,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Outlined icon, Primary tint, 24.dp — unified with bell and cart
                    for (item in actions) {
                        if (item.icon != null)
                            Icon(
                                item.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .size(24.dp)
                                    .clickable { item.action() },
                                tint = Primary
                            )
                        else if (item.image != null)
                            Image(
                                painter = painterResource(id = item.image),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .size(24.dp)
                                    .clickable { item.action() }
                            )
                    }

                    if (suffix != null) {
                        suffix()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}