package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("FunctionName")
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

    var showQrDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    var serialNumber by remember { mutableStateOf("N/A") }
    var oldBatteryCollected by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var otp by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.job_number)}$jobId".take(12), 
                        color = Color.Black, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                    
                    // Premium Job Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Product Info Header
                            if (!order.orderItems.isNullOrEmpty()) {
                                val item = order.orderItems.first()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val imageModifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF1F5F9))

                                    if (!item.productImage.isNullOrEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                .data(item.productImage)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Product Image",
                                            modifier = imageModifier,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = imageModifier,
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(androidx.compose.material.icons.Icons.Outlined.Inventory2, contentDescription = null, tint = Color.LightGray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = item.productName ?: "${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.product_id_prefix)} ${item.productId.take(8)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.qty_prefix)} ${item.quantity}", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Customer Details
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(androidx.compose.material.icons.Icons.Outlined.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(order.customerName ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.unknown_customer), fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                    Text(order.customerPhone ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.no_phone), fontSize = 13.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(androidx.compose.material.icons.Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.shippingAddress ?: androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.no_address_provided), fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment Highlight
                            val isPaid = order.paymentStatus?.uppercase() == "PAID"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (isPaid) androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.amount_paid) else androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.amount_due), fontWeight = FontWeight.SemiBold, color = if (isPaid) Color(0xFF166534) else Color(0xFF991B1B))
                                    Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isPaid) Color(0xFF166534) else Color(0xFF991B1B))
                                }
                            }
                        }
                    }

                    // Step 1: Old Battery & Payment
                    if (step >= 2) {
                        val qrImageUrl by viewModel.qrImageUrl.collectAsState()
                        val qrLoading by viewModel.qrLoading.collectAsState()
                        val qrPaymentVerified by viewModel.qrPaymentVerified.collectAsState()

                        Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.step_1_battery_payment), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val requiresExchange = order.orderItems?.any { it.exchangeOldBattery } == true
                        if (requiresExchange) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = oldBatteryCollected,
                                    onCheckedChange = { oldBatteryCollected = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFD32F2F),
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.old_battery_collected))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        val isPaid = order.paymentStatus?.uppercase() == "PAID"
                        if (isPaid) {
                            Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.payment_status_label), fontWeight = FontWeight.SemiBold)
                            Text("${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.amount_paid_online)} (₹${order.totalAmount})", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        } else {
                            // Payment Collection Mode Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.payment_collection_mode), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF475569))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = paymentMode == "CASH", 
                                            onClick = { paymentMode = "CASH" }, 
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F))
                                        )
                                        Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.cash), fontWeight = if (paymentMode == "CASH") FontWeight.Bold else FontWeight.Normal)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        RadioButton(
                                            selected = paymentMode == "UPI", 
                                            onClick = { paymentMode = "UPI" }, 
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F))
                                        )
                                        Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.upi_qr_code), fontWeight = if (paymentMode == "UPI") FontWeight.Bold else FontWeight.Normal)
                                    }

                                    // Razorpay QR Code Section
                                    if (paymentMode == "UPI") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (qrPaymentVerified) {
                                            // Payment verified success state
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFDCFCE7))
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("✅", fontSize = 32.sp)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.payment_received), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF166534))
                                                    Text("₹${order.totalAmount} ${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.paid_via_upi)}", fontSize = 13.sp, color = Color(0xFF166534))
                                                }
                                            }
                                        } else {
                                            // Show button to generate or open dialog
                                            Button(
                                                onClick = {
                                                    if (qrImageUrl != null) {
                                                        showQrDialog = true
                                                    } else {
                                                        viewModel.generateQrCode { msg ->
                                                            if (msg.contains("successfully", ignoreCase = true)) {
                                                                showQrDialog = true
                                                            } else {
                                                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                                            }
                                                        }
                                                    }
                                                },
                                                enabled = !qrLoading,
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                            ) {
                                                if (qrLoading) {
                                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Text(if (qrImageUrl != null) androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.show_upi_qr) else androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.generate_show_qr), fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (step == 2) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val canProceed = paymentMode == "CASH" || (paymentMode == "UPI" && qrPaymentVerified)
                            Button(
                                onClick = { viewModel.setStep(3) },
                                enabled = canProceed,
                                modifier = Modifier.align(Alignment.End).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    disabledContainerColor = Color(0xFFBDBDBD)
                                )
                            ) {
                                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.next), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Step 2: OTP Verification
                    if (step >= 3) {
                        Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.step_2_verify_otp), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.send_otp_to_customer), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            OutlinedTextField(
                                value = otp,
                                onValueChange = { newValue -> 
                                    if (newValue.all { it.isDigit() }) {
                                        otp = newValue 
                                    }
                                },
                                label = { Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.enter_otp_from_customer)) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD32F2F),
                                    focusedLabelColor = Color(0xFFD32F2F),
                                    cursorColor = Color(0xFFD32F2F),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { 
                                viewModel.sendOtp { msg -> 
                                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                } 
                            }) {
                                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.resend_otp), color = Color(0xFFD32F2F))
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    val finalPaymentMode = if (order.paymentStatus?.uppercase() == "PAID") {
                                        order.paymentMethod?.uppercase() ?: "ONLINE"
                                    } else {
                                        paymentMode
                                    }
                                    viewModel.completeJob(oldBatteryCollected, finalPaymentMode, otp) { success, msg ->
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
                                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.mark_job_as_completed), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
        }
    }

    if (showQrDialog && uiState is JobExecutionState.Success) {
        val order = (uiState as JobExecutionState.Success).order
        val qrImageUrl by viewModel.qrImageUrl.collectAsState()

        Dialog(
            onDismissRequest = { showQrDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showQrDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.amount_due)}: ₹${order.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.ask_customer_to_scan_qr), fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (qrImageUrl != null) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(qrImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Razorpay UPI QR Code",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFFD32F2F),
                        trackColor = Color(0xFFF1F5F9)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.auto_verifying_payment), color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        LaunchedEffect(showQrDialog) {
            while (isActive && showQrDialog) {
                delay(3000.milliseconds) // Poll every 3 seconds
                val isPaid = viewModel.checkPaymentStatusSilent()
                if (isPaid) {
                    showQrDialog = false
                    showSuccessAnimation = true
                    delay(2500.milliseconds)
                    showSuccessAnimation = false
                    viewModel.setStep(3) // Advance to OTP step
                    break
                }
            }
        }
    }

    // Success Animation Overlay
    AnimatedVisibility(
        visible = showSuccessAnimation,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCCFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF388E3C),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.payment_received), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF388E3C))
            }
        }
    }
    }
}
