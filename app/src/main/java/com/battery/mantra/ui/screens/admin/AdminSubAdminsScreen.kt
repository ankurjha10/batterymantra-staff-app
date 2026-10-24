package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
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
import com.battery.mantra.data.models.UserResponse

private val BrandRed = Color(0xFFD32F2F)
private val SurfaceBg = Color(0xFFF8F9FA)
private val CardBg = Color.White
private val TextPrimary = Color.Black
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
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showFormSheet by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserResponse?>(null) }
    var userToDelete by remember { mutableStateOf<UserResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val subAdmins = remember(usersState) {
        if (usersState is AdminDataState.Success) {
            usersState.data.filter { it.role == "SUB_ADMIN" }
        } else {
            emptyList()
        }
    }

    val filteredSubAdmins = remember(subAdmins, searchQuery) {
        if (searchQuery.isBlank()) {
            subAdmins
        } else {
            val q = searchQuery.trim().lowercase()
            subAdmins.filter {
                (it.name?.lowercase()?.contains(q) == true) ||
                (it.email?.lowercase()?.contains(q) == true) ||
                (it.phone?.lowercase()?.contains(q) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Sub-Admins", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.Black)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, phone or email...", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = BrandRed)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    cursorColor = BrandRed,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (usersState) {
                    is AdminDataState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandRed)
                    }
                    is AdminDataState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to Load Sub-Admins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = usersState.message,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                    is AdminDataState.Success -> {
                        if (filteredSubAdmins.isEmpty()) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No sub-admins match \"$searchQuery\"" else "No Sub-Admins found",
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredSubAdmins, key = { it.userId.ifEmpty { it.phone ?: it.email ?: it.hashCode().toString() } }) { subAdmin ->
                                    SubAdminCard(
                                        subAdmin = subAdmin,
                                        onEdit = {
                                            userToEdit = subAdmin
                                            showFormSheet = true
                                        },
                                        onDelete = { userToDelete = subAdmin },
                                        onToggle = { isActive ->
                                            onToggleStatus(subAdmin.userId, isActive, {
                                                Toast.makeText(context, if (isActive) "Sub-Admin activated" else "Sub-Admin suspended", Toast.LENGTH_SHORT).show()
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
        }

        if (showFormSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFormSheet = false },
                containerColor = CardBg,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                SubAdminForm(
                    initialUser = userToEdit,
                    onSubmitCreate = { request, onResult ->
                        onCreateSubAdmin(request, {
                            onResult(true)
                            showFormSheet = false
                            Toast.makeText(context, "Sub-Admin created successfully!", Toast.LENGTH_SHORT).show()
                        }, { err ->
                            onResult(false)
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        })
                    },
                    onSubmitUpdate = { userId, request, onResult ->
                        onUpdateSubAdmin(userId, request, {
                            onResult(true)
                            showFormSheet = false
                            Toast.makeText(context, "Sub-Admin updated successfully!", Toast.LENGTH_SHORT).show()
                        }, { err ->
                            onResult(false)
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
                title = { Text("Delete Sub-Admin", fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text("Are you sure you want to delete ${user.name ?: "this sub-admin"}? This action cannot be undone.", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteSubAdmin(user.userId, {
                                Toast.makeText(context, "Sub-Admin deleted", Toast.LENGTH_SHORT).show()
                                userToDelete = null
                            }, { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                userToDelete = null
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { userToDelete = null }) {
                        Text("Cancel", color = Color.Black)
                    }
                },
                containerColor = CardBg
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubAdminCard(
    subAdmin: UserResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val isActive = (subAdmin.isActive == true) || (subAdmin.active == true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isActive) BrandRed.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isActive) BrandRed else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subAdmin.name?.ifBlank { "Sub-Admin" } ?: "Sub-Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isActive) Color.Black else Color.Gray
                    )
                    
                    if (!subAdmin.phone.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(subAdmin.phone, fontSize = 13.sp, color = Color(0xFF374151))
                        }
                    }

                    if (!subAdmin.email.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(subAdmin.email, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
                    }
                    IconButton(onClick = { onToggle(!isActive) }) {
                        if (isActive) {
                            Icon(Icons.Default.Block, contentDescription = "Suspend", tint = Color(0xFFE65100))
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Activate", tint = Color(0xFF2E7D32))
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Badge & Permissions Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "SUSPENDED",
                        color = if (isActive) Color(0xFF2E7D32) else BrandRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val perms = subAdmin.permissions ?: emptyList()
                if (perms.isNotEmpty()) {
                    Text(
                        text = "${perms.size} permissions assigned",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Show permission chips if any
            val perms = subAdmin.permissions ?: emptyList()
            if (perms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    perms.forEach { perm ->
                        val label = perm.removePrefix("MANAGE_").replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubAdminForm(
    initialUser: UserResponse?,
    onSubmitCreate: (AdminCreateSubAdminRequest, (Boolean) -> Unit) -> Unit,
    onSubmitUpdate: (String, AdminUpdateSubAdminRequest, (Boolean) -> Unit) -> Unit,
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
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(
                if (isEdit) "Edit Sub-Admin" else "Add New Sub-Admin",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
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
                label = { Text("Phone Number") },
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
                    label = { Text("Email Address") },
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
                Text(
                    "Email and Password cannot be changed from this screen.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Module Permissions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Permissions Checklist
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PermissionCheckbox("Manage Orders", canManageOrders) { canManageOrders = it }
                PermissionCheckbox("Manage Products & Inventory", canManageProducts) { canManageProducts = it }
                PermissionCheckbox("Manage Engineers", canManageEngineers) { canManageEngineers = it }
                PermissionCheckbox("Manage Partners", canManagePartners) { canManagePartners = it }
                PermissionCheckbox("Manage Customer Enquiries", canManageEnquiries) { canManageEnquiries = it }
                PermissionCheckbox("Manage Users & Customers", canManageUsers) { canManageUsers = it }
                PermissionCheckbox("Manage Sub-Admins", canManageSubAdmins) { canManageSubAdmins = it }
                PermissionCheckbox("Manage Coupons & Offers", canManageCoupons) { canManageCoupons = it }
                PermissionCheckbox("Manage Leave Requests", canManageLeaves) { canManageLeaves = it }
                PermissionCheckbox("Manage Callbacks", canManageCallbacks) { canManageCallbacks = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSubmitting
                ) {
                    Text("Cancel", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
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
                                AdminUpdateSubAdminRequest(name.trim(), phone.trim(), permissions)
                            ) { success ->
                                if (!success) isSubmitting = false
                            }
                        } else {
                            onSubmitCreate(
                                AdminCreateSubAdminRequest(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    password = password,
                                    role = "SUB_ADMIN",
                                    permissions = permissions
                                )
                            ) { success ->
                                if (!success) isSubmitting = false
                            }
                        }
                    },
                    enabled = !isSubmitting && name.isNotBlank() && phone.trim().length >= 10 && (isEdit || (email.isNotBlank() && email.contains("@") && password.isNotBlank())),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (isEdit) "Update Sub-Admin" else "Create Sub-Admin")
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
        Text(text = label, fontSize = 14.sp, color = Color.Black)
    }
}
