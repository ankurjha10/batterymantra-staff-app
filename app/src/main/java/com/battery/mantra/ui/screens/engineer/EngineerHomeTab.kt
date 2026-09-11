package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.AttendanceResponse
import com.battery.mantra.data.models.LeaveRequestResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.UserResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EngineerHomeTab(
    profile: UserResponse?,
    attendance: List<AttendanceResponse>,
    activeJobs: List<OrderResponse>,
    leaves: List<LeaveRequestResponse>,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val zoneId = java.time.ZoneId.of("Asia/Kolkata")
    val now = java.time.ZonedDateTime.now(zoneId)
    val today = now.toLocalDate()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, dd MMM"))
    val todayStr = today.toString()
    val todayAttendance = attendance.firstOrNull { it.date == todayStr }

    val greeting = when (now.hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp)
    ) {
        // Personalized Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$greeting, ${profile?.name?.split(" ")?.firstOrNull() ?: "Engineer"}! \uD83D\uDC4B",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats / KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Tasks Stat
            Card(
                modifier = Modifier.weight(1f).clickable { onTabSelected(1) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${activeJobs.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Text(text = "Active Tasks", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            // Leaves Stat
            val pendingLeaves = leaves.count { it.status == "PENDING" }
            Card(
                modifier = Modifier.weight(1f).clickable { onTabSelected(3) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$pendingLeaves", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    Text(text = "Pending Leaves", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Task Widget
        Text(
            text = "Next Task",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (activeJobs.isNotEmpty()) {
            val nextJob = activeJobs.first()
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onTabSelected(1) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDEAEA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = nextJob.orderItems?.firstOrNull()?.productName ?: "Battery Check", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                        Text(text = nextJob.shippingAddress ?: "Customer Location", color = Color.DarkGray, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No active tasks right now. \uD83C\uDF89", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Compact Attendance Card
        Text(
            text = "Attendance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isPresent = todayAttendance?.status == "PRESENT"
                Column {
                    Text(
                        text = "Status: ${todayAttendance?.status ?: "PENDING"}",
                        fontWeight = FontWeight.Bold,
                        color = if (isPresent) Color(0xFF166534) else Color(0xFF334155)
                    )
                    if (isPresent && todayAttendance?.checkInTime != null) {
                        Text(text = "In: ${todayAttendance.checkInTime.substringBefore(":")}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                val activeAttendance = attendance.firstOrNull { it.checkInTime != null && it.checkOutTime == null }
                if (activeAttendance != null) {
                    Button(
                        onClick = onCheckOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Check Out", color = Color.White)
                    }
                } else if (todayAttendance != null && todayAttendance.checkOutTime != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Day Completed", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onCheckIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Check In", color = Color.White)
                    }
                }
            }
        }
    }
}
