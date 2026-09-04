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

    if (activeJobs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.Text("No active tasks found", color = androidx.compose.ui.graphics.Color.Gray)
        }
    } else {
        val context = androidx.compose.ui.platform.LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeJobs) { job ->
                val status = job.orderStatus?.uppercase() ?: "UNKNOWN"
                val isCompleted = listOf("COMPLETED", "DELIVERED", "INSTALLED", "CANCELLED", "FAILED").contains(status)
                
                EngineerTaskCard(
                    orderId = job.orderId.take(8).uppercase(),
                    customerName = job.customerName ?: "Unknown",
                    address = job.shippingAddress ?: "No address",
                    price = "₹${job.totalAmount ?: 0.0}",
                    status = status,
                    actionText = if (isCompleted) "" else "Execute Job",
                    onActionClick = { onNavigateToJobExecution(job.orderId) },
                    onCallClick = { onCallClick(job.orderId, job.customerPhone ?: "") },
                    onNavigateClick = {
                        val uri = if (job.latitude != null && job.longitude != null) {
                            android.net.Uri.parse("google.navigation:q=${job.latitude},${job.longitude}")
                        } else {
                            val addressEncoded = android.net.Uri.encode(job.shippingAddress ?: "")
                            android.net.Uri.parse("google.navigation:q=$addressEncoded")
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            // Fallback if Google Maps is not installed
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                        }
                    }
                )
            }
        }
    }
}
