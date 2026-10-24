package com.battery.mantra.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.PartnerResponse

@Composable
fun AdminEngineersTab(
    engineersState: AdminDataState<List<EngineerResponse>>,
    partnersState: AdminDataState<List<PartnerResponse>>,
    onCreateEngineer: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateEngineer: (String, CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onToggleEngineerStatus: (EngineerResponse, Boolean, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingEngineer by remember { mutableStateOf<EngineerResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val partnersList = (partnersState as? AdminDataState.Success)?.data ?: emptyList()
    val partnerMap = remember(partnersList) {
        partnersList.associate { it.id to (it.businessName ?: it.contactPerson ?: "Partner") }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search engineers by name, phone, city...", color = Color(0xFF757575), fontSize = 14.sp) },
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
                if (engineersState is AdminDataState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFD32F2F))
                        }
                    }
                } else if (engineersState is AdminDataState.Success) {
                    val query = searchQuery.trim().lowercase()
                    val filteredEngineers = engineersState.data.filter { eng ->
                        if (query.isEmpty()) return@filter true
                        val name = "${eng.firstName ?: ""} ${eng.lastName ?: ""}".lowercase()
                        val email = (eng.email ?: "").lowercase()
                        val phone = (eng.phoneNumber ?: "").lowercase()
                        val city = (eng.city ?: "").lowercase()
                        name.contains(query) || email.contains(query) || phone.contains(query) || city.contains(query)
                    }

                    if (filteredEngineers.isEmpty()) {
                        item {
                            Text(
                                if (searchQuery.isNotEmpty()) "No engineers matching \"$searchQuery\"" else "No engineers found", 
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredEngineers, key = { it.id }) { engineer ->
                            val assignedPartnerName = engineer.partnerId?.let { partnerMap[it] } ?: engineer.partnerBusinessName
                            EngineerCard(
                                engineer = engineer,
                                partnerName = assignedPartnerName,
                                onEditClick = {
                                    editingEngineer = engineer
                                    showAddEditDialog = true
                                },
                                onToggleStatusClick = { newStatus ->
                                    onToggleEngineerStatus(
                                        engineer,
                                        newStatus,
                                        {
                                            val msg = if (newStatus) "Engineer activated successfully" else "Engineer suspended successfully"
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
                editingEngineer = null
                showAddEditDialog = true
            },
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Engineer")
        }

        if (showAddEditDialog) {
            AddEditEngineerDialog(
                engineerToEdit = editingEngineer,
                partners = partnersList,
                onDismiss = {
                    showAddEditDialog = false
                    editingEngineer = null
                },
                onSave = { request, onSuccess, onError ->
                    if (editingEngineer != null) {
                        onUpdateEngineer(editingEngineer!!.id, request, onSuccess, onError)
                    } else {
                        onCreateEngineer(request, onSuccess, onError)
                    }
                }
            )
        }
    }
}

@Composable
fun EngineerCard(
    engineer: EngineerResponse,
    partnerName: String?,
    onEditClick: () -> Unit,
    onToggleStatusClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isActive = engineer.active == true || engineer.isActive == true
    val fullName = "${engineer.firstName ?: ""} ${engineer.lastName ?: ""}".trim().ifEmpty { "Unknown Engineer" }
    val initial = fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "E"

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
                        text = fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (!partnerName.isNullOrBlank()) "Partner: $partnerName" else "Direct Company Engineer",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
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
                        .clickable(enabled = !engineer.phoneNumber.isNullOrBlank()) {
                            engineer.phoneNumber?.let {
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
                        text = engineer.phoneNumber ?: "No phone",
                        fontSize = 13.sp,
                        color = if (!engineer.phoneNumber.isNullOrBlank()) Color(0xFF1976D2) else Color.Gray,
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
                        text = engineer.email ?: "No email",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEngineerDialog(
    engineerToEdit: EngineerResponse? = null,
    partners: List<PartnerResponse>,
    onDismiss: () -> Unit,
    onSave: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val isEditing = engineerToEdit != null
    var firstName by remember { mutableStateOf(engineerToEdit?.firstName ?: "") }
    var lastName by remember { mutableStateOf(engineerToEdit?.lastName ?: "") }
    var email by remember { mutableStateOf(engineerToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(engineerToEdit?.phoneNumber ?: "") }
    var alternatePhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedPartnerId by remember { mutableStateOf(engineerToEdit?.partnerId) }
    var isActive by remember { mutableStateOf(engineerToEdit?.let { it.active == true || it.isActive == true } ?: true) }
    
    var partnerDropdownExpanded by remember { mutableStateOf(false) }
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
                        text = if (isEditing) "Edit Engineer" else "Add Engineer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
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
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isEditing && password.isBlank()) {
                            Toast.makeText(context, "Password is required for new engineer", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isSaving = true
                        val request = CreateEngineerRequest(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phoneNumber = phone.trim(),
                            alternatePhone = alternatePhone.trim().ifEmpty { null },
                            address = address.trim().ifEmpty { null },
                            city = city.trim().ifEmpty { null },
                            password = password.trim().ifEmpty { null },
                            partnerId = selectedPartnerId,
                            isActive = isActive
                        )
                        onSave(
                            request,
                            { 
                                isSaving = false
                                Toast.makeText(context, if (isEditing) "Engineer updated" else "Engineer created", Toast.LENGTH_SHORT).show()
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
                    Text(if (isSaving) "Saving..." else if (isEditing) "Update Engineer" else "Save Engineer", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
