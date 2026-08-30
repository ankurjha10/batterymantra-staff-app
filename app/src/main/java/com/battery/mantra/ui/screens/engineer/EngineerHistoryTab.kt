package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.ui.components.EngineerTaskCard

@Composable
fun EngineerHistoryTab(
    historyJobs: List<OrderResponse>
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(historyJobs) { job ->
            EngineerTaskCard(
                orderId = job.orderId.take(8).uppercase(),
                customerName = job.customerName ?: "Unknown",
                address = job.shippingAddress ?: "No address",
                status = job.orderStatus ?: "UNKNOWN",
                price = "₹${job.totalAmount ?: 0.0}",
                isActive = false
            )
        }
    }
}
