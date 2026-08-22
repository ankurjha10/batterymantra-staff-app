package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.UserResponse

@Composable
fun AdminOverviewTab(
    ordersState: AdminDataState<List<OrderResponse>>,
    partnersState: AdminDataState<List<PartnerResponse>>,
    engineersState: AdminDataState<List<EngineerResponse>>,
    usersState: AdminDataState<List<UserResponse>>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard Overview",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

            }
        }

        item {
            val totalOrders = if (ordersState is AdminDataState.Success) ordersState.data.size else 0
            val activeOrders = if (ordersState is AdminDataState.Success) {
                ordersState.data.count { it.orderStatus == "PENDING" || it.orderStatus == "ASSIGNED" }
            } else 0
            
            StatCard(title = "Total Orders", value = totalOrders.toString(), subtitle = "$activeOrders Active")
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val totalPartners = if (partnersState is AdminDataState.Success) partnersState.data.size else 0
                val activePartners = if (partnersState is AdminDataState.Success) {
                    partnersState.data.count { it.active == true || it.isActive == true }
                } else 0
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(title = "Partners", value = totalPartners.toString(), subtitle = "$activePartners Active")
                }

                val totalEngineers = if (engineersState is AdminDataState.Success) engineersState.data.size else 0
                val activeEngineers = if (engineersState is AdminDataState.Success) {
                    engineersState.data.count { it.active == true || it.isActive == true }
                } else 0
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(title = "Engineers", value = totalEngineers.toString(), subtitle = "$activeEngineers Active")
                }
            }
        }

        item {
            val totalUsers = if (usersState is AdminDataState.Success) usersState.data.size else 0
            StatCard(title = "Total Users", value = totalUsers.toString(), subtitle = "Registered Customers")
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color(0xFFD32F2F), fontSize = 12.sp)
        }
    }
}
