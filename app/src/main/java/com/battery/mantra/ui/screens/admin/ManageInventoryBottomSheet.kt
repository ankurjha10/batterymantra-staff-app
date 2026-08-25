package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInventoryBottomSheet(
    engineerId: String,
    engineerName: String,
    onDismiss: () -> Unit,
    onLoad: (String, Int, () -> Unit, (String) -> Unit) -> Unit,
    onUnload: (String, Int, () -> Unit, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var productId by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Manage Inventory",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Engineer: $engineerName",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = productId,
                onValueChange = { productId = it },
                label = { Text("Product ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val qty = quantity.toIntOrNull() ?: 0
                        if (productId.isNotBlank() && qty > 0) {
                            isLoading = true
                            onUnload(productId, qty, {
                                isLoading = false
                                Toast.makeText(context, "Unloaded successfully", Toast.LENGTH_SHORT).show()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            }, { err ->
                                isLoading = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    enabled = !isLoading
                ) {
                    Text("Unload (Take)")
                }
                
                Button(
                    onClick = {
                        val qty = quantity.toIntOrNull() ?: 0
                        if (productId.isNotBlank() && qty > 0) {
                            isLoading = true
                            onLoad(productId, qty, {
                                isLoading = false
                                Toast.makeText(context, "Loaded successfully", Toast.LENGTH_SHORT).show()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            }, { err ->
                                isLoading = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = !isLoading
                ) {
                    Text("Load (Give)")
                }
            }
        }
    }
}
