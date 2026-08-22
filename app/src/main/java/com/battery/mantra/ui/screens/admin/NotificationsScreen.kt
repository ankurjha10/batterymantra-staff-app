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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val notificationsState by viewModel.notificationsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (notificationsState is com.battery.mantra.ui.screens.admin.AdminDataState.Success &&
                        (notificationsState as com.battery.mantra.ui.screens.admin.AdminDataState.Success).data.isNotEmpty()
                    ) {
                        IconButton(onClick = { viewModel.clearAllNotifications() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Clear All", tint = Color.Red)
                        }
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
                    val inputFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()) }
                    val outputFormat = remember { java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()) }
                    val orderIdRegexFull = remember { Regex("Order #([a-f0-9\\-]{8})[a-f0-9\\-]+") }
                    val orderIdRegexSimple = remember { Regex("Order #([a-f0-9\\-]+)") }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications, key = { it.id }) { notification ->
                            var isVisible by remember { mutableStateOf(true) }
                            
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { it * 0.5f }, // Require 50% swipe
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                        isVisible = false
                                        viewModel.deleteNotification(notification.id)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            LaunchedEffect(notifications) {
                                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled && isVisible) {
                                    dismissState.reset()
                                }
                                if (notifications.any { it.id == notification.id } && !isVisible) {
                                    // If it was reverted by ViewModel (failure), make it visible again and reset
                                    isVisible = true
                                    dismissState.reset()
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isVisible,
                                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                            ) {
                                SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) Color.Red else Color.Transparent,
                                        label = "color"
                                    )
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                val formattedTime = try {
                                    val date = inputFormat.parse(notification.createdAt)
                                    if (date != null) outputFormat.format(date) else notification.createdAt
                                } catch (e: Exception) {
                                    notification.createdAt
                                }

                                val formattedMessage = notification.message.replace(
                                    orderIdRegexFull,
                                    "Order #$1..."
                                )

                                val isNewOrder = notification.title.contains("Order", true)
                                val orderIdMatch = orderIdRegexSimple.find(notification.message)
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
                }
            }

            else -> {}
        }
    }
}

@Composable
fun NotificationCard(title: String, message: String, time: String, onClick: () -> Unit = {}) {
    val (icon, tintColor) = when {
        title.contains("Cancel", ignoreCase = true) -> Pair(Icons.Outlined.Cancel, Color(0xFFEF4444)) // Red
        title.contains("Low Stock", ignoreCase = true) -> Pair(Icons.Outlined.WarningAmber, Color(0xFFF59E0B)) // Orange
        title.contains("Callback", ignoreCase = true) || title.contains("Enquiry", ignoreCase = true) -> Pair(Icons.Outlined.PhoneCallback, Color(0xFF3B82F6)) // Blue
        title.contains("Update", ignoreCase = true) || title.contains("Completed", ignoreCase = true) || title.contains("Assigned", ignoreCase = true) -> Pair(Icons.Outlined.CheckCircle, Color(0xFF10B981)) // Green
        title.contains("Order", ignoreCase = true) -> Pair(Icons.Outlined.ShoppingBag, Color(0xFFD32F2F)) // Brand Red
        else -> Pair(Icons.Outlined.NotificationsActive, Color(0xFFD32F2F))
    }

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
                    .background(tintColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tintColor)
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
