package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.AdminCreateSubAdminRequest
import com.battery.mantra.data.models.AdminUpdateSubAdminRequest
import com.battery.mantra.ui.screens.admin.AdminDataState
import com.battery.mantra.data.models.UserResponse

private val BrandRed = Color(0xFFDE2027)
private val SurfaceBg = Color(0xFFF8F9FA)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubAdminsScreen(
    usersState: AdminDataState<List<UserResponse>>,
    onCreateSubAdmin: (AdminCreateSubAdminRequest, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateSubAdmin: (String, AdminUpdateSubAdminRequest, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteSubAdmin: (String, () -> Unit, (String) -> Unit) -> Unit,
    onToggleStatus: (String, Boolean, () -> Unit, (String) -> Unit) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showFormSheet by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserResponse?>(null) }
    var userToDelete by remember { mutableStateOf<UserResponse?>(null) }

    val subAdmins = remember(usersState) {
        if (usersState is AdminDataState.Success) {
            usersState.data.filter { it.role == "SUB_ADMIN" }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Sub-Admins", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    userToEdit = null
                    showFormSheet = true 
                },
                containerColor = BrandRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Sub-Admin")
            }
        },
        containerColor = SurfaceBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (usersState) {
                is AdminDataState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandRed)
                }
                is AdminDataState.Error -> {
                    Text(
                        text = usersState.message,
                        color = BrandRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AdminDataState.Success -> {
                    if (subAdmins.isEmpty()) {
                        Text(
                            text = "No Sub-Admins found",
                            color = TextSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(subAdmins) { subAdmin ->
                                SubAdminCard(
                                    subAdmin = subAdmin,
                                    onEdit = {
                                        userToEdit = subAdmin
                                        showFormSheet = true
                                    },
                                    onDelete = { userToDelete = subAdmin },
                                    onToggle = { isActive ->
                                        onToggleStatus(subAdmin.userId, isActive, {
                                            Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show()
                                        }, { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showFormSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFormSheet = false },
                containerColor = CardBg,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                SubAdminForm(
                    initialUser = userToEdit,
                    onSubmitCreate = { request ->
                        onCreateSubAdmin(request, {
                            showFormSheet = false
                            Toast.makeText(context, "Sub-Admin created successfully!", Toast.LENGTH_SHORT).show()
                        }, { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        })
                    },
                    onSubmitUpdate = { userId, request ->
                        onUpdateSubAdmin(userId, request, {
                            showFormSheet = false
                            Toast.makeText(context, "Sub-Admin updated successfully!", Toast.LENGTH_SHORT).show()
                        }, { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        })
                    },
                    onCancel = { showFormSheet = false }
                )
            }
        }

        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete Sub-Admin") },
                text = { Text("Are you sure you want to delete ${user.name}? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteSubAdmin(user.userId, {
                                Toast.makeText(context, "Sub-Admin deleted", Toast.LENGTH_SHORT).show()
                                userToDelete = null
                            }, { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                userToDelete = null
                            })
                        }
                    ) {
                        Text("Delete", color = BrandRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SubAdminCard(
    subAdmin: UserResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val isActive = subAdmin.isActive ?: true
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = if(isActive) BrandRed else Color.Gray, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(subAdmin.name ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if(isActive) TextPrimary else Color.Gray)
                Text(subAdmin.email ?: subAdmin.phone ?: subAdmin.userId, fontSize = 14.sp, color = TextSecondary)
                if (!isActive) {
                    Text("Blocked", fontSize = 12.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
            }
            IconButton(onClick = { onToggle(!isActive) }) {
                if (isActive) {
                    Icon(Icons.Default.Block, contentDescription = "Block", tint = Color(0xFFE65100))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Unblock", tint = Color(0xFF2E7D32))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandRed)
            }
        }
    }
}

@Composable
private fun SubAdminForm(
    initialUser: UserResponse?,
    onSubmitCreate: (AdminCreateSubAdminRequest) -> Unit,
    onSubmitUpdate: (String, AdminUpdateSubAdminRequest) -> Unit,
    onCancel: () -> Unit
) {
    val isEdit = initialUser != null
    var name by remember { mutableStateOf(initialUser?.name ?: "") }
    var phone by remember { mutableStateOf(initialUser?.phone ?: "") }
    var email by remember { mutableStateOf(initialUser?.email ?: "") }
    var password by remember { mutableStateOf("") }

    val initialPerms = initialUser?.permissions ?: emptyList()

    // Permissions state
    var canManageOrders by remember { mutableStateOf(initialPerms.contains("MANAGE_ORDERS")) }
    var canManageProducts by remember { mutableStateOf(initialPerms.contains("MANAGE_PRODUCTS")) }
    var canManageEngineers by remember { mutableStateOf(initialPerms.contains("MANAGE_ENGINEERS")) }
    var canManagePartners by remember { mutableStateOf(initialPerms.contains("MANAGE_PARTNERS")) }
    var canManageEnquiries by remember { mutableStateOf(initialPerms.contains("MANAGE_ENQUIRIES")) }
    var canManageUsers by remember { mutableStateOf(initialPerms.contains("MANAGE_USERS")) }
    var canManageSubAdmins by remember { mutableStateOf(initialPerms.contains("MANAGE_SUB_ADMINS")) }
    var canManageCoupons by remember { mutableStateOf(initialPerms.contains("MANAGE_COUPONS")) }
    var canManageLeaves by remember { mutableStateOf(initialPerms.contains("MANAGE_LEAVES")) }
    var canManageCallbacks by remember { mutableStateOf(initialPerms.contains("MANAGE_CALLBACKS")) }

    var isSubmitting by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = BrandRed,
        unfocusedLabelColor = TextSecondary,
        cursorColor = BrandRed,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(if (isEdit) "Edit Sub-Admin" else "Add New Sub-Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isEdit) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text("Email and Password cannot be changed from here.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Permissions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Permissions Checklist
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PermissionCheckbox("Manage Orders", canManageOrders) { canManageOrders = it }
                PermissionCheckbox("Manage Products", canManageProducts) { canManageProducts = it }
                PermissionCheckbox("Manage Engineers", canManageEngineers) { canManageEngineers = it }
                PermissionCheckbox("Manage Partners", canManagePartners) { canManagePartners = it }
                PermissionCheckbox("Manage Enquiries", canManageEnquiries) { canManageEnquiries = it }
                PermissionCheckbox("Manage Users", canManageUsers) { canManageUsers = it }
                PermissionCheckbox("Manage Sub-Admins", canManageSubAdmins) { canManageSubAdmins = it }
                PermissionCheckbox("Manage Coupons", canManageCoupons) { canManageCoupons = it }
                PermissionCheckbox("Manage Leave Requests", canManageLeaves) { canManageLeaves = it }
                PermissionCheckbox("Manage Callbacks", canManageCallbacks) { canManageCallbacks = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isSubmitting = true
                        val permissions = mutableListOf<String>()
                        if (canManageOrders) permissions.add("MANAGE_ORDERS")
                        if (canManageProducts) permissions.add("MANAGE_PRODUCTS")
                        if (canManageEngineers) permissions.add("MANAGE_ENGINEERS")
                        if (canManagePartners) permissions.add("MANAGE_PARTNERS")
                        if (canManageEnquiries) permissions.add("MANAGE_ENQUIRIES")
                        if (canManageUsers) permissions.add("MANAGE_USERS")
                        if (canManageSubAdmins) permissions.add("MANAGE_SUB_ADMINS")
                        if (canManageCoupons) permissions.add("MANAGE_COUPONS")
                        if (canManageLeaves) permissions.add("MANAGE_LEAVES")
                        if (canManageCallbacks) permissions.add("MANAGE_CALLBACKS")

                        if (isEdit) {
                            onSubmitUpdate(
                                initialUser!!.userId,
                                AdminUpdateSubAdminRequest(name, phone, permissions)
                            )
                        } else {
                            onSubmitCreate(
                                AdminCreateSubAdminRequest(name, phone, email, password, "SUB_ADMIN", permissions)
                            )
                        }
                    },
                    enabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank() && (isEdit || (email.isNotBlank() && password.isNotBlank())),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (isEdit) "Update" else "Create")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionCheckbox(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = BrandRed)
        )
        Text(text = label, fontSize = 14.sp, color = TextPrimary)
    }
}
