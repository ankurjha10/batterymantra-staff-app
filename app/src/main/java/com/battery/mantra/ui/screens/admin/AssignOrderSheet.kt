package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
    var selectedTab by remember { mutableIntStateOf(0) }


    val canAssignPartner = order.assignedEngineer == null
    val canAssignEngineer = order.assignedPartner == null
    
    // Automatically switch to the only available tab if one is disabled
    LaunchedEffect(Unit) {
        if (!canAssignPartner && canAssignEngineer) {
            selectedTab = 1
        }
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
                    text = "Assign Order",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order #${order.orderId?.take(8)?.uppercase() ?: "Unknown"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tabs
            if (canAssignPartner && canAssignEngineer) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFD32F2F),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFD32F2F)
                        )
                    }
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
            } else if (canAssignPartner) {
                Text(
                    "Assign Partner",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color(0xFFD32F2F)
                )
                selectedTab = 0
            } else if (canAssignEngineer) {
                Text(
                    "Assign Engineer",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color(0xFFD32F2F)
                )
                selectedTab = 1
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(0.6f)) {
                when (selectedTab) {
                    0 -> {
                        when (val state = partnersState) {
                            is AdminDataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Error -> Text("Error loading partners", modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Success -> {
                                LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                                    items(state.data, key = { it.id }) { partner ->
                                        PartnerItem(
                                            name = partner.businessName ?: partner.contactPerson ?: "Unknown",
                                            detail = partner.phoneNumber ?: "No Phone",
                                            onAssign = {
                                                onAssignPartner(
                                                    order.orderId ?: "",
                                                    partner.id,
                                                    onDismiss,
                                                    { /* Handle error */ }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    1 -> {
                        when (val state = engineersState) {
                            is AdminDataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Error -> Text("Error loading engineers", modifier = Modifier.align(Alignment.Center))
                            is AdminDataState.Success -> {
                                // ONLY SHOW DIRECT ADMIN ENGINEERS
                                val directEngineers = state.data.filter { it.partnerId == null }
                                if (directEngineers.isEmpty()) {
                                    Text("No direct admin engineers available", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                                } else {
                                    LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                                        items(directEngineers, key = { it.id }) { engineer ->
                                            EngineerItem(
                                                name = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}".trim(),
                                                phone = engineer.phoneNumber ?: "No phone",
                                                onAssign = {
                                                    onAssignEngineer(
                                                        order.orderId ?: "",
                                                        engineer.id,
                                                        onDismiss,
                                                        { /* Handle error */ }
                                                    )
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
private fun PartnerItem(name: String, detail: String, onAssign: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
private fun EngineerItem(name: String, phone: String, onAssign: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
