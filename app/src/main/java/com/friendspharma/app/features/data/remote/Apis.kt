package com.friendspharma.app.features.data.remote

import com.friendspharma.app.MainActivity
import com.friendspharma.app.features.data.remote.entity.AddReturn
import com.friendspharma.app.features.data.remote.entity.ChangeAddress
import com.friendspharma.app.features.data.remote.entity.ChangePassword
import com.friendspharma.app.features.data.remote.entity.DeleteAddress
import com.friendspharma.app.features.data.remote.entity.InsertAddress
import com.friendspharma.app.features.data.remote.entity.ProductAdd
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.entity.ReturnCartRemove
import com.friendspharma.app.features.data.remote.entity.SignUp
import com.friendspharma.app.features.data.remote.entity.SubmitOrder
import com.friendspharma.app.features.data.remote.entity.SubmitReturn
import com.friendspharma.app.features.data.remote.entity.UpdateProfile
import com.friendspharma.app.features.data.remote.model.AddReturnDto
import com.friendspharma.app.features.data.remote.model.AddToCartRestrictDto
import com.friendspharma.app.features.data.remote.model.AddressDto
import com.friendspharma.app.features.data.remote.model.AllCategoryDto
import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.DefaultDto
import com.friendspharma.app.features.data.remote.model.DeliveredDto
import com.friendspharma.app.features.data.remote.model.DistrictListDto
import com.friendspharma.app.features.data.remote.model.DivisionListDto
import com.friendspharma.app.features.data.remote.model.LoginDto
import com.friendspharma.app.features.data.remote.model.OrderDetailsDto
import com.friendspharma.app.features.data.remote.model.OrdersDto
import com.friendspharma.app.features.data.remote.model.PendingDeliveryDto
import com.friendspharma.app.features.data.remote.model.ProductAddDto
import com.friendspharma.app.features.data.remote.model.ProductRemoveDto
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnListDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDto
import com.friendspharma.app.features.data.remote.model.SubmitOrderDto
import com.friendspharma.app.features.data.remote.model.SubmitReturnDto
import com.friendspharma.app.features.data.remote.model.ThanaListDto
import com.friendspharma.app.features.data.remote.model.TokenDto
import com.friendspharma.app.features.data.remote.model.TrackOrderDto
import com.friendspharma.app.features.data.remote.model.UserDetailsDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface Apis {

    @Headers("Content-Type: application/json")
    @GET("auth/Token")
    suspend fun getToken(): TokenDto

    @Headers("Content-Type: application/json")
    @GET("user/login")
    suspend fun loginDto(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("userName") userName: String,
        @Query("passWord") passWord: String,
    ): LoginDto

    @Headers("Content-Type: application/json")
    @POST("user/insUser")
    suspend fun signUp(
        @Header("Authorization") token: String = MainActivity.token,
        @Body signUp: SignUp
    ): LoginDto

    @Headers("Content-Type: application/json")
    @PUT("user/changePassword")
    suspend fun changePassword(
        @Header("Authorization") token: String = MainActivity.token,
        @Body changePassword: ChangePassword
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @GET("product/getProduct")
    suspend fun getProducts(
        @Header("Authorization") token: String = MainActivity.token,
    ): ProductsDto

    @Headers("Content-Type: application/json")
    @GET("product/GetProductRetail")
    suspend fun getRetailProducts(
        @Header("Authorization") token: String = MainActivity.token,
    ): ProductsDto

    @Headers("Content-Type: application/json")
    @GET("product/GetProductSpecial")
    suspend fun getSpecialProducts(
        @Header("Authorization") token: String = MainActivity.token,
    ): ProductsDto

    @Headers("Content-Type: application/json")
    @GET("product/GetProductByCompany")
    suspend fun getProductsByCompany(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pCompany") id: String
    ): ProductsDto

    @Headers("Content-Type: application/json")
    @GET("product/GetProductByCategory")
    suspend fun getProductsByCategory(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pCategory") id: String
    ): ProductsDto

    @Headers("Content-Type: application/json")
    @POST("addcart/productadd")
    suspend fun productAdd(
        @Header("Authorization") token: String = MainActivity.token,
        @Body product: ProductAdd
    ): ProductAddDto

    @Headers("Content-Type: application/json")
    @GET("addcart/CartInfo")
    suspend fun getCartInfo(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("mobile_no") mobile: String
    ): CartInfoDto

    @Headers("Content-Type: application/json")
    @POST("addcart/productRemove")
    suspend fun productRemove(
        @Header("Authorization") token: String = MainActivity.token,
        @Body productRemove: ProductRemove
    ): ProductRemoveDto

    @Headers("Content-Type: application/json")
    @POST("addcart/SubmitOrder")
    suspend fun submitOrder(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pArea") area: String,
        @Body submitOrder: SubmitOrder
    ): SubmitOrderDto

    @Headers("Content-Type: application/json")
    @GET("manageorder/GetOrderList")
    suspend fun getOrders(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("mobile_no") mobile: String
    ): OrdersDto

    @Headers("Content-Type: application/json")
    @GET("manageorder/GetorderDetails")
    suspend fun getOrderDetails(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pid_tran_mst") id: String
    ): OrderDetailsDto

    @Headers("Content-Type: application/json")
    @GET("user/getAddress")
    suspend fun getAddress(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("userid") id: String
    ): AddressDto

    @Headers("Content-Type: application/json")
    @POST("user/insAddress")
    suspend fun insertAddress(
        @Header("Authorization") token: String = MainActivity.token,
        @Body body: InsertAddress
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @PUT("user/changeAddress")
    suspend fun changeAddress(
        @Header("Authorization") token: String = MainActivity.token,
        @Body body: ChangeAddress
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "user/deleteAddress", hasBody = true)
    suspend fun deleteAddress(
        @Header("Authorization") token: String = MainActivity.token,
        @Body body: DeleteAddress
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @POST("user/insWholesaler")
    suspend fun signUpWholeSeller(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("userName") userName: String,
        @Query("email") email: String,
        @Query("passWordNo") password: String,
        @Query("address") address: String,
        @Query("drugno") drugNo: String,
        @Query("l_mimetype") mimeType: String,
        @Query("l_ext") ext: String?,
        @Query("mobileNo") mobileNo: String,
        @Query("lisimg_name") fileName: String,
        @Query("usertype") userType: Int,
        @Header("Content-Type") contentType: String,
        @Body file: RequestBody
    ): LoginDto

    @Headers("Content-Type: application/json")
    @GET("user/getUser")
    suspend fun getUser(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("userName") id: String
    ): UserDetailsDto

    @Headers("Content-Type: application/json")
    @GET("company/getCompany")
    suspend fun getAllCompanies(
        @Header("Authorization") token: String = MainActivity.token
    ): AllCompanyDto

    @Headers("Content-Type: application/json")
    @GET("category/getCategory")
    suspend fun getAllCategory(
        @Header("Authorization") token: String = MainActivity.token
    ): AllCategoryDto

    @Headers("Content-Type: application/json")
    @PUT("user/updateUser")
    suspend fun updateProfile(
        @Header("Authorization") token: String = MainActivity.token,
        @Body profile: UpdateProfile
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @GET("addcart/AddCartRestrict")
    suspend fun addToCartRestrict(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("mobile_no") mobile: String
    ): AddToCartRestrictDto

    // ── Delivery: Tab 0 - Order List ──────────────────────────────────────────
    @Headers("Content-Type: application/json")
    @GET("delivery/GetPendingDelivery")
    suspend fun getPendingDeliveries(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pDelivaryman") id: String
    ): PendingDeliveryDto

    // ── Delivery: Tab 0 Action - Confirm Pickup → Intransit ───────────────────
    @Headers("Content-Type: application/json")
    @PUT("delivery/DeliverIntransitInvoice")
    suspend fun confirmPickup(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pid_tran_mst") id: String,
        @Body body: RequestBody = "{}".toRequestBody("text/plain".toMediaType())
    ): DeliveredDto

    // ── Delivery: Tab 1 - Intransit List ─────────────────────────────────────
    @Headers("Content-Type: application/json")
    @GET("delivery/GetIntrasitInvoice")
    suspend fun getIntransitDeliveries(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pDelivaryman") id: String
    ): PendingDeliveryDto

    // ── Delivery: Tab 1 Action - Confirm Delivered → Delivered ───────────────
    @Headers("Content-Type: application/json")
    @PUT("delivery/DeliverPendingInvoice")
    suspend fun confirmDelivery(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pid_tran_mst") id: String,
        @Query("pUser") user: String,
        @Body() body: RequestBody = "{}".toRequestBody("text/plain".toMediaType())
    ): DeliveredDto

    // ── Delivery: Tab 2 - Delivered List ─────────────────────────────────────
    @Headers("Content-Type: application/json")
    @GET("delivery/GetDeliveryDone")
    suspend fun getDeliveryDone(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pDelivaryman") id: String
    ): PendingDeliveryDto

    // ── Delivery: Tab 2 Action - Cash Collection → Paid ──────────────────────
    @Headers("Content-Type: application/json")
    @PUT("delivery/CashCollection")
    suspend fun confirmCashCollection(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pid_tran_mst") id: String,
        @Query("pUser") user: String,
        @Body() body: RequestBody = "{}".toRequestBody("text/plain".toMediaType())
    ): DeliveredDto

    // ── Delivery: Tab 3 - Cash Collection List ────────────────────────────────
    @Headers("Content-Type: application/json")
    @GET("delivery/GetPaidOrderList")
    suspend fun getPaidDeliveries(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pDelivaryman") id: String
    ): PendingDeliveryDto

    @Headers("Content-Type: application/json")
    @GET("manageorder/OrderTracking")
    suspend fun trackOrder(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pid_tran_mst") id: String,
    ): TrackOrderDto

    @Headers("Content-Type: application/json")
    @GET("return/ProductReturn")
    suspend fun returnProduct(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pPID_TRAN_MST") id: String,
    ): ReturnProductDto

    @Headers("Content-Type: application/json")
    @POST("return/AddReturn")
    suspend fun addToReturn(
        @Header("Authorization") token: String = MainActivity.token,
        @Body addReturn: AddReturn
    ): AddReturnDto

    @Headers("Content-Type: application/json")
    @GET("return/returnCartinfo")
    suspend fun returnCartInfo(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("mobile_no") mobile: String
    ): ReturnCartInfoDto

    @Headers("Content-Type: application/json")
    @PUT("return/SubmitReturn")
    suspend fun submitReturn(
        @Header("Authorization") token: String = MainActivity.token,
        @Body submitReturn: SubmitReturn
    ): SubmitReturnDto

    @Headers("Content-Type: application/json")
    @GET("return/ReturnList")
    suspend fun getReturnList(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("pUser") user: String
    ): ReturnListDto

    @Headers("Content-Type: application/json")
    @POST("return/ReturnCartRemove")
    suspend fun returnCartRemove(
        @Header("Authorization") token: String = MainActivity.token,
        @Body remove: ReturnCartRemove
    ): DefaultDto

    @Headers("Content-Type: application/json")
    @GET("user/getDivision")
    suspend fun getDivision(
        @Header("Authorization") token: String = MainActivity.token,
    ): DivisionListDto

    @Headers("Content-Type: application/json")
    @GET("user/getDistrict")
    suspend fun getDistrict(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("division") id: String
    ): DistrictListDto

    @Headers("Content-Type: application/json")
    @GET("user/getThana")
    suspend fun getThana(
        @Header("Authorization") token: String = MainActivity.token,
        @Query("district") id: String
    ): ThanaListDto
}