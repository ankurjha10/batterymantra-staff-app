package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ProductResponse(
    @SerializedName("productId") val id: UUID,
    @SerializedName("productName") val name: String,
    @SerializedName("brandName") val brand: String,
    @SerializedName("productPrice") val price: Double,
    @SerializedName("originalPrice") val originalPrice: Double?,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double?,
    @SerializedName("productImage") val imageUrl: String?,
    @SerializedName("additionalImages") val additionalImages: List<String>?,
    @SerializedName("productCategory") val category: String?,
    @SerializedName("capacity") val capacity: String?,
    @SerializedName("isApproved") val isApproved: Boolean = true,
    @SerializedName("isAutoAssignToPartner") val isAutoAssignToPartner: Boolean = false,
    @SerializedName("createdByPartnerId") val createdByPartnerId: UUID? = null,
    @SerializedName("partnerBusinessName") val partnerBusinessName: String? = null
)

/**
 * Maps exactly to Spring Data's Page<T> JSON response.
 * Spring serializes Page as: content, totalElements, totalPages, size, number, first, last, empty, etc.
 */
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,       // current page number (0-based)
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)
