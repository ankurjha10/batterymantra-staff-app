package com.battery.mantra.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.*
import java.text.NumberFormat
import java.util.Locale

private val BrandRed = Color(0xFFDE2027)
private val BrandRedLight = Color(0xFFFEE2E2)
private val SurfaceBg = Color(0xFFF8F9FA)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

data class CartItem(
    val product: ProductResponse,
    var quantity: Int = 1,
    var withExchange: Boolean = false
) {
    val unitPrice: Double get() = if (withExchange) {
        product.price - (product.exchangeDiscount ?: 0.0)
    } else {
        product.price
    }
    val lineTotal: Double get() = unitPrice * quantity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    usersState: AdminDataState<List<UserResponse>>,
    productsState: List<ProductResponse>,
    onCreateCustomer: (AdminCreateCustomerRequest, (UserResponse) -> Unit, (String) -> Unit) -> Unit,
    onCreateOrder: (AdminCreateOrderRequest, () -> Unit, (String) -> Unit) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // States
    var selectedCustomer by remember { mutableStateOf<UserResponse?>(null) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showCustomerDropdown by remember { mutableStateOf(false) }
    var showNewCustomerSheet by remember { mutableStateOf(false) }

    var productSearchQuery by remember { mutableStateOf("") }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    var paymentMethod by remember { mutableStateOf("COD") }
    var deliveryMethod by remember { mutableStateOf("HOME_INSTALLATION") }
    var installationDate by remember { mutableStateOf("") }
    var extraDiscount by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val subtotal = remember(cartItems.toList()) { cartItems.sumOf { it.lineTotal } }
    val discountAmount = extraDiscount.toDoubleOrNull() ?: 0.0
    val totalAmount = (subtotal - discountAmount).coerceAtLeast(0.0)

    // Filter customers
    val filteredCustomers = remember(usersState, customerSearchQuery) {
        if (usersState is AdminDataState.Success && customerSearchQuery.isNotBlank()) {
            usersState.data.filter { user ->
                (user.name?.contains(customerSearchQuery, true) == true) ||
                (user.email?.contains(customerSearchQuery, true) == true) ||
                (user.userId.contains(customerSearchQuery, true))
            }.take(8)
        } else emptyList()
    }

    // Filter products
    val filteredProducts = remember(productsState, productSearchQuery) {
        if (productSearchQuery.isNotBlank()) {
            productsState.filter {
                it.name.contains(productSearchQuery, true) ||
                it.brand.contains(productSearchQuery, true)
            }
        } else productsState
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Manual Order", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("For walk-in or phone customer", fontSize = 12.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            // Sticky Summary Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBg,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items (${cartItems.sumOf { it.quantity }})", color = TextSecondary, fontSize = 14.sp)
                        Text(currencyFormat.format(subtotal), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    if (discountAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = Color(0xFF10B981), fontSize = 14.sp)
                            Text("- ${currencyFormat.format(discountAmount)}", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text(currencyFormat.format(totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = BrandRed)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (cartItems.isEmpty()) {
                                Toast.makeText(context, "Please add at least one product", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            val request = AdminCreateOrderRequest(
                                customerId = selectedCustomer!!.userId,
                                items = cartItems.map { item ->
                                    AdminOrderItemRequest(
                                        productId = item.product.id.toString(),
                                        quantity = item.quantity,
                                        exchangeOldBattery = item.withExchange
                                    )
                                },
                                paymentMethod = paymentMethod,
                                deliveryMethod = deliveryMethod,
                                discount = discountAmount,
                                installationDate = installationDate.ifBlank { null }
                            )
                            onCreateOrder(request, {
                                isSubmitting = false
                                Toast.makeText(context, "Order created successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }, { err ->
                                isSubmitting = false
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            })
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting && selectedCustomer != null && cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandRed,
                            disabledContainerColor = BrandRed.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Create Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        },
        containerColor = SurfaceBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Section 1: Customer Information ───
            item {
                SectionCard(title = "Customer Information", icon = Icons.Outlined.Person) {
                    // Search + New Customer button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = customerSearchQuery,
                                onValueChange = {
                                    customerSearchQuery = it
                                    showCustomerDropdown = it.isNotBlank()
                                    if (it.isBlank()) selectedCustomer = null
                                },
                                placeholder = { Text("Search by name or phone...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                    focusedBorderColor = BrandRed,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = BrandRed
                                )
                            )

                            DropdownMenu(
                                expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
                                onDismissRequest = { showCustomerDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.65f)
                            ) {
                                filteredCustomers.forEach { user ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(user.name ?: "Unknown", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text(user.email ?: user.userId.take(8), fontSize = 12.sp, color = TextSecondary)
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = user
                                            customerSearchQuery = user.name ?: ""
                                            showCustomerDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showNewCustomerSheet = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                            border = BorderStroke(1.dp, BrandRed),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Selected Customer chip
                    if (selectedCustomer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selectedCustomer!!.name ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF065F46))
                                    Text(selectedCustomer!!.email ?: "ID: ${selectedCustomer!!.userId.take(8)}", fontSize = 12.sp, color = Color(0xFF047857))
                                }
                                IconButton(onClick = { selectedCustomer = null; customerSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ─── Section 2: Select Products ───
            item {
                SectionCard(title = "Select Products", icon = Icons.Outlined.Inventory2) {
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("Search product name...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BrandRed
                        )
                    )
                }
            }

            // Product list
            items(filteredProducts.take(20)) { product ->
                val cartItem = cartItems.find { it.product.id == product.id }
                val isInCart = cartItem != null

                ProductSelectionCard(
                    product = product,
                    cartItem = cartItem,
                    currencyFormat = currencyFormat,
                    onAddToCart = {
                        cartItems.add(CartItem(product = product))
                    },
                    onRemoveFromCart = {
                        cartItems.removeAll { it.product.id == product.id }
                    },
                    onQuantityChange = { qty ->
                        val idx = cartItems.indexOfFirst { it.product.id == product.id }
                        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(quantity = qty)
                    },
                    onExchangeToggle = { withExchange ->
                        val idx = cartItems.indexOfFirst { it.product.id == product.id }
                        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(withExchange = withExchange)
                    }
                )
            }

            // ─── Section 3: Order Settings ───
            item {
                SectionCard(title = "Order Settings", icon = Icons.Outlined.Settings) {
                    // Payment Method
                    Text("Payment Method", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingChip("COD", paymentMethod == "COD") { paymentMethod = "COD" }
                        SettingChip("Online", paymentMethod == "ONLINE") { paymentMethod = "ONLINE" }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Delivery Method
                    Text("Delivery Method", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingChip("Home Install", deliveryMethod == "HOME_INSTALLATION") { deliveryMethod = "HOME_INSTALLATION" }
                        SettingChip("Store Pickup", deliveryMethod == "STORE_PICKUP") { deliveryMethod = "STORE_PICKUP" }
                        SettingChip("Delivery", deliveryMethod == "STANDARD_DELIVERY") { deliveryMethod = "STANDARD_DELIVERY" }
                    }

                    if (deliveryMethod == "HOME_INSTALLATION") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OrderFormField(
                            value = installationDate,
                            onValueChange = { installationDate = it },
                            label = "Installation Date (yyyy-mm-dd)"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Extra Discount
                    Text("Extra Discount (₹)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OrderFormField(
                        value = extraDiscount,
                        onValueChange = { extraDiscount = it },
                        label = "Enter discount amount",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                }
            }

            // Bottom spacer for the sticky footer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // New Customer Bottom Sheet
    if (showNewCustomerSheet) {
        NewCustomerBottomSheet(
            onDismiss = { showNewCustomerSheet = false },
            onCreateCustomer = { request, onSuccess, onError ->
                onCreateCustomer(request, { user ->
                    selectedCustomer = user
                    customerSearchQuery = user.name ?: ""
                    showNewCustomerSheet = false
                    onSuccess(user)
                }, onError)
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ProductSelectionCard(
    product: ProductResponse,
    cartItem: CartItem?,
    currencyFormat: NumberFormat,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onExchangeToggle: (Boolean) -> Unit
) {
    val isInCart = cartItem != null
    val borderColor = if (isInCart) BrandRed.copy(alpha = 0.3f) else Color(0xFFF3F4F6)
    val bgColor = if (isInCart) BrandRedLight.copy(alpha = 0.3f) else CardBg

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 2.dp else 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.BatteryChargingFull, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${product.brand} • ${product.category ?: ""}", fontSize = 12.sp, color = TextSecondary)
                }

                if (!isInCart) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                        border = BorderStroke(1.dp, BrandRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    IconButton(onClick = onRemoveFromCart, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = BrandRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (isInCart && cartItem != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Qty: ", fontSize = 13.sp, color = TextSecondary)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (cartItem.quantity > 1) onQuantityChange(cartItem.quantity - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("−", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                }
                                Text(
                                    "${cartItem.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { onQuantityChange(cartItem.quantity + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandRed)
                                }
                            }
                        }
                    }

                    // Exchange toggle
                    Column(horizontalAlignment = Alignment.End) {
                        val withOldPrice = product.price - (product.exchangeDiscount ?: 0.0)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = cartItem.withExchange,
                                onClick = { onExchangeToggle(true) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currencyFormat.format(withOldPrice), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (cartItem.withExchange) BrandRed else TextSecondary)
                            Text(" (Old)", fontSize = 10.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !cartItem.withExchange,
                                onClick = { onExchangeToggle(false) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currencyFormat.format(product.price), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (!cartItem.withExchange) BrandRed else TextSecondary)
                            Text(" (New)", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandRedLight else Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, if (isSelected) BrandRed.copy(alpha = 0.3f) else BorderColor)
    ) {
        Text(
            text = label,
            color = if (isSelected) BrandRed else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
