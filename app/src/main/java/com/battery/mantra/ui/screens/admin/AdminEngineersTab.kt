package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.battery.mantra.ui.theme.TextGray
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.PartnerResponse

@Composable
fun AdminEngineersTab(
    engineersState: AdminDataState<List<EngineerResponse>>,
    partnersState: AdminDataState<List<PartnerResponse>>,
    onCreateEngineer: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPartnerId by remember { mutableStateOf<String?>(null) } // null means "All Engineers"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pills Section
            if (partnersState is AdminDataState.Success) {
                val partners = partnersState.data
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedPartnerId == null,
                            onClick = { selectedPartnerId = null },
                            label = { Text("All Engineers") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(percent = 50)
                        )
                    }
                    items(partners) { partner ->
                        FilterChip(
                            selected = selectedPartnerId == partner.id,
                            onClick = { selectedPartnerId = partner.id },
                            label = { Text(partner.businessName ?: partner.contactPerson ?: "Unknown") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(percent = 50)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (engineersState is AdminDataState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFD32F2F))
                        }
                    }
                } else if (engineersState is AdminDataState.Success) {
                    val filteredEngineers = if (selectedPartnerId == null) {
                        engineersState.data
                    } else {
                        engineersState.data.filter { it.partnerId == selectedPartnerId }
                    }

                    if (filteredEngineers.isEmpty()) {
                        item {
                            Text(
                                "No engineers found", 
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredEngineers) { engineer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val name = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}".trim()
                                Text(text = name.ifEmpty { "Unknown" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Email: ${engineer.email ?: "N/A"}", color = TextGray, fontSize = 14.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Phone: ${engineer.phoneNumber ?: "N/A"}", fontSize = 14.sp, color = Color.Black)
                                    val isActive = engineer.active == true || engineer.isActive == true
                                    Text(
                                        text = if (isActive) "ACTIVE" else "INACTIVE", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color(0xFF10B981) else Color.Red
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Engineer")
        }

        if (showAddDialog) {
            AddEngineerDialog(
                onDismiss = { showAddDialog = false },
                onSave = { request, onSuccess, onError ->
                    onCreateEngineer(request, onSuccess, onError)
                }
            )
        }
    }
}

@Composable
fun AddEngineerDialog(
    onDismiss: () -> Unit,
    onSave: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Engineer", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F))
                    )
                    Text("Active Account", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isSaving = true
                        val request = CreateEngineerRequest(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phoneNumber = phone.trim(),
                            password = password.trim(),
                            isActive = isActive
                        )
                        onSave(
                            request,
                            { 
                                isSaving = false
                                Toast.makeText(context, "Engineer Added", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            { error -> 
                                isSaving = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show() 
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save Engineer", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
