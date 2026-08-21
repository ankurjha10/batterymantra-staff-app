package com.battery.mantra.data.models

import java.util.UUID

data class BrandResponse(
    val brandId: UUID,
    val brandName: String,
    val brandLogo: String?,
    val featured: Boolean,
    val description: String?,
    val productCount: Int
)
