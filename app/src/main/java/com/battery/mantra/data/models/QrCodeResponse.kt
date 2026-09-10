package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class QrCodeResponse(
    @SerializedName("qrCodeId") val qrCodeId: String,
    @SerializedName("imageUrl") val imageUrl: String
)
