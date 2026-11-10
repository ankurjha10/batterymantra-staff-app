package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.ui.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignEngineerSheet(
    order: OrderResponse,
    engineersState: PartnerDataState<List<EngineerResponse>>,
    isSubmitting: Boolean = false,
    onAssignEngineer: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val activeEngineers = remember(engineersState, searchQuery) {
        val list = (engineersState as? PartnerDataState.Success)?.data.orEmpty()
            .filter { it.isActive == true || it.active == true }
            .sortedWith(
                compareBy<EngineerResponse> {
                    when (it.dutyStatus?.uppercase()) {
                        "ON_DUTY" -> 0
                        "ON_JOB" -> 1
                        else -> 2
                    }
                }.thenBy { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
            )
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) list
        else list.filter { eng ->
            val name = "${eng.firstName ?: ""} ${eng.lastName ?: ""}".lowercase()
            name.contains(query) ||
                (eng.phoneNumber ?: "").lowercase().contains(query) ||
                (eng.city ?: "").lowercase().contains(query)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assign Engineer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Order #${order.orderId.take(8)} • ${order.customerName ?: "Customer"}",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search engineer by name or phone...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (engineersState) {
                is PartnerDataState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandRed)
                    }
                }
                is PartnerDataState.Error -> {
                    Text(
                        text = engineersState.message,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    if (activeEngineers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No active engineers available in your branch." else "No engineers matching \"$searchQuery\"",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(activeEngineers, key = { it.id }) { engineer ->
                                val isCurrentlyAssigned = order.assignedEngineer?.id == engineer.id
                                val fullName = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}"
                                    .trim()
                                    .ifEmpty { engineer.email ?: "Engineer" }
                                val (dutyLabel, dutyBg, dutyText) = when (engineer.dutyStatus?.uppercase()) {
                                    "ON_DUTY" -> Triple("ON DUTY", Color(0xFFD1FAE5), Color(0xFF047857))
                                    "ON_JOB" -> Triple("ON JOB", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                                    else -> Triple("OFF DUTY", Color(0xFFF3F4F6), Color(0xFF4B5563))
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isSubmitting) { onAssignEngineer(engineer.id) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentlyAssigned) Color(0xFFFEF2F2) else Color.White
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrentlyAssigned) BrandRed else Color(0xFFE5E7EB)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "E",
                                                fontWeight = FontWeight.Bold,
                                                color = BrandRed,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = Color(0xFF111827)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = dutyBg
                                                ) {
                                                    Text(
                                                        text = dutyLabel,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = dutyText,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = listOfNotNull(
                                                    engineer.phoneNumber,
                                                    engineer.city?.takeIf { it.isNotBlank() }
                                                ).joinToString(" • "),
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                        if (isCurrentlyAssigned) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Assigned",
                                                tint = BrandRed,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else if (isSubmitting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = BrandRed
                                            )
                                        } else {
                                            Text(
                                                text = "Assign",
                                                color = BrandRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
