package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.battery.mantra.ui.components.OrderCard

@Composable
fun PartnerOrdersTab() {
    val orders = listOf(
        mapOf("id" to "1001", "name" to "Rahul Sharma", "status" to "PENDING", "car" to "Maruti Swift", "battery" to "Amaron Pro 45Ah", "address" to "123, MG Road, Pune", "price" to "₹4,500"),
        mapOf("id" to "1005", "name" to "Anita Desai", "status" to "DISPATCHED", "car" to "Honda City", "battery" to "Exide 40Ah", "address" to "Kalyani Nagar, Pune", "price" to "₹4,200")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            OrderCard(
                orderId = order["id"] ?: "",
                itemsCount = 1,
                price = order["price"] ?: "",
                customerName = order["name"] ?: "",
                customerPhone = "9876543210",
                customerEmail = "test@example.com",
                paymentMethod = "COD",
                deliveryMethod = "Standard Delivery",
                address = order["address"] ?: "",
                placedAt = "2026-07-10T12:00:00",
                status = order["status"] ?: "",
                partnerName = "Battery Wala",
                onActionClick = { /* Show AssignEngineerSheet */ }
            )
        }
    }
}
