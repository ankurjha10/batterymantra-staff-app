package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.battery.mantra.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BrandRed = Color(0xFFDE2027)
private val BrandRedLight = Color(0xFFFEE2E2)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

data class EditableOrderItem(
    val productId: String,
    val productName: String,
    val productImage: String?,
    var quantity: Int,
    var exchangeOldBattery: Boolean
)

data class ParsedAddress(
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val state: String,
    val pincode: String
)

fun parseOrderShippingAddress(rawAddress: String?, selectedCity: String?): ParsedAddress {
    if (rawAddress.isNullOrBlank()) {
        return ParsedAddress(
            addressLine1 = "",
            addressLine2 = "",
            city = selectedCity ?: "",
            state = "",
            pincode = ""
        )
    }

    val addr = rawAddress.trim()
    val parts = addr.split(",").map { it.trim() }
    val pinMatch = Regex("\\b\\d{6}\\b").find(addr)?.value ?: ""
    val city = selectedCity?.takeIf { it.isNotBlank() }
        ?: if (parts.size >= 2) parts[parts.size - 2] else ""
    val state = if (parts.size >= 3) {
        parts[parts.size - 1].replace(Regex("\\b\\d{6}\\b"), "").trim()
    } else ""
    val addressLine1 = if (parts.isNotEmpty()) parts[0] else ""
    val endIdx = maxOf(1, parts.size - 2)
    val addressLine2 = if (parts.size > 1 && endIdx > 1) {
        parts.subList(1, endIdx).joinToString(", ")
    } else ""

    return ParsedAddress(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        state = state,
        pincode = pinMatch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderBottomSheet(
    order: OrderResponse,
    productSearchResults: AdminDataState<List<ProductResponse>>,
    onSearchProducts: (String) -> Unit,
    onDismiss: () -> Unit,
    onUpdateOrder: (String, AdminUpdateOrderRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Active Tab: 0 = Customer & Address, 1 = Items, 2 = Billing & Notes
    var selectedTab by remember { mutableIntStateOf(0) }

    // Parse structured address exactly like the website dashboard
    val initialAddress = remember(order.shippingAddress, order.selectedCity) {
        parseOrderShippingAddress(order.shippingAddress, order.selectedCity)
    }

    // Recipient & Address States
    var recipientName by remember { mutableStateOf(order.customerName ?: "") }
    var recipientPhone by remember { mutableStateOf(order.customerPhone ?: "") }
    var addressLine1 by remember { mutableStateOf(initialAddress.addressLine1) }
    var addressLine2 by remember { mutableStateOf(initialAddress.addressLine2) }
    var city by remember { mutableStateOf(initialAddress.city) }
    var state by remember { mutableStateOf(initialAddress.state) }
    var pincode by remember { mutableStateOf(initialAddress.pincode) }
    var selectedBranchCity by remember { mutableStateOf(order.selectedCity ?: initialAddress.city) }

    // Fulfillment & Payment States
    var deliveryMethod by remember { mutableStateOf(order.deliveryMethod ?: "HOME_INSTALLATION") }
    var installationDate by remember { mutableStateOf(order.installationDate ?: "") }
    var paymentMethod by remember { mutableStateOf(order.paymentMethod ?: "COD") }
    var paymentStatus by remember { mutableStateOf(order.paymentStatus ?: "PENDING") }
    var manualDiscountText by remember {
        mutableStateOf(order.exchangeDiscount?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var totalAmountText by remember {
        mutableStateOf(order.totalAmount?.let { it.toString() } ?: "")
    }
    var engineerNotes by remember { mutableStateOf("") }

    // Items List
    val editableItems = remember {
        mutableStateListOf<EditableOrderItem>().apply {
            order.orderItems?.forEach { item ->
                add(
                    EditableOrderItem(
                        productId = item.productId,
                        productName = item.productName ?: "Battery",
                        productImage = item.productImage,
                        quantity = item.quantity.coerceAtLeast(1),
                        exchangeOldBattery = item.exchangeOldBattery
                    )
                )
            }
        }
    }

    // Product search state
    var productSearchQuery by remember { mutableStateOf("") }
    var showProductSearch by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Debounced product search
    LaunchedEffect(productSearchQuery) {
        if (productSearchQuery.length >= 2) {
            delay(400)
            onSearchProducts(productSearchQuery)
        } else {
            onSearchProducts("")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandRedLight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Edit Order #${order.orderId.take(8).lowercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Text(
                                    text = order.orderStatus ?: "PENDING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Modify customer address, items, quantities, and fulfillment.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs (Matching Website: Customer & Address | Items (N) | Billing & Notes)
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandRed,
                divider = { HorizontalDivider(color = BorderColor) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Customer & Address", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Items (${editableItems.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Billing & Notes", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // ── Tab 0: Customer & Address ──
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OrderFormField(
                                    value = recipientName,
                                    onValueChange = { recipientName = it },
                                    label = "Recipient / Customer Name *",
                                    modifier = Modifier.weight(1f)
                                )
                                OrderFormField(
                                    value = recipientPhone,
                                    onValueChange = { recipientPhone = it },
                                    label = "Recipient Phone Number *",
                                    keyboardType = KeyboardType.Phone,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OrderFormField(
                                value = addressLine1,
                                onValueChange = { addressLine1 = it },
                                label = "Delivery Address Line 1 *"
                            )

                            OrderFormField(
                                value = addressLine2,
                                onValueChange = { addressLine2 = it },
                                label = "Address Line 2 (Optional)"
                            )

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
                                    label = "State",
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

                            OrderFormField(
                                value = selectedBranchCity,
                                onValueChange = { selectedBranchCity = it },
                                label = "Website Branch / Selected City"
                            )
                        }
                    }

                    1 -> {
                        // ── Tab 1: Order Items ──
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (editableItems.isEmpty()) {
                                Text(
                                    "No items in order. Please add at least one product.",
                                    color = Color.Red,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                editableItems.forEachIndexed { index, item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEEF2F6)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!item.productImage.isNullOrEmpty()) {
                                                        AsyncImage(
                                                            model = ImageRequest.Builder(context)
                                                                .data(item.productImage)
                                                                .crossfade(true)
                                                                .build(),
                                                            contentDescription = item.productName,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Icon(
                                                            Icons.Outlined.BatteryChargingFull,
                                                            contentDescription = null,
                                                            tint = TextSecondary,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        item.productName,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = TextPrimary,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { editableItems.removeAt(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.Delete,
                                                        contentDescription = "Remove Item",
                                                        tint = Color.Red.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Quantity Selector
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Qty: ", fontSize = 12.sp, color = TextSecondary)
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color.White,
                                                        border = BorderStroke(1.dp, BorderColor)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            IconButton(
                                                                onClick = {
                                                                    if (item.quantity > 1) {
                                                                        editableItems[index] = item.copy(quantity = item.quantity - 1)
                                                                    }
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Text("−", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                                            }
                                                            Text(
                                                                "${item.quantity}",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                modifier = Modifier.padding(horizontal = 6.dp)
                                                            )
                                                            IconButton(
                                                                onClick = {
                                                                    editableItems[index] = item.copy(quantity = item.quantity + 1)
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandRed)
                                                            }
                                                        }
                                                    }
                                                }

                                                // Exchange Old Battery Toggle
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable {
                                                        editableItems[index] = item.copy(exchangeOldBattery = !item.exchangeOldBattery)
                                                    }
                                                ) {
                                                    Checkbox(
                                                        checked = item.exchangeOldBattery,
                                                        onCheckedChange = { checked ->
                                                            editableItems[index] = item.copy(exchangeOldBattery = checked)
                                                        },
                                                        colors = CheckboxDefaults.colors(checkedColor = BrandRed),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Exchange Old Battery", fontSize = 12.sp, color = TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Toggle Add / Replace Product Search
                            if (!showProductSearch) {
                                OutlinedButton(
                                    onClick = { showProductSearch = true },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                                    border = BorderStroke(1.dp, BrandRed)
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Add / Replace Product", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Search Product Catalog", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        IconButton(onClick = {
                                            showProductSearch = false
                                            productSearchQuery = ""
                                            onSearchProducts("")
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Close search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = productSearchQuery,
                                        onValueChange = { productSearchQuery = it },
                                        placeholder = { Text("Search by brand or product name...", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = BrandRed,
                                            unfocusedBorderColor = BorderColor
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    when (productSearchResults) {
                                        is AdminDataState.Loading -> {
                                            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandRed)
                                            }
                                        }
                                        is AdminDataState.Success -> {
                                            if (productSearchResults.data.isEmpty()) {
                                                Text("No matching products found", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(8.dp))
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    productSearchResults.data.take(5).forEach { product ->
                                                        Surface(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color.White,
                                                            border = BorderStroke(1.dp, BorderColor)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(8.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                    Text("${product.brand} • ₹${product.price}", fontSize = 11.sp, color = TextSecondary)
                                                                }
                                                                Button(
                                                                    onClick = {
                                                                        editableItems.add(
                                                                            EditableOrderItem(
                                                                                productId = product.id.toString(),
                                                                                productName = product.name,
                                                                                productImage = product.imageUrl,
                                                                                quantity = 1,
                                                                                exchangeOldBattery = false
                                                                            )
                                                                        )
                                                                        Toast.makeText(context, "Added ${product.name}", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                    modifier = Modifier.height(30.dp)
                                                                ) {
                                                                    Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ── Tab 2: Billing & Notes ──
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Delivery Method", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("HOME_INSTALLATION", "STORE_PICKUP", "EXPRESS").forEach { method ->
                                    val isSelected = deliveryMethod == method
                                    Surface(
                                        onClick = { deliveryMethod = method },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandRedLight else Color(0xFFF9FAFB),
                                        border = BorderStroke(1.dp, if (isSelected) BrandRed else BorderColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = method.replace("_", " "),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandRed else TextSecondary,
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            OrderFormField(
                                value = installationDate,
                                onValueChange = { installationDate = it },
                                label = "Installation Date (YYYY-MM-DD)"
                            )

                            Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("COD", "ONLINE", "UPI").forEach { method ->
                                    val isSelected = paymentMethod == method
                                    Surface(
                                        onClick = { paymentMethod = method },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandRedLight else Color(0xFFF9FAFB),
                                        border = BorderStroke(1.dp, if (isSelected) BrandRed else BorderColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = method,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandRed else TextSecondary,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Text("Payment Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("PENDING", "PAID", "FAILED").forEach { status ->
                                    val isSelected = paymentStatus == status
                                    Surface(
                                        onClick = { paymentStatus = status },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFFD1FAE5) else Color(0xFFF9FAFB),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else BorderColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = status,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF047857) else TextSecondary,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OrderFormField(
                                    value = manualDiscountText,
                                    onValueChange = { manualDiscountText = it },
                                    label = "Manual Discount (₹)",
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                                OrderFormField(
                                    value = totalAmountText,
                                    onValueChange = { totalAmountText = it },
                                    label = "Total Amount (₹) *",
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OrderFormField(
                                value = engineerNotes,
                                onValueChange = { engineerNotes = it },
                                label = "Engineer Notes (Optional)",
                                singleLine = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Actions: Cancel & Save Order Changes (Matching Website DialogFooter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Cancel", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (recipientName.isBlank()) {
                            selectedTab = 0
                            Toast.makeText(context, "Recipient name cannot be blank", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (recipientPhone.isBlank()) {
                            selectedTab = 0
                            Toast.makeText(context, "Recipient phone cannot be blank", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (addressLine1.isBlank()) {
                            selectedTab = 0
                            Toast.makeText(context, "Address Line 1 cannot be blank", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (pincode.isBlank()) {
                            selectedTab = 0
                            Toast.makeText(context, "Pincode is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (editableItems.isEmpty()) {
                            selectedTab = 1
                            Toast.makeText(context, "Order must have at least one product", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        val request = AdminUpdateOrderRequest(
                            recipientName = recipientName.trim(),
                            recipientPhone = recipientPhone.trim(),
                            addressLine1 = addressLine1.trim(),
                            addressLine2 = addressLine2.trim().ifBlank { null },
                            city = city.trim().ifBlank { selectedBranchCity.trim().ifBlank { null } },
                            state = state.trim().ifBlank { null },
                            pincode = pincode.trim(),
                            selectedCity = selectedBranchCity.trim().ifBlank { city.trim().ifBlank { null } },
                            items = editableItems.map {
                                AdminOrderItemRequest(
                                    productId = it.productId,
                                    quantity = it.quantity,
                                    exchangeOldBattery = it.exchangeOldBattery
                                )
                            },
                            manualDiscount = manualDiscountText.toDoubleOrNull(),
                            totalAmount = totalAmountText.toDoubleOrNull(),
                            deliveryMethod = deliveryMethod,
                            installationDate = installationDate.trim().ifBlank { null },
                            paymentMethod = paymentMethod,
                            paymentStatus = paymentStatus,
                            engineerNotes = engineerNotes.trim().ifBlank { null }
                        )

                        onUpdateOrder(
                            order.orderId,
                            request,
                            {
                                isSubmitting = false
                                Toast.makeText(context, "Order #${order.orderId.take(8).lowercase()} updated successfully!", Toast.LENGTH_SHORT).show()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            },
                            { err ->
                                isSubmitting = false
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier.weight(1.5f).height(48.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRed,
                        disabledContainerColor = BrandRed.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Order Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
