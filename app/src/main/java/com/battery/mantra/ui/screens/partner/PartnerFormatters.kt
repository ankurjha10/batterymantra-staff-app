package com.battery.mantra.ui.screens.partner

import com.battery.mantra.data.models.OrderResponse
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal fun formatInr(amount: Double?): String {
    val value = amount ?: 0.0
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 2
        minimumFractionDigits = 0
    }
    return "₹${formatter.format(value)}"
}

internal fun formatOrderDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "N/A"
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault())
            val parsed = parser.parse(raw)
            if (parsed != null) {
                return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed)
            }
        } catch (_: Exception) {
            // Try next pattern
        }
    }
    return raw.take(10)
}

internal fun OrderResponse.isActionNeeded(): Boolean =
    orderStatus?.uppercase() in setOf("PENDING", "CONFIRMED", "PROCESSING", "ASSIGNED", "ACCEPTED")

internal fun OrderResponse.isDispatched(): Boolean =
    orderStatus?.uppercase() in setOf("SHIPPED", "OUT_FOR_DELIVERY", "DISPATCHED")

internal fun OrderResponse.isCompletedOrder(): Boolean =
    orderStatus?.uppercase() in setOf("DELIVERED", "INSTALLED", "COMPLETED")

internal fun OrderResponse.isTerminalOrder(): Boolean =
    orderStatus?.uppercase() in setOf("DELIVERED", "INSTALLED", "COMPLETED", "CANCELLED")

