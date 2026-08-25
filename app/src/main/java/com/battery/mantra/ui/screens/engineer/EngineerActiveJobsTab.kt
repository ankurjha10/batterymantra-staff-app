package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.battery.mantra.data.repository.EngineerTask
import com.battery.mantra.ui.components.EngineerTaskCard

@Composable
fun EngineerActiveJobsTab(
    activeJobs: List<EngineerTask>,
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
                orderId = job.id.take(8).uppercase(),
                customerName = job.customerName,
                address = job.address,
                price = job.price,
                status = job.status,
                actionText = "Start Dispatch",
                onActionClick = { onNavigateToJobExecution(job.id) },
                onCallClick = { onCallClick(job.id, job.customerPhone) }
            )
        }
    }
}
