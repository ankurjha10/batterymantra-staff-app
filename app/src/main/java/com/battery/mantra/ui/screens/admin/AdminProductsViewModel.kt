package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.ProductResponse
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val products: List<ProductResponse> = emptyList(),
    val brands: List<BrandResponse> = emptyList(),
    val error: String? = null,
    val selectedBrandId: String? = null,    // UUID string of selected brand
    val selectedBrandName: String? = null,  // display name
    val currentPage: Int = 0,
    val hasMore: Boolean = true
)

class AdminProductsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        fetchBrands()
        fetchProducts(reset = true)
    }

    private fun fetchBrands() {
        viewModelScope.launch {
            val result = repository.getBrands()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(brands = result.getOrDefault(emptyList()))
            }
        }
    }



    fun onBrandSelected(brandId: String?, brandName: String?) {
        if (_uiState.value.selectedBrandId != brandId) {
            _uiState.value = _uiState.value.copy(
                selectedBrandId = brandId,
                selectedBrandName = brandName
            )
            fetchProducts(reset = true)
        }
    }

    fun loadMore() {
        if (!_uiState.value.isLoadingMore && _uiState.value.hasMore && !_uiState.value.isLoading) {
            fetchProducts(reset = false)
        }
    }

    private fun fetchProducts(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    currentPage = 0,
                    products = emptyList(),
                    hasMore = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            }

            val page = _uiState.value.currentPage
            val brandId = _uiState.value.selectedBrandId

            val result = repository.getProducts(
                page = page,
                size = 20,
                brandId = brandId,
                keyword = null
            )

            if (result.isSuccess) {
                val pageData = result.getOrNull()
                if (pageData != null) {
                    val currentProducts = if (reset) emptyList() else _uiState.value.products
                    val newProducts = currentProducts + pageData.content
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        products = newProducts,
                        currentPage = if (!pageData.last) pageData.number + 1 else pageData.number,
                        hasMore = !pageData.last
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: AdminRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminProductsViewModel(repository) as T
                }
            }
    }
}
