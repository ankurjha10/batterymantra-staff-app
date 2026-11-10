package com.battery.mantra.ui.screens.partner

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.battery.mantra.data.models.CityProductStockItem
import com.battery.mantra.data.models.PartnerInventoryItemResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.ui.theme.BrandRed

@Composable
fun PartnerInventoryTab(
    profileState: PartnerDataState<PartnerResponse>,
    selectedCityId: String?,
    inventoryState: PartnerDataState<List<PartnerInventoryItemResponse>>,
    onSelectCity: (String) -> Unit,
    onRetry: () -> Unit,
    onUpdateSingleStock: (productId: String, cityId: String, stock: Int, () -> Unit, (String) -> Unit) -> Unit,
    onBulkUpdateStock: (cityId: String, updates: List<CityProductStockItem>, (String) -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val cities = (profileState as? PartnerDataState.Success)?.data?.operatingCities.orEmpty()
    val items = (inventoryState as? PartnerDataState.Success)?.data.orEmpty()

    var searchQuery by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf("All") }
    val pendingEdits = remember(selectedCityId) { mutableStateMapOf<String, Int>() }
    var savingProductId by remember { mutableStateOf<String?>(null) }
    var isBulkSaving by remember { mutableStateOf(false) }

    val filters = listOf("All", "In Stock", "Low Stock", "Out of Stock")

    val filteredItems = remember(items, searchQuery, stockFilter) {
        val q = searchQuery.trim().lowercase()
        items.filter { item ->
            val matchesSearch = if (q.isEmpty()) true else {
                item.productName.lowercase().contains(q) ||
                    (item.brandName?.lowercase()?.contains(q) == true) ||
                    (item.capacity?.lowercase()?.contains(q) == true)
            }
            val matchesFilter = when (stockFilter) {
                "In Stock" -> item.productStock > 3
                "Low Stock" -> item.productStock in 1..3
                "Out of Stock" -> item.productStock <= 0
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val totalSkus = items.size
    val totalUnits = items.sumOf { it.productStock }
    val outOfStockCount = items.count { it.productStock <= 0 }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OperatingCitySelectorRow(
                cities = cities,
                selectedCityId = selectedCityId,
                onSelectCity = onSelectCity
            )

            // Summary KPI Row
            if (inventoryState is PartnerDataState.Success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryKpiChip("Total SKUs", totalSkus.toString(), Color(0xFF1E293B), Modifier.weight(1f))
                    InventoryKpiChip("Total Units", totalUnits.toString(), Color(0xFF047857), Modifier.weight(1f))
                    InventoryKpiChip("Out of Stock", outOfStockCount.toString(), Color(0xFFB91C1C), Modifier.weight(1f))
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search branch stock...", color = Color.Gray, fontSize = 14.sp) },
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = stockFilter == filter
                    Surface(
                        onClick = { stockFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFFFEE2E2) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) BrandRed else Color(0xFFD1D5DB)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFB91C1C) else Color(0xFF4B5563)
                            )
                        }
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
                            OutlinedButton(onClick = onRetry) {
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
                            Text("No inventory items found.", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        val bottomPadding = if (pendingEdits.isNotEmpty()) 88.dp else 24.dp
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredItems, key = { it.productId }) { item ->
                                val draftStock = pendingEdits[item.productId] ?: item.productStock
                                val isModified = pendingEdits.containsKey(item.productId) &&
                                    pendingEdits[item.productId] != item.productStock

                                InventoryStockItemCard(
                                    item = item,
                                    draftStock = draftStock,
                                    isModified = isModified,
                                    isSaving = savingProductId == item.productId,
                                    onStockChange = { newVal ->
                                        val clamped = newVal.coerceAtLeast(0)
                                        if (clamped == item.productStock) {
                                            pendingEdits.remove(item.productId)
                                        } else {
                                            pendingEdits[item.productId] = clamped
                                        }
                                    },
                                    onSaveSingle = {
                                        val cityId = selectedCityId ?: return@InventoryStockItemCard
                                        savingProductId = item.productId
                                        onUpdateSingleStock(
                                            item.productId,
                                            cityId,
                                            draftStock,
                                            {
                                                savingProductId = null
                                                pendingEdits.remove(item.productId)
                                                Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show()
                                            },
                                            { err ->
                                                savingProductId = null
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
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
                            text = if (cities.isEmpty()) "No operating cities configured for your branch." else "Select a branch city to manage inventory.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Sticky Bulk Update Bar
        if (pendingEdits.isNotEmpty() && selectedCityId != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color(0xFF111827),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pendingEdits.size} unsaved change${if (pendingEdits.size > 1) "s" else ""}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { pendingEdits.clear() },
                            enabled = !isBulkSaving
                        ) {
                            Text("Discard", color = Color(0xFFD1D5DB))
                        }
                        Button(
                            onClick = {
                                val updates = pendingEdits.entries.map {
                                    CityProductStockItem(productId = it.key, stock = it.value)
                                }
                                isBulkSaving = true
                                onBulkUpdateStock(
                                    selectedCityId,
                                    updates,
                                    { msg ->
                                        isBulkSaving = false
                                        pendingEdits.clear()
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    { err ->
                                        isBulkSaving = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isBulkSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isBulkSaving) "Saving..." else "Save All",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryKpiChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 17.sp, color = valueColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun InventoryStockItemCard(
    item: PartnerInventoryItemResponse,
    draftStock: Int,
    isModified: Boolean,
    isSaving: Boolean,
    onStockChange: (Int) -> Unit,
    onSaveSingle: () -> Unit
) {
    val context = LocalContext.current
    val (stockBadgeText, stockBadgeBg, stockBadgeColor) = when {
        item.productStock <= 0 -> Triple("OUT OF STOCK", Color(0xFFFEE2E2), Color(0xFFB91C1C))
        item.productStock <= 3 -> Triple("LOW (${item.productStock})", Color(0xFFFEF3C7), Color(0xFFB45309))
        else -> Triple("IN STOCK (${item.productStock})", Color(0xFFD1FAE5), Color(0xFF047857))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isModified) BrandRed else Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imgModifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                        fontSize = 14.sp,
                        color = Color(0xFF111827),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = listOfNotNull(
                        item.brandName?.takeIf { it.isNotBlank() },
                        item.capacity?.takeIf { it.isNotBlank() },
                        formatInr(item.productPrice)
                    ).joinToString(" • ")
                    Text(text = meta, fontSize = 12.sp, color = Color(0xFF6B7280))
                }

                Surface(shape = RoundedCornerShape(50), color = stockBadgeBg) {
                    Text(
                        text = stockBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockBadgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { onStockChange(draftStock - 1) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }

                    OutlinedTextField(
                        value = draftStock.toString(),
                        onValueChange = { str ->
                            val parsed = str.filter { it.isDigit() }.toIntOrNull() ?: 0
                            onStockChange(parsed)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(74.dp)
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRed,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Surface(
                        onClick = { onStockChange(draftStock + 1) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (isModified) {
                    Button(
                        onClick = onSaveSingle,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSaving) "..." else "Save",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
