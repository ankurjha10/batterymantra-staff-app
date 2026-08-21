package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailsSheet(
    order: OrderResponse,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId.take(8).lowercase()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Items
            Text(text = "Order Items", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (order.orderItems.isNullOrEmpty()) {
                Text(text = "No items found", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(order.orderItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Product ID: ${item.productId.take(8)}...", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Quantity: ${item.quantity}", fontSize = 13.sp, color = Color.DarkGray)
                                    if (item.exchangeOldBattery) {
                                        Text(text = "Exchange: Yes", fontSize = 13.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Text(text = "Exchange: No", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Customer Details
            Text(text = "Customer Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Name: ${order.customerName ?: "N/A"}", fontSize = 14.sp)
            Text(text = "Phone: ${order.customerPhone ?: "N/A"}", fontSize = 14.sp)
            Text(text = "Email: ${order.customerEmail ?: "N/A"}", fontSize = 14.sp)
            Text(text = "Address: ${order.shippingAddress ?: "N/A"}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))
            
            // Financials
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total Amount", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = "₹${order.totalAmount ?: 0.0}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD32F2F))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
