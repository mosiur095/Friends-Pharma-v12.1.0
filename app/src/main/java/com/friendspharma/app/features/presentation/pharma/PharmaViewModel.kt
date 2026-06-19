package com.friendspharma.app.features.presentation.pharma

import android.os.Build

import androidx.compose.ui.focus.FocusManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.GetAllCompanyUseCase
import com.friendspharma.app.features.domain.use_case.GetCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PharmaViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val getCartInfoUseCase: GetCartInfoUseCase,
    private val getAllCompanyUseCase: GetAllCompanyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PharmaState())
    val state: StateFlow<PharmaState> = _state.asStateFlow()

    // ✅ FIX: dedicated search job — cancels previous filter on each keystroke
    private var searchJob: Job? = null

    init {
        getCartInfo()
        getAllCompanies()
    }

    private fun getAllCompanies() {
        getAllCompanyUseCase.invoke().onEach { result ->
            when (result) {
                is Async.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading            = false,
                            companies            = result.data ?: AllCompanyDto(),
                            allSearchedCompanies = result.data ?: AllCompanyDto()
                        )
                    }
                }
                is Async.Error<*>   -> {}
                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)
    }

    // ✅ FIX: search text updates SYNCHRONOUSLY outside the coroutine.
    // Only the filtering work runs inside the coroutine.
    fun searchChanged(text: String, focusManager: FocusManager) {
        _state.update { it.copy(search = text) }

        searchJob?.cancel()

        if (text.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                val list: List<AllCompanyDtoItem> = state.value.companies.data?.filter {
                    it.COMPANY_NAME?.lowercase()?.contains(text.lowercase()) == true
                } ?: emptyList()
                _state.update {
                    it.copy(allSearchedCompanies = state.value.allSearchedCompanies.copy(data = list))
                }
            }
        } else {
            focusManager.clearFocus()
            _state.update { it.copy(allSearchedCompanies = state.value.companies) }
        }
    }

    private fun getCartInfo() {
        getCartInfoUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        var quantity = 0
                        val set = HashSet<Int>()
                        for (item in result.data?.data ?: emptyList()) {
                            quantity += item.QUANTITY?.toInt() ?: 0
                            set.add(item.PID_PRODUCT ?: -1)
                        }
                        _state.update { it.copy(cartItemQuantity = quantity) }
                    }
                    is Async.Error   -> {}
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }
}