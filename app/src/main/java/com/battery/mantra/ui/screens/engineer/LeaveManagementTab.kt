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
    onApplyLeaveClick: (String, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ApplyLeaveSheet(
            onDismiss = { showDialog = false },
            onSubmit = { startDate, endDate, reason ->
                showDialog = false
                onApplyLeaveClick(startDate, endDate, reason)
            }
        )
    }

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
                text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.my_leaves),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apply", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.apply_leave), color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (leaves.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.no_leave_requests), color = Color.Gray)

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
                text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.reason_prefix, leave.reason),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate()
                        startDate = date.toString()
                    }
                    showStartDatePicker = false
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.ok_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.cancel_button)) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = startDatePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFFD32F2F),
                    headlineContentColor = Color(0xFFD32F2F),
                    weekdayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    todayContentColor = Color(0xFFD32F2F),
                    todayDateBorderColor = Color(0xFFD32F2F),
                    selectedDayContainerColor = Color(0xFFD32F2F),
                    selectedDayContentColor = Color.White,
                    navigationContentColor = Color.Black,
                    yearContentColor = Color.Black,
                    currentYearContentColor = Color(0xFFD32F2F)
                )
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate()
                        endDate = date.toString()
                    }
                    showEndDatePicker = false
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.ok_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.cancel_button)) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = endDatePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFFD32F2F),
                    headlineContentColor = Color(0xFFD32F2F),
                    weekdayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    todayContentColor = Color(0xFFD32F2F),
                    todayDateBorderColor = Color(0xFFD32F2F),
                    selectedDayContainerColor = Color(0xFFD32F2F),
                    selectedDayContentColor = Color.White,
                    navigationContentColor = Color.Black,
                    yearContentColor = Color.Black,
                    currentYearContentColor = Color(0xFFD32F2F)
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.apply_for_leave),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { },
                readOnly = true,
                label = { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.start_date)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Select Date")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = endDate,
                onValueChange = { },
                readOnly = true,
                label = { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.end_date)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Select Date")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.reason_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.cancel_button), color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onSubmit(startDate, endDate, reason) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.apply_leave), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
