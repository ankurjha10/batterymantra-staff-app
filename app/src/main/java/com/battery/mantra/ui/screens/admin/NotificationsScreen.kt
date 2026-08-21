package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val notificationsState by viewModel.notificationsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when (notificationsState) {
            is AdminDataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            }
            is AdminDataState.Error -> {
                val error = (notificationsState as AdminDataState.Error).message
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = error, color = Color.Red)
                }
            }
            is AdminDataState.Success -> {
                val notifications = (notificationsState as AdminDataState.Success).data
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(text = "No new notifications", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            val formattedTime = try {
                                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                                val outputFormat = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                                val date = inputFormat.parse(notification.createdAt)
                                if (date != null) outputFormat.format(date) else notification.createdAt
                            } catch (e: Exception) {
                                notification.createdAt
                            }
                            
                            val formattedMessage = notification.message.replace(Regex("Order #([a-f0-9\\-]{8})[a-f0-9\\-]+"), "Order #$1...")

                            val isNewOrder = notification.title.contains("Order", true)
                            val orderIdMatch = Regex("Order #([a-f0-9\\-]+)").find(notification.message)
                            val orderId = orderIdMatch?.groups?.get(1)?.value

                            NotificationCard(
                                title = if (notification.title == "New Order Placed") "New Order Received" else notification.title,
                                message = formattedMessage,
                                time = formattedTime,
                                onClick = {
                                    if (isNewOrder && orderId != null) {
                                        viewModel.navigateToOrder(orderId, "New Orders")
                                        onBackClick()
                                    }
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

@Composable
fun NotificationCard(title: String, message: String, time: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFD32F2F).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = Color(0xFFD32F2F))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message, 
                    fontSize = 13.sp, 
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = time, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
