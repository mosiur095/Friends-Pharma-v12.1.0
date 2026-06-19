package com.friendspharma.app.features

import androidx.annotation.StringRes
import androidx.navigation.NavController
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.features.Screens.CART_SCREEN
import com.friendspharma.app.features.Screens.CATEGORIES_SCREEN
import com.friendspharma.app.features.Screens.CATEGORY_MEDICINE_SCREEN
import com.friendspharma.app.features.Screens.DELIVERY_MAN_SCREEN
import com.friendspharma.app.features.Screens.FORGOT_PASSWORD_SCREEN
import com.friendspharma.app.features.Screens.HOME_SCREEN
import com.friendspharma.app.features.Screens.LOGIN_SCREEN
import com.friendspharma.app.features.Screens.MAIN_SCREEN
import com.friendspharma.app.features.Screens.MY_ORDERS_SCREEN
import com.friendspharma.app.features.Screens.NOTIFICATION_SCREEN
import com.friendspharma.app.features.Screens.ORDER_DETAILS_SCREEN
import com.friendspharma.app.features.Screens.OTP_SCREEN
import com.friendspharma.app.features.Screens.PATHAO_COURIER_SCREEN
import com.friendspharma.app.features.Screens.PHARMAA_MEDICINE_SCREEN
import com.friendspharma.app.features.Screens.PHARMA_SCREEN
import com.friendspharma.app.features.Screens.PROFILE_SCREEN
import com.friendspharma.app.features.Screens.RESET_PASSWORD_SCREEN
import com.friendspharma.app.features.Screens.RETURN_CART_SCREEN
import com.friendspharma.app.features.Screens.RETURN_LIST_SCREEN
import com.friendspharma.app.features.Screens.RETURN_MEDICINE_SCREEN
import com.friendspharma.app.features.Screens.SEARCH_SCREEN
import com.friendspharma.app.features.Screens.SIGN_UP_SCREEN
import com.friendspharma.app.features.Screens.SPLASH_SCREEN
import com.friendspharma.app.features.Screens.STEAD_FAST_COURIER_SCREEN
import com.friendspharma.app.features.Screens.UPDATE_PASSWORD_SCREEN
import com.friendspharma.app.features.Screens.UPDATE_PROFILE_SCREEN
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.UserDetailsDtoData
import com.google.gson.Gson

object Screens {
    const val SPLASH_SCREEN = "splashScreen"
    const val LOGIN_SCREEN = "loginScreen"
    const val SIGN_UP_SCREEN = "signUpScreen"
    const val HOME_SCREEN = "homeScreen"
    const val UPDATE_PASSWORD_SCREEN = "updatePasswordScreen"
    const val PROFILE_SCREEN = "profileScreen"
    const val CART_SCREEN = "cartScreen"
    const val RETURN_CART_SCREEN = "returnCartScreen"
    const val MY_ORDERS_SCREEN = "myOrdersScreen"
    const val ORDER_DETAILS_SCREEN = "orderDetailsScreen"
    const val PATHAO_COURIER_SCREEN = "pathoaCourierScreen"
    const val STEAD_FAST_COURIER_SCREEN = "steadFastCourierScreen"
    const val PHARMA_SCREEN = "pharmaScreen"
    const val MAIN_SCREEN = "mainScreen"
    const val PHARMAA_MEDICINE_SCREEN = "pharmaMedicineScreen"
    const val RETURN_MEDICINE_SCREEN = "returnMedicineScreen"
    const val CATEGORY_MEDICINE_SCREEN = "categoryMedicineScreen"
    const val CATEGORIES_SCREEN = "categoriesScreen"
    const val UPDATE_PROFILE_SCREEN = "updateProfileScreen"
    const val FORGOT_PASSWORD_SCREEN = "forgotPasswordScreen"
    const val OTP_SCREEN = "otpScreen"
    const val RESET_PASSWORD_SCREEN = "resetPasswordScreen"
    const val DELIVERY_MAN_SCREEN = "deliveryManScreen"
    const val RETURN_LIST_SCREEN = "returnListScreen"
    const val SEARCH_SCREEN = "searchScreen"
    const val NOTIFICATION_SCREEN = "notificationScreen"
}

object ScreenArgs {
    const val DATA = "data"
}

sealed class ScreenRoute(
    val route: String,
    @param:StringRes val title: Int? = null,
    val icon: Int? = null,
) {
    data object Splash : ScreenRoute(SPLASH_SCREEN)
    data object Login : ScreenRoute("${LOGIN_SCREEN}/{${ScreenArgs.DATA}}")
    data object SignUp : ScreenRoute("${SIGN_UP_SCREEN}/{${ScreenArgs.DATA}}")
    data object Main : ScreenRoute(MAIN_SCREEN)
    //data object Home : ScreenRoute(HOME_SCREEN, R.string.app_name, icon = R.drawable.baseline_home_24)
    data object Home : ScreenRoute(HOME_SCREEN, R.string.home_tab, icon = R.drawable.baseline_home_24)



    data object ChangePassword : ScreenRoute(UPDATE_PASSWORD_SCREEN)
    data object Profile : ScreenRoute(PROFILE_SCREEN)
    data object Cart : ScreenRoute(CART_SCREEN)
    data object ReturnCart : ScreenRoute(RETURN_CART_SCREEN)
    data object ReturnList : ScreenRoute(RETURN_LIST_SCREEN)
    data object MyOrders : ScreenRoute(MY_ORDERS_SCREEN)
    data object DeliveryMan : ScreenRoute(DELIVERY_MAN_SCREEN)
    data object OrderDetails : ScreenRoute("${ORDER_DETAILS_SCREEN}/{${ScreenArgs.DATA}}")
    data object PathaoCourier :
        ScreenRoute("${PATHAO_COURIER_SCREEN}?${ScreenArgs.DATA}={${ScreenArgs.DATA}}")

    data object SteadFastCourier :
        ScreenRoute("${STEAD_FAST_COURIER_SCREEN}?${ScreenArgs.DATA}={${ScreenArgs.DATA}}")

    data object Pharma :
        ScreenRoute(PHARMA_SCREEN, R.string.pharma, R.drawable.baseline_warehouse_24)

    data object Categories :
        ScreenRoute(CATEGORIES_SCREEN, R.string.categories, R.drawable.baseline_category_24)


    data object PharmaMedicine :
        ScreenRoute("${PHARMAA_MEDICINE_SCREEN}?${ScreenArgs.DATA}={${ScreenArgs.DATA}}")

    data object ReturnMedicine :
        ScreenRoute("${RETURN_MEDICINE_SCREEN}/{${ScreenArgs.DATA}}")

    data object CategoryMedicine :
        ScreenRoute("${CATEGORY_MEDICINE_SCREEN}?${ScreenArgs.DATA}={${ScreenArgs.DATA}}")

    data object UpdateProfileScreen :
        ScreenRoute("${UPDATE_PROFILE_SCREEN}?${ScreenArgs.DATA}={${ScreenArgs.DATA}}")

    data object ForgetPassword :
        ScreenRoute(FORGOT_PASSWORD_SCREEN, R.string.forgot_password)

    data object Otp :
        ScreenRoute("${OTP_SCREEN}/{${ScreenArgs.DATA}}", R.string.verification)

    data object ResetPassword :
        ScreenRoute(
            "${RESET_PASSWORD_SCREEN}/{${ScreenArgs.DATA}}",
            R.string.reset_password
        )

    data object Search: ScreenRoute(SEARCH_SCREEN)

    data object Notification : ScreenRoute(NOTIFICATION_SCREEN)

}

class MainNavigation(private val navController: NavController) {
    fun pop() {
        navController.navigateUp()
    }
}

class NavigationActions(private val navController: NavController) {

    fun pop() {
        navController.navigateUp()
    }

    fun navToSearch(){
        navController.navigate(ScreenRoute.Search.route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToNotification() {
        navController.navigate(ScreenRoute.Notification.route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToReturnList(){
        navController.navigate(ScreenRoute.ReturnList.route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToReturnMedicine(id: String) {
        navController.navigate("${RETURN_MEDICINE_SCREEN}/$id") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToResetPassword(mobile: String) {
        navController.navigate("${RESET_PASSWORD_SCREEN}/$mobile") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToOtp(mobile: String) {
        navController.navigate("${OTP_SCREEN}/$mobile") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToForgotPassword() {
        navController.navigate(ScreenRoute.ForgetPassword.route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToUpdateProfile(user: UserDetailsDtoData) {
        navController.navigate(
            "${UPDATE_PROFILE_SCREEN}?${ScreenArgs.DATA}=${
                Gson().toJson(user)
            }"
        ) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToCategoryMedicine(category: AllCategoryDtoItem) {
        navController.navigate(
            "${CATEGORY_MEDICINE_SCREEN}?${ScreenArgs.DATA}=${
                Gson().toJson(category)
            }"
        ) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToPharmaMedicine(company: AllCompanyDtoItem) {
        navController.navigate(
            "${PHARMAA_MEDICINE_SCREEN}?${ScreenArgs.DATA}=${
                Gson().toJson(company)
            }"
        ) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToCart() {
        if (MainActivity.isLoggedIn.value) {
            navController.navigate(CART_SCREEN) {
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navToLogin()
        }
    }

    fun navToReturnCart(){
        if (MainActivity.isLoggedIn.value) {
            navController.navigate(RETURN_CART_SCREEN) {
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navToLogin()
        }
    }

    fun navToSignUp(userType: String) {
        navController.navigate("${SIGN_UP_SCREEN}/${userType}") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToMain() {
        navController.navigate(MAIN_SCREEN) {
            launchSingleTop = true
            restoreState = true
            popUpTo(0)
        }
    }

    fun navToDeliveryMan() {
        navController.navigate(DELIVERY_MAN_SCREEN) {
            popUpTo(0)
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToResetPassword() {
        navController.navigate(UPDATE_PASSWORD_SCREEN) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToProfile() {
        if (MainActivity.isLoggedIn.value) {
            navController.navigate(PROFILE_SCREEN) {
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navToLogin()
        }
    }

    fun navToLogin() {
        navController.navigate("${LOGIN_SCREEN}/${null}") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToMyOrders() {
        if (MainActivity.isLoggedIn.value) {
            navController.navigate(MY_ORDERS_SCREEN) {
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navToLogin()
        }
    }

    fun navToOrderDetails(id: String) {
        navController.navigate("${ORDER_DETAILS_SCREEN}/${id}") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToPathaoCourier(order: OrderDetailsDtoItem) {
        navController.navigate(
            "${PATHAO_COURIER_SCREEN}?${ScreenArgs.DATA}=${
                Gson().toJson(
                    order
                )
            }"
        ) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToSteadFastCourier(order: OrderDetailsDtoItem) {
        navController.navigate(
            "${STEAD_FAST_COURIER_SCREEN}?${ScreenArgs.DATA}=${
                Gson().toJson(
                    order
                )
            }"
        ) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navToLoginForLogOut(mobile: String? = null) {
        navController.navigate("${LOGIN_SCREEN}/$mobile") {
            popUpTo(ScreenRoute.Main.route)
            launchSingleTop = true
            restoreState = true
        }
    }

}