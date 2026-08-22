package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.components.OrderCard
import com.battery.mantra.data.models.OrderResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersTab(
    ordersState: AdminDataState<List<OrderResponse>>,
    partnersState: AdminDataState<List<com.battery.mantra.data.models.PartnerResponse>>,
    engineersState: AdminDataState<List<com.battery.mantra.data.models.EngineerResponse>>,
    targetSearchQuery: String? = null,
    targetFilter: String? = null,
    onTargetConsumed: () -> Unit = {},
    onAssignPartner: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onAssignEngineer: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateStatus: (String, String) -> Unit = { _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("All Orders") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderForDetails by remember { mutableStateOf<OrderResponse?>(null) }
    var selectedOrderIdToAssign by remember { mutableStateOf<String?>(null) }
    
    // Consume deep link arguments
    LaunchedEffect(targetSearchQuery, targetFilter, ordersState) {
        var consumed = false
        
        if (targetFilter != null) {
            selectedFilter = targetFilter
            if (targetSearchQuery == null) consumed = true
        }
        
        if (targetSearchQuery != null) {
            searchQuery = targetSearchQuery
            if (ordersState is AdminDataState.Success) {
                val foundOrder = ordersState.data.find { it.orderId.contains(targetSearchQuery, ignoreCase = true) }
                if (foundOrder != null) {
                    selectedOrderForDetails = foundOrder
                }
                consumed = true
            }
        }
        
        if (consumed) {
            onTargetConsumed()
        }
    }
    
    val filters = listOf(
        "All Orders", "Main Branch (Admin Direct)", "Partner Assigned", 
        "New Orders", "Ready For Dispatch", "Dispatched", "Delivered", "Cancelled"
    )
    
    val allOrders = remember(ordersState) {
        if (ordersState is AdminDataState.Success) {
            ordersState.data.sortedWith(
                compareByDescending<OrderResponse> { it.orderStatus == "PENDING" }
                    .thenByDescending { it.placedAt ?: "" }
            )
        } else {
            emptyList()
        }
    }
    
    // Dynamic Stats Calculation
    val pendingCount = remember(allOrders) { 
        allOrders.count { it.orderStatus == "PENDING" || it.orderStatus == "CONFIRMED" || it.orderStatus == "UNASSIGNED" }
    }
    
    val todayOrdersCount = remember(allOrders) {
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        allOrders.count { it.placedAt?.startsWith(todayDateString) == true }
    }

    val filteredOrders = remember(allOrders, searchQuery, selectedFilter) {
        allOrders.filter { order ->
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                (order.orderId.contains(searchQuery, true)) || 
                (order.customerName?.contains(searchQuery, true) == true) ||
                (order.shippingAddress?.contains(searchQuery, true) == true)
            } else true
            
            val matchesFilter = when (selectedFilter) {
                "All Orders" -> true
                "Main Branch (Admin Direct)" -> order.assignedPartner == null
                "Partner Assigned" -> order.assignedPartner != null
                "New Orders" -> order.orderStatus == "PENDING" || order.orderStatus == "CONFIRMED" || order.orderStatus == "UNASSIGNED"
                "Ready For Dispatch" -> order.orderStatus == "PROCESSING" || order.orderStatus == "ASSIGNED"
                "Dispatched" -> order.orderStatus == "SHIPPED" || order.orderStatus == "OUT_FOR_DELIVERY" || order.orderStatus == "DISPATCHED"
                "Delivered" -> order.orderStatus == "DELIVERED" || order.orderStatus == "COMPLETED" || order.orderStatus == "INSTALLED"
                "Cancelled" -> order.orderStatus == "CANCELLED"
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Search and Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Order ID or City...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
        
        // Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    
                    // Display count in tab for first 3
                    val countStr = when (filter) {
                        "All Orders" -> " (${allOrders.size})"
                        "Main Branch (Admin Direct)" -> " (${allOrders.count { it.assignedPartner == null }})"
                        "Partner Assigned" -> " (${allOrders.count { it.assignedPartner != null }})"
                        else -> ""
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter + countStr, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.1f),
                            selectedLabelColor = Color(0xFFD32F2F)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFFD32F2F).copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
            }
        }
        
        // Metrics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending
                Card(
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pending", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pendingCount.toString(), color = Color(0xFF991B1B), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                // Today Orders
                Card(
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(todayOrdersCount.toString(), color = Color(0xFF1E3A8A), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        
        // Orders List Header
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Order Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Review, process and update customer orders.", fontSize = 13.sp, color = Color.DarkGray)
            }
        }
        
        // Orders List
        if (ordersState is AdminDataState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            }
        } else if (filteredOrders.isEmpty()) {
            item {
                Text(
                    "No orders found matching criteria", 
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(filteredOrders, key = { it.orderId }) { order ->
                OrderCard(
                    orderId = order.orderId.take(8).lowercase(),
                    itemsCount = order.orderItems?.size ?: 1,
                    price = order.totalAmount?.let { "₹$it" } ?: "₹0.0",
                    customerName = order.customerName ?: "Unknown Customer",
                    customerPhone = order.customerPhone ?: "No Phone",
                    customerEmail = order.customerEmail ?: "No Email",
                    paymentMethod = order.paymentMethod ?: "COD",
                    deliveryMethod = order.deliveryMethod ?: "Standard Delivery",
                    address = order.shippingAddress ?: "India",
                    placedAt = order.placedAt ?: "2026-07-10T12:00:00",
                    status = order.orderStatus ?: "UNASSIGNED",
                    partnerName = order.assignedPartner?.businessName ?: order.assignedPartner?.contactPerson,
                    engineerName = order.assignedEngineer?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }?.takeIf { it.isNotEmpty() },
                    onActionClick = { selectedOrderForDetails = order }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (selectedOrderForDetails != null) {
        AdminOrderDetailsSheet(
            order = selectedOrderForDetails!!,
            onDismiss = { selectedOrderForDetails = null },
            onAssignEngineer = { orderId -> 
                selectedOrderForDetails = null
                selectedOrderIdToAssign = orderId 
            },
            onUpdateStatus = onUpdateStatus
        )
    }

    if (selectedOrderIdToAssign != null) {
        val orderToAssign = allOrders.find { it.orderId == selectedOrderIdToAssign }
        if (orderToAssign != null) {
            AssignOrderSheet(
                order = orderToAssign,
                partnersState = partnersState,
                engineersState = engineersState,
                onAssignPartner = onAssignPartner,
                onAssignEngineer = onAssignEngineer,
                onDismiss = { selectedOrderIdToAssign = null }
            )
        }
    }
}
