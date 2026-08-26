package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.UserResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandRed = Color(0xFFDE2027)

@Composable
fun AdminOverviewTab(
    ordersState: AdminDataState<List<OrderResponse>>,
    partnersState: AdminDataState<List<PartnerResponse>>,
    engineersState: AdminDataState<List<EngineerResponse>>,
    usersState: AdminDataState<List<UserResponse>>,
    onCreateOrderClick: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val totalOrders = if (ordersState is AdminDataState.Success) ordersState.data.size else 0
    val activeOrders = if (ordersState is AdminDataState.Success) {
        ordersState.data.count { it.orderStatus == "PENDING" || it.orderStatus == "ASSIGNED" || it.orderStatus == "CONFIRMED" }
    } else 0
    val todayOrders = if (ordersState is AdminDataState.Success) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        ordersState.data.count { it.placedAt?.startsWith(todayStr) == true }
    } else 0
    val totalPartners = if (partnersState is AdminDataState.Success) partnersState.data.size else 0
    val activePartners = if (partnersState is AdminDataState.Success) {
        partnersState.data.count { it.active == true || it.isActive == true }
    } else 0
    val totalEngineers = if (engineersState is AdminDataState.Success) engineersState.data.size else 0
    val activeEngineers = if (engineersState is AdminDataState.Success) {
        engineersState.data.count { it.active == true || it.isActive == true }
    } else 0
    val totalUsers = if (usersState is AdminDataState.Success) usersState.data.size else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Dashboard Overview",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        // Quick Actions Section
        item {
            Text(
                text = "Quick Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Outlined.AddShoppingCart,
                    title = "Create Order",
                    subtitle = "Manual entry",
                    gradientColors = listOf(Color(0xFFDE2027), Color(0xFFE74C3C)),
                    onClick = onCreateOrderClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Outlined.Inventory2,
                    title = "Products",
                    subtitle = "View catalog",
                    gradientColors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
                    onClick = { onNavigateToTab(1) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Outlined.Engineering,
                    title = "Engineers",
                    subtitle = "Manage team",
                    gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
                    onClick = { onNavigateToTab(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stats Cards
        item {
            Text(
                text = "Statistics",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "Total Orders",
                        value = totalOrders.toString(),
                        subtitle = "$activeOrders Active • $todayOrders Today",
                        accentColor = BrandRed
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "Customers",
                        value = totalUsers.toString(),
                        subtitle = "Registered",
                        accentColor = Color(0xFF7C3AED)
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "Partners",
                        value = totalPartners.toString(),
                        subtitle = "$activePartners Active",
                        accentColor = Color(0xFFEA580C)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "Engineers",
                        value = totalEngineers.toString(),
                        subtitle = "$activeEngineers Active",
                        accentColor = Color(0xFF059669)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, accentColor: Color = BrandRed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color(0xFF6B7280), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color(0xFF111827), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
