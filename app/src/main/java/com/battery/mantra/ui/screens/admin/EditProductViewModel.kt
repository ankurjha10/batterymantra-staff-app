package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.*
import com.battery.mantra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProductUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val product: ProductDetailResponse? = null,
    val brands: List<BrandResponse> = emptyList(),
    val categories: List<CategoryResponse> = emptyList(),

    // Editable form fields
    val productName: String = "",
    val productDescription: String = "",
    val sellingPrice: String = "",
    val originalPrice: String = "",
    val exchangeDiscount: String = "",
    val stockQuantity: String = "",
    val capacity: String = "",
    val selectedBrandId: String? = null,
    val selectedBrandName: String = "",
    val selectedCategoryId: String? = null,
    val selectedCategoryName: String = "",
    val isAutoAssignToPartner: Boolean = false,

    // SEO fields
    val seoSlug: String = "",
    val seoTitle: String = "",
    val seoKeywords: String = "",
    val seoDescription: String = "",
    val seoTitleCity: String = "",
    val seoKeywordsCity: String = "",
    val seoDescriptionCity: String = "",
    val ogTitle: String = "",
    val ogDescription: String = "",
    val ogTitleCity: String = "",
    val ogDescriptionCity: String = "",

    // UI state for unsaved changes tracking
    val hasUnsavedChanges: Boolean = false,
    val formattedOriginalDescription: String? = null
)

class EditProductViewModel(
    private val repository: AdminRepository,
    private val productId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load brands and categories in parallel
            val brandsResult = repository.getBrands()
            val categoriesResult = repository.getCategories()
            val productResult = repository.getProductById(productId)

            if (productResult.isSuccess) {
                val product = productResult.getOrNull()!!
                val brands = brandsResult.getOrDefault(emptyList())
                val categories = categoriesResult.getOrDefault(emptyList())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    product = product,
                    brands = brands,
                    categories = flattenCategories(categories),

                    // Populate form fields from product
                    productName = product.productName,
                    productDescription = product.productDescription ?: "",
                    formattedOriginalDescription = product.productDescription ?: "",
                    sellingPrice = product.productPrice.toLong().toString(),
                    originalPrice = (product.originalPrice?.toLong() ?: 0).toString(),
                    exchangeDiscount = (product.exchangeDiscount?.toLong() ?: 0).toString(),
                    stockQuantity = (product.productStock ?: 0).toString(),
                    capacity = product.capacity ?: "",
                    selectedBrandId = product.brandId?.toString(),
                    selectedBrandName = product.brandName,
                    selectedCategoryId = product.categoryId?.toString(),
                    selectedCategoryName = product.categoryName ?: "",
                    isAutoAssignToPartner = product.isAutoAssignToPartner,

                    // SEO fields
                    seoSlug = product.seo?.slug ?: "",
                    seoTitle = product.seo?.metaTitle ?: "",
                    seoKeywords = product.seo?.metaKeywords ?: "",
                    seoDescription = product.seo?.metaDescription ?: "",
                    seoTitleCity = product.seo?.metaTitleCity ?: "",
                    seoKeywordsCity = product.seo?.metaKeywordsCity ?: "",
                    seoDescriptionCity = product.seo?.metaDescriptionCity ?: "",
                    ogTitle = product.seo?.ogTitle ?: "",
                    ogDescription = product.seo?.ogDescription ?: "",
                    ogTitleCity = product.seo?.ogTitleCity ?: "",
                    ogDescriptionCity = product.seo?.ogDescriptionCity ?: ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = productResult.exceptionOrNull()?.message ?: "Failed to load product"
                )
            }
        }
    }

    /**
     * Flatten nested categories into a single list for dropdown
     */
    private fun flattenCategories(categories: List<CategoryResponse>): List<CategoryResponse> {
        val result = mutableListOf<CategoryResponse>()
        for (cat in categories) {
            result.add(cat)
            cat.subCategories?.let { result.addAll(flattenCategories(it)) }
        }
        return result
    }

    // --- Form field update methods ---
    private fun updateState(updater: (EditProductUiState) -> EditProductUiState) {
        val newState = updater(_uiState.value)
        _uiState.value = newState.copy(hasUnsavedChanges = calculateHasUnsavedChanges(newState))
    }

    private fun calculateHasUnsavedChanges(state: EditProductUiState): Boolean {
        val p = state.product ?: return false
        return state.productName != p.productName ||
                state.productDescription != (state.formattedOriginalDescription ?: "") ||
                state.sellingPrice != p.productPrice.toLong().toString() ||
                state.originalPrice != (p.originalPrice?.toLong() ?: 0).toString() ||
                state.exchangeDiscount != (p.exchangeDiscount?.toLong() ?: 0).toString() ||
                state.stockQuantity != (p.productStock ?: 0).toString() ||
                state.capacity != (p.capacity ?: "") ||
                state.selectedBrandId != p.brandId?.toString() ||
                state.selectedCategoryId != p.categoryId?.toString() ||
                state.isAutoAssignToPartner != p.isAutoAssignToPartner ||
                state.seoSlug != (p.seo?.slug ?: "") ||
                state.seoTitle != (p.seo?.metaTitle ?: "") ||
                state.seoKeywords != (p.seo?.metaKeywords ?: "") ||
                state.seoDescription != (p.seo?.metaDescription ?: "") ||
                state.seoTitleCity != (p.seo?.metaTitleCity ?: "") ||
                state.seoKeywordsCity != (p.seo?.metaKeywordsCity ?: "") ||
                state.seoDescriptionCity != (p.seo?.metaDescriptionCity ?: "") ||
                state.ogTitle != (p.seo?.ogTitle ?: "") ||
                state.ogDescription != (p.seo?.ogDescription ?: "") ||
                state.ogTitleCity != (p.seo?.ogTitleCity ?: "") ||
                state.ogDescriptionCity != (p.seo?.ogDescriptionCity ?: "")
    }

    fun onInitialHtmlFormatted(formattedHtml: String) {
        _uiState.value = _uiState.value.copy(
            formattedOriginalDescription = formattedHtml,
            productDescription = formattedHtml
        )
        // Recalculate unsaved changes just in case
        _uiState.value = _uiState.value.copy(hasUnsavedChanges = calculateHasUnsavedChanges(_uiState.value))
    }

    // --- Form field update methods ---
    fun onProductNameChange(value: String) = updateState { it.copy(productName = value) }
    fun onDescriptionChange(value: String) = updateState { it.copy(productDescription = value) }
    fun onSellingPriceChange(value: String) = updateState { it.copy(sellingPrice = value) }
    fun onOriginalPriceChange(value: String) = updateState { it.copy(originalPrice = value) }
    fun onExchangeDiscountChange(value: String) = updateState { it.copy(exchangeDiscount = value) }
    fun onStockQuantityChange(value: String) = updateState { it.copy(stockQuantity = value) }
    fun onCapacityChange(value: String) = updateState { it.copy(capacity = value) }
    fun onBrandSelected(brandId: String, brandName: String) = updateState { it.copy(selectedBrandId = brandId, selectedBrandName = brandName) }
    fun onCategorySelected(categoryId: String, categoryName: String) = updateState { it.copy(selectedCategoryId = categoryId, selectedCategoryName = categoryName) }
    fun onAutoAssignChanged(value: Boolean) = updateState { it.copy(isAutoAssignToPartner = value) }

    // SEO field setters
    fun onSeoSlugChange(value: String) = updateState { it.copy(seoSlug = value) }
    fun onSeoTitleChange(value: String) = updateState { it.copy(seoTitle = value) }
    fun onSeoKeywordsChange(value: String) = updateState { it.copy(seoKeywords = value) }
    fun onSeoDescriptionChange(value: String) = updateState { it.copy(seoDescription = value) }
    fun onSeoTitleCityChange(value: String) = updateState { it.copy(seoTitleCity = value) }
    fun onSeoKeywordsCityChange(value: String) = updateState { it.copy(seoKeywordsCity = value) }
    fun onSeoDescriptionCityChange(value: String) = updateState { it.copy(seoDescriptionCity = value) }
    fun onOgTitleChange(value: String) = updateState { it.copy(ogTitle = value) }
    fun onOgDescriptionChange(value: String) = updateState { it.copy(ogDescription = value) }
    fun onOgTitleCityChange(value: String) = updateState { it.copy(ogTitleCity = value) }
    fun onOgDescriptionCityChange(value: String) = updateState { it.copy(ogDescriptionCity = value) }

    fun saveProduct() {
        val state = _uiState.value
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null, saveSuccess = false)

            val seo = SeoMetadata(
                slug = state.seoSlug.ifBlank { null },
                metaTitle = state.seoTitle.ifBlank { null },
                metaDescription = state.seoDescription.ifBlank { null },
                metaKeywords = state.seoKeywords.ifBlank { null },
                metaTitleCity = state.seoTitleCity.ifBlank { null },
                metaDescriptionCity = state.seoDescriptionCity.ifBlank { null },
                metaKeywordsCity = state.seoKeywordsCity.ifBlank { null },
                ogTitle = state.ogTitle.ifBlank { null },
                ogDescription = state.ogDescription.ifBlank { null },
                ogTitleCity = state.ogTitleCity.ifBlank { null },
                ogDescriptionCity = state.ogDescriptionCity.ifBlank { null }
            )

            val request = UpdateProductRequest(
                productName = state.productName,
                productDescription = state.productDescription.ifBlank { null },
                productPrice = state.sellingPrice.toDoubleOrNull(),
                originalPrice = state.originalPrice.toDoubleOrNull(),
                exchangeDiscount = state.exchangeDiscount.toDoubleOrNull(),
                categoryId = state.selectedCategoryId,
                brandId = state.selectedBrandId,
                productStock = state.stockQuantity.toIntOrNull(),
                capacity = state.capacity.ifBlank { null },
                seo = seo,
                isAutoAssignToPartner = state.isAutoAssignToPartner
            )

            val result = repository.updateProduct(productId, request)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    product = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save product"
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    companion object {
        fun provideFactory(repository: AdminRepository, productId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditProductViewModel(repository, productId) as T
                }
            }
    }
}
