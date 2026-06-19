package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.entity.AddReturn
import com.friendspharma.app.features.data.remote.entity.ChangeAddress
import com.friendspharma.app.features.data.remote.entity.ChangePassword
import com.friendspharma.app.features.data.remote.entity.DeleteAddress
import com.friendspharma.app.features.data.remote.entity.InsertAddress
import com.friendspharma.app.features.data.remote.entity.ProductAdd
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.entity.ReturnCartRemove
import com.friendspharma.app.features.data.remote.entity.SignUp
import com.friendspharma.app.features.data.remote.entity.SignUpSeller
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
import java.io.File

interface ApiRepo {

    suspend fun getToken(): TokenDto
    suspend fun login(userName: String, password: String): LoginDto
    suspend fun signUp(signUp: SignUp): LoginDto
    suspend fun changePassword(changePassword: ChangePassword): DefaultDto
    suspend fun getProducts(): ProductsDto
    suspend fun getRetailProducts(): ProductsDto
    suspend fun getSpecialProducts(): ProductsDto
    suspend fun productAdd(product: ProductAdd): ProductAddDto
    suspend fun getCartInfo(mobile: String): CartInfoDto
    suspend fun productRemove(productRemove: ProductRemove): ProductRemoveDto
    suspend fun submitOrder(area: String, submitOrder: SubmitOrder): SubmitOrderDto
    suspend fun getOrders(mobile: String): OrdersDto
    suspend fun getOrderDetails(id: String): OrderDetailsDto
    suspend fun getAddress(id: String): AddressDto
    suspend fun insertAddress(body: InsertAddress): DefaultDto
    suspend fun changeAddress(body: ChangeAddress): DefaultDto
    suspend fun deleteAddress(body: DeleteAddress): DefaultDto
    suspend fun signUpWholeSeller(signUp: SignUpSeller, image: File?): LoginDto
    suspend fun getUser(id: String): UserDetailsDto
    suspend fun getAllCompany(): AllCompanyDto
    suspend fun getAllCategory(): AllCategoryDto
    suspend fun updateProfile(profile: UpdateProfile): DefaultDto
    suspend fun getProductsByCompany(id: String): ProductsDto
    suspend fun getProductsByCategory(id: String): ProductsDto
    suspend fun addToCartRestrict(mobile: String): AddToCartRestrictDto

    // ── Tab 0: Order List ─────────────────────────────────────────────────────
    suspend fun getPendingDeliveries(id: String): PendingDeliveryDto

    // ── Tab 0 Action: Confirm Pickup → Intransit ──────────────────────────────
    suspend fun confirmPickup(id: String): DeliveredDto

    // ── Tab 1: Intransit List ─────────────────────────────────────────────────
    suspend fun getIntransitDeliveries(id: String): PendingDeliveryDto

    // ── Tab 1 Action: Confirm Delivered → Delivered ───────────────────────────
    suspend fun confirmDelivery(id: String, user: String): DeliveredDto

    // ── Tab 2: Delivered List ─────────────────────────────────────────────────
    suspend fun getDeliveryDone(id: String): PendingDeliveryDto

    // ── Tab 2 Action: Cash Collection ─────────────────────────────────────────
    suspend fun confirmCashCollection(id: String, user: String): DeliveredDto

    // ── Tab 3: Cash Collection List ───────────────────────────────────────────
    suspend fun getPaidDeliveries(id: String): PendingDeliveryDto

    suspend fun trackOrder(id: String): TrackOrderDto
    suspend fun returnProduct(id: String): ReturnProductDto
    suspend fun addToReturn(addReturn: AddReturn): AddReturnDto
    suspend fun returnCartInfo(mobile: String): ReturnCartInfoDto
    suspend fun submitReturn(submitReturn: SubmitReturn): SubmitReturnDto
    suspend fun getReturnList(user: String): ReturnListDto
    suspend fun returnCartRemove(remove: ReturnCartRemove): DefaultDto

    suspend fun getDivision(): DivisionListDto
    suspend fun getDistrict(divisionId: String): DistrictListDto
    suspend fun getThana(districtId: String): ThanaListDto
}