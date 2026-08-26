package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.AdminCreateCustomerRequest
import com.battery.mantra.data.models.UserResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCustomerBottomSheet(
    onDismiss: () -> Unit,
    onCreateCustomer: (AdminCreateCustomerRequest, (UserResponse) -> Unit, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && phone.isNotBlank() && addressLine1.isNotBlank() &&
            city.isNotBlank() && state.isNotBlank() && pincode.isNotBlank()

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
            Text(
                text = "Create New Customer",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fill in the customer's details below",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                label = "Email (Optional)",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            OrderFormField(
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                label = "Address Line 1 *"
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
                    label = "City *",
                    modifier = Modifier.weight(1f)
                )
                OrderFormField(
                    value = state,
                    onValueChange = { state = it },
                    label = "State *",
                    modifier = Modifier.weight(1f)
                )
                OrderFormField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = "Pincode *",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        onCreateCustomer(
                            AdminCreateCustomerRequest(
                                name = name.trim(),
                                phone = phone.trim(),
                                email = email.trim().ifBlank { null },
                                addressLine1 = addressLine1.trim(),
                                addressLine2 = addressLine2.trim().ifBlank { null },
                                city = city.trim(),
                                state = state.trim(),
                                pincode = pincode.trim()
                            ),
                            { _ ->
                                isLoading = false
                                Toast.makeText(context, "Customer created!", Toast.LENGTH_SHORT).show()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            },
                            { err ->
                                isLoading = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDE2027),
                    disabledContainerColor = Color(0xFFDE2027).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create Customer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun OrderFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF9FAFB),
            focusedBorderColor = Color(0xFFDE2027),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = Color(0xFFDE2027),
            focusedLabelColor = Color(0xFFDE2027),
            unfocusedLabelColor = Color(0xFF6B7280)
        )
    )
}
