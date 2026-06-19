package com.friendspharma.app.features.data.repository

import android.webkit.MimeTypeMap
import com.friendspharma.app.features.data.remote.Apis
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
import com.friendspharma.app.features.domain.repository.ApiRepo
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepoImpl @Inject constructor(
    private val api: Apis
) : ApiRepo {

    override suspend fun getToken(): TokenDto = api.getToken()

    override suspend fun login(userName: String, password: String): LoginDto =
        api.loginDto(userName = userName, passWord = password)

    override suspend fun signUp(signUp: SignUp): LoginDto =
        api.signUp(signUp = signUp)

    override suspend fun changePassword(changePassword: ChangePassword): DefaultDto =
        api.changePassword(changePassword = changePassword)

    override suspend fun getProducts(): ProductsDto = api.getProducts()

    override suspend fun getRetailProducts(): ProductsDto = api.getRetailProducts()

    override suspend fun getSpecialProducts(): ProductsDto = api.getSpecialProducts()

    override suspend fun productAdd(product: ProductAdd): ProductAddDto =
        api.productAdd(product = product)

    override suspend fun getCartInfo(mobile: String): CartInfoDto =
        api.getCartInfo(mobile = mobile)

    override suspend fun productRemove(productRemove: ProductRemove): ProductRemoveDto =
        api.productRemove(productRemove = productRemove)

    override suspend fun submitOrder(area: String, submitOrder: SubmitOrder): SubmitOrderDto =
        api.submitOrder(area = area, submitOrder = submitOrder)

    override suspend fun getOrders(mobile: String): OrdersDto =
        api.getOrders(mobile = mobile)

    override suspend fun getOrderDetails(id: String): OrderDetailsDto =
        api.getOrderDetails(id = id)

    override suspend fun getAddress(id: String): AddressDto =
        api.getAddress(id = id)

    override suspend fun insertAddress(body: InsertAddress): DefaultDto =
        api.insertAddress(body = body)

    override suspend fun changeAddress(body: ChangeAddress): DefaultDto =
        api.changeAddress(body = body)

    override suspend fun deleteAddress(body: DeleteAddress): DefaultDto =
        api.deleteAddress(body = body)

    override suspend fun signUpWholeSeller(signUp: SignUpSeller, image: File?): LoginDto {
        val extension = image?.extension ?: "jpeg"
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/jpeg"
        return api.signUpWholeSeller(
            userName  = signUp.userName,
            email     = signUp.email,
            password  = signUp.passWordNo,
            address   = signUp.address,
            drugNo    = signUp.drugno,
            mimeType  = mimeType,
            ext       = extension,
            mobileNo  = signUp.mobileNo,
            fileName  = image?.name ?: "license",
            userType  = signUp.usertype.toInt(),
            contentType = mimeType,
            file      = (image ?: File("")).asRequestBody(mimeType.toMediaTypeOrNull())
        )
    }

    override suspend fun getUser(id: String): UserDetailsDto =
        api.getUser(id = id)

    override suspend fun getAllCompany(): AllCompanyDto = api.getAllCompanies()

    override suspend fun getAllCategory(): AllCategoryDto = api.getAllCategory()

    override suspend fun updateProfile(profile: UpdateProfile): DefaultDto =
        api.updateProfile(profile = profile)

    override suspend fun getProductsByCompany(id: String): ProductsDto =
        api.getProductsByCompany(id = id)

    override suspend fun getProductsByCategory(id: String): ProductsDto =
        api.getProductsByCategory(id = id)

    override suspend fun addToCartRestrict(mobile: String): AddToCartRestrictDto =
        api.addToCartRestrict(mobile = mobile)

    // ── Tab 0: Order List ─────────────────────────────────────────────────────
    override suspend fun getPendingDeliveries(id: String): PendingDeliveryDto =
        api.getPendingDeliveries(id = id)

    // ── Tab 0 Action: Confirm Pickup → Intransit ──────────────────────────────
    override suspend fun confirmPickup(id: String): DeliveredDto =
        api.confirmPickup(id = id)

    // ── Tab 1: Intransit List ─────────────────────────────────────────────────
    override suspend fun getIntransitDeliveries(id: String): PendingDeliveryDto =
        api.getIntransitDeliveries(id = id)

    // ── Tab 1 Action: Confirm Delivered → Delivered ───────────────────────────
    override suspend fun confirmDelivery(id: String, user: String): DeliveredDto =
        api.confirmDelivery(id = id, user = user)

    // ── Tab 2: Delivered List ─────────────────────────────────────────────────
    override suspend fun getDeliveryDone(id: String): PendingDeliveryDto =
        api.getDeliveryDone(id = id)

    // ── Tab 2 Action: Cash Collection ─────────────────────────────────────────
    override suspend fun confirmCashCollection(id: String, user: String): DeliveredDto =
        api.confirmCashCollection(id = id, user = user)

    // ── Tab 3: Cash Collection List ───────────────────────────────────────────
    override suspend fun getPaidDeliveries(id: String): PendingDeliveryDto =
        api.getPaidDeliveries(id = id)

    override suspend fun trackOrder(id: String): TrackOrderDto =
        api.trackOrder(id = id)

    override suspend fun returnProduct(id: String): ReturnProductDto =
        api.returnProduct(id = id)

    override suspend fun addToReturn(addReturn: AddReturn): AddReturnDto =
        api.addToReturn(addReturn = addReturn)

    override suspend fun returnCartInfo(mobile: String): ReturnCartInfoDto =
        api.returnCartInfo(mobile = mobile)

    override suspend fun submitReturn(submitReturn: SubmitReturn): SubmitReturnDto =
        api.submitReturn(submitReturn = submitReturn)

    override suspend fun getReturnList(user: String): ReturnListDto =
        api.getReturnList(user = user)

    override suspend fun returnCartRemove(remove: ReturnCartRemove): DefaultDto =
        api.returnCartRemove(remove = remove)

    override suspend fun getDivision(): DivisionListDto = api.getDivision()

    override suspend fun getDistrict(divisionId: String): DistrictListDto =
        api.getDistrict(id = divisionId)

    override suspend fun getThana(districtId: String): ThanaListDto =
        api.getThana(id = districtId)
}