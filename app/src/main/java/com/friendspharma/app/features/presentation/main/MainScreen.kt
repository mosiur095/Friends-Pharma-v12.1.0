package com.friendspharma.app.features.presentation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ActionItem
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.Drawer
import com.friendspharma.app.core.components.NotificationButton
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.MainNavigation
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.ScreenRoute
import com.friendspharma.app.features.presentation.categories.CategoriesScreen
import com.friendspharma.app.features.presentation.home.HomeScreen
import com.friendspharma.app.features.presentation.home.HomeViewModel
import com.friendspharma.app.features.presentation.home.comonents.CartButton
import com.friendspharma.app.features.presentation.pharma.PharmaScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    navItems: List<ScreenRoute> = listOf(
        ScreenRoute.Home,
        ScreenRoute.Categories,
        ScreenRoute.Pharma
    ),
    navAction: NavigationActions,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
    mainNavAction: MainNavigation = remember(navController) {
        MainNavigation(navController)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val config = LocalConfiguration.current
    val homeScrollSate: LazyGridState = rememberLazyGridState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Drawer(
                    closeDrawer = { scope.launch { drawerState.close() } },
                    navAction = navAction,
                    productByBox = {
                        homeViewModel.productsByBox(it)
                        scope.launch { drawerState.close() }
                    },
                    sortByPharmaceutical = {
                        scope.launch {
                            drawerState.close()
                            homeViewModel.sortByPharmaceutical(homeScrollSate)
                        }
                    },
                    sortByCategory = {
                        scope.launch {
                            drawerState.close()
                            homeViewModel.sortByCategory(homeScrollSate)
                        }
                    }
                )
            }
        }
    ) {
        val currentScreen = remember { mutableIntStateOf(R.string.app_name) }

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(id = currentScreen.intValue),
                    navAction = navAction,
                    icon = R.drawable.logo_icon,
                    isBack = currentScreen.intValue != R.string.app_name,
                    actions = listOf(
                        ActionItem(
                            Icons.Outlined.Person,
                            action = navAction::navToProfile
                        )
                    ),
                    suffix = {
                        val cartQuantity by MainActivity.cartQuantity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NotificationButton(unreadCount = state.unreadCount) {
                                navAction.navToNotification()
                            }
                            CartButton(cartItemQuantity = cartQuantity) {
                                navAction.navToCart()
                            }
                        }
                    },
                    openDrawer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    navItems.onEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                unselectedIconColor = Gray
                            ),
                            icon = {
                                Image(
                                    painter = painterResource(id = screen.icon ?: 0),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = screen.title ?: 0),
                                    textAlign = TextAlign.Center
                                )
                            },
                            onClick = {
                                currentScreen.intValue = screen.title ?: R.string.app_name
                                if (screen.route != currentDestination?.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = ScreenRoute.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(ScreenRoute.Home.route) {
                    HomeScreen(navAction = navAction, scrollSate = homeScrollSate)
                }
                composable(ScreenRoute.Categories.route) {
                    CategoriesScreen(navAction = navAction, mainNavAction = mainNavAction)
                }
                composable(ScreenRoute.Pharma.route) {
                    PharmaScreen(navAction = navAction, mainNavAction = mainNavAction)
                }
            }
        }
    }
}