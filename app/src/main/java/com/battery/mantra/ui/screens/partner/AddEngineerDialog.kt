package com.battery.mantra.ui.screens.partner

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun AddEngineerDialog(
    engineerToEdit: EngineerResponse? = null,
    onDismiss: () -> Unit,
    onSave: (CreateEngineerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val isEditing = engineerToEdit != null
    var firstName by remember { mutableStateOf(engineerToEdit?.firstName ?: "") }
    var lastName by remember { mutableStateOf(engineerToEdit?.lastName ?: "") }
    var email by remember { mutableStateOf(engineerToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(engineerToEdit?.phoneNumber ?: "") }
    var alternatePhone by remember { mutableStateOf(engineerToEdit?.alternatePhone ?: "") }
    var city by remember { mutableStateOf(engineerToEdit?.city ?: "") }
    var address by remember { mutableStateOf(engineerToEdit?.address ?: "") }
    var password by remember { mutableStateOf("") }
    var isActive by remember {
        mutableStateOf(engineerToEdit?.let { it.isActive == true || it.active == true } ?: true)
    }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = BrandRed,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedLabelColor = BrandRed,
        unfocusedLabelColor = Color(0xFF616161),
        cursorColor = BrandRed
    )

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
                        text = if (isEditing) "Edit Engineer" else "Add New Engineer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Login Username) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alternatePhone,
                    onValueChange = { alternatePhone = it },
                    label = { Text("Alternate Phone (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(if (isEditing) "New Password (optional)" else "Password *")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )

                if (isEditing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandRed)
                        )
                        Text("Active Account", color = Color.Black, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
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
                            isActive = isActive
                        )
                        onSave(
                            request,
                            {
                                isSaving = false
                                Toast.makeText(
                                    context,
                                    if (isEditing) "Engineer updated" else "Engineer added",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            },
                            { err ->
                                isSaving = false
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else if (isEditing) "Update Engineer" else "Add Engineer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
