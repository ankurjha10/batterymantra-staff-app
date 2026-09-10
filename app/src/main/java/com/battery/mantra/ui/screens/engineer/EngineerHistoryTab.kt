package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.ui.components.EngineerTaskCard

@Composable
fun EngineerHistoryTab(
    historyJobs: List<OrderResponse>
) {
    var selectedOrderForDetails by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<OrderResponse?>(null) }

    if (historyJobs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.Text("No past tasks found", color = androidx.compose.ui.graphics.Color.Gray)
        }
    } else {
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
                    paymentMethod = job.paymentMethod,
                    paymentStatus = job.paymentStatus,
                    isActive = false,
                    onClick = {
                        selectedOrderForDetails = job
                    }
                )
            }
        }
        
        if (selectedOrderForDetails != null) {
            com.battery.mantra.ui.components.SharedOrderDetailsSheet(
                order = selectedOrderForDetails!!,
                isAdmin = false,
                onDismiss = { selectedOrderForDetails = null }
            )
        }
    }
}
