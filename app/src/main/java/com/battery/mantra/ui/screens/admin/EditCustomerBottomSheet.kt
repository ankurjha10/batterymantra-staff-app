package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.AdminUpdateCustomerRequest
import com.battery.mantra.data.models.UserResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerBottomSheet(
    customer: UserResponse,
    onDismiss: () -> Unit,
    onUpdateCustomer: (String, AdminUpdateCustomerRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf(customer.name ?: "") }
    var phone by remember { mutableStateOf(customer.phone ?: "") }
    var email by remember { mutableStateOf(customer.email ?: "") }
    var addressLine1 by remember { mutableStateOf(customer.addressLine1 ?: "") }
    var addressLine2 by remember { mutableStateOf(customer.addressLine2 ?: "") }
    var city by remember { mutableStateOf(customer.city ?: "") }
    var state by remember { mutableStateOf(customer.state ?: "") }
    var pincode by remember { mutableStateOf(customer.pincode ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && phone.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFFDE2027),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Edit Customer Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Update profile & address details",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                IconButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name & Phone Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrderFormField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name *",
                    modifier = Modifier.weight(1f)
                )
                OrderFormField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone *",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OrderFormField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            OrderFormField(
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                label = "Address Line 1"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OrderFormField(
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                label = "Address Line 2 (Optional)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // City, State, Pincode Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrderFormField(
                    value = city,
                    onValueChange = { city = it },
                    label = "City",
                    modifier = Modifier.weight(1f)
                )
                OrderFormField(
                    value = state,
                    onValueChange = { state = it },
                    label = "State",
                    modifier = Modifier.weight(1f)
                )
                OrderFormField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = "Pincode",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        val request = AdminUpdateCustomerRequest(
                            name = name.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                            email = email.trim().ifBlank { null },
                            addressLine1 = addressLine1.trim().ifBlank { null },
                            addressLine2 = addressLine2.trim().ifBlank { null },
                            city = city.trim().ifBlank { null },
                            state = state.trim().ifBlank { null },
                            pincode = pincode.trim().ifBlank { null }
                        )
                        onUpdateCustomer(
                            customer.userId,
                            request,
                            {
                                isLoading = false
                                Toast.makeText(context, "Customer updated successfully!", Toast.LENGTH_SHORT).show()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            },
                            { err ->
                                isLoading = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDE2027),
                    disabledContainerColor = Color(0xFFDE2027).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}
