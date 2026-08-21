package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Full product detail response from GET /api/products/id/{id}
 */
data class ProductDetailResponse(
    @SerializedName("productId") val productId: UUID,
    @SerializedName("productName") val productName: String,
    @SerializedName("productDescription") val productDescription: String?,
    @SerializedName("brandName") val brandName: String,
    @SerializedName("brandId") val brandId: UUID?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("categoryId") val categoryId: UUID?,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("originalPrice") val originalPrice: Double?,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double?,
    @SerializedName("productStock") val productStock: Int?,
    @SerializedName("productImage") val productImage: String?,
    @SerializedName("additionalImages") val additionalImages: List<String>?,
    @SerializedName("specs") val specs: Map<String, Any>?,
    @SerializedName("specDetails") val specDetails: List<Map<String, String>>?,
    @SerializedName("highlightedSpecAttributeIds") val highlightedSpecAttributeIds: List<UUID>?,
    @SerializedName("specAttributeIcons") val specAttributeIcons: Map<String, String>?,
    @SerializedName("capacity") val capacity: String?,
    @SerializedName("cityPrices") val cityPrices: List<CityPricingDto>?,
    @SerializedName("createdByPartnerId") val createdByPartnerId: UUID?,
    @SerializedName("partnerBusinessName") val partnerBusinessName: String?,
    @SerializedName("seo") val seo: SeoMetadata?,
    @SerializedName("isAutoAssignToPartner") val isAutoAssignToPartner: Boolean = false,
    @SerializedName("isApproved") val isApproved: Boolean = true
)

data class SeoMetadata(
    val slug: String? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null,
    val metaTitleCity: String? = null,
    val metaDescriptionCity: String? = null,
    val metaKeywordsCity: String? = null,
    val ogTitle: String? = null,
    val ogDescription: String? = null,
    val ogTitleCity: String? = null,
    val ogDescriptionCity: String? = null,
    val canonicalUrl: String? = null
)

data class CityPricingDto(
    val cityId: UUID?,
    val price: Double?,
    val exchangeDiscount: Double?,
    val stock: Int?
)

/**
 * PATCH /api/admin/products/id/{id} request body
 */
data class UpdateProductRequest(
    val productName: String? = null,
    val productDescription: String? = null,
    val productPrice: Double? = null,
    val originalPrice: Double? = null,
    val exchangeDiscount: Double? = null,
    val categoryId: String? = null,
    val brandId: String? = null,
    val productImage: String? = null,
    val additionalImages: List<String>? = null,
    val productStock: Int? = null,
    val capacity: String? = null,
    val seo: SeoMetadata? = null,
    val isAutoAssignToPartner: Boolean? = null
)

data class CategoryResponse(
    val categoryId: UUID,
    val categoryName: String,
    val categoryDescription: String?,
    val iconUrl: String?,
    val displayOrder: Int?,
    val parentId: UUID?,
    val subCategories: List<CategoryResponse>?
)
