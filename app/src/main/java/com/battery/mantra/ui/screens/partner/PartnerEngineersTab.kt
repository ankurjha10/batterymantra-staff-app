package com.battery.mantra.ui.screens.partner

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun PartnerEngineersTab(
    engineersState: PartnerDataState<List<EngineerResponse>>,
    onRetry: () -> Unit,
    onCreateEngineer: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateEngineer: (String, CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onToggleEngineerStatus: (EngineerResponse, Boolean, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingEngineer by remember { mutableStateOf<EngineerResponse?>(null) }
    var engineerToSuspend by remember { mutableStateOf<EngineerResponse?>(null) }

    val allEngineers = (engineersState as? PartnerDataState.Success)?.data.orEmpty()
    val filters = listOf("All", "On Duty", "Active", "Suspended")

    val filteredEngineers = remember(allEngineers, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        allEngineers.filter { eng ->
            val isActive = eng.isActive == true || eng.active == true
            val matchesFilter = when (selectedFilter) {
                "On Duty" -> isActive && eng.dutyStatus.equals("ON_DUTY", ignoreCase = true)
                "Active" -> isActive
                "Suspended" -> !isActive
                else -> true
            }
            val matchesSearch = if (q.isEmpty()) true else {
                val fullName = "${eng.firstName ?: ""} ${eng.lastName ?: ""}".lowercase()
                fullName.contains(q) ||
                    (eng.phoneNumber ?: "").lowercase().contains(q) ||
                    (eng.email ?: "").lowercase().contains(q) ||
                    (eng.city ?: "").lowercase().contains(q)
            }
            matchesFilter && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search engineers by name, phone, city...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = BrandRed) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = BrandRed
                )
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val count = when (filter) {
                        "All" -> allEngineers.size
                        "On Duty" -> allEngineers.count {
                            (it.isActive == true || it.active == true) &&
                                it.dutyStatus.equals("ON_DUTY", ignoreCase = true)
                        }
                        "Active" -> allEngineers.count { it.isActive == true || it.active == true }
                        "Suspended" -> allEngineers.count { !(it.isActive == true || it.active == true) }
                        else -> 0
                    }
                    val isSelected = selectedFilter == filter
                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFFFEE2E2) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) BrandRed else Color(0xFFD1D5DB)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = "$filter ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFB91C1C) else Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }

            when (engineersState) {
                is PartnerDataState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandRed)
                    }
                }
                is PartnerDataState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(engineersState.message, color = Color(0xFFB91C1C), textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(onClick = onRetry) {
                                Text("Retry", color = BrandRed)
                            }
                        }
                    }
                }
                is PartnerDataState.Success -> {
                    if (filteredEngineers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedFilter != "All") {
                                    "No engineers matching criteria."
                                } else {
                                    "No engineers added to your branch yet.\nTap + to add your first technician."
                                },
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredEngineers, key = { it.id }) { engineer ->
                                PartnerEngineerCard(
                                    engineer = engineer,
                                    onEditClick = {
                                        editingEngineer = engineer
                                        showAddEditDialog = true
                                    },
                                    onToggleStatusClick = { newStatus ->
                                        if (!newStatus) {
                                            engineerToSuspend = engineer
                                        } else {
                                            onToggleEngineerStatus(
                                                engineer,
                                                true,
                                                { Toast.makeText(context, "Engineer activated", Toast.LENGTH_SHORT).show() },
                                                { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                            )
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

        FloatingActionButton(
            onClick = {
                editingEngineer = null
                showAddEditDialog = true
            },
            containerColor = BrandRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Engineer")
        }
    }

    if (showAddEditDialog) {
        AddEngineerDialog(
            engineerToEdit = editingEngineer,
            onDismiss = {
                showAddEditDialog = false
                editingEngineer = null
            },
            onSave = { request, onSuccess, onError ->
                val target = editingEngineer
                if (target != null) {
                    onUpdateEngineer(target.id, request, onSuccess, onError)
                } else {
                    onCreateEngineer(request, onSuccess, onError)
                }
            }
        )
    }

    engineerToSuspend?.let { target ->
        val fullName = "${target.firstName ?: ""} ${target.lastName ?: ""}".trim().ifEmpty { "this engineer" }
        AlertDialog(
            onDismissRequest = { engineerToSuspend = null },
            containerColor = Color.White,
            title = { Text("Suspend Engineer?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to suspend $fullName? They won't be able to log in or receive new assignments.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toSuspend = target
                        engineerToSuspend = null
                        onToggleEngineerStatus(
                            toSuspend,
                            false,
                            { Toast.makeText(context, "Engineer suspended", Toast.LENGTH_SHORT).show() },
                            { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Suspend", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { engineerToSuspend = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun PartnerEngineerCard(
    engineer: EngineerResponse,
    onEditClick: () -> Unit,
    onToggleStatusClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isActive = engineer.isActive == true || engineer.active == true
    val fullName = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}".trim().ifEmpty { "Engineer" }
    val initial = fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "E"

    val (dutyLabel, dutyBg, dutyText) = when (engineer.dutyStatus?.uppercase()) {
        "ON_DUTY" -> Triple("ON DUTY", Color(0xFFD1FAE5), Color(0xFF047857))
        "ON_JOB" -> Triple("ON JOB", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        else -> Triple("OFF DUTY", Color(0xFFF3F4F6), Color(0xFF4B5563))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (isActive) "ACTIVE" else "SUSPENDED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = dutyBg
                            ) {
                                Text(
                                    text = dutyLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dutyText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !engineer.phoneNumber.isNullOrBlank()) {
                        engineer.phoneNumber?.let {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                        }
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = engineer.phoneNumber ?: "No phone",
                        fontSize = 13.sp,
                        color = if (!engineer.phoneNumber.isNullOrBlank()) Color(0xFF1976D2) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = engineer.email ?: "No email",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }

                if (!engineer.city.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = engineer.city,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2)),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isActive) {
                    OutlinedButton(
                        onClick = { onToggleStatusClick(false) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Suspend", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onToggleStatusClick(true) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Activate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
