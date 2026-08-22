package com.battery.mantra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.theme.TextGray

@Composable
fun OrderCard(
    orderId: String,
    itemsCount: Int,
    price: String,
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    paymentMethod: String,
    deliveryMethod: String,
    address: String,
    placedAt: String,
    status: String,
    partnerName: String? = null,
    engineerName: String? = null,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
    onStatusChange: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row containing the main details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Column 1: Order Details
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(text = "Order Details", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "#$orderId", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(text = "$itemsCount item • $price", fontSize = 13.sp, color = Color.DarkGray)
                }

                // Column 2: Customer
                Column(modifier = Modifier.weight(2f)) {
                    Text(text = "Customer", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = customerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = customerPhone, fontSize = 12.sp, color = Color.DarkGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = customerEmail, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Column 3: Payment
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Payment", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    val isCod = paymentMethod.contains("COD", ignoreCase = true)
                    val pillColor = if (isCod) Color(0xFFE65100) else Color(0xFF1976D2)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, pillColor.copy(alpha = 0.5f)),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = paymentMethod.uppercase(),
                            color = pillColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Column 4: Fulfillment
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(text = "Fulfillment", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (deliveryMethod.contains("pickup", true)) Icons.Outlined.Store else Icons.Outlined.LocalShipping
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = deliveryMethod, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = address, fontSize = 12.sp, color = Color.DarkGray, maxLines = 1)
                    }
                }

                // Column 5: Date & Time
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = "Date & Time", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = placedAt.substringBefore("T"), fontSize = 13.sp, color = Color.Black)
                    Text(text = placedAt.substringAfter("T").substringBefore("."), fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 6: Status Update
                Column {
                    Text(text = "Status Update", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(status = status)
                        if (engineerName != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Eng: $engineerName", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                        } else if (partnerName != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Partner: $partnerName", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Main Branch", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (onActionClick != null) {
                    IconButton(onClick = onActionClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "View Details", tint = Color.DarkGray)
                    }
                }
            }
        }
    }
}
