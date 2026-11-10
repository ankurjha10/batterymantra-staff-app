package com.battery.mantra.ui.screens.partner

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CityPricingRequestDto
import com.battery.mantra.data.models.CityPricingRequestResponse
import com.battery.mantra.data.models.PartnerCityPricingUpdateDto
import com.battery.mantra.data.models.PartnerInventoryItemResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun PartnerProductsPricingTab(
    profileState: PartnerDataState<PartnerResponse>,
    selectedCityId: String?,
    inventoryState: PartnerDataState<List<PartnerInventoryItemResponse>>,
    pendingRequestsState: PartnerDataState<List<CityPricingRequestResponse>>,
    brands: List<BrandResponse>,
    onSelectCity: (String) -> Unit,
    onRetryInventory: () -> Unit,
    onRetryPendingRequests: () -> Unit,
    onSubmitPricingRequest: (productId: String, CityPricingRequestDto, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateDirectPricing: (productId: String, PartnerCityPricingUpdateDto, () -> Unit, (String) -> Unit) -> Unit,
    onNavigateToProposeProduct: () -> Unit
) {
    val context = LocalContext.current
    val cities = (profileState as? PartnerDataState.Success)?.data?.operatingCities.orEmpty()
    val pendingRequests = (pendingRequestsState as? PartnerDataState.Success)?.data.orEmpty()

    var subTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var selectedItemForPricing by remember { mutableStateOf<PartnerInventoryItemResponse?>(null) }

    val items = (inventoryState as? PartnerDataState.Success)?.data.orEmpty()
    val brandNames = remember(items, brands) {
        val fromItems = items.mapNotNull { it.brandName?.takeIf { b -> b.isNotBlank() } }.distinct().sorted()
        if (fromItems.isNotEmpty()) fromItems else brands.map { it.brandName }.distinct().sorted()
    }

    val filteredItems = remember(items, searchQuery, selectedBrand) {
        val q = searchQuery.trim().lowercase()
        items.filter { item ->
            val matchesBrand = selectedBrand == null || item.brandName.equals(selectedBrand, ignoreCase = true)
            val matchesSearch = if (q.isEmpty()) true else {
                item.productName.lowercase().contains(q) ||
                    (item.brandName?.lowercase()?.contains(q) == true) ||
                    (item.capacity?.lowercase()?.contains(q) == true) ||
                    (item.productCategory?.lowercase()?.contains(q) == true)
            }
            matchesBrand && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OperatingCitySelectorRow(
                cities = cities,
                selectedCityId = selectedCityId,
                onSelectCity = onSelectCity
            )

            PrimaryTabRow(
                selectedTabIndex = subTabIndex,
                containerColor = Color.White,
                contentColor = BrandRed,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(subTabIndex),
                        color = BrandRed
                    )
                }
            ) {
                Tab(
                    selected = subTabIndex == 0,
                    onClick = { subTabIndex = 0 },
                    text = {
                        Text(
                            "Catalog & Pricing",
                            fontWeight = if (subTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = subTabIndex == 1,
                    onClick = { subTabIndex = 1 },
                    text = {
                        Text(
                            text = if (pendingRequests.isNotEmpty()) {
                                "Price Requests (${pendingRequests.size})"
                            } else {
                                "Price Requests"
                            },
                            fontWeight = if (subTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (subTabIndex == 0) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product, brand, capacity...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrandRed,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                if (brandNames.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedBrand == null,
                                onClick = { selectedBrand = null },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandRed.copy(alpha = 0.12f),
                                    selectedLabelColor = BrandRed
                                )
                            )
                        }
                        items(brandNames) { brand ->
                            FilterChip(
                                selected = selectedBrand == brand,
                                onClick = { selectedBrand = if (selectedBrand == brand) null else brand },
                                label = { Text(brand) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandRed.copy(alpha = 0.12f),
                                    selectedLabelColor = BrandRed
                                )
                            )
                        }
                    }
                }

                when (inventoryState) {
                    is PartnerDataState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandRed)
                        }
                    }
                    is PartnerDataState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(inventoryState.message, color = Color(0xFFB91C1C), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(onClick = onRetryInventory) {
                                    Text("Retry", color = BrandRed)
                                }
                            }
                        }
                    }
                    is PartnerDataState.Success -> {
                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No products found.", color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredItems, key = { it.productId }) { item ->
                                    PartnerProductPricingCard(
                                        item = item,
                                        onEditPriceClick = {
                                            if (selectedCityId == null) {
                                                Toast.makeText(context, "No operating city assigned", Toast.LENGTH_SHORT).show()
                                            } else {
                                                selectedItemForPricing = item
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (cities.isEmpty()) "No operating cities configured for your branch." else "Select a branch city to view pricing.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                when (pendingRequestsState) {
                    is PartnerDataState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandRed)
                        }
                    }
                    is PartnerDataState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(pendingRequestsState.message, color = Color(0xFFB91C1C), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(onClick = onRetryPendingRequests) {
                                    Text("Retry", color = BrandRed)
                                }
                            }
                        }
                    }
                    else -> {
                        if (pendingRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No pending price change requests.",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(pendingRequests, key = { it.requestId }) { req ->
                                    PendingPriceRequestCard(req)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (subTabIndex == 0) {
            ExtendedFloatingActionButton(
                onClick = onNavigateToProposeProduct,
                containerColor = BrandRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Propose Product", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    selectedItemForPricing?.let { item ->
        val cityId = selectedCityId ?: return@let
        val cityName = cities.find { it.cityId == cityId }?.cityName ?: "Selected City"
        CityPricingChangeDialog(
            item = item,
            cityId = cityId,
            cityName = cityName,
            onDismiss = { selectedItemForPricing = null },
            onSubmitRequest = { dto, onSuccess, onError ->
                onSubmitPricingRequest(item.productId, dto, onSuccess, onError)
            },
            onDirectUpdate = { dto, onSuccess, onError ->
                onUpdateDirectPricing(item.productId, dto, onSuccess, onError)
            }
        )
    }
}

@Composable
private fun PartnerProductPricingCard(
    item: PartnerInventoryItemResponse,
    onEditPriceClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgModifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6))
            if (!item.productImage.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(item.productImage).crossfade(true).build(),
                    contentDescription = item.productName,
                    modifier = imgModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = imgModifier, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val meta = listOfNotNull(
                    item.brandName?.takeIf { it.isNotBlank() },
                    item.capacity?.takeIf { it.isNotBlank() },
                    item.productCategory?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = meta, fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formatInr(item.productPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = BrandRed
                    )
                    if ((item.exchangeDiscount ?: 0.0) > 0.0) {
                        Text(
                            text = "Exch: -${formatInr(item.exchangeDiscount)}",
                            fontSize = 11.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Branch Stock: ${item.productStock} units",
                    fontSize = 12.sp,
                    color = if (item.productStock > 0) Color(0xFF374151) else Color(0xFFB91C1C),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onEditPriceClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                border = BorderStroke(1.dp, BrandRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Price", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PendingPriceRequestCard(req: CityPricingRequestResponse) {
    val (bg, textColor) = when (req.status.uppercase()) {
        "APPROVED" -> Color(0xFFD1FAE5) to Color(0xFF047857)
        "REJECTED" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = req.productName ?: "Product #${req.productId.take(6)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = bg) {
                    Text(
                        text = req.status.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "City: ${req.cityName ?: req.cityId.take(8)} • Requested: ${formatInr(req.price)}",
                fontSize = 13.sp,
                color = Color(0xFF374151),
                fontWeight = FontWeight.Medium
            )
            val extras = listOfNotNull(
                req.exchangeDiscount?.takeIf { it > 0 }?.let { "Exch Discount: ${formatInr(it)}" },
                req.stock?.let { "Stock: $it" }
            ).joinToString(" • ")
            if (extras.isNotBlank()) {
                Text(text = extras, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            if (!req.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Reason: ${req.reason}", fontSize = 12.sp, color = Color(0xFF4B5563))
            }
            if (!req.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rejection Note: ${req.rejectionReason}",
                    fontSize = 12.sp,
                    color = Color(0xFFB91C1C)
                )
            }
        }
    }
}

@Composable
private fun CityPricingChangeDialog(
    item: PartnerInventoryItemResponse,
    cityId: String,
    cityName: String,
    onDismiss: () -> Unit,
    onSubmitRequest: (CityPricingRequestDto, () -> Unit, (String) -> Unit) -> Unit,
    onDirectUpdate: (PartnerCityPricingUpdateDto, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var priceText by remember { mutableStateOf(item.productPrice.toString()) }
    var discountText by remember { mutableStateOf((item.exchangeDiscount ?: 0.0).toString()) }
    var stockText by remember { mutableStateOf(item.productStock.toString()) }
    var reason by remember { mutableStateOf("") }
    var useApprovalFlow by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        focusedLabelColor = BrandRed,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Update City Pricing", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text("${item.productName} • $cityName", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = useApprovalFlow,
                        onClick = { useApprovalFlow = true },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandRed)
                    )
                    Text(
                        text = "Submit for Admin Approval (Recommended)",
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.clickable { useApprovalFlow = true }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !useApprovalFlow,
                        onClick = { useApprovalFlow = false },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandRed)
                    )
                    Text(
                        text = "Direct Live City Update",
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.clickable { useApprovalFlow = false }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("City Price (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Exchange Discount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(if (useApprovalFlow) "Stock (Optional)" else "Branch Stock *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                if (useApprovalFlow) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Change (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = textFieldColors
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull()
                        val discount = discountText.toDoubleOrNull() ?: 0.0
                        val stock = stockText.toIntOrNull()
                        if (price == null || price < 0.01) {
                            Toast.makeText(context, "Enter a valid price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!useApprovalFlow && (stock == null || stock < 0)) {
                            Toast.makeText(context, "Enter a valid stock count", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true
                        if (useApprovalFlow) {
                            onSubmitRequest(
                                CityPricingRequestDto(
                                    cityId = cityId,
                                    price = price,
                                    exchangeDiscount = discount,
                                    stock = stock,
                                    reason = reason.trim().ifEmpty { null }
                                ),
                                {
                                    isSaving = false
                                    Toast.makeText(context, "Price change request submitted", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                { err ->
                                    isSaving = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            onDirectUpdate(
                                PartnerCityPricingUpdateDto(
                                    cityId = cityId,
                                    price = price,
                                    exchangeDiscount = discount,
                                    stock = stock ?: 0
                                ),
                                {
                                    isSaving = false
                                    Toast.makeText(context, "City pricing updated", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                { err ->
                                    isSaving = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text(
                        text = if (isSaving) "Submitting..." else if (useApprovalFlow) "Submit for Approval" else "Update Pricing",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

