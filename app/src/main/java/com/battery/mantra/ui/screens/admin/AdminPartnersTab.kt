package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.CreatePartnerRequest

@Composable
fun AdminPartnersTab(
    partnersState: AdminDataState<List<PartnerResponse>>,
    citiesState: AdminDataState<List<com.battery.mantra.data.models.CityResponse>>,
    onCreatePartner: (CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (partnersState is AdminDataState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD32F2F))
                    }
                }
            } else if (partnersState is AdminDataState.Success) {
                if (partnersState.data.isEmpty()) {
                    item {
                        Text(
                            "No partners found", 
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(partnersState.data) { partner ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = partner.businessName ?: partner.contactPerson ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Email: ${partner.email ?: "N/A"}", color = TextGray, fontSize = 14.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Phone: ${partner.phoneNumber ?: "N/A"}", fontSize = 14.sp, color = Color.Black)
                                    val isActive = partner.active == true || partner.isActive == true
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Partner")
        }

        if (showAddDialog) {
            AddPartnerDialog(
                citiesState = citiesState,
                onDismiss = { showAddDialog = false },
                onSave = { request, onSuccess, onError ->
                    onCreatePartner(request, onSuccess, onError)
                }
            )
        }
    }
}

@Composable
fun AddPartnerDialog(
    citiesState: AdminDataState<List<com.battery.mantra.data.models.CityResponse>>,
    onDismiss: () -> Unit,
    onSave: (CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var alternatePhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    val availableCities = if (citiesState is AdminDataState.Success) citiesState.data else emptyList()
    var selectedCityIds by remember { mutableStateOf(setOf<String>()) }
    
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
                    Text("Add Partner", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person") },
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
                    value = alternatePhone,
                    onValueChange = { alternatePhone = it },
                    label = { Text("Alternate Phone (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Business Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
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
                
                Text("Operating Cities", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Select the cities this partner will manage.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid using LazyVerticalGrid inside a fixed height box or FlowRow
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (citiesState is AdminDataState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = Color(0xFFD32F2F))
                    } else if (availableCities.isEmpty()) {
                        Text("No cities available", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        availableCities.forEach { city ->
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCityIds.contains(city.cityId),
                                    onCheckedChange = { checked ->
                                        selectedCityIds = if (checked) {
                                            selectedCityIds + city.cityId
                                        } else {
                                            selectedCityIds - city.cityId
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F))
                                )
                                Text(text = city.cityName, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }

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
                        if (businessName.isBlank() || contactPerson.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isSaving = true
                        val request = CreatePartnerRequest(
                            businessName = businessName.trim(),
                            contactPerson = contactPerson.trim(),
                            email = email.trim(),
                            phoneNumber = phone.trim(),
                            alternatePhone = alternatePhone.trim().ifEmpty { null },
                            address = address.trim().ifEmpty { null },
                            password = password.trim(),
                            operatingCityIds = selectedCityIds.toList().ifEmpty { null },
                            isActive = isActive
                        )
                        onSave(
                            request,
                            {
                                isSaving = false
                                Toast.makeText(context, "Partner Added", Toast.LENGTH_SHORT).show()
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
                    Text(if (isSaving) "Saving..." else "Save Partner", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
