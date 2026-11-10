package com.battery.mantra.ui.screens.partner

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CategoryResponse
import com.battery.mantra.data.models.CityPricingDto
import com.battery.mantra.data.models.PartnerCreateProductRequest
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.ui.theme.BrandRed
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private enum class ProposeSpecMode(val label: String) {
    VEHICLE("Vehicle"),
    TUBULAR_INVERTER("Tubular/Inverter Batt."),
    INVERTER_UPS("Inverter/UPS")
}

private const val DRAFT_PREFS = "partner_propose_product_draft"
private const val DRAFT_KEY = "draft_json"

private fun flattenCategoriesList(categories: List<CategoryResponse>): List<CategoryResponse> {
    val result = mutableListOf<CategoryResponse>()
    for (cat in categories) {
        result.add(cat)
        cat.subCategories?.let { result.addAll(flattenCategoriesList(it)) }
    }
    return result
}

private fun detectSpecModeFromCategory(categoryName: String?): ProposeSpecMode {
    val name = categoryName?.lowercase() ?: return ProposeSpecMode.TUBULAR_INVERTER
    return when {
        name.contains("tubular") || name.contains("solar") ||
            (name.contains("inverter") && name.contains("battery")) -> ProposeSpecMode.TUBULAR_INVERTER
        name.contains("inverter") || name.contains("ups") -> ProposeSpecMode.INVERTER_UPS
        else -> ProposeSpecMode.VEHICLE
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProposeProductScreen(
    profileState: PartnerDataState<PartnerResponse>,
    selectedCityId: String?,
    brands: List<BrandResponse>,
    categories: List<CategoryResponse>,
    onBackClick: () -> Unit,
    onSubmit: (PartnerCreateProductRequest, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(DRAFT_PREFS, Context.MODE_PRIVATE) }
    val flatCategories = remember(categories) { flattenCategoriesList(categories) }

    val profile = (profileState as? PartnerDataState.Success)?.data
    val cities = profile?.operatingCities.orEmpty()
    val branchCityName = cities.find { it.cityId == selectedCityId }?.cityName
        ?: cities.firstOrNull()?.cityName
        ?: "Your Branch"

    // Restore initial draft once
    val initialDraft = remember {
        val raw = prefs.getString(DRAFT_KEY, null)
        if (!raw.isNullOrBlank()) {
            try {
                JSONObject(raw)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    var selectedCategoryId by remember { mutableStateOf(initialDraft?.optString("categoryId")?.takeIf { it.isNotBlank() }) }
    var selectedBrandId by remember { mutableStateOf(initialDraft?.optString("brandId")?.takeIf { it.isNotBlank() }) }
    var productName by remember { mutableStateOf(initialDraft?.optString("productName") ?: "") }

    var specMode by remember {
        val savedMode = initialDraft?.optString("specMode")
        val mode = ProposeSpecMode.entries.find { it.name == savedMode } ?: ProposeSpecMode.TUBULAR_INVERTER
        mutableStateOf(mode)
    }
    var capacity by remember { mutableStateOf(initialDraft?.optString("capacity") ?: "") }
    var rlCode by remember { mutableStateOf(initialDraft?.optString("rlCode") ?: "") }
    var warranty by remember { mutableStateOf(initialDraft?.optString("warranty") ?: "") }
    var batteryTechnology by remember { mutableStateOf(initialDraft?.optString("batteryTechnology") ?: "") }

    var primaryImageUrl by remember { mutableStateOf(initialDraft?.optString("primaryImageUrl") ?: "") }
    var localPreviewUri by remember { mutableStateOf<Uri?>(null) }
    val galleryUrls = remember {
        mutableStateListOf<String>().apply {
            val arr = initialDraft?.optJSONArray("galleryUrls")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val v = arr.optString(i)
                    if (v.isNotBlank()) add(v)
                }
            }
        }
    }

    var bulletHighlights by remember { mutableStateOf(initialDraft?.optString("bulletHighlights") ?: "") }
    var description by remember { mutableStateOf(initialDraft?.optString("description") ?: "") }
    var searchKeywords by remember { mutableStateOf(initialDraft?.optString("searchKeywords") ?: "") }
    var proposalReason by remember { mutableStateOf(initialDraft?.optString("proposalReason") ?: "") }

    var sellingPriceText by remember { mutableStateOf(initialDraft?.optString("sellingPrice") ?: "") }
    var mrpText by remember { mutableStateOf(initialDraft?.optString("mrp") ?: "") }
    var exchangeDiscountText by remember { mutableStateOf(initialDraft?.optString("exchangeDiscount") ?: "") }
    var initialStockText by remember { mutableStateOf(initialDraft?.optString("initialStock")?.ifBlank { "10" } ?: "10") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var draftStatusText by remember {
        mutableStateOf(if (initialDraft != null) "Draft restored" else "Draft auto-saves locally")
    }

    val selectedCategory = flatCategories.find { it.categoryId.toString() == selectedCategoryId }
    val selectedBrand = brands.find { it.brandId.toString() == selectedBrandId }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            localPreviewUri = uri
            Toast.makeText(
                context,
                "Photo previewed. Please also paste an image URL below for submission.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Auto-save draft locally every 1.2 seconds when form edits pause
    LaunchedEffect(
        selectedCategoryId,
        selectedBrandId,
        productName,
        specMode,
        capacity,
        rlCode,
        warranty,
        batteryTechnology,
        primaryImageUrl,
        galleryUrls.toList(),
        bulletHighlights,
        description,
        searchKeywords,
        proposalReason,
        sellingPriceText,
        mrpText,
        exchangeDiscountText,
        initialStockText
    ) {
        delay(1200)
        val json = JSONObject().apply {
            put("categoryId", selectedCategoryId ?: "")
            put("brandId", selectedBrandId ?: "")
            put("productName", productName)
            put("specMode", specMode.name)
            put("capacity", capacity)
            put("rlCode", rlCode)
            put("warranty", warranty)
            put("batteryTechnology", batteryTechnology)
            put("primaryImageUrl", primaryImageUrl)
            put("galleryUrls", JSONArray(galleryUrls.toList()))
            put("bulletHighlights", bulletHighlights)
            put("description", description)
            put("searchKeywords", searchKeywords)
            put("proposalReason", proposalReason)
            put("sellingPrice", sellingPriceText)
            put("mrp", mrpText)
            put("exchangeDiscount", exchangeDiscountText)
            put("initialStock", initialStockText)
        }
        prefs.edit().putString(DRAFT_KEY, json.toString()).apply()
        draftStatusText = "Draft auto-saved locally"
    }

    fun clearAllDraft() {
        prefs.edit().remove(DRAFT_KEY).apply()
        selectedCategoryId = null
        selectedBrandId = null
        productName = ""
        specMode = ProposeSpecMode.TUBULAR_INVERTER
        capacity = ""
        rlCode = ""
        warranty = ""
        batteryTechnology = ""
        primaryImageUrl = ""
        localPreviewUri = null
        galleryUrls.clear()
        bulletHighlights = ""
        description = ""
        searchKeywords = ""
        proposalReason = ""
        sellingPriceText = ""
        mrpText = ""
        exchangeDiscountText = ""
        initialStockText = "10"
        draftStatusText = "Draft cleared"
    }

    val isCategoryAndBrandReady = selectedCategory != null && selectedBrand != null
    val isNameReady = productName.isNotBlank()
    val isImageReady = primaryImageUrl.isNotBlank()
    val parsedSellingPrice = sellingPriceText.toDoubleOrNull()
    val isPriceReady = parsedSellingPrice != null && parsedSellingPrice > 0.0
    val isReasonReady = proposalReason.isNotBlank()
    val isReadyToSubmit = isCategoryAndBrandReady && isNameReady && isImageReady && isPriceReady && isReasonReady

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandRed,
        unfocusedBorderColor = Color(0xFFD1D5DB),
        focusedLabelColor = BrandRed,
        unfocusedLabelColor = Color(0xFF4B5563),
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Propose New Product",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "Admin Approval",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Branch: $branchCityName • $draftStatusText",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    TextButton(onClick = { clearAllDraft() }, enabled = !isSubmitting) {
                        Text("Clear", color = BrandRed, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Submit complete battery specs, pricing & photos — Admin can review and publish with 1 click.",
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )

            // 1. Category, Brand & Product Title
            FormSectionCard(
                stepNumber = "1",
                title = "Category, Brand & Product Title",
                subtitle = "Choose the product category first — irrelevant fields automatically hide based on category."
            ) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.categoryName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Category *") },
                        placeholder = { Text("Select category (e.g. Car Battery, Inverter)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        flatCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.categoryName) },
                                onClick = {
                                    selectedCategoryId = cat.categoryId.toString()
                                    specMode = detectSpecModeFromCategory(cat.categoryName)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = brandExpanded,
                    onExpandedChange = { brandExpanded = !brandExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBrand?.brandName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Brand *") },
                        placeholder = { Text("Select brand (Exide, Amaron...)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = brandExpanded,
                        onDismissRequest = { brandExpanded = false }
                    ) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.brandName) },
                                onClick = {
                                    selectedBrandId = brand.brandId.toString()
                                    brandExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Full Product / Model Name *") },
                    placeholder = { Text("e.g. Luminous Red Charge RC 18000 (150Ah) Tall Tubular Battery") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }

            // 2. Technical Specs & Compatibility
            FormSectionCard(
                stepNumber = "2",
                title = "Technical Specs & Compatibility",
                subtitle = null
            ) {
                val modeBanner = when (specMode) {
                    ProposeSpecMode.VEHICLE ->
                        "Vehicle Battery Mode: Ah capacity, R/L Terminal layout & battery technology active."
                    ProposeSpecMode.TUBULAR_INVERTER ->
                        "Inverter / Tubular / Solar Battery Mode: Ah capacity & battery type active (R/L Code hidden)."
                    ProposeSpecMode.INVERTER_UPS ->
                        "Inverter / UPS Mode: VA rating & UPS technology active (R/L Code hidden)."
                }

                Surface(
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = modeBanner, fontSize = 12.sp, color = Color(0xFF1E3A8A))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProposeSpecMode.entries.forEach { mode ->
                        val active = specMode == mode
                        Surface(
                            onClick = { specMode = mode },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) BrandRed else Color(0xFFF3F4F6),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) Color.White else Color(0xFF374151),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val capacityLabel = if (specMode == ProposeSpecMode.INVERTER_UPS) {
                    "VA Capacity Rating"
                } else {
                    "Ah Capacity Rating"
                }
                val capacityChips = when (specMode) {
                    ProposeSpecMode.VEHICLE -> listOf("35Ah", "40Ah", "45Ah", "55Ah", "65Ah", "75Ah", "88Ah", "100Ah")
                    ProposeSpecMode.TUBULAR_INVERTER -> listOf("100Ah", "135Ah", "150Ah", "180Ah", "200Ah", "220Ah", "250Ah")
                    ProposeSpecMode.INVERTER_UPS -> listOf("700VA", "900VA", "1050VA", "1100VA", "1500VA", "2000VA", "3000VA")
                }

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text(capacityLabel) },
                    placeholder = { Text(if (specMode == ProposeSpecMode.INVERTER_UPS) "e.g. 1100VA" else "e.g. 150Ah, 200Ah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(6.dp))
                QuickPresetChips(
                    options = capacityChips,
                    selectedValue = capacity,
                    onSelect = { capacity = it }
                )

                if (specMode == ProposeSpecMode.VEHICLE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rlCode,
                        onValueChange = { rlCode = it },
                        label = { Text("R/L Terminal / Layout Code") },
                        placeholder = { Text("e.g. L (Left +ve), R (Right +ve), DIN50") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuickPresetChips(
                        options = listOf("L (Left +ve)", "R (Right +ve)", "DIN44", "DIN50", "DIN55", "DIN66"),
                        selectedValue = rlCode,
                        onSelect = { rlCode = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = warranty,
                    onValueChange = { warranty = it },
                    label = { Text("Manufacturer Warranty") },
                    placeholder = { Text("e.g. 48 Months (24 Free + 24 Pro-rata)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(6.dp))
                QuickPresetChips(
                    options = listOf(
                        "18 Months",
                        "24 Months",
                        "36 Months",
                        "48 Months (24F + 24P)",
                        "60 Months (36F + 24P)",
                        "72 Months"
                    ),
                    selectedValue = warranty,
                    onSelect = { warranty = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                val techLabel = if (specMode == ProposeSpecMode.INVERTER_UPS) {
                    "Inverter / UPS Technology"
                } else {
                    "Battery Technology / Plate Type"
                }
                val techChips = when (specMode) {
                    ProposeSpecMode.VEHICLE -> listOf("Maintenance Free (SMF)", "Calcium-Calcium", "EFB / Start-Stop", "AGM", "Lead Acid")
                    ProposeSpecMode.TUBULAR_INVERTER -> listOf("Tall Tubular", "Short Tubular", "Jumbo Tubular", "Flat Plate", "SMF / VRLA", "Solar Tubular")
                    ProposeSpecMode.INVERTER_UPS -> listOf("Pure Sine Wave", "Home UPS", "Solar Hybrid", "Online UPS")
                }

                OutlinedTextField(
                    value = batteryTechnology,
                    onValueChange = { batteryTechnology = it },
                    label = { Text(techLabel) },
                    placeholder = { Text("e.g. Tall Tubular, Short Tubular, SMF / VRLA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(6.dp))
                QuickPresetChips(
                    options = techChips,
                    selectedValue = batteryTechnology,
                    onSelect = { batteryTechnology = it }
                )
            }

            // 3. Product Photos
            FormSectionCard(
                stepNumber = "3",
                title = "Product Photos",
                subtitle = "Add clear front-facing product photo URLs (supports gallery preview + gallery URLs)."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewModel: Any? = when {
                        primaryImageUrl.isNotBlank() -> primaryImageUrl.trim()
                        localPreviewUri != null -> localPreviewUri
                        else -> null
                    }
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(previewModel).crossfade(true).build(),
                                contentDescription = "Primary Photo Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Gray)
                                Text("Preview", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Primary Product Image *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Front-facing photo with clear model/brand label.",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Photo (Preview)", fontSize = 12.sp)
                        }
                    }
                }

                if (localPreviewUri != null && primaryImageUrl.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Local photo selected for preview. Paste an HTTPS image URL below so Admin can view & publish it.",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = primaryImageUrl,
                    onValueChange = { primaryImageUrl = it },
                    label = { Text("Primary Image URL *") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                galleryUrls.forEachIndexed { idx, url ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { galleryUrls[idx] = it },
                            label = { Text("Gallery Photo ${idx + 1} URL") },
                            placeholder = { Text("https://...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors
                        )
                        IconButton(onClick = { galleryUrls.removeAt(idx) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove photo", tint = BrandRed)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { galleryUrls.add("") },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF374151))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Gallery Photo", fontSize = 12.sp, color = Color(0xFF374151))
                }
            }

            // 4. Description & Proposal Reason
            FormSectionCard(
                stepNumber = "4",
                title = "Description & Proposal Reason",
                subtitle = "Add customer-facing highlights plus your branch justification for Admin review."
            ) {
                OutlinedTextField(
                    value = bulletHighlights,
                    onValueChange = { bulletHighlights = it },
                    label = { Text("Key Bullet Highlights (One per line, optional)") },
                    placeholder = {
                        Text(
                            "Zero-maintenance sealed calcium-calcium technology\nHigh cranking power even in winters\nFactory charged & ready for doorstep installation"
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description (Optional)") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchKeywords,
                    onValueChange = { searchKeywords = it },
                    label = { Text("Search Keywords / Common Names (Optional)") },
                    placeholder = { Text("e.g. swift diesel battery, creta din50, amaron flo 50ah") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = proposalReason,
                    onValueChange = { proposalReason = it },
                    label = { Text("Reason for Proposal (Internal Note to Admin) *") },
                    placeholder = { Text("e.g. Customers regularly request this 2026 variant; stock available at distributor.") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }

            // 5. Pricing & Branch Stock
            FormSectionCard(
                stepNumber = "5",
                title = "Pricing & Branch Stock",
                subtitle = "Set your branch's proposed selling price, MRP, exchange discount and opening stock."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Proposed Selling Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it },
                        label = { Text("MRP / Original Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = exchangeDiscountText,
                        onValueChange = { exchangeDiscountText = it },
                        label = { Text("Old Battery Exchange Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = initialStockText,
                        onValueChange = { initialStockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Initial Branch Stock (Units)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scrap discount deducted when customer exchanges their old battery.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Storefront Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Storefront Preview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val previewModel: Any? = primaryImageUrl.trim().ifBlank { null } ?: localPreviewUri
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewModel != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(previewModel).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBrand?.brandName ?: "Brand",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandRed
                            )
                            Text(
                                text = productName.ifBlank { "Battery Model Name" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val specTags = listOfNotNull(
                                capacity.takeIf { it.isNotBlank() },
                                batteryTechnology.takeIf { it.isNotBlank() },
                                warranty.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")
                            if (specTags.isNotBlank()) {
                                Text(text = specTags, fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = formatInr(parsedSellingPrice ?: 0.0),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF111827)
                                )
                                val parsedMrp = mrpText.toDoubleOrNull()
                                if (parsedMrp != null && parsedMrp > (parsedSellingPrice ?: 0.0)) {
                                    Text(
                                        text = formatInr(parsedMrp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                        }
                    }

                    val highlightsPreview = bulletHighlights.lines().map { it.trim() }.filter { it.isNotBlank() }.take(3)
                    if (highlightsPreview.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(8.dp))
                        highlightsPreview.forEach { bullet ->
                            Text(text = "• $bullet", fontSize = 12.sp, color = Color(0xFF374151))
                        }
                    }
                }
            }

            // Readiness Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Submission Checklist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    ChecklistItem("Category & Brand selected", isCategoryAndBrandReady)
                    ChecklistItem("Product Name filled", isNameReady)
                    ChecklistItem("Primary Image URL provided", isImageReady)
                    ChecklistItem("Selling Price entered", isPriceReady)
                    ChecklistItem("Proposal Reason provided", isReasonReady)
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val category = selectedCategory
                    val brand = selectedBrand
                    val price = parsedSellingPrice
                    val stock = initialStockText.toIntOrNull() ?: 0
                    if (category == null || brand == null || productName.isBlank() || primaryImageUrl.isBlank() || price == null || price <= 0.0 || proposalReason.isBlank()) {
                        Toast.makeText(context, "Please complete all required (*) checklist fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val combinedDescription = buildStructuredDescription(
                        description = description,
                        bulletHighlights = bulletHighlights,
                        capacity = capacity,
                        rlCode = if (specMode == ProposeSpecMode.VEHICLE) rlCode else "",
                        warranty = warranty,
                        technology = batteryTechnology,
                        searchKeywords = searchKeywords,
                        proposalReason = proposalReason,
                        branchName = branchCityName
                    )

                    val parsedExchange = exchangeDiscountText.toDoubleOrNull()
                    val cityPricing = selectedCityId?.let { cityIdStr ->
                        try {
                            listOf(
                                CityPricingDto(
                                    cityId = UUID.fromString(cityIdStr),
                                    price = price,
                                    exchangeDiscount = parsedExchange ?: 0.0,
                                    stock = stock
                                )
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    val request = PartnerCreateProductRequest(
                        categoryId = category.categoryId.toString(),
                        brandId = brand.brandId.toString(),
                        productName = productName.trim(),
                        productImage = primaryImageUrl.trim(),
                        productStock = stock,
                        productPrice = price,
                        originalPrice = mrpText.toDoubleOrNull(),
                        exchangeDiscount = parsedExchange,
                        capacity = capacity.trim().ifEmpty { null },
                        productDescription = combinedDescription,
                        additionalImages = galleryUrls.map { it.trim() }.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() },
                        cityPrices = cityPricing
                    )

                    isSubmitting = true
                    onSubmit(
                        request,
                        {
                            isSubmitting = false
                            clearAllDraft()
                            Toast.makeText(context, "Product submitted for Admin approval!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        { err ->
                            isSubmitting = false
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = isReadyToSubmit && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Text(
                    text = if (isSubmitting) "Submitting Proposal..." else "Submit for Admin Approval",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormSectionCard(
    stepNumber: String,
    title: String,
    subtitle: String?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827)
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPresetChips(
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedValue.equals(option, ignoreCase = true)
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFFFEE2E2) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, if (isSelected) BrandRed else Color(0xFFD1D5DB))
            ) {
                Text(
                    text = option,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFFB91C1C) else Color(0xFF374151),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun ChecklistItem(label: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) Color(0xFF16A34A) else Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (done) Color(0xFF111827) else Color(0xFF6B7280),
            fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun buildStructuredDescription(
    description: String,
    bulletHighlights: String,
    capacity: String,
    rlCode: String,
    warranty: String,
    technology: String,
    searchKeywords: String,
    proposalReason: String,
    branchName: String
): String {
    val parts = mutableListOf<String>()
    if (description.isNotBlank()) {
        parts.add(description.trim())
    }
    val bullets = bulletHighlights.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (bullets.isNotEmpty()) {
        parts.add("Key Highlights:\n" + bullets.joinToString("\n") { "• $it" })
    }
    val specs = listOfNotNull(
        capacity.takeIf { it.isNotBlank() }?.let { "Capacity: $it" },
        rlCode.takeIf { it.isNotBlank() }?.let { "R/L Code: $it" },
        warranty.takeIf { it.isNotBlank() }?.let { "Warranty: $it" },
        technology.takeIf { it.isNotBlank() }?.let { "Technology: $it" }
    )
    if (specs.isNotEmpty()) {
        parts.add("Technical Specifications:\n" + specs.joinToString("\n"))
    }
    if (searchKeywords.isNotBlank()) {
        parts.add("Search Keywords: ${searchKeywords.trim()}")
    }
    parts.add("Partner Proposal Note ($branchName): ${proposalReason.trim()}")
    return parts.joinToString("\n\n")
}

