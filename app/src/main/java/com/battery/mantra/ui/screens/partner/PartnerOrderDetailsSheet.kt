package com.battery.mantra.ui.screens.partner

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.ui.components.CityMismatchBanner
import com.battery.mantra.ui.components.DetailRow
import com.battery.mantra.ui.components.InfoCard
import com.battery.mantra.ui.components.StatusBadge
import com.battery.mantra.ui.theme.BrandRed
import com.battery.mantra.util.checkCityMismatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerOrderDetailsSheet(
    order: OrderResponse,
    isActionInProgress: Boolean = false,
    onDismiss: () -> Unit,
    onAssignEngineerClick: (OrderResponse) -> Unit,
    onUnassignEngineer: (String) -> Unit,
    onUpdateStatus: (orderId: String, newStatus: String, cancellationReason: String?) -> Unit,
    onAddNote: (orderId: String, note: String, onCleared: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var noteText by remember(order.orderId) { mutableStateOf("") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }

    val statusUpper = order.orderStatus?.uppercase() ?: "PENDING"
    val isTerminal = order.isTerminalOrder()
    val unpaidOnline = order.paymentMethod.equals("ONLINE", ignoreCase = true) &&
        !order.paymentStatus.equals("PAID", ignoreCase = true)

    val nextAction: Pair<String, String>? = when {
        unpaidOnline || isTerminal -> null
        statusUpper == "PENDING" || statusUpper == "CONFIRMED" -> "PROCESSING" to "Start Processing"
        statusUpper == "PROCESSING" -> "SHIPPED" to "Mark Shipped"
        statusUpper == "SHIPPED" || statusUpper == "OUT_FOR_DELIVERY" -> "DELIVERED" to "Mark Delivered"
        else -> null
    }
    val canProceedToShipped = nextAction?.first != "SHIPPED" || order.assignedEngineer != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF5F7FA),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order Summary",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "#${order.orderId.take(8).uppercase()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = statusUpper)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = CircleShape, color = Color.White, shadowElevation = 1.dp) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (unpaidOnline && !isTerminal) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Online payment is pending. Forward status updates are blocked until payment is PAID.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                if (checkCityMismatch(order.selectedCity, order.shippingAddress)) {
                    item {
                        CityMismatchBanner(selectedCity = order.selectedCity ?: "")
                    }
                }

                // Order Items
                item {
                    InfoCard(title = "Order Items", icon = Icons.Outlined.Inventory2) {
                        val items = order.orderItems.orEmpty()
                        if (items.isEmpty()) {
                            Text("No items found", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val imgModifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9))
                                            if (!item.productImage.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(item.productImage)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = item.productName,
                                                    modifier = imgModifier,
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = imgModifier,
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.Inventory2,
                                                        contentDescription = null,
                                                        tint = Color.LightGray
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.productName ?: "Product #${item.productId.take(6)}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF111827)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Qty: ${item.quantity}",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                    if (item.exchangeOldBattery) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "• With Exchange",
                                                            fontSize = 12.sp,
                                                            color = BrandRed,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (item.subtotal != null) {
                                            Text(
                                                text = formatInr(item.subtotal),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF111827)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Order & Payment Info
                item {
                    InfoCard(title = "Order & Payment Details", icon = Icons.Outlined.Info) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(
                                icon = Icons.Outlined.CalendarToday,
                                text = "Placed: ${formatOrderDate(order.placedAt)}"
                            )
                            if (!order.installationDate.isNullOrBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.EventAvailable,
                                    text = "Installation Date: ${order.installationDate}"
                                )
                            }
                            DetailRow(
                                icon = Icons.Outlined.LocalShipping,
                                text = "Delivery: ${order.deliveryMethod ?: "STANDARD"}"
                            )
                            val isPaid = order.paymentStatus.equals("PAID", ignoreCase = true)
                            DetailRow(
                                icon = Icons.Outlined.Payments,
                                text = "Payment: ${order.paymentStatus ?: "PENDING"} (${order.paymentMethod ?: "COD"})"
                            )
                            DetailRow(
                                icon = Icons.Outlined.AttachMoney,
                                text = "Total Amount: ${formatInr(order.totalAmount)}"
                            )
                            if (!order.deliverySecurityCode.isNullOrBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.Key,
                                    text = "Delivery Security OTP: ${order.deliverySecurityCode}"
                                )
                            }
                            if (isPaid && !order.paymentMode.isNullOrBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.Payments,
                                    text = "Payment Mode: ${order.paymentMode}"
                                )
                            }
                            if (!order.cancellationReason.isNullOrBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.Cancel,
                                    text = "Cancel Reason: ${order.cancellationReason}"
                                )
                            }
                        }
                    }
                }

                // Customer & Address
                item {
                    InfoCard(title = "Customer & Delivery Address", icon = Icons.Outlined.Person) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(icon = Icons.Outlined.Badge, text = order.customerName ?: "N/A")
                            DetailRow(icon = Icons.Outlined.Phone, text = order.customerPhone ?: "N/A")
                            DetailRow(icon = Icons.Outlined.LocationOn, text = order.shippingAddress ?: "N/A")
                            if (!order.selectedCity.isNullOrBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.Public,
                                    text = "Selected City: ${order.selectedCity}"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val phone = order.customerPhone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!phone.isNullOrBlank()) {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        )
                                    } else {
                                        Toast.makeText(context, "Phone number unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    if (!phone.isNullOrBlank()) {
                                        val clean = phone.filter { it.isDigit() }.takeLast(10)
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://api.whatsapp.com/send?phone=91$clean")
                                            )
                                        )
                                    } else {
                                        Toast.makeText(context, "Phone number unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Engineer Assignment Card
                item {
                    InfoCard(title = "Assigned Engineer", icon = Icons.Outlined.Engineering) {
                        val eng = order.assignedEngineer
                        if (eng != null) {
                            val engName = "${eng.firstName ?: ""} ${eng.lastName ?: ""}".trim().ifEmpty { "Engineer" }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = engName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF047857)
                                    )
                                    if (!eng.phoneNumber.isNullOrBlank()) {
                                        Text(
                                            text = eng.phoneNumber,
                                            fontSize = 13.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }
                                if (!isTerminal) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { onAssignEngineerClick(order) },
                                            enabled = !isActionInProgress,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Change", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { onUnassignEngineer(order.orderId) },
                                            enabled = !isActionInProgress,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                                            border = BorderStroke(1.dp, BrandRed),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Unassign", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (!isTerminal) {
                                Button(
                                    onClick = { onAssignEngineerClick(order) },
                                    enabled = !isActionInProgress,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Outlined.Engineering, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Assign Engineer", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("No engineer was assigned", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Internal Notes Card
                item {
                    InfoCard(title = "Internal Notes", icon = Icons.AutoMirrored.Outlined.NoteAdd) {
                        if (!order.engineerNotes.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = order.engineerNotes,
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("Add branch note...", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandRed,
                                    unfocusedBorderColor = Color(0xFFD1D5DB),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            Button(
                                onClick = {
                                    val trimmed = noteText.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onAddNote(order.orderId, trimmed) { noteText = "" }
                                    }
                                },
                                enabled = noteText.isNotBlank() && !isActionInProgress,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Order Status Actions
                if (!isTerminal) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFFE5E7EB))
                            if (nextAction != null) {
                                if (!canProceedToShipped) {
                                    Text(
                                        text = "Assign an engineer before marking this order as Shipped.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Button(
                                    onClick = { onUpdateStatus(order.orderId, nextAction.first, null) },
                                    enabled = canProceedToShipped && !isActionInProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isActionInProgress) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(nextAction.second, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                enabled = !isActionInProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                                border = BorderStroke(1.dp, BrandRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Order", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = Color.White,
            title = { Text("Cancel Order #${order.orderId.take(8)}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel this order? Stock will be restored automatically.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        label = { Text("Cancellation Reason (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            focusedLabelColor = BrandRed,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onUpdateStatus(
                            order.orderId,
                            "CANCELLED",
                            cancellationReason.trim().ifEmpty { null }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Confirm Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order", color = Color.Gray)
                }
            }
        )
    }
}

