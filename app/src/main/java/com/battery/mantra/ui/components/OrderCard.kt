package com.battery.mantra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val cardModifier = if (onActionClick != null) {
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onActionClick() }
    } else {
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$orderId",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Details (Stacked)
            Text(
                text = customerName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = customerPhone, fontSize = 13.sp, color = Color(0xFF4B5563))
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customerEmail, 
                    fontSize = 13.sp, 
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            // Order Specs & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedPrice = price.replace("₹", "")
                Text(
                    text = "$itemsCount item${if (itemsCount > 1) "s" else ""} • ₹$formattedPrice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
                val formattedDate = placedAt.replace("T", " ").substringBeforeLast(":")
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Payment, Fulfillment, Assignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    val isCod = paymentMethod.contains("COD", ignoreCase = true)
                    val pillColor = if (isCod) Color(0xFFEA580C) else Color(0xFF2563EB)
                    val pillBg = if (isCod) Color(0xFFFFF7ED) else Color(0xFFEFF6FF)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = pillBg,
                            border = BorderStroke(1.dp, pillColor.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = paymentMethod.uppercase(),
                                color = pillColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val deliveryIcon = if (deliveryMethod.contains("pickup", true)) Icons.Outlined.Store else Icons.Outlined.LocalShipping
                        Icon(deliveryIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = deliveryMethod.replace("_", " "), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B5563))
                    }
                }
                
                // Assigned Info
                Column(horizontalAlignment = Alignment.End) {
                    if (engineerName != null) {
                        Text(text = "Assigned Engineer", fontSize = 10.sp, color = Color(0xFF6B7280))
                        Text(text = engineerName, fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    } else if (partnerName != null) {
                        Text(text = "Assigned Partner", fontSize = 10.sp, color = Color(0xFF6B7280))
                        Text(text = partnerName, fontSize = 12.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "Unassigned", fontSize = 10.sp, color = Color(0xFF6B7280))
                        Text(text = "Main Branch", fontSize = 12.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
