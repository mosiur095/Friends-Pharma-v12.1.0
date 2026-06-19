package com.friendspharma.app.features.presentation.profile

import android.os.Build
import androidx.annotation.RequiresApi

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.presentation.profile.components.ActionItem
import com.friendspharma.app.features.presentation.profile.components.UserDataItem


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navAction: NavigationActions
) {

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.profile),
                navAction = navAction,
            )
        }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val width = (LocalConfiguration.current.screenWidthDp / 1.75).dp

        LaunchedEffect(Unit) {
            viewModel.getUser()
        }


        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Primary,
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .height(width),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Box(
                        modifier = Modifier.background(
                            color = BackGroundDark,
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            Icons.Filled.Person, contentDescription = null, modifier = Modifier
                                .size(80.dp)
                                .padding(10.dp),
                            tint = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = state.user.USER_NAME ?: "", fontSize = 20.sp,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .padding(
                                top = (width - 40.dp),
                                start = 20.dp,
                                end = 20.dp,
                                bottom = 20.dp
                            )
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(5.dp)
                    ) {
                        Row {
                            Column(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .weight(1f)
                            ) {
                                //UserDataItem(title = stringResource(id = R.string.id), data = state.user.USER_ID.toString())
                                UserDataItem(
                                    title = stringResource(id = R.string.full_name),
                                    data = state.user.USER_NAME ?: ""
                                )
                                UserDataItem(
                                    title = stringResource(id = R.string.mobile_number),
                                    data = state.user.MOBILE_NO ?: ""
                                )
                                UserDataItem(
                                    title = stringResource(id = R.string.email),
                                    data = state.user.EMAIL ?: ""
                                )
                                UserDataItem(
                                    title = stringResource(id = R.string.address),
                                    data = state.user.ADDRESS ?: ""
                                )
                            }
                        }

                        Row(modifier = Modifier.padding(horizontal = 20.dp)) {

                            ActionItem(modifier = Modifier
                                .clickable { navAction.navToUpdateProfile(state.user) }
                                .weight(.33f),
                                text = R.string.update_profile,
                                Icons.Default.Update)

                            Spacer(modifier = Modifier.width(10.dp))

                            ActionItem(modifier = Modifier
                                .clickable { navAction.navToResetPassword() }
                                .weight(.33f),
                                text = R.string.change_password,
                                Icons.Filled.Password)

                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.padding(horizontal = 20.dp)) {

                            ActionItem(modifier = Modifier
                                .clickable { viewModel.logOut(navAction) }
                                .weight(.33f),
                                text = R.string.log_out,
                                Icons.AutoMirrored.Filled.Logout,
                                tint = Color.Yellow)

                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            AppName()
        }
    }
}
