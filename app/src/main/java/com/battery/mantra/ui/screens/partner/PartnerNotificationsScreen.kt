package com.battery.mantra.ui.screens.partner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.battery.mantra.ui.screens.admin.NotificationCard
import com.battery.mantra.ui.theme.BrandRed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerNotificationsScreen(
    viewModel: PartnerViewModel,
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
                    val items = (notificationsState as? PartnerDataState.Success)?.data.orEmpty()
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllNotifications() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Clear All", tint = BrandRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when (val state = notificationsState) {
            is PartnerDataState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandRed)
                }
            }
            is PartnerDataState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = BrandRed)
                }
            }
            is PartnerDataState.Success -> {
                val notifications = state.data
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No notifications", color = Color.Gray)
                    }
                } else {
                    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }
                    val outputFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                    val orderIdRegex = remember { Regex("Order #([a-f0-9\\-]+)", RegexOption.IGNORE_CASE) }

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
                                positionalThreshold = { it * 0.5f },
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
                                if (notifications.any { it.id == notification.id } && !isVisible) {
                                    isVisible = true
                                    dismissState.reset()
                                }
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color by animateColorAsState(
                                            targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                                BrandRed
                                            } else {
                                                Color.Transparent
                                            },
                                            label = "swipeBg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                                Alignment.CenterStart
                                            } else {
                                                Alignment.CenterEnd
                                            }
                                        ) {
                                            if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    val formattedTime = try {
                                        inputFormat.parse(notification.createdAt)?.let { outputFormat.format(it) }
                                            ?: notification.createdAt
                                    } catch (_: Exception) {
                                        notification.createdAt
                                    }

                                    val orderIdMatch = orderIdRegex.find(notification.message)
                                    val orderId = orderIdMatch?.groups?.get(1)?.value

                                    NotificationCard(
                                        title = notification.title,
                                        message = notification.message,
                                        time = formattedTime,
                                        onClick = {
                                            val text = "${notification.title} ${notification.message}".lowercase()
                                            when {
                                                text.contains("order") -> {
                                                    viewModel.navigateToOrders(
                                                        filter = "All",
                                                        orderIdSearch = orderId
                                                    )
                                                    onBackClick()
                                                }
                                                text.contains("stock") || text.contains("inventory") -> {
                                                    viewModel.onTabSelected(4)
                                                    onBackClick()
                                                }
                                                text.contains("price") || text.contains("product") -> {
                                                    viewModel.onTabSelected(3)
                                                    onBackClick()
                                                }
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

