package com.battery.mantra.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.battery.mantra.data.models.EnquiryResponse
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val BrandRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEnquiriesScreen(
    viewModel: AdminEnquiriesViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedEnquiry by remember { mutableStateOf<EnquiryResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Enquiries", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs for filtering
            TabRow(
                selectedTabIndex = when(uiState.selectedFilter) {
                    "Quotation" -> 0
                    "Corporate" -> 1
                    else -> 2 // All or others
                },
                containerColor = Color.White,
                contentColor = BrandRed,
                indicator = { tabPositions ->
                    if (uiState.selectedFilter == "Quotation") {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[0]),
                            color = BrandRed
                        )
                    } else if (uiState.selectedFilter == "Corporate") {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[1]),
                            color = BrandRed
                        )
                    }
                }
            ) {
                Tab(
                    selected = uiState.selectedFilter == "Quotation",
                    onClick = { viewModel.setFilter("Quotation") },
                    text = { Text("Quotation", color = if (uiState.selectedFilter == "Quotation") BrandRed else Color.Gray, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = uiState.selectedFilter == "Corporate",
                    onClick = { viewModel.setFilter("Corporate") },
                    text = { Text("Corporate", color = if (uiState.selectedFilter == "Corporate") BrandRed else Color.Gray, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = uiState.selectedFilter == "All",
                    onClick = { viewModel.setFilter("All") },
                    text = { Text("All", color = if (uiState.selectedFilter == "All") BrandRed else Color.Gray, fontWeight = FontWeight.Bold) }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandRed)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = Color.Red)
                }
            } else if (uiState.enquiries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No enquiries found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.enquiries) { enquiry ->
                        EnquiryCard(
                            enquiry = enquiry,
                            onViewDetails = { selectedEnquiry = enquiry },
                            onUpdateStatus = { status ->
                                viewModel.updateEnquiryStatus(
                                    id = enquiry.id,
                                    status = status,
                                    onSuccess = { Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedEnquiry != null) {
        EnquiryDetailsDialog(
            enquiry = selectedEnquiry!!,
            onDismiss = { selectedEnquiry = null }
        )
    }
}

@Composable
fun EnquiryCard(
    enquiry: EnquiryResponse,
    onViewDetails: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Outlined.MailOutline,
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = enquiry.name ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = enquiry.mobileNumber, color = Color(0xFF424242), fontSize = 14.sp)
                    }
                }
                
                val isResolved = enquiry.status == "RESOLVED"
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = enquiry.status,
                        color = if (isResolved) Color(0xFF2E7D32) else Color(0xFFF57C00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = enquiry.productName ?: "No Product Info", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            if (!enquiry.quantity.isNullOrBlank()) {
                Text(text = "Qty: ${enquiry.quantity}", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatDate(enquiry.createdAt), color = Color(0xFF5F6368), fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Details",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onViewDetails() }
                        .padding(4.dp)
                )

                if (enquiry.status != "RESOLVED") {
                    Text(
                        text = "Mark Done",
                        color = BrandRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { onUpdateStatus("RESOLVED") }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnquiryDetailsDialog(enquiry: EnquiryResponse, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enquiry Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 12.dp))
                
                DetailRow("Type", enquiry.enquiryType)
                DetailRow("Status", enquiry.status)
                DetailRow("Date", formatDate(enquiry.createdAt))
                DetailRow("Name", enquiry.name)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DetailRow("Mobile", enquiry.mobileNumber, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${enquiry.mobileNumber}")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = BrandRed)
                    }
                }

                DetailRow("Email", enquiry.email)
                
                if (!enquiry.companyName.isNullOrBlank()) {
                    DetailRow("Company", enquiry.companyName)
                }
                if (!enquiry.gstin.isNullOrBlank()) {
                    DetailRow("GSTIN", enquiry.gstin)
                }
                if (!enquiry.productName.isNullOrBlank()) {
                    DetailRow("Product", enquiry.productName)
                }
                if (!enquiry.quantity.isNullOrBlank()) {
                    DetailRow("Quantity", enquiry.quantity)
                }
                
                if (!enquiry.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Message", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(enquiry.message, color = Color(0xFF424242), fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String?, modifier: Modifier = Modifier) {
    if (!value.isNullOrBlank()) {
        Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
            Text(text = value, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        if (date != null) {
            return outputFormat.format(date)
        }
    } catch (e: Exception) {}
    return dateStr
}
