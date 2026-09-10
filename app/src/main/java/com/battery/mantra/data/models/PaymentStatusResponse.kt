package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("message") val message: String?
)
