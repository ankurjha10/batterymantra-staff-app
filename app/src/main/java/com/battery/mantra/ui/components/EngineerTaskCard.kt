package com.battery.mantra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    paymentMethod: String? = null,
    paymentStatus: String? = null,
    actionText: String = "",
    isActive: Boolean = true,
    onActionClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
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
                            onClick = onNavigateClick,
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
                                    val isPaid = paymentStatus?.uppercase() == "PAID"
                                    Text(text = if (isPaid) "Amount Paid" else "Collect Payment", color = Color(0xFF5F6368), fontSize = 12.sp)
                                    Text(text = price, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            val methodText = paymentMethod?.uppercase() ?: "COD"
                            val isPaid = paymentStatus?.uppercase() == "PAID"
                            Box(
                                modifier = Modifier
                                    .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = methodText, color = if (isPaid) Color(0xFF166534) else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Tracker
                    val statusUpper = status.uppercase()
                    val isDispatched = statusUpper == "DISPATCHED" || statusUpper == "DELIVERED" || statusUpper == "COMPLETED" || statusUpper == "INSTALLED"
                    val isDelivered = statusUpper == "DELIVERED" || statusUpper == "COMPLETED" || statusUpper == "INSTALLED"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        ProgressStep(
                            modifier = Modifier.weight(1f),
                            label = "Assigned",
                            isCompleted = isDispatched,
                            isCurrent = !isDispatched,
                            isFirst = true,
                            isLast = false
                        )
                        ProgressStep(
                            modifier = Modifier.weight(1f),
                            label = "Dispatched",
                            isCompleted = isDelivered,
                            isCurrent = isDispatched && !isDelivered,
                            isFirst = false,
                            isLast = false
                        )
                        ProgressStep(
                            modifier = Modifier.weight(1f),
                            label = "Delivered",
                            isCompleted = false,
                            isCurrent = isDelivered,
                            isFirst = false,
                            isLast = true
                        )
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
fun ProgressStep(
    modifier: Modifier = Modifier,
    label: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Line before circle
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(2.dp)
                        .background(if (isCompleted || isCurrent) Color(0xFFD32F2F) else Color(0xFFE0E0E0))
                        .align(Alignment.CenterStart)
                )
            }
            // Line after circle
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(2.dp)
                        .background(if (isCompleted) Color(0xFFD32F2F) else Color(0xFFE0E0E0))
                        .align(Alignment.CenterEnd)
                )
            }

            // Circle
            val circleSize = if (isCurrent) 16.dp else 12.dp
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(
                        if (isCompleted) Color(0xFFD32F2F) else if (isCurrent) Color.White else Color(0xFFE0E0E0),
                        CircleShape
                    )
                    .border(
                        if (isCurrent) 4.dp else 0.dp,
                        if (isCurrent) Color(0xFFD32F2F) else Color.Transparent,
                        CircleShape
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isCompleted || isCurrent) Color(0xFFD32F2F) else Color.DarkGray,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}
