package com.friendspharma.app.features.domain.use_case

import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.local.dao.ProductDao
import com.friendspharma.app.features.data.local.dao.RetailProductDao
import com.friendspharma.app.features.data.local.dao.SpecialProductDao
import com.friendspharma.app.features.data.local.entities.LocalProductItem
import com.friendspharma.app.features.data.local.entities.LocalRetailProduct
import com.friendspharma.app.features.data.local.entities.LocalSpecialProduct
import com.friendspharma.app.features.data.remote.Apis
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.repository.ProductRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val FIRST_PAGE = 100

// In-memory cache — avoids repeated DB reads (2930 items = 1-2s each time)
// Keyed by data bucket (see cacheKey): guest("") + retail("1") share "retail",
// wholesale("2") = "wholesale", special("3") = "special".
private val memoryCache = mutableMapOf<String, List<ProductsDtoItem>>()

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepo,
    private val productDao: ProductDao,
    private val retailProductDao: RetailProductDao,
    private val specialProductDao: SpecialProductDao,
    private val apis: Apis
) {
    fun invoke(isForceRefresh: Boolean = false): Flow<Async<ProductsDto>> = flow {
        val userType = MainActivity.userType.value

        emit(Async.Loading())

        if (!isForceRefresh) {
            // Phase 1: First 100 items instantly (~50ms)
            val firstPage = getFirstN(userType, FIRST_PAGE)
            if (firstPage.isNotEmpty()) {
                emit(Async.Success(ProductsDto(data = firstPage)))
            }

            // Phase 2: All items from cache
            val allCached = getAllCached(userType)
            if (allCached.size > firstPage.size) {
                emit(Async.Success(ProductsDto(data = allCached)))
            }

            if (allCached.isNotEmpty()) {
                // Background API refresh
                try {
                    val fresh = callApi(userType).data?.filter { it.PID_PRODUCT != null } ?: emptyList()
                    if (fresh.isNotEmpty()) {
                        val cachedMap = allCached.associateBy { it.PID_PRODUCT }
                        val hasChanges = fresh.any { f ->
                            val c = cachedMap[f.PID_PRODUCT]
                            c == null ||
                                    f.PRODUCT_NAME     != c.PRODUCT_NAME     ||
                                    f.LEAF_SALES_PRICE != c.LEAF_SALES_PRICE ||
                                    f.BOX_SALES_PRICE  != c.BOX_SALES_PRICE  ||
                                    f.LEAF_MRP_PRICE   != c.LEAF_MRP_PRICE   ||
                                    f.BOX_MRP_PRICE    != c.BOX_MRP_PRICE    ||
                                    f.IMAGE_URL        != c.IMAGE_URL
                        }
                        storeProducts(userType, fresh)
                        if (hasChanges) emit(Async.Success(ProductsDto(data = fresh)))
                    }
                } catch (e: Exception) { /* silent */ }
                return@flow
            }
        }

        // No cache — fetch from API
        try {
            val fresh = callApi(userType).data?.filter { it.PID_PRODUCT != null } ?: emptyList()
            if (fresh.isNotEmpty()) {
                emit(Async.Success(ProductsDto(data = fresh)))
                storeProducts(userType, fresh)
            }
        } catch (e: Exception) {
            emit(Async.Error(e.message ?: "Failed to load products"))
        }
    }

    suspend fun getCachedForUserType(userType: String): List<ProductsDtoItem> {
        // Check memory first — zero delay
        memoryCache[cacheKey(userType)]?.let { return it }
        return getAllCached(userType)
    }

    // Maps a userType to its shared data bucket / table.
    // guest("") and retail("1") share the retail bucket; wholesale and special are distinct.
    private fun cacheKey(userType: String): String = when (userType) {
        "2"  -> "wholesale"
        "3"  -> "special"
        else -> "retail"   // "" guest + "1" retail
    }

    private suspend fun callApi(userType: String): ProductsDto = when (userType) {
        "2"  -> apis.getProducts()         // wholesale → product/getProduct
        "3"  -> apis.getSpecialProducts()  // special
        else -> apis.getRetailProducts()   // guest ("") + retail ("1")
    }

    private suspend fun getFirstN(userType: String, n: Int): List<ProductsDtoItem> =
        withContext(Dispatchers.Default) {
            when (userType) {
                "2"  -> productDao.getFirstNByUserType("2", n).asSequence().map { it.toDto() }.toList()  // wholesale
                "3"  -> specialProductDao.getFirstN(n).asSequence().map { it.toDto() }.toList()           // special
                else -> retailProductDao.getFirstN(n).asSequence().map { it.toDto() }.toList()            // guest + retail
            }
        }

    private suspend fun getAllCached(userType: String): List<ProductsDtoItem> {
        // ✅ Check memory cache first — instant, no DB read
        val key = cacheKey(userType)
        memoryCache[key]?.let { return it }

        // DB read — only happens once per bucket per session
        return withContext(Dispatchers.Default) {
            val result = when (userType) {
                "2"  -> productDao.getAll().asSequence().map { it.toDto() }.toList()          // wholesale
                "3"  -> specialProductDao.getAll().asSequence().map { it.toDto() }.toList()    // special
                else -> retailProductDao.getAll().asSequence().map { it.toDto() }.toList()     // guest + retail
            }
            memoryCache[key] = result  // store in memory for next call
            result
        }
    }

    // Public — called by HomeViewModel to sync DB + memory after stock/product changes
    suspend fun storeProductsPublic(userType: String, items: List<ProductsDtoItem>) =
        storeProducts(userType, items)

    private suspend fun storeProducts(userType: String, items: List<ProductsDtoItem>) {
        val key = cacheKey(userType)
        // ✅ Only update memory if new data is MORE complete than current
        // Prevents Phase 1 (100 items) from overwriting full cache (2921 items)
        val current = memoryCache[key]
        if (current == null || items.size >= current.size) {
            memoryCache[key] = items
        }
        // Update DB in background
        when (userType) {
            "2" -> {   // wholesale
                productDao.deleteAll()
                productDao.upsertAll(items.map { it.toProductLocal() })
            }
            "3" -> {   // special
                specialProductDao.deleteAll()
                specialProductDao.upsertAll(items.map { it.toSpecialLocal() })
            }
            else -> {  // guest ("") + retail ("1")
                retailProductDao.deleteAll()
                retailProductDao.upsertAll(items.map { it.toRetailLocal() })
            }
        }
    }

    // Call this on logout to clear memory cache for logged-in user
    fun clearMemoryCache(userType: String) {
        memoryCache.remove(cacheKey(userType))
    }

    // Clears BOTH the in-memory cache and the DB table for a userType's bucket.
    // Used when the server-side userType changes, so the next fetch repopulates
    // the correct table with the correct BOX/LEAF pricing.
    suspend fun clearCacheFor(userType: String) {
        clearMemoryCache(userType)
        when (userType) {
            "2"  -> productDao.deleteAll()         // wholesale
            "3"  -> specialProductDao.deleteAll()  // special
            else -> retailProductDao.deleteAll()   // guest + retail
        }
    }

    fun clearAllMemoryCache() {
        memoryCache.clear()
    }

    // ── Inline mappers — no external extension function dependency ─────────────

    private fun LocalProductItem.toDto() = ProductsDtoItem(
        PID_PRODUCT      = PID_PRODUCT,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF
    )

    private fun LocalSpecialProduct.toDto() = ProductsDtoItem(
        PID_PRODUCT      = PID_PRODUCT,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF
    )

    private fun LocalRetailProduct.toDto() = ProductsDtoItem(
        PID_PRODUCT      = PID_PRODUCT,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF
    )

    private fun ProductsDtoItem.toProductLocal() = LocalProductItem(
        PID_PRODUCT      = PID_PRODUCT ?: 0,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF,
        cachedAt         = System.currentTimeMillis(),
        userType         = MainActivity.userType.value
    )

    private fun ProductsDtoItem.toSpecialLocal() = LocalSpecialProduct(
        PID_PRODUCT      = PID_PRODUCT ?: 0,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF
    )

    private fun ProductsDtoItem.toRetailLocal() = LocalRetailProduct(
        PID_PRODUCT      = PID_PRODUCT ?: 0,
        PID_COMPANY      = PID_COMPANY,
        PID_CATEGORY     = PID_CATEGORY,
        PRODUCT_NAME     = PRODUCT_NAME,
        COMPANY_NAME     = COMPANY_NAME,
        CATEGORY_NAME    = CATEGORY_NAME,
        IMAGE_URL        = IMAGE_URL,
        SKU              = SKU,
        UNIT_NAME        = UNIT_NAME,
        SALES_TYPE       = SALES_TYPE,
        BOX_SIZE_TITLE   = BOX_SIZE_TITLE,
        NO_OF_PCS        = NO_OF_PCS,
        STRIP_QTY        = STRIP_QTY,
        BOX_SALES_PRICE  = BOX_SALES_PRICE,
        BOX_MRP_PRICE    = BOX_MRP_PRICE,
        BOX_SALES_PER    = BOX_SALES_PER,
        BOX_OFFER_VALUE  = BOX_OFFER_VALUE,
        STOCK_QTY_BOX    = STOCK_QTY_BOX,
        LEAF_SALES_PRICE = LEAF_SALES_PRICE,
        LEAF_MRP_PRICE   = LEAF_MRP_PRICE,
        LEAF_SALES_PER   = LEAF_SALES_PER,
        LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
        STOCK_QTY_LEAF   = STOCK_QTY_LEAF
    )
}