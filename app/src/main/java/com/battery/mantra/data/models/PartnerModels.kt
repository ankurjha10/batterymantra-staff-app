package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class OrderNoteRequest(
    @SerializedName("note") val note: String
)

data class PartnerInventoryItemResponse(
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double? = 0.0,
    @SerializedName("productStock") val productStock: Int = 0,
    @SerializedName("brandName") val brandName: String? = null,
    @SerializedName("capacity") val capacity: String? = null,
    @SerializedName("productCategory") val productCategory: String? = null,
    @SerializedName("productImage") val productImage: String? = null
)

data class CityProductStockItem(
    @SerializedName("productId") val productId: String,
    @SerializedName("stock") val stock: Int
)

data class PartnerBulkInventoryUpdateRequest(
    @SerializedName("cityId") val cityId: String,
    @SerializedName("updates") val updates: List<CityProductStockItem>
)

data class CityInventoryBulkUpdateResponse(
    @SerializedName("cityId") val cityId: String,
    @SerializedName("updatedCount") val updatedCount: Int,
    @SerializedName("message") val message: String? = null
)

data class CityPricingRequestDto(
    @SerializedName("cityId") val cityId: String,
    @SerializedName("price") val price: Double,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double? = 0.0,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("reason") val reason: String? = null
)

data class CityPricingRequestResponse(
    @SerializedName("requestId") val requestId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("cityId") val cityId: String,
    @SerializedName("cityName") val cityName: String? = null,
    @SerializedName("partnerId") val partnerId: String? = null,
    @SerializedName("partnerBusinessName") val partnerBusinessName: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double? = 0.0,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("status") val status: String = "PENDING_APPROVAL",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("reviewedBy") val reviewedBy: String? = null,
    @SerializedName("reviewedAt") val reviewedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class PartnerCityPricingUpdateDto(
    @SerializedName("cityId") val cityId: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double? = 0.0,
    @SerializedName("stock") val stock: Int
)

data class PartnerCreateProductRequest(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("brandId") val brandId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productImage") val productImage: String,
    @SerializedName("productStock") val productStock: Int,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("originalPrice") val originalPrice: Double? = null,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double? = null,
    @SerializedName("capacity") val capacity: String? = null,
    @SerializedName("productDescription") val productDescription: String? = null,
    @SerializedName("additionalImages") val additionalImages: List<String>? = null,
    @SerializedName("cityPrices") val cityPrices: List<CityPricingDto>? = null
)

