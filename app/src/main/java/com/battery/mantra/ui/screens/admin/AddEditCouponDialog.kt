package com.battery.mantra.ui.screens.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.battery.mantra.data.models.CouponRequest
import com.battery.mantra.data.models.CouponResponse
import java.text.SimpleDateFormat
import java.util.*

private val BrandRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCouponDialog(
    coupon: CouponResponse? = null,
    onDismiss: () -> Unit,
    onSave: (CouponRequest) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val backendFormat = remember {
        SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val isNew = coupon == null

    // Determine initial lifetime state:
    // If new coupon -> default true
    // If existing coupon -> true if expiryDate is null/blank/"null"
    val initialIsLifetime = remember(coupon) {
        if (isNew) {
            true
        } else {
            val hasExpiry = !coupon?.expiryDate.isNullOrBlank() && coupon?.expiryDate != "null"
            !hasExpiry
        }
    }

    var code by remember(coupon) { mutableStateOf(coupon?.code ?: "") }
    var discountType by remember(coupon) { mutableStateOf(coupon?.discountType ?: "FLAT") }
    var discountValue by remember(coupon) { mutableStateOf(if (coupon != null) coupon.discountValue.toString() else "0") }
    var minOrderValue by remember(coupon) { mutableStateOf(coupon?.minOrderValue?.toString() ?: "") }
    var maxDiscountAmount by remember(coupon) { mutableStateOf(coupon?.maxDiscountAmount?.toString() ?: "") }
    var isLifetime by remember(coupon) { mutableStateOf(initialIsLifetime) }

    var startDate by remember(coupon) {
        mutableStateOf(coupon?.startDate?.let { parseBackendDate(it)?.let { d -> dateFormat.format(d) } } ?: "")
    }
    var expiryDate by remember(coupon) {
        mutableStateOf(coupon?.expiryDate?.let { parseBackendDate(it)?.let { d -> dateFormat.format(d) } } ?: "")
    }

    var usageLimit by remember(coupon) { mutableStateOf(coupon?.usageLimit?.toString() ?: "") }
    var isActive by remember(coupon) { mutableStateOf(coupon?.isActive ?: true) }

    var expandedDiscountType by remember { mutableStateOf(false) }

    // Reusable styling for text fields: Reddish border and label, crisp black text
    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        unfocusedBorderColor = BrandRed.copy(alpha = 0.45f),
        focusedLabelColor = BrandRed,
        unfocusedLabelColor = Color(0xFF616161),
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        cursorColor = BrandRed,
        focusedPlaceholderColor = Color(0xFF9E9E9E),
        unfocusedPlaceholderColor = Color(0xFF9E9E9E)
    )

    val customCheckboxColors = CheckboxDefaults.colors(
        checkedColor = BrandRed,
        uncheckedColor = BrandRed.copy(alpha = 0.6f),
        checkmarkColor = Color.White
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (coupon == null) "Add Coupon" else "Edit Coupon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Coupon Code
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Coupon Code *") },
                        placeholder = { Text("E.G. FESTIVE50") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customTextFieldColors
                    )

                    // Discount Type & Value Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDiscountType,
                            onExpandedChange = { expandedDiscountType = !expandedDiscountType },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = if (discountType == "FLAT") "Flat Amount (₹)" else "Percentage (%)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Discount Type *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDiscountType) },
                                colors = customTextFieldColors,
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDiscountType,
                                onDismissRequest = { expandedDiscountType = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Flat Amount (₹)", color = Color.Black) },
                                    onClick = { discountType = "FLAT"; expandedDiscountType = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Percentage (%)", color = Color.Black) },
                                    onClick = { discountType = "PERCENTAGE"; expandedDiscountType = false }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { discountValue = it },
                            label = { Text("Discount Value *") },
                            placeholder = { Text("0") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = customTextFieldColors
                        )
                    }

                    // Min Order & Max Discount Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = minOrderValue,
                            onValueChange = { minOrderValue = it },
                            label = { Text("Min Order Value (₹)") },
                            placeholder = { Text("Optional") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = customTextFieldColors
                        )
                        OutlinedTextField(
                            value = maxDiscountAmount,
                            onValueChange = { maxDiscountAmount = it },
                            label = { Text("Max Discount (₹)") },
                            placeholder = { Text("Optional") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = customTextFieldColors
                        )
                    }

                    // Lifetime Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLifetime = !isLifetime },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isLifetime,
                            onCheckedChange = { isLifetime = it },
                            colors = customCheckboxColors
                        )
                        Text("Lifetime Coupon (No start or expiry date)", color = Color.Black, fontSize = 14.sp)
                    }

                    // Dates
                    if (!isLifetime) {
                        val context = LocalContext.current
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Start Date
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Start Date") },
                                placeholder = { Text("dd/mm/yyyy") },
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val cal = Calendar.getInstance()
                                        if (startDate.isNotEmpty()) {
                                            try { cal.time = dateFormat.parse(startDate)!! } catch (e: Exception) {}
                                        }
                                        DatePickerDialog(context, { _, y, m, d ->
                                            cal.set(y, m, d)
                                            startDate = dateFormat.format(cal.time)
                                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                    }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Start Date", tint = BrandRed, modifier = Modifier.size(20.dp))
                                    }
                                },
                                colors = customTextFieldColors
                            )

                            // Expiry Date
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Expiry Date") },
                                placeholder = { Text("dd/mm/yyyy") },
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val cal = Calendar.getInstance()
                                        if (expiryDate.isNotEmpty()) {
                                            try { cal.time = dateFormat.parse(expiryDate)!! } catch (e: Exception) {}
                                        }
                                        DatePickerDialog(context, { _, y, m, d ->
                                            cal.set(y, m, d)
                                            expiryDate = dateFormat.format(cal.time)
                                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                    }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Expiry Date", tint = BrandRed, modifier = Modifier.size(20.dp))
                                    }
                                },
                                colors = customTextFieldColors
                            )
                        }
                    }

                    // Usage Limit
                    OutlinedTextField(
                        value = usageLimit,
                        onValueChange = { usageLimit = it },
                        label = { Text("Total Usage Limit") },
                        placeholder = { Text("Optional (e.g. 100 uses)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = customTextFieldColors
                    )

                    // Active Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isActive = !isActive },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = customCheckboxColors
                        )
                        Text("Active (Customers can use it)", color = Color.Black, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                HorizontalDivider(color = BrandRed.copy(alpha = 0.2f))

                // Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coupon != null && onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text("Delete", color = BrandRed, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (code.isBlank() || discountValue.isBlank()) return@Button

                            val startBackendStr = if (!isLifetime && startDate.isNotEmpty()) {
                                try { backendFormat.format(dateFormat.parse(startDate)!!) } catch (e: Exception) { null }
                            } else null

                            val expiryBackendStr = if (!isLifetime && expiryDate.isNotEmpty()) {
                                try { backendFormat.format(dateFormat.parse(expiryDate)!!) } catch (e: Exception) { null }
                            } else null

                            val request = CouponRequest(
                                code = code.trim(),
                                discountType = discountType,
                                discountValue = discountValue.toDoubleOrNull() ?: 0.0,
                                maxDiscountAmount = maxDiscountAmount.toDoubleOrNull(),
                                minOrderValue = minOrderValue.toDoubleOrNull(),
                                startDate = startBackendStr,
                                expiryDate = expiryBackendStr,
                                usageLimit = usageLimit.toIntOrNull(),
                                isActive = isActive
                            )
                            onSave(request)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun parseBackendDate(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank() || dateStr == "null") return null
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )
    for (format in formats) {
        try {
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.parse(dateStr)
        } catch (e: Exception) {}
    }
    return null
}
