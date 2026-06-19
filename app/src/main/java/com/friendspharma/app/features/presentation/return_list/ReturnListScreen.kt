package com.friendspharma.app.features.presentation.return_list

import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.ReturnList
import com.friendspharma.app.features.data.remote.model.ReturnListDtoData
import com.friendspharma.app.features.presentation.return_list.components.ReturnDetailsDialog
import kotlinx.coroutines.CoroutineScope


@Composable
fun ReturnListScreen(
    viewModel: ReturnListViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scope: CoroutineScope = rememberCoroutineScope(),
    scrollSate: LazyListState = rememberLazyListState()
) {

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.return_list),
                navAction = navAction,
                icon = R.drawable.outline_assignment_return_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()

        KeyboardUnFocusHandler()

        if (state.currentItem.invoice != null)
            ReturnDetailsDialog(item = state.currentItem) {
                viewModel.detailsDialog(ReturnList())
            }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)

        ) {

            LazyColumn(
                Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                state = scrollSate
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                items(state.returnList.size) {
                    val item = state.returnList[it]

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .fillMaxWidth()
                            .clickable {
                                viewModel.detailsDialog(item)
                            },
                        elevation = CardDefaults.cardElevation(5.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(BackGroundDark, Color.White)
                                    )
                                )
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(text = item.invoice.toString())
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = (item.customerName) + " (${item.mobile})",
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            item.data?.forEach { data ->
                                Row(modifier = Modifier) {
                                    Text(
                                        text = data.productName.toString(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = data.quantity.toString() + " (৳ ${data.totalPrice})",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                        }
                    }
                }
                if (!state.isLoading && state.returnList.isEmpty())
                    item {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NoContent()
                        }
                    }
            }
            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)

    }
}