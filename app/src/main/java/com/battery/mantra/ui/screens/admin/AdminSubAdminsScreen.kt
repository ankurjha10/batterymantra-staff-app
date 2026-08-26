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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showCreateSheet by remember { mutableStateOf(false) }

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
                onClick = { showCreateSheet = true },
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
                                SubAdminCard(subAdmin)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showCreateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreateSheet = false },
                containerColor = CardBg,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CreateSubAdminForm(
                    onSubmit = { request ->
                        onCreateSubAdmin(request, {
                            showCreateSheet = false
                            Toast.makeText(context, "Sub-Admin created successfully!", Toast.LENGTH_SHORT).show()
                        }, { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        })
                    },
                    onCancel = { showCreateSheet = false }
                )
            }
        }
    }
}

@Composable
private fun SubAdminCard(subAdmin: UserResponse) {
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
            Icon(Icons.Default.Person, contentDescription = null, tint = BrandRed, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(subAdmin.name ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(subAdmin.email ?: subAdmin.userId, fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun CreateSubAdminForm(
    onSubmit: (AdminCreateSubAdminRequest) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Permissions state
    var canManageOrders by remember { mutableStateOf(false) }
    var canManageProducts by remember { mutableStateOf(false) }
    var canManageEngineers by remember { mutableStateOf(false) }
    var canManagePartners by remember { mutableStateOf(false) }
    var canManageEnquiries by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Add New Sub-Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
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

        Text("Permissions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Permissions Checklist
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PermissionCheckbox("Manage Orders", canManageOrders) { canManageOrders = it }
            PermissionCheckbox("Manage Products", canManageProducts) { canManageProducts = it }
            PermissionCheckbox("Manage Engineers", canManageEngineers) { canManageEngineers = it }
            PermissionCheckbox("Manage Partners", canManagePartners) { canManagePartners = it }
            PermissionCheckbox("Manage Enquiries", canManageEnquiries) { canManageEnquiries = it }
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

                    val request = AdminCreateSubAdminRequest(
                        name = name,
                        phone = phone,
                        email = email,
                        password = password,
                        permissions = permissions
                    )
                    onSubmit(request)
                },
                enabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create")
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
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
