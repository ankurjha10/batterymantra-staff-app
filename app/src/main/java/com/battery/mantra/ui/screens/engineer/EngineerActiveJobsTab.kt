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
fun EngineerActiveJobsTab(
    activeJobs: List<OrderResponse>,
    onNavigateToJobExecution: (String) -> Unit,
    onCallClick: (String, String) -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activeJobs) { job ->
            EngineerTaskCard(
                orderId = job.orderId.take(8).uppercase(),
                customerName = job.customerName ?: "Unknown",
                address = job.shippingAddress ?: "No address",
                price = "₹${job.totalAmount ?: 0.0}",
                status = job.orderStatus ?: "UNKNOWN",
                actionText = "Start Dispatch",
                onActionClick = { onNavigateToJobExecution(job.orderId) },
                onCallClick = { onCallClick(job.orderId, job.customerPhone ?: "") }
            )
        }
    }
}
