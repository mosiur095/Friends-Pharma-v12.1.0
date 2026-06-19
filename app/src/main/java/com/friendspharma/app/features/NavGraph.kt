package com.friendspharma.app.features

import android.os.Build
import androidx.annotation.RequiresApi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.friendspharma.app.features.presentation.cart.CartScreen
import com.friendspharma.app.features.presentation.category_medicine.CategoryMedicineScreen
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManScreen
import com.friendspharma.app.features.presentation.forgot_password.ForgotPasswordScreen
import com.friendspharma.app.features.presentation.login.LoginScreen
import com.friendspharma.app.features.presentation.main.MainScreen
import com.friendspharma.app.features.presentation.my_orders.MyOrdersScreen
import com.friendspharma.app.features.presentation.notification.NotificationScreen
import com.friendspharma.app.features.presentation.order_details.OrderDetailsScreen
import com.friendspharma.app.features.presentation.otp.OtpScreen
import com.friendspharma.app.features.presentation.pathao_courier.PathaoCourierScreen
import com.friendspharma.app.features.presentation.pharma_medicines.PharmaMedicineScreen
import com.friendspharma.app.features.presentation.profile.ProfileScreen
import com.friendspharma.app.features.presentation.reset_password.ResetPasswordScreen
import com.friendspharma.app.features.presentation.return_cart.ReturnCartScreen
import com.friendspharma.app.features.presentation.return_list.ReturnListScreen
import com.friendspharma.app.features.presentation.return_products.ReturnMedicineScreen
import com.friendspharma.app.features.presentation.search.SearchScreen
import com.friendspharma.app.features.presentation.update_password.UpdatePasswordScreen
import com.friendspharma.app.features.presentation.sign_up.SignUpScreen
import com.friendspharma.app.features.presentation.splash.SplashScreen
import com.friendspharma.app.features.presentation.stead_fast_courier.SteadFastCourierScreen
import com.friendspharma.app.features.presentation.update_profile.UpdateProfileScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    navAction: NavigationActions = remember(navController) {
        NavigationActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(ScreenRoute.Splash.route) {
            SplashScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.Login.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            LoginScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.SignUp.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            SignUpScreen(navAction = navAction)
        }

        composable(ScreenRoute.Main.route) {
            MainScreen(navAction = navAction)
        }

        composable(ScreenRoute.ChangePassword.route) {
            UpdatePasswordScreen(navAction = navAction)
        }

        composable(ScreenRoute.Profile.route) {
            ProfileScreen(navAction = navAction)
        }

        composable(ScreenRoute.Cart.route) {
            CartScreen(navAction = navAction)
        }

        composable(ScreenRoute.Notification.route) {
            NotificationScreen(navAction = navAction)
        }

        composable(ScreenRoute.ReturnCart.route) {
            ReturnCartScreen(navAction = navAction)
        }

        composable(ScreenRoute.ReturnList.route) {
            ReturnListScreen(navAction = navAction)
        }

        composable(ScreenRoute.MyOrders.route) {
            MyOrdersScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.OrderDetails.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            OrderDetailsScreen(navAction = navAction)
        }

        composable(ScreenRoute.PathaoCourier.route) {
            PathaoCourierScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.SteadFastCourier.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            SteadFastCourierScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.PharmaMedicine.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            PharmaMedicineScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.ReturnMedicine.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            ReturnMedicineScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.CategoryMedicine.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            CategoryMedicineScreen(navAction = navAction)
        }

        composable(
            ScreenRoute.UpdateProfileScreen.route,
            arguments = listOf(navArgument(ScreenArgs.DATA) {
                type = NavType.StringType; nullable = true
            })
        ) {
            UpdateProfileScreen(navAction = navAction)
        }

        composable(ScreenRoute.ForgetPassword.route) {
            ForgotPasswordScreen(
                navAction = navAction,
                navToOtp = { mobile -> navAction.navToOtp(mobile) })
        }

        composable(ScreenRoute.ResetPassword.route) {
            ResetPasswordScreen(
                navAction = navAction,
                navToLogin = { mobile -> navAction.navToLoginForLogOut(mobile) }
            )
        }

        composable(ScreenRoute.Otp.route, arguments = listOf(navArgument(ScreenArgs.DATA) {
            type = NavType.StringType
        })) {
            OtpScreen(
                navAction = navAction,
                navToResetPassword = { mobile -> navAction.navToResetPassword(mobile) })
        }

        composable(ScreenRoute.DeliveryMan.route) {
            DeliveryManScreen(navAction = navAction)
        }

        composable(ScreenRoute.Search.route) {
            SearchScreen(navAction = navAction)
        }
    }
}