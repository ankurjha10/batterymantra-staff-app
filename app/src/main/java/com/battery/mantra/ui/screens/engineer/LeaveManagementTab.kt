package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.LeaveRequestResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementTab(
    leaves: List<LeaveRequestResponse>,
    onApplyLeaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Leaves",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Button(
                onClick = onApplyLeaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apply")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply Leave")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (leaves.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No leave requests found", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(leaves) { leave ->
                    LeaveCard(leave)
                }
            }
        }
    }
}

@Composable
fun LeaveCard(leave: LeaveRequestResponse) {
    val statusColor = when (leave.status.uppercase()) {
        "APPROVED" -> Color(0xFF2E7D32)
        "REJECTED" -> Color(0xFFC62828)
        else -> Color(0xFFF57C00) // PENDING
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${leave.startDate} to ${leave.endDate}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = leave.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reason: ${leave.reason}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}
