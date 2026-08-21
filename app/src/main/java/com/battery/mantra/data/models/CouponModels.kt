package com.battery.mantra.data.models

data class CouponResponse(
    val couponId: String,
    val code: String,
    val discountType: String, // "PERCENTAGE" or "FLAT"
    val discountValue: Double,
    val maxDiscountAmount: Double?,
    val minOrderValue: Double?,
    val startDate: String?,
    val expiryDate: String?,
    val usageLimit: Int?,
    val usedCount: Int?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class CouponRequest(
    val code: String,
    val discountType: String,
    val discountValue: Double,
    val maxDiscountAmount: Double?,
    val minOrderValue: Double?,
    val startDate: String?,
    val expiryDate: String?,
    val usageLimit: Int?,
    val isActive: Boolean
)
