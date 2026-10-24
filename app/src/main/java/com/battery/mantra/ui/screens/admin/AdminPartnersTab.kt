package com.battery.mantra.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.battery.mantra.data.models.CreatePartnerRequest
import com.battery.mantra.data.models.PartnerResponse

@Composable
fun AdminPartnersTab(
    partnersState: AdminDataState<List<PartnerResponse>>,
    citiesState: AdminDataState<List<com.battery.mantra.data.models.CityResponse>>,
    onCreatePartner: (CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onUpdatePartner: (String, CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onTogglePartnerStatus: (PartnerResponse, Boolean, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingPartner by remember { mutableStateOf<PartnerResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search partners by name, phone, city...", color = Color(0xFF757575), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color(0xFFD32F2F))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF757575))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFFD32F2F)
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (partnersState is AdminDataState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFD32F2F))
                        }
                    }
                } else if (partnersState is AdminDataState.Success) {
                    val query = searchQuery.trim().lowercase()
                    val filteredPartners = partnersState.data.filter { p ->
                        if (query.isEmpty()) return@filter true
                        val bName = (p.businessName ?: "").lowercase()
                        val cPerson = (p.contactPerson ?: "").lowercase()
                        val email = (p.email ?: "").lowercase()
                        val phone = (p.phoneNumber ?: "").lowercase()
                        val address = (p.address ?: "").lowercase()
                        bName.contains(query) || cPerson.contains(query) || email.contains(query) || phone.contains(query) || address.contains(query)
                    }

                    if (filteredPartners.isEmpty()) {
                        item {
                            Text(
                                if (searchQuery.isNotEmpty()) "No partners matching \"$searchQuery\"" else "No partners found", 
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredPartners, key = { it.id }) { partner ->
                            PartnerCard(
                                partner = partner,
                                onEditClick = {
                                    editingPartner = partner
                                    showAddEditDialog = true
                                },
                                onToggleStatusClick = { newStatus ->
                                    onTogglePartnerStatus(
                                        partner,
                                        newStatus,
                                        {
                                            val msg = if (newStatus) "Partner activated successfully" else "Partner suspended successfully"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        },
                                        { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingPartner = null
                showAddEditDialog = true
            },
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Partner")
        }

        if (showAddEditDialog) {
            AddEditPartnerDialog(
                partnerToEdit = editingPartner,
                citiesState = citiesState,
                onDismiss = {
                    showAddEditDialog = false
                    editingPartner = null
                },
                onSave = { request, onSuccess, onError ->
                    if (editingPartner != null) {
                        onUpdatePartner(editingPartner!!.id, request, onSuccess, onError)
                    } else {
                        onCreatePartner(request, onSuccess, onError)
                    }
                }
            )
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerResponse,
    onEditClick: () -> Unit,
    onToggleStatusClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isActive = partner.active == true || partner.isActive == true
    val displayName = partner.businessName?.takeIf { it.isNotBlank() }
        ?: partner.contactPerson?.takeIf { it.isNotBlank() }
        ?: "Unknown Partner"
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name & Status Chip
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
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    if (!partner.contactPerson.isNullOrBlank() && partner.contactPerson != partner.businessName) {
                        Text(
                            text = "Contact: ${partner.contactPerson}",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Active / Suspended Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isActive) "ACTIVE" else "SUSPENDED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Details: Phone & Email
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !partner.phoneNumber.isNullOrBlank()) {
                            partner.phoneNumber?.let {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
                                context.startActivity(intent)
                            }
                        }
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = partner.phoneNumber ?: "No phone",
                        fontSize = 13.sp,
                        color = if (!partner.phoneNumber.isNullOrBlank()) Color(0xFF1976D2) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = partner.email ?: "No email",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Edit and Suspend / Activate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF90CAF9))),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isActive) {
                    OutlinedButton(
                        onClick = { onToggleStatusClick(false) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Suspend", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onToggleStatusClick(true) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF86EFAC))),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Activate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditPartnerDialog(
    partnerToEdit: PartnerResponse? = null,
    citiesState: AdminDataState<List<com.battery.mantra.data.models.CityResponse>>,
    onDismiss: () -> Unit,
    onSave: (CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val isEditing = partnerToEdit != null
    var businessName by remember { mutableStateOf(partnerToEdit?.businessName ?: "") }
    var contactPerson by remember { mutableStateOf(partnerToEdit?.contactPerson ?: "") }
    var email by remember { mutableStateOf(partnerToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(partnerToEdit?.phoneNumber ?: "") }
    var alternatePhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(partnerToEdit?.let { it.active == true || it.isActive == true } ?: true) }
    
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
                .fillMaxWidth(0.92f)
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
                    Text(
                        text = if (isEditing) "Edit Partner" else "Add Partner",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedLabelColor = Color(0xFF616161),
                    cursorColor = Color(0xFFD32F2F)
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = alternatePhone,
                    onValueChange = { alternatePhone = it },
                    label = { Text("Alternate Phone (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Business Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEditing) "Password (leave blank to keep current)" else "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Operating Cities", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Select the cities this partner will manage.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
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
                        if (businessName.isBlank() || contactPerson.isBlank() || email.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isEditing && password.isBlank()) {
                            Toast.makeText(context, "Password is required for new partner", Toast.LENGTH_SHORT).show()
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
                            password = password.trim().ifEmpty { null },
                            operatingCityIds = selectedCityIds.toList().ifEmpty { null },
                            isActive = isActive
                        )
                        onSave(
                            request,
                            {
                                isSaving = false
                                Toast.makeText(context, if (isEditing) "Partner updated" else "Partner created", Toast.LENGTH_SHORT).show()
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
                    Text(if (isSaving) "Saving..." else if (isEditing) "Update Partner" else "Save Partner", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
