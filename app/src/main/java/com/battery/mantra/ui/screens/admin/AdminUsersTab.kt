package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.AdminUpdateCustomerRequest
import com.battery.mantra.data.models.UserResponse
import com.battery.mantra.ui.theme.TextGray

@Composable
fun AdminUsersTab(
    usersState: AdminDataState<List<UserResponse>>,
    onUpdateCustomer: (String, AdminUpdateCustomerRequest, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForEdit by remember { mutableStateOf<UserResponse?>(null) }

    val allUsers = remember(usersState) {
        if (usersState is AdminDataState.Success) usersState.data else emptyList()
    }

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                (user.name?.contains(searchQuery, ignoreCase = true) == true) ||
                        (user.phone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (user.email?.contains(searchQuery, ignoreCase = true) == true) ||
                        (user.city?.contains(searchQuery, ignoreCase = true) == true) ||
                        (user.userId.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by name, phone, or email...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFDE2027),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (usersState is AdminDataState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD32F2F))
                    }
                }
            } else if (usersState is AdminDataState.Success) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No users matching \"$searchQuery\"" else "No users found",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(filteredUsers, key = { it.userId }) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name ?: "Unknown User",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEEF2F6)
                                        ) {
                                            Text(
                                                text = user.role ?: "CUSTOMER",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { selectedUserForEdit = user },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDE2027)),
                                        border = BorderStroke(1.dp, Color(0xFFDE2027)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "Edit Customer",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(10.dp))

                                if (!user.phone.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Phone,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = user.phone, fontSize = 13.sp, color = Color(0xFF334155))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                if (!user.email.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Email,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = user.email, fontSize = 13.sp, color = TextGray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                val addressSummary = listOfNotNull(
                                    user.addressLine1,
                                    user.city,
                                    user.pincode
                                ).filter { it.isNotBlank() }.joinToString(", ")

                                if (addressSummary.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = addressSummary, fontSize = 13.sp, color = Color(0xFF475569))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedUserForEdit != null) {
        EditCustomerBottomSheet(
            customer = selectedUserForEdit!!,
            onDismiss = { selectedUserForEdit = null },
            onUpdateCustomer = onUpdateCustomer
        )
    }
}
