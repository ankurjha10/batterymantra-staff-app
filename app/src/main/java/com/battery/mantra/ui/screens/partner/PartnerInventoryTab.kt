package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.battery.mantra.ui.theme.TextGray

@Composable
fun PartnerInventoryTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Inventory and City Pricing", color = TextGray)
    }
}
