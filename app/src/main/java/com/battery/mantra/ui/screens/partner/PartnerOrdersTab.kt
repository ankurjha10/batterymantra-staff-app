package com.battery.mantra.ui.screens.partner

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.ui.components.OrderCard
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun PartnerOrdersTab(
    ordersState: PartnerDataState<List<OrderResponse>>,
    engineersState: PartnerDataState<List<EngineerResponse>>,
    targetFilter: String? = null,
    targetSearch: String? = null,
    onTargetConsumed: () -> Unit = {},
    onRetry: () -> Unit,
    onAssignEngineer: (orderId: String, engineerId: String, onSuccess: (OrderResponse) -> Unit, onError: (String) -> Unit) -> Unit,
    onUnassignEngineer: (orderId: String, onSuccess: (OrderResponse) -> Unit, onError: (String) -> Unit) -> Unit,
    onUpdateStatus: (orderId: String, newStatus: String, reason: String?, onSuccess: (OrderResponse) -> Unit, onError: (String) -> Unit) -> Unit,
    onAddNote: (orderId: String, note: String, onSuccess: (OrderResponse) -> Unit, onError: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedOrderForDetails by remember { mutableStateOf<OrderResponse?>(null) }
    var orderToAssignEngineer by remember { mutableStateOf<OrderResponse?>(null) }
    var isActionInProgress by remember { mutableStateOf(false) }

    val allOrders = (ordersState as? PartnerDataState.Success)?.data.orEmpty()

    LaunchedEffect(targetFilter, targetSearch, allOrders) {
        var consumed = false
        if (!targetFilter.isNullOrBlank()) {
            selectedFilter = targetFilter
            if (targetSearch.isNullOrBlank()) consumed = true
        }
        if (!targetSearch.isNullOrBlank()) {
            searchQuery = targetSearch
            val match = allOrders.find { it.orderId.contains(targetSearch, ignoreCase = true) }
            if (match != null) {
                selectedOrderForDetails = match
                consumed = true
            }
        }
        if (consumed) onTargetConsumed()
    }

    // Keep details sheet synced if order updates
    LaunchedEffect(allOrders) {
        selectedOrderForDetails?.let { open ->
            allOrders.find { it.orderId == open.orderId }?.let { updated ->
                selectedOrderForDetails = updated
            }
        }
    }

    val filters = listOf("All", "Action Needed", "Dispatched", "Completed", "Cancelled")

    val filteredOrders = remember(allOrders, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        allOrders.filter { order ->
            val matchesSearch = if (q.isEmpty()) {
                true
            } else {
                order.orderId.lowercase().contains(q) ||
                    (order.customerName?.lowercase()?.contains(q) == true) ||
                    (order.customerPhone?.lowercase()?.contains(q) == true) ||
                    (order.shippingAddress?.lowercase()?.contains(q) == true) ||
                    (order.selectedCity?.lowercase()?.contains(q) == true)
            }
            val matchesFilter = when (selectedFilter) {
                "Action Needed" -> order.isActionNeeded()
                "Dispatched" -> order.isDispatched()
                "Completed" -> order.isCompletedOrder()
                "Cancelled" -> order.orderStatus.equals("CANCELLED", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search order ID, customer, phone, city...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BrandRed,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = BrandRed
            )
        )

        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val count = when (filter) {
                    "All" -> allOrders.size
                    "Action Needed" -> allOrders.count { it.isActionNeeded() }
                    "Dispatched" -> allOrders.count { it.isDispatched() }
                    "Completed" -> allOrders.count { it.isCompletedOrder() }
                    "Cancelled" -> allOrders.count { it.orderStatus.equals("CANCELLED", ignoreCase = true) }
                    else -> 0
                }
                val isSelected = selectedFilter == filter
                val showAlertDot = filter == "Action Needed" && count > 0

                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) Color(0xFFFEE2E2) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) BrandRed else Color(0xFFD1D5DB)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        if (showAlertDot) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(BrandRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "$filter ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFB91C1C) else Color(0xFF4B5563)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (ordersState) {
            is PartnerDataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandRed)
                }
            }
            is PartnerDataState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ordersState.message, color = Color(0xFFB91C1C), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onRetry) {
                            Text("Retry", color = BrandRed)
                        }
                    }
                }
            }
            is PartnerDataState.Success -> {
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedFilter != "All") {
                                "No orders matching your filter."
                            } else {
                                "No assigned orders yet."
                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                    ) {
                        items(filteredOrders, key = { it.orderId }) { order ->
                            val engName = order.assignedEngineer?.let {
                                "${it.firstName ?: ""} ${it.lastName ?: ""}".trim()
                            }?.takeIf { it.isNotEmpty() }

                            OrderCard(
                                orderId = order.orderId.take(8).lowercase(),
                                itemsCount = order.orderItems?.sumOf { it.quantity } ?: 1,
                                price = formatInr(order.totalAmount),
                                customerName = order.customerName ?: "Customer",
                                customerPhone = order.customerPhone ?: "No Phone",
                                customerEmail = order.customerEmail ?: "No Email",
                                paymentMethod = order.paymentMethod ?: "COD",
                                deliveryMethod = order.deliveryMethod ?: "Standard Delivery",
                                address = order.shippingAddress ?: "",
                                placedAt = order.placedAt ?: "",
                                status = order.orderStatus ?: "PENDING",
                                partnerName = order.assignedPartner?.businessName ?: order.assignedPartner?.contactPerson,
                                engineerName = engName,
                                selectedCity = order.selectedCity,
                                onActionClick = { selectedOrderForDetails = order }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }

    selectedOrderForDetails?.let { openOrder ->
        PartnerOrderDetailsSheet(
            order = openOrder,
            isActionInProgress = isActionInProgress,
            onDismiss = { selectedOrderForDetails = null },
            onAssignEngineerClick = { orderToAssignEngineer = it },
            onUnassignEngineer = { orderId ->
                isActionInProgress = true
                onUnassignEngineer(
                    orderId,
                    { updated ->
                        isActionInProgress = false
                        selectedOrderForDetails = updated
                        Toast.makeText(context, "Engineer unassigned", Toast.LENGTH_SHORT).show()
                    },
                    { err ->
                        isActionInProgress = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onUpdateStatus = { orderId, newStatus, reason ->
                isActionInProgress = true
                onUpdateStatus(
                    orderId,
                    newStatus,
                    reason,
                    { updated ->
                        isActionInProgress = false
                        selectedOrderForDetails = updated
                        Toast.makeText(context, "Order marked $newStatus", Toast.LENGTH_SHORT).show()
                    },
                    { err ->
                        isActionInProgress = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onAddNote = { orderId, note, onCleared ->
                isActionInProgress = true
                onAddNote(
                    orderId,
                    note,
                    { updated ->
                        isActionInProgress = false
                        selectedOrderForDetails = updated
                        onCleared()
                        Toast.makeText(context, "Note added", Toast.LENGTH_SHORT).show()
                    },
                    { err ->
                        isActionInProgress = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    orderToAssignEngineer?.let { targetOrder ->
        AssignEngineerSheet(
            order = targetOrder,
            engineersState = engineersState,
            isSubmitting = isActionInProgress,
            onAssignEngineer = { engineerId ->
                isActionInProgress = true
                onAssignEngineer(
                    targetOrder.orderId,
                    engineerId,
                    { updated ->
                        isActionInProgress = false
                        orderToAssignEngineer = null
                        selectedOrderForDetails = updated
                        Toast.makeText(context, "Engineer assigned successfully", Toast.LENGTH_SHORT).show()
                    },
                    { err ->
                        isActionInProgress = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { orderToAssignEngineer = null }
        )
    }
}
