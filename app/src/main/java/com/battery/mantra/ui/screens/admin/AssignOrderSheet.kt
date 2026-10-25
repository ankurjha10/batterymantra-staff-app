@file:Suppress("FunctionName")

package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.OrderResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignOrderSheet(
    order: OrderResponse,
    partnersState: AdminDataState<List<com.battery.mantra.data.models.PartnerResponse>>,
    engineersState: AdminDataState<List<com.battery.mantra.data.models.EngineerResponse>>,
    onAssignPartner: (orderId: String, partnerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onAssignEngineer: (orderId: String, engineerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val isReassign = order.assignedPartner != null || order.assignedEngineer != null
    val currentAssignedName = order.assignedEngineer?.let {
        "Engineer: ${it.firstName ?: ""} ${it.lastName ?: ""}".trim()
    } ?: order.assignedPartner?.let {
        "Partner: ${it.businessName ?: it.contactPerson ?: ""}".trim()
    }

    var selectedTab by remember {
        mutableIntStateOf(if (order.assignedEngineer != null && order.assignedPartner == null) 1 else 0)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF5F7FA),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = if (isReassign) "Reassign Order" else "Assign Order",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order #${order.orderId.take(8).uppercase()}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (isReassign && !currentAssignedName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Current: ",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = currentAssignedName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tabs - Admin can always toggle between Partners and Engineers
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFD32F2F)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Partners", color = if (selectedTab == 0) Color(0xFFD32F2F) else Color.Gray, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Engineers", color = if (selectedTab == 1) Color(0xFFD32F2F) else Color.Gray, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(0.6f)) {
                when (selectedTab) {
                    0 -> {
                        when (partnersState) {
                            is AdminDataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Error -> Text("Error loading partners", modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Success -> {
                                LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                                    items(partnersState.data, key = { it.id }) { partner ->
                                        val isCurrentlyAssigned = order.assignedPartner?.id == partner.id
                                        PartnerItem(
                                            name = partner.businessName ?: partner.contactPerson ?: "Unknown",
                                            detail = partner.phoneNumber ?: "No Phone",
                                            isCurrentlyAssigned = isCurrentlyAssigned,
                                            onAssign = {
                                                onAssignPartner(
                                                    order.orderId,
                                                    partner.id,
                                                    onDismiss
                                                ) { /* Handle error */ }
                                            }
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    1 -> {
                        when (engineersState) {
                            is AdminDataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Error -> Text("Error loading engineers", modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Success -> {
                                // ONLY SHOW DIRECT ADMIN ENGINEERS
                                val directEngineers = engineersState.data.filter { it.partnerId == null }
                                if (directEngineers.isEmpty()) {
                                    Text("No direct admin engineers available", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                                } else {
                                    LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                                        items(directEngineers, key = { it.id }) { engineer ->
                                            val isCurrentlyAssigned = order.assignedEngineer?.id == engineer.id
                                            EngineerItem(
                                                name = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}".trim(),
                                                phone = engineer.phoneNumber ?: "No phone",
                                                isCurrentlyAssigned = isCurrentlyAssigned,
                                                onAssign = {
                                                    onAssignEngineer(
                                                        order.orderId,
                                                        engineer.id,
                                                        onDismiss
                                                    ) { /* Handle error */ }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerItem(
    name: String,
    detail: String,
    isCurrentlyAssigned: Boolean = false,
    onAssign: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyAssigned) Color(0xFFECFDF5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentlyAssigned) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = Color(0xFF3B82F6))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text(text = detail, fontSize = 13.sp, color = Color.Gray)
                }
            }
            
            if (isCurrentlyAssigned) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "Currently Assigned",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Currently Assigned",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onAssign,
                    modifier = Modifier
                        .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Assign", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun EngineerItem(
    name: String,
    phone: String,
    isCurrentlyAssigned: Boolean = false,
    onAssign: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyAssigned) Color(0xFFECFDF5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentlyAssigned) BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Engineering, contentDescription = null, tint = Color(0xFFF59E0B))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name.ifEmpty { "Unknown Engineer" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text(text = phone, fontSize = 13.sp, color = Color.Gray)
                }
            }
            
            if (isCurrentlyAssigned) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "Currently Assigned",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Currently Assigned",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onAssign,
                    modifier = Modifier
                        .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Assign", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
