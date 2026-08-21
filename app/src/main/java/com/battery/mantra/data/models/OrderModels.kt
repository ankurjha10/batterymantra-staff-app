package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("shippingAddress") val shippingAddress: String?,
    @SerializedName("placedAt") val placedAt: String?,
    @SerializedName("totalAmount") val totalAmount: Double?,
    @SerializedName("deliveryMethod") val deliveryMethod: String?,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("installationDate") val installationDate: String?,
    @SerializedName("exchangeDiscount") val exchangeDiscount: Double?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerEmail") val customerEmail: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("deliverySecurityCode") val deliverySecurityCode: String?,
    @SerializedName("orderItems") val orderItems: List<OrderItemResponse>?,
    @SerializedName("assignedPartner") val assignedPartner: PartnerResponse?
)

data class OrderItemResponse(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("exchangeOldBattery") val exchangeOldBattery: Boolean
)
