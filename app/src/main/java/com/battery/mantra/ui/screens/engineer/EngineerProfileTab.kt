package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.UserResponse
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

@Composable
fun EngineerProfileTab(
    profile: UserResponse?,
    historyJobs: List<OrderResponse>,
    onChangePasswordClick: () -> Unit,
    onChangeLanguageClick: () -> Unit
) {
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFD32F2F))
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initial = profile.name?.firstOrNull()?.toString()?.uppercase()
            if (initial != null) {
                Text(text = initial, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = profile.name ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.unknown_engineer),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val isActive = profile.isActive == true || profile.active == true
        Badge(
            containerColor = if (isActive) Color(0xFF2E7D32) else Color(0xFFD32F2F),
            contentColor = Color.White
        ) {
            Text(
                text = if (isActive) androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.active) else androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.inactive),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Professional Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.professional_details), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileDetailRow(icon = Icons.Default.Badge, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.employee_id), value = profile.userId)
                Spacer(modifier = Modifier.height(12.dp))
                
                val notApplicableString = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.not_applicable)
                val joinedDate = try {
                    if (!profile.createdAt.isNullOrBlank()) {
                        ZonedDateTime.parse(profile.createdAt).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    } else notApplicableString
                } catch (e: Exception) {
                    notApplicableString
                }
                ProfileDetailRow(icon = Icons.Default.DateRange, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.joined_on), value = joinedDate)
                Spacer(modifier = Modifier.height(12.dp))
                
                val completedJobs = profile.totalJobsCompleted ?: historyJobs.filter { it.orderStatus == "COMPLETED" || it.orderStatus == "DELIVERED" || it.orderStatus == "INSTALLED" }.size
                ProfileDetailRow(icon = Icons.Default.Work, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.total_jobs_completed), value = "$completedJobs")
                Spacer(modifier = Modifier.height(12.dp))
                
                ProfileDetailRow(icon = Icons.Default.LocationCity, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.assigned_hub), value = profile.assignedPartnerName ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.not_applicable))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Contact Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.contact_details), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileDetailRow(icon = Icons.Default.Phone, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.phone), value = profile.phone ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.not_provided))
                Spacer(modifier = Modifier.height(12.dp))
                ProfileDetailRow(icon = Icons.Default.Email, label = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.email), value = profile.email ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.not_provided))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.settings), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp, start = 8.dp))
                
                SettingsRow(icon = Icons.Default.Lock, title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.change_password), onClick = onChangePasswordClick)
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsRow(icon = Icons.Default.Language, title = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.app_language), onClick = onChangeLanguageClick)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}
