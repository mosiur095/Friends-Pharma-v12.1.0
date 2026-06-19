package com.friendspharma.app.features.presentation.pharma

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.SearchBar
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.MainNavigation
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("RememberInComposition", "ConfigurationScreenWidthHeight")

@Composable
fun PharmaScreen(
    viewModel: PharmaViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    mainNavAction: MainNavigation,
    scrollSate: LazyGridState = rememberLazyGridState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val focusManager  = LocalFocusManager.current
    val width         = LocalConfiguration.current.screenWidthDp.dp
    val productWidth  = width / 2
    val productHeight = productWidth * 9 / 16
    val bannerHeight  = (width * 9 / 20)

    val pagerState = rememberPagerState(initialPage = 0) { state.companies.data?.size ?: 0 }

    // Original auto-scroll logic — unchanged
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            if (pagerState.canScrollForward || pagerState.currentPage > 0) {
                val target = if (pagerState.currentPage < (state.companies.data?.size ?: 0) - 1)
                    pagerState.currentPage + 1 else 0
                pagerState.animateScrollToPage(target)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        SearchBar(
            value         = state.search,
            onValueChange = { viewModel.searchChanged(it, focusManager) },
            placeholder   = stringResource(R.string.search_for_company),
            focusManager  = focusManager
        )

        LazyVerticalGrid(
            modifier       = Modifier.weight(1f).fillMaxSize(),
            state          = scrollSate,
            columns        = GridCells.Fixed(2),
            contentPadding = PaddingValues(5.dp)
        ) {
            // Original company banner — restored exactly
            if (state.search.isEmpty())
                item(span = { GridItemSpan(2) }) {
                    HorizontalPager(state = pagerState) { page ->
                        val item = remember { state.companies.data?.get(page) }
                        AsyncImage(
                            model              = item?.IMAGE_BANNER_URL ?: "",
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .padding(10.dp)
                                .width(width)
                                .height(bannerHeight)
                                .clip(shape = RoundedCornerShape(15.dp))
                                .clickable {
                                    navAction.navToPharmaMedicine(item ?: AllCompanyDtoItem())
                                }
                        )
                    }
                }

            // Company cards — original exactly
            items(state.allSearchedCompanies.data?.size ?: 0) {
                val item = state.allSearchedCompanies.data?.get(it)
                Card(
                    colors    = CardDefaults.cardColors(Color.White),
                    modifier  = Modifier
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .clickable {
                            scope.launch {
                                navAction.navToPharmaMedicine(
                                    item?.copy(ADDRESS = item.ADDRESS?.replace("#", " "))
                                        ?: AllCompanyDtoItem()
                                )
                            }
                        }
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(5.dp),
                    shape     = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model              = item?.IMAGE_LOGO_URL ?: "",
                            contentDescription = null,
                            modifier           = Modifier.height(productHeight).padding(10.dp),
                            contentScale       = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text       = item?.COMPANY_NAME ?: "",
                            color      = Primary,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.W500,
                            maxLines   = 1,
                            modifier   = Modifier.padding(5.dp),
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(10.dp)) }
        }

        AppName()
    }

    if (state.isLoading) Loader(paddingValues = PaddingValues())
}