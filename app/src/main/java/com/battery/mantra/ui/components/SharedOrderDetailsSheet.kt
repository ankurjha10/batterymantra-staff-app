package com.battery.mantra.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedOrderDetailsSheet(
    order: OrderResponse,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onAssignEngineer: (String) -> Unit = {},
    onUpdateStatus: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF5F7FA), // Light elegant background
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.order_summary),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "#${order.orderId.take(8).uppercase()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.close_button), tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Badge
                item {
                    val statusColor = when (order.orderStatus) {
                        "DELIVERED", "INSTALLED" -> Color(0xFF10B981)
                        "CANCELLED" -> Color(0xFFEF4444)
                        "PENDING" -> Color(0xFFF59E0B)
                        else -> Color(0xFF3B82F6)
                    }
                    var statusExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    
                    val statusFlow = listOf(
                        "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED",
                        "OUT_FOR_DELIVERY", "DELIVERED", "INSTALLED"
                    )
                    
                    val isTerminalState = order.orderStatus == "CANCELLED" || 
                                          order.orderStatus == "DELIVERED" || 
                                          order.orderStatus == "INSTALLED"
                                          
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .clickable(enabled = isAdmin && !isTerminalState) { statusExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = order.orderStatus ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.unknown_status),
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            DropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val currentIdx = statusFlow.indexOf(order.orderStatus ?: "PENDING")
                                val allowedStatuses = statusFlow.filterIndexed { index, status -> 
                                    index > currentIdx && status != "DELIVERED" && status != "INSTALLED" 
                                }.toMutableList()
                                if (!isTerminalState) allowedStatuses.add("CANCELLED")
                                
                                allowedStatuses.forEach { newStatus ->
                                    val itemColor = when (newStatus) {
                                        "DELIVERED", "INSTALLED" -> Color(0xFF10B981)
                                        "CANCELLED" -> Color(0xFFEF4444)
                                        "PENDING" -> Color(0xFFF59E0B)
                                        else -> Color(0xFF3B82F6)
                                    }
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = newStatus.replace("_", " "),
                                                color = itemColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ) 
                                        },
                                        onClick = {
                                            statusExpanded = false
                                            if (newStatus != order.orderStatus) {
                                                onUpdateStatus(order.orderId ?: "", newStatus)
                                                onDismiss()
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (!isTerminalState) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.tap_to_update), fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Order Items Card
                item {
                    InfoCard(title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.order_items), icon = Icons.Outlined.Inventory2) {
                        if (order.orderItems.isNullOrEmpty()) {
                            Text(text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.no_items_found), color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                order.orderItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val imageModifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9))

                                            if (!item.productImage.isNullOrEmpty()) {
                                                coil.compose.AsyncImage(
                                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                        .data(item.productImage)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Product Image",
                                                    modifier = imageModifier,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = imageModifier,
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color.LightGray)
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = item.productName ?: "PID: ${item.productId.take(8)}", 
                                                    fontWeight = FontWeight.SemiBold, 
                                                    fontSize = 14.sp
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.qty_prefix_format, item.quantity), fontSize = 12.sp, color = Color.Gray)
                                                    if (item.exchangeOldBattery) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.with_exchange), fontSize = 12.sp, color = Color(0xFFD32F2F))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Order Details (Payment, Delivery)
                item {
                    InfoCard(title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.order_details), icon = Icons.Outlined.Info) {
                        // Dates
                        DetailRow(icon = Icons.Outlined.CalendarToday, text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.placed_prefix, order.placedAt?.take(10) ?: "N/A"))
                        DetailRow(icon = Icons.Outlined.EventAvailable, text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.installation_prefix, order.installationDate ?: "N/A"))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Delivery
                        DetailRow(icon = Icons.Outlined.LocalShipping, text = "Delivery Method: ${order.deliveryMethod ?: "STANDARD"}")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        
                        // Payment
                        val isPaid = order.paymentStatus?.uppercase() == "PAID"
                        val paymentColor = if (isPaid) Color(0xFF10B981) else Color(0xFFD32F2F)
                        val paymentMethod = order.paymentMethod ?: "COD"
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Payments, contentDescription = "Payment", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment: ", fontSize = 14.sp, color = Color.DarkGray)
                            Text(
                                text = "${if (isPaid) "PAID" else "PENDING"} ($paymentMethod)",
                                fontSize = 14.sp, color = paymentColor, fontWeight = FontWeight.Bold
                            )
                        }
                        DetailRow(icon = Icons.Outlined.AttachMoney, text = "Total Amount: ₹${order.totalAmount ?: 0.0}")
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Customer Details Card
                item {
                    InfoCard(title = "Customer Details", icon = Icons.Outlined.Person) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(icon = Icons.Outlined.Badge, text = order.customerName ?: "N/A")
                            DetailRow(icon = Icons.Outlined.Phone, text = order.customerPhone ?: "N/A")
                            DetailRow(icon = Icons.Outlined.LocationOn, text = order.shippingAddress ?: "N/A")
                        }
                    }
                }

                // Financials Card
                item {
                    InfoCard(title = "Payment Summary", icon = Icons.Outlined.Payments) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Total Amount", color = Color.Gray, fontSize = 13.sp)
                                Text(
                                    text = order.paymentMethod ?: "CASH",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "₹${order.totalAmount ?: 0.0}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Action Buttons Footer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val phone = order.customerPhone
                    // Call Button
                    Button(
                        onClick = {
                            if (phone != null) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Phone, contentDescription = "Call", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call", fontWeight = FontWeight.SemiBold)
                    }

                    // WhatsApp Button
                    Button(
                        onClick = {
                            if (phone != null) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=91$phone")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366), // WhatsApp Green
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            // Assign Engineer Button
            val isAssigned = order.assignedPartner != null || order.assignedEngineer != null
            val assignedName = order.assignedEngineer?.let { "Eng: ${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
                ?: order.assignedPartner?.let { "Partner: ${it.businessName ?: it.contactPerson}" }
                
            if (isAssigned) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (order.assignedEngineer != null) Icons.Outlined.Engineering else Icons.Outlined.Storefront,
                        contentDescription = "Assigned",
                        tint = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Assigned To", fontSize = 12.sp, color = Color.Gray)
                        Text(assignedName ?: "Unknown", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 16.sp)
                    }
                }
            } else if (isAdmin && (order.orderStatus == "PENDING" || order.orderStatus == "CONFIRMED")) {
                Button(
                    onClick = { 
                        onDismiss()
                        onAssignEngineer(order.orderId ?: "")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Engineering, contentDescription = "Assign", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign Engineer / Partner", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InfoCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF334155))
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
    }
}
