package com.battery.mantra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EngineerTaskCard(
    orderId: String,
    customerName: String,
    address: String,
    price: String,
    status: String,
    actionText: String = "",
    isActive: Boolean = true,
    onActionClick: () -> Unit = {},
    onCallClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Red Top Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (isActive) Color(0xFFD32F2F) else Color(0xFF9E9E9E))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order #$orderId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    // Status Badge
                    val (badgeBg, badgeText) = when (status.uppercase()) {
                        "COMPLETED" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        "FAILED" -> Pair(Color(0xFFFDE8E8), Color(0xFFC81E1E))
                        else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status,
                            color = badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (isActive) "Current Task" else "Past Task",
                    color = Color(0xFF5F6368),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = Color(0xFFEEEEEE))
                
                Spacer(modifier = Modifier.height(12.dp))

                // Customer Details
                Text(
                    text = "Customer",
                    color = Color(0xFF5F6368),
                    fontSize = 12.sp
                )
                Text(
                    text = customerName,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Address Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Delivery Address", color = Color(0xFF5F6368), fontSize = 12.sp)
                            Text(text = address, color = Color.Black, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                if (isActive) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Call and Navigate Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF1F8E9))
                        ) {
                            Icon(Icons.Outlined.Call, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CALL", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { /* Navigate */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "NAVIGATE", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF3F4F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Collect Payment", color = Color(0xFF5F6368), fontSize = 12.sp)
                                    Text(text = price, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "COD", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressStep(label = "Assigned", isSelected = true)
                        ProgressStep(label = "Dispatched", isSelected = false)
                        ProgressStep(label = "Delivered", isSelected = false)
                    }
                }

                if (actionText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    // Start Dispatch Button
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text(
                            text = actionText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStep(label: String, isSelected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, Color(0xFFD32F2F), CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFE0E0E0), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFFD32F2F) else Color.DarkGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
