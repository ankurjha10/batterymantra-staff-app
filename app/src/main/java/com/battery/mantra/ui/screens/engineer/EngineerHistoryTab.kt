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
fun EngineerHistoryTab(
    historyJobs: List<EngineerTask>
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(historyJobs) { job ->
            EngineerTaskCard(
                orderId = job.id.take(8).uppercase(),
                customerName = job.customerName,
                address = job.address,
                status = job.status,
                price = job.price,
                isActive = false
            )
        }
    }
}
