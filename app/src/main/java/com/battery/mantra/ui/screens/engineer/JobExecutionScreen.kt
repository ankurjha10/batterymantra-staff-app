package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobExecutionScreen(
    jobId: String,
    viewModel: JobExecutionViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val step by viewModel.step.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var serialNumber by remember { mutableStateOf("") }
    var oldBatteryCollected by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var otp by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Job #$jobId".take(12), 
                        color = Color(0xFFD32F2F), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFD32F2F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            when (uiState) {
                is JobExecutionState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD32F2F))
                    }
                }
                is JobExecutionState.Error -> {
                    Text((uiState as JobExecutionState.Error).message, color = Color.Red)
                }
                is JobExecutionState.Success -> {
                    val order = (uiState as JobExecutionState.Success).order
                    
                    // Job Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Customer: ${order.customerName ?: "Unknown"}", fontWeight = FontWeight.Bold)
                            if (!order.orderItems.isNullOrEmpty()) {
                                Text("Product ID: ${order.orderItems.first().productId}")
                            }
                            Text("Phone: ${order.customerPhone ?: "No Phone"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Address: ${order.shippingAddress ?: "No address"}", color = Color.Gray)
                            Text("Amount Due: ₹${order.totalAmount}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step 1: Serial Number
                    Text("1. Enter New Battery Serial Number", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = step == 1,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (step == 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                if (serialNumber.isNotBlank()) viewModel.setStep(2)
                                else coroutineScope.launch { snackbarHostState.showSnackbar("Enter serial number") }
                            },
                            modifier = Modifier.align(Alignment.End).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step 2: Old Battery & Payment
                    if (step >= 2) {
                        Text("2. Old Battery & Payment", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = oldBatteryCollected,
                                onCheckedChange = { oldBatteryCollected = it },
                                enabled = step == 2
                            )
                            Text("Old Battery Collected (Scrap Discount applied)")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Payment Collection Mode:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = paymentMode == "CASH", onClick = { paymentMode = "CASH" }, enabled = step == 2)
                            Text("Cash")
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = paymentMode == "UPI", onClick = { paymentMode = "UPI" }, enabled = step == 2)
                            Text("UPI / Online")
                        }

                        if (step == 2) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.setStep(3) },
                                modifier = Modifier.align(Alignment.End).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Next", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Step 3: OTP Verification
                    if (step >= 3) {
                        Text("3. Customer OTP Verification", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!otpSent) {
                            Button(
                                onClick = { 
                                    viewModel.sendOtp { msg -> 
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    } 
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Send OTP to Customer", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedTextField(
                                value = otp,
                                onValueChange = { otp = it },
                                label = { Text("Enter OTP from Customer") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { 
                                viewModel.sendOtp { msg -> 
                                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                } 
                            }) {
                                Text("Resend OTP", color = Color(0xFF1976D2))
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    viewModel.completeJob(serialNumber, oldBatteryCollected, paymentMode, otp) { success, msg ->
                                        if (success) {
                                            onBackClick() // Go back on success
                                        } else {
                                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    }
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Mark Job as Completed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
