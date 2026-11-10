package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.ui.components.StatusBadge
import com.battery.mantra.ui.theme.BrandRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerOverviewTab(
    profileState: PartnerDataState<PartnerResponse>,
    ordersState: PartnerDataState<List<OrderResponse>>,
    engineersState: PartnerDataState<List<EngineerResponse>>,
    onManageOrdersClick: () -> Unit,
    onFilterOrdersClick: (String) -> Unit,
    onManageEngineersClick: () -> Unit,
    onOrderClick: (OrderResponse) -> Unit,
    onRetryOrders: () -> Unit
) {
    val profile = (profileState as? PartnerDataState.Success)?.data
    val orders = (ordersState as? PartnerDataState.Success)?.data.orEmpty()
    val engineers = (engineersState as? PartnerDataState.Success)?.data.orEmpty()

    val totalAssigned = orders.size
    val actionNeededCount = orders.count { it.isActionNeeded() }
    val completedCount = orders.count { it.isCompletedOrder() }
    val activeEngineersCount = engineers.count { it.isActive == true || it.active == true }
    val onDutyEngineersCount = engineers.count { it.dutyStatus.equals("ON_DUTY", ignoreCase = true) }
    val recentOrders = orders.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section matching Website Overview
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Partner Overview",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Monitor performance, assigned orders, and key metrics.",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onManageOrdersClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("All Orders", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                val cities = profile?.operatingCities.orEmpty()
                if (cities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cities.forEach { city ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.LocationCity,
                                        contentDescription = null,
                                        tint = BrandRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = city.cityName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3 KPI Metric Cards (Total Assigned, Action Needed, Completed)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewMetricCard(
                    title = "TOTAL ASSIGNED",
                    value = totalAssigned.toString(),
                    subtitle = "All time assigned orders",
                    accentColor = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF),
                    icon = Icons.Outlined.Inventory2,
                    onClick = { onFilterOrdersClick("All") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OverviewMetricCard(
                        title = "ACTION NEEDED",
                        value = actionNeededCount.toString(),
                        subtitle = "Pending / Processing",
                        accentColor = Color(0xFFEA580C),
                        iconBg = Color(0xFFFFF7ED),
                        icon = Icons.Outlined.Schedule,
                        modifier = Modifier.weight(1f),
                        onClick = { onFilterOrdersClick("Action Needed") }
                    )
                    OverviewMetricCard(
                        title = "COMPLETED",
                        value = completedCount.toString(),
                        subtitle = "Delivered & Installed",
                        accentColor = Color(0xFF16A34A),
                        iconBg = Color(0xFFF0FDF4),
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.weight(1f),
                        onClick = { onFilterOrdersClick("Completed") }
                    )
                }
            }
        }

        // 2 Quick Action Cards (Assigned Orders & Technicians & Engineers)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickManageCard(
                    title = "Assigned Orders",
                    description = "View and manage orders assigned to your branch.",
                    buttonText = "Manage Orders",
                    icon = Icons.Outlined.ShoppingCart,
                    iconTint = BrandRed,
                    onClick = onManageOrdersClick,
                    modifier = Modifier.weight(1f)
                )
                QuickManageCard(
                    title = "Technicians & Engineers",
                    description = "$activeEngineersCount active • $onDutyEngineersCount on duty",
                    buttonText = "Manage Engineers",
                    icon = Icons.Outlined.Engineering,
                    iconTint = BrandRed,
                    onClick = onManageEngineersClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Orders Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Latest orders assigned to your branch",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                TextButton(onClick = onManageOrdersClick) {
                    Text("View All →", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        when (ordersState) {
            is PartnerDataState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandRed)
                    }
                }
            }
            is PartnerDataState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(ordersState.message, color = Color(0xFFB91C1C), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onRetryOrders) {
                                Text("Retry", color = BrandRed)
                            }
                        }
                    }
                }
            }
            is PartnerDataState.Success -> {
                if (recentOrders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No orders assigned to your branch yet.",
                                    color = Color(0xFF6B7280),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(recentOrders, key = { it.orderId }) { order ->
                        RecentOrderRowCard(order = order, onClick = { onOrderClick(order) })
                    }
                }
            }
            else -> {}
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    iconBg: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(92.dp)
                    .background(accentColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (title == "TOTAL ASSIGNED") Color(0xFF111827) else accentColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickManageCard(
    title: String,
    description: String,
    buttonText: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                lineHeight = 15.sp,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RecentOrderRowCard(
    order: OrderResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId.take(8)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatOrderDate(order.placedAt),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                StatusBadge(status = order.orderStatus ?: "PENDING")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName ?: "Customer",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!order.shippingAddress.isNullOrBlank()) {
                        Text(
                            text = order.shippingAddress,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    order.assignedEngineer?.let { eng ->
                        val engName = "${eng.firstName ?: ""} ${eng.lastName ?: ""}".trim()
                        if (engName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Eng: $engName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formatInr(order.totalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

