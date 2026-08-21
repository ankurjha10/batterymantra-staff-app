package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.CouponResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(
    viewModel: AdminCouponsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showDialog by remember { mutableStateOf(false) }
    var editingCoupon by remember { mutableStateOf<CouponResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupons", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    editingCoupon = null
                    showDialog = true 
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                text = { Text("Add Coupon") },
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Manage discount codes and promotions.",
                color = Color(0xFF424242),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = Color.Red)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.coupons) { coupon ->
                        CouponCard(
                            coupon = coupon,
                            onClick = {
                                editingCoupon = coupon
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        key(editingCoupon?.couponId ?: "new_coupon") {
            AddEditCouponDialog(
                coupon = editingCoupon,
                onDismiss = { showDialog = false },
                onSave = { request ->
                    if (editingCoupon == null) {
                        viewModel.createCoupon(request, 
                            onSuccess = { 
                                showDialog = false
                                Toast.makeText(context, "Coupon created successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    } else {
                        viewModel.updateCoupon(editingCoupon!!.couponId, request,
                            onSuccess = { 
                                showDialog = false
                                Toast.makeText(context, "Coupon updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    }
                },
                onDelete = if (editingCoupon != null) {
                    {
                        viewModel.deleteCoupon(editingCoupon!!.couponId,
                            onSuccess = { 
                                showDialog = false
                                Toast.makeText(context, "Coupon deleted successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun CouponCard(coupon: CouponResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocalOffer,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = coupon.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Discount", color = Color(0xFF5F6368), fontSize = 10.sp)
                        val discountText = if (coupon.discountType == "PERCENTAGE") "${coupon.discountValue}%" else "₹${coupon.discountValue}"
                        Text(
                            text = discountText,
                            color = Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(text = "Limits", color = Color(0xFF5F6368), fontSize = 10.sp)
                        val limitText = coupon.minOrderValue?.let { "Min ₹$it" } ?: "None"
                        Text(
                            text = limitText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Column {
                        Text(text = "Usage", color = Color(0xFF5F6368), fontSize = 10.sp)
                        val usageText = "${coupon.usedCount ?: 0}/${coupon.usageLimit ?: "\u221E"}"
                        Text(text = usageText, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (coupon.isActive) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (coupon.isActive) "Active" else "Inactive",
                        color = if (coupon.isActive) Color(0xFF2E7D32) else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
