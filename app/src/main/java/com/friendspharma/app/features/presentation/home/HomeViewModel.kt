package com.friendspharma.app.features.presentation.home

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.focus.FocusManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.MainActivity.Companion.isRestrict
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Utils
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.entity.ProductAdd
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem
import com.friendspharma.app.features.data.remote.model.CategoryProducts
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.data.remote.model.UserDto
import com.friendspharma.app.features.domain.services.LocalConstant
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.AddToCartRestrictUseCase
import com.friendspharma.app.features.domain.use_case.GetAllCompanyUseCase
import com.friendspharma.app.features.domain.use_case.GetCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.GetProductsUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.ProductAddUseCase
import com.friendspharma.app.features.domain.use_case.ProductRemoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCartInfoUseCase: GetCartInfoUseCase,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val productAddUseCase: ProductAddUseCase,
    private val productRemoveUseCase: ProductRemoveUseCase,
    private val getAllCompanyUseCase: GetAllCompanyUseCase,
    private val getRestrictUseCase: AddToCartRestrictUseCase,
    application: Application,
    private val getUserUseCase: GetUserUseCase
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val pendingAddProducts = mutableSetOf<Int>()

    private var isForcedLogout: Boolean = false

    fun notifyForcedLogout() {
        isForcedLogout = true
        // ✅ Do NOT reset lastKnownLoginState here!
        // hasActiveSession() reads it — must stay true so LaunchedEffect(isLoggedIn)
        // calls reloadForLogout() after force logout.
        // reloadForLogout() itself will reset lastKnownLoginState = false.
    }

    // ── Jobs ──────────────────────────────────────────────────────────────────
    private var filterJob: Job? = null
    private var stockPollJob: Job? = null
    private var cartPollJob: Job? = null
    private var searchJob: Job? = null

    // ── Track background time ─────────────────────────────────────────────────
    private var backgroundedAt: Long = 0L

    // ── Track login state ─────────────────────────────────────────────────────
    private var lastKnownLoginState: Boolean = false
    private var lastKnownUserType: String = ""
    private var isInitializing: Boolean = false  // ✅ guard against double init()

    // ─────────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────────

    fun init(navAction: NavigationActions) {
        if (isInitializing) return
        isInitializing = true

        viewModelScope.launch {
            val isLoggedIn = MainActivity.isLoggedIn.value
            val userType   = MainActivity.userType.value

            // DEBUG LOGS — uncomment to enable FP_FLOW logging
            //android.util.Log.d("FP_FLOW", "init() — isLoggedIn=$isLoggedIn userType=$userType")

            // Delivery man has no product screen — skip
            if (userType == "4") {
                isInitializing = false
                return@launch
            }

            // Set correct Box/Leaf mode FIRST
            checkBoxOrLeaf()

            // Try memory cache first (instant), then DB
            val cached = getProductsUseCase.getCachedForUserType(userType)
            //android.util.Log.d("FP_FLOW", "cache: ${cached.size} items ${if(cached.isEmpty()) "(no cache)" else "(hit)"}")

            if (cached.isNotEmpty()) {
                // ✅ Cache hit — show products immediately, no skeleton
                _state.update {
                    it.copy(
                        products  = ProductsDto(data = cached),
                        isLoading = false
                    )
                }
                filterProducts()
            } else {
                // ✅ No cache — show skeleton while API loads
                _state.update {
                    it.copy(
                        products   = ProductsDto(),
                        allProduct = arrayListOf(),
                        isLoading  = true
                    )
                }
                // Pre-warm other userType caches in background
                // So next login/logout hits memory cache instantly
                launch {
                    listOf("", "1", "2")
                        .filter { it != userType }
                        .forEach { t -> getProductsUseCase.getCachedForUserType(t) }
                }
            }

            // Always fetch fresh from API in background
            if (!isLoggedIn) {
                launch { getAllCompanies() }
                launch { getProducts() }
            } else {
                launch { getAllCompanies() }
                launch { getCartInfo() }
                launch { checkRestriction() }
                launch { getProducts() }
                launch { getUserProfile(navAction) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    fun onAppForegrounded(navAction: NavigationActions) {
        val awayMs       = System.currentTimeMillis() - backgroundedAt
        val firstLoad    = state.value.products.data.isNullOrEmpty()

        // ✅ Capture BEFORE updating lastKnown values
        val isNowLoggedIn = MainActivity.isLoggedIn.value
        val isNowUserType = MainActivity.userType.value
        val loginChanged  = isNowLoggedIn != lastKnownLoginState
        val userChanged   = isNowUserType != lastKnownUserType

        // Update AFTER computing diff
        lastKnownLoginState = isNowLoggedIn
        lastKnownUserType   = isNowUserType

        when {
            // ✅ LOGOUT detected — LaunchedEffect handles via reloadForLogout()
            loginChanged && !isNowLoggedIn -> {
                // reloadForLogout() via LaunchedEffect handles everything
                // Do NOT call init() — causes race condition + double API call
            }

            firstLoad || loginChanged || userChanged -> {
                // ✅ LOGIN or userType change — reset guard and init()
                isInitializing = false  // reset so init() can run
                init(navAction)         // always call — guard is inside init()
            }
            else -> {
                viewModelScope.launch {
                    // checkBoxOrLeaf() skipped — LaunchedEffect(isLoggedIn)
                    // in HomeScreen already handles it on any login state change.
                    // Calling it again here causes double filterProducts().
                    if (MainActivity.isLoggedIn.value) {
                        launch { getUserProfile(navAction) }
                        launch { getCartInfo() }
                    }
                    if (awayMs > 5 * 60 * 1000) refreshStockSilently()
                }
            }
        }

        startStockPolling()
        startCartPolling()
    }

    fun hasActiveSession(): Boolean = lastKnownLoginState

    fun reloadForLogout(navAction: NavigationActions) {
        //android.util.Log.d("FP_FLOW", "reloadForLogout called")
        lastKnownLoginState = false
        lastKnownUserType   = ""    // ✅ sync with MainActivity.userType=""
        isForcedLogout      = false
        isInitializing      = true  // ✅ block init() from firing simultaneously
// Step 1: Clear cart state immediately
        _state.update {
            it.copy(
                cartInfo         = CartInfoDto(),
                cartItemQuantity = 0,
                cartIds          = HashSet(),
                addToCartLoading = "",
                isBox            = false
            )
        }
        MainActivity.cartQuantity.intValue = 0
        stopPolling()

        // Step 2: Load guest products from correct "product" table
        viewModelScope.launch {
            val guestCache = getProductsUseCase.getCachedForUserType("")
            //android.util.Log.d("FP_FLOW", "reloadForLogout — guest cache: ${guestCache.size} items")
            if (guestCache.isNotEmpty()) {
                // ✅ Guest cache exists — set products AND allProduct immediately
                // Don't rely on filterProductsWithEmptyCart async — do it inline
                _state.update {
                    it.copy(
                        products  = ProductsDto(data = guestCache),
                        isLoading = false
                    )
                }
                // filter runs on Default dispatcher but products already visible
                filterProductsWithEmptyCart(isBox = false)
// ✅ No background API call here — stock polling handles refresh every 15s
                // Calling getProducts() here causes double API call with init()
            } else {
                // No guest cache — show skeleton, fetch from API
                _state.update {
                    it.copy(
                        products   = ProductsDto(),
                        allProduct = arrayListOf(),
                        isLoading  = true
                    )
                }
                launch { getAllCompanies() }
                launch { getProducts() }
            }
            isInitializing = false  // ✅ allow next init() after logout complete
        }
    }

    fun onAppBackgrounded() {
        backgroundedAt = System.currentTimeMillis()
        stopPolling()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POLLING
    // ─────────────────────────────────────────────────────────────────────────

    fun startStockPolling() {
        stockPollJob?.cancel()
        stockPollJob = viewModelScope.launch {
            while (true) {
                delay(15_000)
                refreshStockSilently()
            }
        }
    }

    fun onProductDetailOpened() {
        stockPollJob?.cancel()
        stockPollJob = viewModelScope.launch {
            while (true) {
                delay(5_000)
                refreshStockSilently()
            }
        }
    }

    fun onProductDetailClosed() {
        startStockPolling()
    }

    private fun startCartPolling() {
        if (!MainActivity.isLoggedIn.value) return
        cartPollJob?.cancel()
        cartPollJob = viewModelScope.launch {
            while (true) {
                delay(20_000)
                getCartInfo()
            }
        }
    }

    fun stopPolling() {
        stockPollJob?.cancel()
        cartPollJob?.cancel()
    }

    // ── Surgical stock update — zero UI flicker ───────────────────────────────
    // Only updates STOCK_QTY fields in-place per product item.
    // GetProductsUseCase detects stock-only changes and skips emitting
    // a full product list — this function handles the UI update instead.
    private fun refreshStockSilently() {
        // Fetch fresh from API every 15s
        // Handles: stock changes + new products + removed products
        //
        // isForceRefresh = true → skip the cache path so this poll receives ONLY
        // the single fresh API list. Without it, invoke() also replays the 100-item
        // Phase-1 cache emission, which the removed-products diff below misreads as
        // "2400+ products removed" and shrinks the UI to 100 until the next emission.
        getProductsUseCase.invoke(isForceRefresh = true).onEach { result ->
            when (result) {
                is Async.Success -> {
                    val freshProducts = result.data?.data ?: return@onEach
                    val currentData   = _state.value.products.data ?: return@onEach

                    val freshMap   = freshProducts.associateBy { it.PID_PRODUCT }
                    val currentMap = currentData.associateBy { it.PID_PRODUCT }

                    // Check 1: Stock changed
                    var anyStockChanged = false
                    val updatedData = currentData.map { existing ->
                        val fresh = freshMap[existing.PID_PRODUCT]
                        if (fresh != null &&
                            (fresh.STOCK_QTY_BOX  != existing.STOCK_QTY_BOX ||
                                    fresh.STOCK_QTY_LEAF != existing.STOCK_QTY_LEAF)
                        ) {
                            anyStockChanged = true
                            existing.copy(
                                STOCK_QTY_BOX  = fresh.STOCK_QTY_BOX,
                                STOCK_QTY_LEAF = fresh.STOCK_QTY_LEAF
                            )
                        } else existing
                    }

                    // Check 2: New product added in backend
                    val hasNewProducts = freshProducts.any { it.PID_PRODUCT !in currentMap }

                    // Check 3: Product removed from backend
                    val hasRemovedProducts = currentData.any { it.PID_PRODUCT !in freshMap }

                    when {
                        // New products added — append to UI + update DB & memory
                        hasNewProducts -> {
                            val newItems   = freshProducts.filter { it.PID_PRODUCT !in currentMap }
                            val mergedData = updatedData + newItems
                            // ✅ Update UI (no glitch — append only)
                            _state.update {
                                it.copy(products = ProductsDto(data = mergedData))
                            }
                            filterProducts()
                            // ✅ Update DB + memory cache silently in background
                            viewModelScope.launch {
                                getProductsUseCase.storeProductsPublic(
                                    MainActivity.userType.value,
                                    mergedData
                                )
                            }
                        }
                        // Products removed — remove from UI + update DB & memory
                        hasRemovedProducts -> {
                            val validData = updatedData.filter { it.PID_PRODUCT in freshMap }
                            // ✅ Update UI surgically
                            _state.update {
                                it.copy(products = ProductsDto(data = validData))
                            }
                            filterProducts()
                            // ✅ Update DB + memory cache silently in background
                            viewModelScope.launch {
                                getProductsUseCase.storeProductsPublic(
                                    MainActivity.userType.value,
                                    validData
                                )
                            }
                        }
                        // Only stock changed — surgical in-place update
                        anyStockChanged -> {
                            _state.update {
                                it.copy(products = it.products.copy(data = updatedData))
                            }
                            filterProducts()
                            // ✅ Update DB + memory cache for stock changes too
                            viewModelScope.launch {
                                getProductsUseCase.storeProductsPublic(
                                    MainActivity.userType.value,
                                    updatedData
                                )
                            }
                        }
                        // Nothing changed — do nothing
                    }
                }
                is Async.Error   -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER PROFILE
    // ─────────────────────────────────────────────────────────────────────────

    fun getUserProfile(navAction: NavigationActions) {
        if (!MainActivity.isLoggedIn.value) return

        getUserUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        // firstOrNull() — safe, no crash if API returns empty list
                        val user = result.data?.data?.firstOrNull() ?: return@onEach

                        sharedPreferenceHelper.saveUser(
                            UserDto(
                                MOBILE_NO       = user.MOBILE_NO,
                                PASSWORD        = user.PASSWORD,
                                USER_ID         = user.USER_ID,
                                USER_NAME       = user.USER_NAME,
                                USER_TYPE       = user.USER_TYPE.toString(),
                                APPROVAL_STATUS = sharedPreferenceHelper.getUser().APPROVAL_STATUS
                            )
                        )

                        val freshUserType = user.USER_TYPE?.toString() ?: return@onEach

                        // Delivery man has no product screen — set type, route out,
                        // and stop here so the product re-fetch below never runs for
                        // type "4" (which would otherwise pull retail products).
                        if (freshUserType == "4") {
                            MainActivity.userType.value = freshUserType
                            navAction.navToDeliveryMan()
                            return@onEach
                        }

                        if (freshUserType != MainActivity.userType.value) {
                            // Server-resolved userType differs from the type we ALREADY
                            // fetched products under. init() launches getProducts() and
                            // getUserProfile() concurrently, so getProducts() can win the
                            // race using a stale type — leaving state.products holding the
                            // WRONG bucket's pricing (e.g. wholesale BOX_SALES_PER shown to
                            // a special user). Clearing + re-filtering is NOT enough; we
                            // must re-fetch under the correct type.
                            val oldUserType = MainActivity.userType.value
                            MainActivity.userType.value = freshUserType  // set FIRST so invoke() captures it
                            viewModelScope.launch {
                                getProductsUseCase.clearCacheFor(oldUserType) // drop stale old-bucket data
                                checkBoxOrLeaf()                              // correct Box/Leaf for new type
                                getProducts()                                 // ✅ re-fetch correct-type pricing
                            }
                        } else {
                            MainActivity.userType.value = freshUserType
                        }
                    }
                    is Async.Error<*>   -> {}
                    is Async.Loading<*> -> {}
                }
            }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESTRICTION CHECK
    // ─────────────────────────────────────────────────────────────────────────


    private fun checkRestriction() {
        isRestrict.intValue = sharedPreferenceHelper.getIntData(LocalConstant.isRestrict)
        getRestrictUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success<*> -> {
                        isRestrict.intValue = result.data?.isexists ?: -1
                        sharedPreferenceHelper.saveIntData(
                            LocalConstant.isRestrict,
                            isRestrict.intValue
                        )
                    }
                    is Async.Error<*>   -> {}
                    is Async.Loading<*> -> {}
                }
            }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOX / LEAF
    // ─────────────────────────────────────────────────────────────────────────

    fun checkBoxOrLeaf() {
        val userType   = MainActivity.userType.value
        val isLoggedIn = MainActivity.isLoggedIn.value

        // Behaviour table:
        // guest  (not logged in / userType "")  → leaf (false)
        // type 1 (retailer)                     → leaf (false)
        // type 2 (wholesaler)                   → box  (true)
        // type 3 (special customer)             → box  (true)
        val isBox = isLoggedIn && (userType == "2" || userType == "3")

        _state.update { it.copy(isBox = isBox) }
        if (!state.value.products.data.isNullOrEmpty()) {
            filterProducts(isBox)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPANIES
    // ─────────────────────────────────────────────────────────────────────────

    private fun getAllCompanies() {
        getAllCompanyUseCase.invoke().onEach { result ->
            when (result) {
                is Async.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            companies = result.data ?: AllCompanyDto()
                        )
                    }
                }
                is Async.Error<*>   -> {}
                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRODUCTS
    // ─────────────────────────────────────────────────────────────────────────

    fun getProducts(isRefresh: Boolean = false) {
        getProductsUseCase.invoke().onEach { result ->
            when (result) {
                is Async.Success -> {
                    val count = result.data?.data?.size ?: 0
                    //android.util.Log.d("FP_FLOW", "getProducts Success — $count products")
                    _state.update {
                        it.copy(
                            products       = result.data ?: ProductsDto(),
                            refreshLoading = false
                        )
                    }
                    filterProducts()
                    isInitializing = false
                }
                is Async.Error -> {
                    _state.update { it.copy(isLoading = false, refreshLoading = false) }
                    isInitializing = false
                }
                is Async.Loading -> {
                    if (isRefresh) {
                        _state.update { it.copy(refreshLoading = true) }
                    }
                    // Don't set isLoading here — already set in init() if needed
                    // Avoids skeleton flash when cache is about to be served
                }
            }
        }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTER PRODUCTS
    // ─────────────────────────────────────────────────────────────────────────

    private fun filterProductsWithEmptyCart(isBox: Boolean = state.value.isBox) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            if (state.value.products.data.isNullOrEmpty()) return@launch

            val emptyBoxIds   = hashSetOf<Int>()
            val emptyStripIds = hashSetOf<Int>()

            fun isVisible(product: ProductsDtoItem): Boolean {
                val pid = product.PID_PRODUCT
                return if (isBox) {
                    // If BOX stock is null/0, fall back to LEAF stock
                    // Some API endpoints may not return BOX stock separately
                    val boxStock  = product.STOCK_QTY_BOX  ?: 0.0
                    val leafStock = product.STOCK_QTY_LEAF ?: 0.0
                    emptyBoxIds.contains(pid) || boxStock >= 1.0 || leafStock >= 1.0
                } else {
                    emptyStripIds.contains(pid) || (product.STOCK_QTY_LEAF ?: 0.0) >= 1.0
                }
            }

            if (state.value.isSortedeByCategory) {
                val grouped = state.value.products.data
                    ?.filter { isVisible(it) }
                    ?.groupBy { it.PID_CATEGORY }
                    ?.map { (_, productsInCategory) ->
                        CategoryProducts(
                            category = productsInCategory.first().CATEGORY_NAME ?: "",
                            data     = productsInCategory
                        )
                    } ?: emptyList()

                _state.update {
                    it.copy(
                        allProduct           = ArrayList(grouped),
                        currentCategoryIndex = -1,
                        isLoading            = false,
                        refreshLoading       = false
                    )
                }
            } else {
                val products: ArrayList<CategoryProducts> =
                    ArrayList(state.value.companies.data?.size ?: 0)
                for (pharma in state.value.companies.data ?: emptyList()) {
                    val filtered = state.value.products.data
                        ?.filter { product ->
                            product.PID_COMPANY == pharma.PID_COMPANY && isVisible(product)
                        } ?: emptyList()
                    products.add(CategoryProducts(pharma.COMPANY_NAME ?: "", filtered))
                }
                _state.update {
                    it.copy(
                        allProduct           = products,
                        isLoading            = false,
                        currentCategoryIndex = -1,
                        isSortedeByCategory  = false,
                        refreshLoading       = false
                    )
                }
            }
        }
    }

    private fun filterProducts(isBox: Boolean = state.value.isBox) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {

            if (state.value.products.data.isNullOrEmpty()) return@launch

            val cartBoxIds = state.value.cartInfo.data
                ?.filter { it.SALES_UNIT == "BOX" }
                ?.mapNotNull { it.PID_PRODUCT }
                ?.toHashSet() ?: hashSetOf()

            val cartStripIds = state.value.cartInfo.data
                ?.filter { it.SALES_UNIT == "STRIP" }
                ?.mapNotNull { it.PID_PRODUCT }
                ?.toHashSet() ?: hashSetOf()

            if (state.value.isSortedeByCategory) {
                val grouped = state.value.products.data
                    ?.filter { product ->
                        val pid = product.PID_PRODUCT
                        if (isBox) {
                            val boxStock  = product.STOCK_QTY_BOX  ?: 0.0
                            val leafStock = product.STOCK_QTY_LEAF ?: 0.0
                            cartBoxIds.contains(pid) || boxStock >= 1.0 || leafStock >= 1.0
                        } else {
                            cartStripIds.contains(pid) || (product.STOCK_QTY_LEAF ?: 0.0) >= 1.0
                        }
                    }
                    ?.groupBy { it.PID_CATEGORY }
                    ?.map { (_, productsInCategory) ->
                        CategoryProducts(
                            category = productsInCategory.first().CATEGORY_NAME ?: "",
                            data     = productsInCategory
                        )
                    } ?: emptyList()

                _state.update {
                    it.copy(
                        allProduct           = ArrayList(grouped),
                        currentCategoryIndex = -1,
                        isLoading            = false,
                        refreshLoading       = false
                    )
                }
            } else {
                val products: ArrayList<CategoryProducts> = ArrayList(
                    state.value.companies.data?.size ?: 0
                )
                for (pharma in state.value.companies.data ?: emptyList()) {
                    val filtered = state.value.products.data?.filter { product ->
                        val pid = product.PID_PRODUCT
                        val stockOk = if (isBox) {
                            val boxStock  = product.STOCK_QTY_BOX  ?: 0.0
                            val leafStock = product.STOCK_QTY_LEAF ?: 0.0
                            cartBoxIds.contains(pid) || boxStock >= 1.0 || leafStock >= 1.0
                        } else {
                            cartStripIds.contains(pid) || (product.STOCK_QTY_LEAF ?: 0.0) >= 1.0
                        }
                        product.PID_COMPANY == pharma.PID_COMPANY && stockOk
                    } ?: emptyList()
                    products.add(CategoryProducts(pharma.COMPANY_NAME ?: "", filtered))
                }
                _state.update {
                    it.copy(
                        allProduct           = products,
                        isLoading            = false,
                        currentCategoryIndex = -1,
                        isSortedeByCategory  = false,
                        refreshLoading       = false
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SORT
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun sortByPharmaceutical(scrollSate: LazyGridState? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            filterProducts(state.value.isBox)
        }
        scrollSate?.animateScrollToItem(0)
    }

    suspend fun sortByCategory(scrollSate: LazyGridState) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isSortedeByCategory = true) }
            filterProducts(state.value.isBox)
        }
        scrollSate.animateScrollToItem(0)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CURRENT ITEM
    // ─────────────────────────────────────────────────────────────────────────

    fun updateCurrentItem(item: ProductsDtoItem) {
        _state.update { it.copy(currentItem = item) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────────────────

    fun searchChanged(text: String, focusManager: FocusManager) {
        _state.update { it.copy(search = text) }
        searchJob?.cancel()

        if (text.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(300)   // ✅ 300ms debounce — smooth typing, no lag
                if (state.value.search == text) {
                    val items = state.value.products.data?.filter {
                        it.PRODUCT_NAME?.lowercase()?.contains(text.lowercase()) == true ||
                                it.COMPANY_NAME?.lowercase()?.contains(text.lowercase()) == true ||
                                it.CATEGORY_NAME?.lowercase()?.contains(text.lowercase()) == true
                    } ?: emptyList()
                    _state.update { it.copy(searchedProduct = items) }
                }
            }
        } else {
            focusManager.clearFocus()
            _state.update { it.copy(searchedProduct = emptyList()) }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CART INFO
    // ─────────────────────────────────────────────────────────────────────────

    private fun getCartInfo() {
        if (MainActivity.isLoggedIn.value) {
            getCartInfoUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
                .onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            val mergedItems = result.data?.data
                                ?.groupBy { Pair(it.PID_PRODUCT, it.SALES_UNIT) }
                                ?.map { (_, items) ->
                                    val firstItem     = items.first()
                                    val totalQuantity = items.sumOf { it.QUANTITY?.toDouble() ?: 0.0 }
                                    val totalPrice    = items.sumOf { it.TOTAL_PRICE ?: 0.0 }
                                    firstItem.copy(
                                        QUANTITY    = Math.round(totalQuantity),
                                        TOTAL_PRICE = totalPrice
                                    )
                                } ?: emptyList()

                            var quantity = 0
                            val set = HashSet<Int>()
                            for (item in mergedItems) {
                                quantity += item.QUANTITY?.toInt() ?: 0
                                set.add(item.PID_PRODUCT ?: -1)
                            }

                            val cleanCartInfo = CartInfoDto(
                                data    = mergedItems,
                                message = result.data?.message
                            )

                            _state.update {
                                it.copy(
                                    cartInfo         = cleanCartInfo,
                                    cartItemQuantity = quantity,
                                    cartIds          = set,
                                    addToCartLoading = ""
                                )
                            }
                            MainActivity.cartQuantity.intValue = quantity
                            filterProducts()
                        }
                        is Async.Error   -> {}
                        is Async.Loading -> {}
                    }
                }.launchIn(viewModelScope)
        } else {
            _state.update {
                it.copy(
                    cartInfo         = CartInfoDto(),
                    cartItemQuantity = 0,
                    cartIds          = HashSet()
                )
            }
            filterProducts()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADD TO CART
    // ─────────────────────────────────────────────────────────────────────────

    fun addToCartSafe(
        product: ProductsDtoItem,
        quantity: Int,
        salesUnit: String,
        context: Context
    ) {
        val pid = product.PID_PRODUCT ?: return

        val alreadyInCartWithSameUnit = state.value.cartInfo.data?.any {
            it.PID_PRODUCT == pid && it.SALES_UNIT == salesUnit
        } == true

        val isPending = pendingAddProducts.contains(pid)

        when {
            isPending -> {
                Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
            }
            alreadyInCartWithSameUnit -> {
                Toast.makeText(
                    context,
                    "${product.PRODUCT_NAME} is already in your cart.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val updatedIds = state.value.cartIds.toMutableSet()
                updatedIds.add(pid)
                pendingAddProducts.add(pid)
                _state.update {
                    it.copy(
                        cartIds          = updatedIds as HashSet<Int>,
                        addToCartLoading = pid.toString()
                    )
                }
                filterProducts()
                addToCart(product, maxOf(1, quantity), salesUnit, context)
            }
        }
    }

    fun addToCart(
        product: ProductsDtoItem,
        quantity: Int,
        salesUnit: String,
        context: Context
    ) {
        val safeQuantity = maxOf(1, quantity)

        productAddUseCase.invoke(
            ProductAdd(
                mobile_no   = sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                pid_product = product.PID_PRODUCT.toString(),
                pqty        = safeQuantity.toString(),
                salesunit   = salesUnit
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    pendingAddProducts.remove(product.PID_PRODUCT)
                    getCartInfo()
                    Toast.makeText(context, result.data?.message ?: "", Toast.LENGTH_SHORT).show()
                }
                is Async.Error -> {
                    pendingAddProducts.remove(product.PID_PRODUCT)

                    val errorMsg     = result.data?.message?.lowercase() ?: ""
                    val isOutOfStock = errorMsg.contains("stock") ||
                            errorMsg.contains("unavailable") ||
                            errorMsg.contains("out")

                    if (isOutOfStock) {
                        val updatedData = _state.value.products.data?.map {
                            if (it.PID_PRODUCT == product.PID_PRODUCT)
                                it.copy(STOCK_QTY_BOX = 0.0, STOCK_QTY_LEAF = 0.0)
                            else it
                        }
                        val revertedIds = _state.value.cartIds.toMutableSet()
                        revertedIds.remove(product.PID_PRODUCT)
                        _state.update {
                            it.copy(
                                products         = it.products.copy(data = updatedData),
                                cartIds          = revertedIds as HashSet<Int>,
                                addToCartLoading = ""
                            )
                        }
                        filterProducts()
                        refreshStockSilently()
                        Toast.makeText(
                            context,
                            "Sorry, ${product.PRODUCT_NAME} is currently unavailable. Please check back soon!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val revertedIds = _state.value.cartIds.toMutableSet()
                        revertedIds.remove(product.PID_PRODUCT)
                        _state.update {
                            it.copy(
                                cartIds          = revertedIds as HashSet<Int>,
                                addToCartLoading = ""
                            )
                        }
                        Toast.makeText(
                            context,
                            "Couldn't add to cart. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Async.Loading -> {
                    _state.update {
                        it.copy(addToCartLoading = product.PID_PRODUCT.toString())
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REMOVE FROM CART
    // ─────────────────────────────────────────────────────────────────────────

    fun removeFromCart(item: ProductsDtoItem, salesUnit: String, context: Context) {
        val products = state.value.cartInfo.data?.filter {
            it.PID_PRODUCT == item.PID_PRODUCT
        } ?: emptyList()

        val updatedIds = state.value.cartIds.toMutableSet()
        updatedIds.remove(item.PID_PRODUCT)
        pendingAddProducts.remove(item.PID_PRODUCT)
        _state.update {
            it.copy(
                cartIds          = updatedIds as HashSet<Int>,
                addToCartLoading = ""
            )
        }
        filterProducts()

        products.forEach { cartItem ->
            productRemoveUseCase.invoke(
                ProductRemove(
                    sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                    pid_product  = cartItem.PID_PRODUCT.toString(),
                    pid_tran_dtl = cartItem.PID_TRAN_DTL.toString(),
                    salesunit    = cartItem.SALES_UNIT ?: salesUnit
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        getCartInfo()
                        Toast.makeText(context, result.data?.message ?: "", Toast.LENGTH_SHORT).show()
                    }
                    is Async.Error -> {
                        val revertedIds = state.value.cartIds.toMutableSet()
                        revertedIds.add(item.PID_PRODUCT ?: -1)
                        _state.update { it.copy(cartIds = revertedIds as HashSet<Int>) }
                        filterProducts()
                        Toast.makeText(
                            context,
                            result.data?.message ?: "Failed to remove",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Async.Loading -> {
                        _state.update { it.copy(addToCartLoading = item.PID_PRODUCT.toString()) }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun getProductFromCart(pidProduct: Int?): CartInfoDtoItem {
        for (item in state.value.cartInfo.data ?: emptyList()) {
            if (pidProduct == item.PID_PRODUCT) return item
        }
        return CartInfoDtoItem()
    }

    fun switch() {
        val userType  = MainActivity.userType.value
        val isLoggedIn = MainActivity.isLoggedIn.value

        // Only user type 2 and 3 can toggle between Box and Leaf
        if (!isLoggedIn || userType == "1") return

        val newIsBox = !state.value.isBox
        _state.update { it.copy(isBox = newIsBox) }
        filterProducts(newIsBox)
    }

    fun productsByBox(isBox: Boolean) {
        _state.update { it.copy(isBox = isBox) }
    }

    fun navToWhatsApp(context: Context) {
        Utils.openWhatsAppChat(context, "8801826034230")
    }
}