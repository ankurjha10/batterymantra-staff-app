package com.battery.mantra.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
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
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CategoryResponse

private val BrandRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    uiState: EditProductUiState,
    onBackClick: () -> Unit,
    onProductNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSellingPriceChange: (String) -> Unit,
    onOriginalPriceChange: (String) -> Unit,
    onExchangeDiscountChange: (String) -> Unit,
    onStockQuantityChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onBrandSelected: (String, String) -> Unit,
    onCategorySelected: (String, String) -> Unit,
    onAutoAssignChanged: (Boolean) -> Unit,
    onSeoSlugChange: (String) -> Unit,
    onSeoTitleChange: (String) -> Unit,
    onSeoKeywordsChange: (String) -> Unit,
    onSeoDescriptionChange: (String) -> Unit,
    onSeoTitleCityChange: (String) -> Unit,
    onSeoKeywordsCityChange: (String) -> Unit,
    onSeoDescriptionCityChange: (String) -> Unit,
    onOgTitleChange: (String) -> Unit,
    onOgDescriptionChange: (String) -> Unit,
    onOgTitleCityChange: (String) -> Unit,
    onOgDescriptionCityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onSaveSuccessAck: () -> Unit,
    onInitialHtmlFormatted: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Intercept back button if there are unsaved changes
    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(text = "Unsaved Changes", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text("You have unsaved changes. Are you sure you want to discard them and go back?") },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onBackClick() // Proceed with going back
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Discard & Go Back")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUnsavedDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Show toast and navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Product updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
            onSaveSuccessAck()
            onBackClick()
        }
    }

    // Show snackbar on error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && !uiState.isLoading) {
            snackbarHostState.showSnackbar("Error: ${uiState.error}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hasUnsavedChanges) {
                            showUnsavedDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = !uiState.isSaving && !uiState.isLoading && uiState.hasUnsavedChanges
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrandRed,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save, 
                                contentDescription = "Save", 
                                tint = if (!uiState.isSaving && !uiState.isLoading && uiState.hasUnsavedChanges) BrandRed else Color.LightGray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Bottom bar removed
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandRed)
            }
        } else if (uiState.product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Failed to load product.", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // === GENERAL INFORMATION ===
                SectionCard(title = "General Information") {
                    FormTextField(
                        label = "Product Name *",
                        value = uiState.productName,
                        onValueChange = onProductNameChange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Product Description",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        ProductDescriptionEditor(
                            initialHtml = uiState.productDescription ?: "",
                            onHtmlChange = onDescriptionChange,
                            onInitialHtmlFormatted = onInitialHtmlFormatted
                        )
                    }
                    
                    Text(
                        text = "You can use variables: {city_name}, {product_name}, {brand_name}, {category_name}, {manufacturer_name}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // === MEDIA (Read-only) ===
                SectionCard(title = "Media") {
                    Text("Main Image and additional gallery images.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Primary Image", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!uiState.product.productImage.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uiState.product.productImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F0F0)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No image", color = Color.Gray)
                        }
                    }

                    // Additional images
                    val additionalImages = uiState.product.additionalImages
                    if (!additionalImages.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Additional Images (${additionalImages.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            additionalImages.take(4).forEach { imgUrl ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imgUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Additional Image",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF0F0F0)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // === SPECIFICATIONS (Read-only) ===
                val specs = uiState.product.specs
                if (specs != null && specs.isNotEmpty()) {
                    SectionCard(title = "Specifications") {
                        Text(
                            text = "Read-only. Edit specifications from the web admin panel.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        specs.forEach { (category, specMap) ->
                            Text(
                                text = category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            if (specMap is Map<*, *>) {
                                specMap.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color(0xFFFAFAFA), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = key?.toString() ?: "",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = value?.toString() ?: "—",
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))
                        }
                    }
                }

                // === PRICING & INVENTORY ===
                SectionCard(title = "Pricing & Inventory") {
                    FormTextField(
                        label = "Selling Price (₹) *",
                        value = uiState.sellingPrice,
                        onValueChange = onSellingPriceChange,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(
                        label = "Original Price / MRP (₹)",
                        value = uiState.originalPrice,
                        onValueChange = onOriginalPriceChange,
                        keyboardType = KeyboardType.Number,
                        helperText = "Strikethrough price. Leave 0 if none."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(
                        label = "Price Old Item (₹)",
                        value = uiState.exchangeDiscount,
                        onValueChange = onExchangeDiscountChange,
                        keyboardType = KeyboardType.Number,
                        helperText = "Discount amount for old battery (Scrap Value)."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(
                        label = "Stock Quantity",
                        value = uiState.stockQuantity,
                        onValueChange = onStockQuantityChange,
                        keyboardType = KeyboardType.Number
                    )
                }

                // === ORGANIZATION ===
                SectionCard(title = "Organization") {
                    // Category Dropdown
                    DropdownSelector(
                        label = "Category *",
                        selectedValue = uiState.selectedCategoryName,
                        options = uiState.categories.map { it.categoryId.toString() to it.categoryName },
                        onSelected = { id, name -> onCategorySelected(id, name) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brand Dropdown
                    DropdownSelector(
                        label = "Brand *",
                        selectedValue = uiState.selectedBrandName,
                        options = uiState.brands.map { it.brandId.toString() to it.brandName },
                        onSelected = { id, name -> onBrandSelected(id, name) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-assign toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.isAutoAssignToPartner,
                            onCheckedChange = onAutoAssignChanged,
                            colors = CheckboxDefaults.colors(checkedColor = BrandRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto-Assign Order to Partner",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                            Text(
                                "When checked, orders for this product will be automatically forwarded to the local partner based on the customer's city.",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                // === SEO INFORMATION ===
                SectionCard(title = "SEO Information") {
                    Text(
                        "Configure search engine optimization for this product",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FormTextField(
                        label = "Product URL (Slug)",
                        value = uiState.seoSlug,
                        onValueChange = onSeoSlugChange,
                        helperText = "Leave blank to auto-generate from name. e.g., exide-mileage-ml38b20l-battery"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "SEO Title", value = uiState.seoTitle, onValueChange = onSeoTitleChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "Search / SEO Keywords", value = uiState.seoKeywords, onValueChange = onSeoKeywordsChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "SEO Description", value = uiState.seoDescription, onValueChange = onSeoDescriptionChange, minLines = 2)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                    Text("City Variants", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    Text("Use {city_name}, {delivery_time} variables", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                    FormTextField(label = "SEO Title City", value = uiState.seoTitleCity, onValueChange = onSeoTitleCityChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "SEO Keywords City", value = uiState.seoKeywordsCity, onValueChange = onSeoKeywordsCityChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "SEO Description City", value = uiState.seoDescriptionCity, onValueChange = onSeoDescriptionCityChange, minLines = 2)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                    Text("Open Graph", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    FormTextField(label = "OG Title", value = uiState.ogTitle, onValueChange = onOgTitleChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "OG Description", value = uiState.ogDescription, onValueChange = onOgDescriptionChange, minLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "OG Title City", value = uiState.ogTitleCity, onValueChange = onOgTitleCityChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormTextField(label = "OG Description City", value = uiState.ogDescriptionCity, onValueChange = onOgDescriptionCityChange, minLines = 2)
                }

                // Bottom padding for the bottom bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// === Reusable Components ===

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    helperText: String? = null,
    minLines: Int = 1,
    maxLines: Int = if (minLines > 1) minLines else 1,
    readOnly: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandRed,
                unfocusedBorderColor = Color(0xFFDDDDDD),
                focusedLabelColor = BrandRed,
                cursorColor = BrandRed
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
        )
        if (helperText != null) {
            Text(
                text = helperText,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>, // id to name
    onSelected: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name, color = Color.Black) },
                        onClick = {
                            onSelected(id, name)
                            expanded = false
                        },
                        leadingIcon = if (name == selectedValue) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDescriptionEditor(
    initialHtml: String,
    onHtmlChange: (String) -> Unit,
    onInitialHtmlFormatted: (String) -> Unit
) {
    val state = com.mohamedrejeb.richeditor.model.rememberRichTextState()
    var isInitialized by remember { mutableStateOf(false) }
    var skipFirst by remember { mutableStateOf(true) }

    // Initialize HTML once
    LaunchedEffect(initialHtml) {
        if (!isInitialized && initialHtml.isNotEmpty()) {
            state.setHtml(initialHtml)
            isInitialized = true
        }
    }

    // Report changes back, but skip the first automatic emission
    LaunchedEffect(state.annotatedString) {
        if (isInitialized) {
            if (skipFirst) {
                skipFirst = false
                onInitialHtmlFormatted(state.toHtml())
            } else {
                onHtmlChange(state.toHtml())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(4.dp))
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("B", fontWeight = FontWeight.Bold, color = if (state.currentSpanStyle.fontWeight == FontWeight.Bold) BrandRed else Color.Black)
            }
            IconButton(
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = if (state.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic) BrandRed else Color.Black)
            }
            IconButton(
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("U", textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, color = if (state.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.Underline) == true) BrandRed else Color.Black)
            }
            // Basic Headers
            IconButton(
                onClick = { state.toggleParagraphStyle(androidx.compose.ui.text.ParagraphStyle()) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("P", color = Color.Black)
            }
        }
        HorizontalDivider(color = Color(0xFFDDDDDD))
        
        // Editor
        com.mohamedrejeb.richeditor.ui.material3.RichTextEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            colors = com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults.richTextEditorColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandRed
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
        )
    }
}
