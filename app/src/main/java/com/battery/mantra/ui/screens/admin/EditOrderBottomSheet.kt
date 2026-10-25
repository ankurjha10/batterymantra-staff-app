package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
    val unitPrice: Double = 0.0,
    val exchangeDiscount: Double = 0.0,
    val quantity: Int = 1,
    val exchangeOldBattery: Boolean = false
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
    val parts = addr.split(",").map { it.trim() }.filter { !it.equals("india", ignoreCase = true) && it.isNotEmpty() }
    val pinMatch = Regex("\\b\\d{6}\\b").find(addr)?.value ?: ""
    val city = selectedCity?.takeIf { it.isNotBlank() }
        ?: if (parts.size >= 2) parts[parts.size - 2] else ""
    val state = if (parts.isNotEmpty()) parts.last().replace(Regex("\\b\\d{6}\\b"), "").trim() else ""
    val line1 = if (parts.isNotEmpty()) parts.first() else ""
    val line2 = if (parts.size > 2) parts.subList(1, parts.size - 2).joinToString(", ") else ""

    return ParsedAddress(
        addressLine1 = line1,
        addressLine2 = line2,
        city = city,
        state = state,
        pincode = pinMatch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderBottomSheet(
    order: OrderResponse,
    citiesState: AdminDataState<List<CityResponse>> = AdminDataState.Idle,
    productSearchResults: AdminDataState<List<ProductResponse>>,
    onSearchProducts: (String, String?) -> Unit,
    onDismiss: () -> Unit,
    onUpdateOrder: (String, AdminUpdateOrderRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Active Tab: 0 = Customer & Address, 1 = Items, 2 = Billing & Notes
    var selectedTab by remember { mutableIntStateOf(0) }

    // Use direct structured address fields from OrderResponse if available, otherwise fallback string parsing
    val initialAddress = remember(order) {
        if (!order.addressLine1.isNullOrBlank() || !order.city.isNullOrBlank()) {
            ParsedAddress(
                addressLine1 = order.addressLine1 ?: "",
                addressLine2 = order.addressLine2 ?: "",
                city = order.city ?: order.selectedCity ?: "",
                state = order.state ?: "",
                pincode = order.postalCode ?: ""
            )
        } else {
            // Fallback string parsing: filter out "india" to avoid corrupting state
            val raw = order.shippingAddress ?: ""
            val parts = raw.split(",").map { it.trim() }.filter { !it.equals("india", ignoreCase = true) }
            val pinMatch = Regex("\\b\\d{6}\\b").find(raw)?.value ?: ""
            val city = order.selectedCity?.takeIf { it.isNotBlank() } ?: if (parts.size >= 2) parts[parts.size - 2] else ""
            val state = if (parts.isNotEmpty()) parts.last().replace(Regex("\\b\\d{6}\\b"), "").trim() else ""
            val line1 = if (parts.isNotEmpty()) parts.first() else ""
            val line2 = if (parts.size > 2) parts.subList(1, parts.size - 2).joinToString(", ") else ""
            ParsedAddress(line1, line2, city, state, pinMatch)
        }
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

    // Resolve public cities for city-wise pricing and exchange discounts
    val publicCities = remember(citiesState) {
        (citiesState as? AdminDataState.Success)?.data ?: emptyList()
    }

    val resolvedCityId = remember(publicCities, selectedBranchCity, city, order) {
        val targetCity = (selectedBranchCity.ifBlank { city }.ifBlank { order.selectedCity ?: order.city ?: "" }).trim().lowercase()
        if (targetCity.isBlank() || publicCities.isEmpty()) null
        else {
            publicCities.firstOrNull { c ->
                val cName = c.cityName.trim().lowercase()
                cName == targetCity || targetCity.contains(cName) || cName.contains(targetCity)
            }?.cityId
        }
    }

    val matchedCityName = remember(publicCities, resolvedCityId) {
        publicCities.firstOrNull { it.cityId == resolvedCityId }?.cityName
    }

    // Fulfillment & Payment States
    var deliveryMethod by remember { mutableStateOf(order.deliveryMethod ?: "HOME_INSTALLATION") }
    var installationDate by remember { mutableStateOf(order.installationDate ?: "") }
    var paymentMethod by remember { mutableStateOf(order.paymentMethod ?: "COD") }
    var paymentStatus by remember { mutableStateOf(order.paymentStatus ?: "PENDING") }
    var paymentMode by remember { mutableStateOf(order.paymentMode ?: "UPI") }
    var manualDiscountText by remember {
        mutableStateOf(order.discountAmount?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var totalAmountText by remember {
        mutableStateOf(order.totalAmount?.let { it.toString() } ?: "")
    }
    var engineerNotes by remember { mutableStateOf("") }

    // Items List
    val editableItems = remember {
        mutableStateListOf<EditableOrderItem>().apply {
            order.orderItems?.forEach { item ->
                val unitPrice = item.priceAtPurchase
                    ?: (if (item.quantity > 0 && item.subtotal != null) item.subtotal / item.quantity else 0.0)
                val exDiscount = item.exchangeDiscount ?: 0.0
                add(
                    EditableOrderItem(
                        productId = item.productId,
                        productName = item.productName ?: "Battery",
                        productImage = item.productImage,
                        unitPrice = unitPrice,
                        exchangeDiscount = exDiscount,
                        quantity = item.quantity.coerceAtLeast(1),
                        exchangeOldBattery = item.exchangeOldBattery
                    )
                )
            }
        }
    }

    // Dynamic Calculations
    val calculatedSubtotal by remember {
        derivedStateOf {
            editableItems.sumOf { it.unitPrice * it.quantity }
        }
    }

    val calculatedScrapDiscount by remember {
        derivedStateOf {
            editableItems
                .filter { it.exchangeOldBattery }
                .sumOf { it.exchangeDiscount * it.quantity }
        }
    }

    val manualDiscountValue = manualDiscountText.toDoubleOrNull() ?: 0.0

    val calculatedTotal by remember {
        derivedStateOf {
            maxOf(0.0, calculatedSubtotal - calculatedScrapDiscount - manualDiscountValue)
        }
    }

    // Product search state
    var productSearchQuery by remember { mutableStateOf("") }
    var showProductSearch by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Debounced product search passing cityId for city-wise pricing
    LaunchedEffect(productSearchQuery, resolvedCityId) {
        if (productSearchQuery.length >= 2) {
            delay(400)
            onSearchProducts(productSearchQuery, resolvedCityId)
        } else {
            onSearchProducts("", resolvedCityId)
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
                                                    if (item.unitPrice > 0) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "₹${item.unitPrice.toInt()} each" + if (item.exchangeOldBattery && item.exchangeDiscount > 0) " • Scrap Disc: -₹${(item.exchangeDiscount * item.quantity).toInt()}" else if (item.exchangeDiscount > 0) " • Scrap Value: ₹${item.exchangeDiscount.toInt()}" else "",
                                                            fontSize = 11.sp,
                                                            color = if (item.exchangeOldBattery && item.exchangeDiscount > 0) Color(0xFF059669) else TextSecondary
                                                        )
                                                    }
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
                                                    Text(
                                                        if (item.exchangeDiscount > 0) "Exchange Old Battery (-₹${item.exchangeDiscount.toInt()}/pc)" else "Exchange Old Battery",
                                                        fontSize = 12.sp,
                                                        color = TextPrimary
                                                    )
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
                                        .animateContentSize()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Search Product Catalog", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (matchedCityName != null) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFEFF6FF),
                                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                                ) {
                                                    Text(
                                                        text = "City: $matchedCityName",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF1D4ED8),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(onClick = {
                                            showProductSearch = false
                                            productSearchQuery = ""
                                            onSearchProducts("", resolvedCityId)
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
                                                                    Text(
                                                                        "${product.brand} • ₹${product.price.toInt()}" + if ((product.exchangeDiscount ?: 0.0) > 0) " • Scrap: -₹${product.exchangeDiscount?.toInt()}" else "",
                                                                        fontSize = 11.sp,
                                                                        color = TextSecondary
                                                                    )
                                                                }
                                                                Button(
                                                                    onClick = {
                                                                        editableItems.add(
                                                                            EditableOrderItem(
                                                                                productId = product.id.toString(),
                                                                                productName = product.name,
                                                                                productImage = product.imageUrl,
                                                                                unitPrice = product.price,
                                                                                exchangeDiscount = product.exchangeDiscount ?: 0.0,
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
                                listOf("COD", "ONLINE").forEach { method ->
                                    val isSelected = paymentMethod == method
                                    Surface(
                                        onClick = { paymentMethod = method },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandRedLight else Color(0xFFF9FAFB),
                                        border = BorderStroke(1.dp, if (isSelected) BrandRed else BorderColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { paymentMethod = method },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = method,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) BrandRed else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Text("Payment Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("PENDING", "PAID", "FAILED", "REFUNDED").forEach { status ->
                                    val isSelected = paymentStatus == status
                                    val chipBg = when {
                                        isSelected && status == "PAID" -> Color(0xFFD1FAE5)
                                        isSelected && status == "PENDING" -> Color(0xFFFEF3C7)
                                        isSelected -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFF9FAFB)
                                    }
                                    val chipBorder = when {
                                        isSelected && status == "PAID" -> Color(0xFF10B981)
                                        isSelected && status == "PENDING" -> Color(0xFFF59E0B)
                                        isSelected -> Color(0xFFEF4444)
                                        else -> BorderColor
                                    }
                                    val chipText = when {
                                        isSelected && status == "PAID" -> Color(0xFF047857)
                                        isSelected && status == "PENDING" -> Color(0xFFB45309)
                                        isSelected -> Color(0xFFDC2626)
                                        else -> TextSecondary
                                    }
                                    Surface(
                                        onClick = { paymentStatus = status },
                                        shape = RoundedCornerShape(8.dp),
                                        color = chipBg,
                                        border = BorderStroke(1.dp, chipBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = status,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipText,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            if (paymentMethod == "ONLINE") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("UPI", "CARD", "NET_BANKING", "WALLET", "CASH").forEach { mode ->
                                            val isSelected = paymentMode == mode
                                            Surface(
                                                onClick = { paymentMode = mode },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) BrandRedLight else Color(0xFFF9FAFB),
                                                border = BorderStroke(1.dp, if (isSelected) BrandRed else BorderColor),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = mode.replace("_", " "),
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) BrandRed else TextSecondary,
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OrderFormField(
                                value = manualDiscountText,
                                onValueChange = { manualDiscountText = it },
                                label = "Manual Discount (₹)",
                                keyboardType = KeyboardType.Number
                            )

                            // Dynamic Live Price Breakdown (Matching Website Modal)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Order Pricing Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Items Subtotal (${editableItems.sumOf { it.quantity }} items):", fontSize = 12.sp, color = TextSecondary)
                                        Text("₹${calculatedSubtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    }

                                    if (calculatedScrapDiscount > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Scrap Exchange Discount:", fontSize = 12.sp, color = Color(0xFF059669))
                                            Text("- ₹${calculatedScrapDiscount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        }
                                    }

                                    if (manualDiscountValue > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Manual Discount:", fontSize = 12.sp, color = BrandRed)
                                            Text("- ₹${manualDiscountValue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandRed)
                                        }
                                    }

                                    HorizontalDivider(color = BorderColor.copy(alpha = 0.8f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Final Calculated Total:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            "₹${calculatedTotal.toInt()}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandRed
                                        )
                                    }
                                }
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
                        val finalTotal = if (calculatedTotal > 0.0 || editableItems.isNotEmpty()) calculatedTotal else (totalAmountText.toDoubleOrNull() ?: 0.0)
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
                            manualDiscount = manualDiscountValue,
                            totalAmount = finalTotal,
                            deliveryMethod = deliveryMethod,
                            installationDate = installationDate.trim().ifBlank { null },
                            paymentMethod = paymentMethod,
                            paymentStatus = paymentStatus,
                            paymentMode = if (paymentMethod == "ONLINE") paymentMode else null,
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
