package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CategoryResponse
import com.battery.mantra.data.models.CityPricingRequestDto
import com.battery.mantra.data.models.CityPricingRequestResponse
import com.battery.mantra.data.models.CityProductStockItem
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerCityPricingUpdateDto
import com.battery.mantra.data.models.PartnerCreateProductRequest
import com.battery.mantra.data.models.PartnerInventoryItemResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.ui.theme.BrandRed
import kotlinx.coroutines.launch

private data class PartnerNavTab(
    val index: Int,
    val drawerLabel: String,
    val shortLabel: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    profileState: PartnerDataState<PartnerResponse>,
    ordersState: PartnerDataState<List<OrderResponse>>,
    engineersState: PartnerDataState<List<EngineerResponse>>,
    selectedCityId: String?,
    inventoryState: PartnerDataState<List<PartnerInventoryItemResponse>>,
    pendingRequestsState: PartnerDataState<List<CityPricingRequestResponse>>,
    brands: List<BrandResponse>,
    categories: List<CategoryResponse>,
    selectedTabIndex: Int,
    targetOrderFilter: String?,
    targetOrderSearch: String?,
    unreadNotificationsCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onNavigateToOrdersFiltered: (String) -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onConsumeOrderTarget: () -> Unit,
    onSelectCity: (String) -> Unit,
    onRetryProfile: () -> Unit,
    onRetryOrders: () -> Unit,
    onRetryEngineers: () -> Unit,
    onRetryInventory: () -> Unit,
    onRetryPendingRequests: () -> Unit,
    onAssignEngineer: (String, String, (OrderResponse) -> Unit, (String) -> Unit) -> Unit,
    onUnassignEngineer: (String, (OrderResponse) -> Unit, (String) -> Unit) -> Unit,
    onUpdateOrderStatus: (String, String, String?, (OrderResponse) -> Unit, (String) -> Unit) -> Unit,
    onAddOrderNote: (String, String, (OrderResponse) -> Unit, (String) -> Unit) -> Unit,
    onCreateEngineer: (CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateEngineer: (String, CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit,
    onToggleEngineerStatus: (EngineerResponse, Boolean, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateSingleStock: (String, String, Int, () -> Unit, (String) -> Unit) -> Unit,
    onBulkUpdateStock: (String, List<CityProductStockItem>, (String) -> Unit, (String) -> Unit) -> Unit,
    onSubmitPricingRequest: (String, CityPricingRequestDto, () -> Unit, (String) -> Unit) -> Unit,
    onUpdateDirectPricing: (String, PartnerCityPricingUpdateDto, () -> Unit, (String) -> Unit) -> Unit,
    onRequestNewProduct: (PartnerCreateProductRequest, () -> Unit, (String) -> Unit) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val profile = (profileState as? PartnerDataState.Success)?.data
    val orders = (ordersState as? PartnerDataState.Success)?.data.orEmpty()
    val actionNeededCount = orders.count { it.isActionNeeded() }

    val navTabs = listOf(
        PartnerNavTab(0, "Overview", "Overview", Icons.Outlined.Dashboard),
        PartnerNavTab(1, "Assigned Orders", "Orders", Icons.Outlined.ShoppingCart),
        PartnerNavTab(2, "My Engineers", "Engineers", Icons.Outlined.Engineering),
        PartnerNavTab(3, "Products & Pricing", "Pricing", Icons.Outlined.LocalOffer),
        PartnerNavTab(4, "Branch Inventory", "Inventory", Icons.Outlined.Inventory2)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                // Brand Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2))
                        .padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Partner Portal",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandRed
                            )
                            Text(
                                text = profile?.businessName?.takeIf { it.isNotBlank() } ?: "Branch Workspace",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    val signedInLabel = profile?.email?.takeIf { it.isNotBlank() }
                        ?: profile?.contactPerson?.takeIf { it.isNotBlank() }
                    if (!signedInLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Signed in as: $signedInLabel",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                navTabs.forEach { tab ->
                    val isSelected = selectedTabIndex == tab.index
                    NavigationDrawerItem(
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = {
                            Text(
                                text = tab.drawerLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        badge = {
                            if (tab.index == 1 && actionNeededCount > 0) {
                                Badge(containerColor = BrandRed) {
                                    Text(actionNeededCount.toString(), color = Color.White)
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onTabSelected(tab.index)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFFFEF2F2),
                            selectedIconColor = BrandRed,
                            selectedTextColor = BrandRed,
                            unselectedIconColor = Color(0xFF4B5563),
                            unselectedTextColor = Color(0xFF1F2937)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                    label = { Text("Change Password") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToChangePassword()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF4B5563),
                        unselectedTextColor = Color(0xFF1F2937)
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null) },
                    label = { Text("Sign out", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(bottom = 8.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = BrandRed,
                        unselectedTextColor = BrandRed
                    )
                )

                Text(
                    text = androidx.compose.ui.res.stringResource(com.battery.mantra.R.string.app_version),
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = profile?.businessName?.takeIf { it.isNotBlank() } ?: "Partner Portal",
                                color = BrandRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = navTabs.getOrNull(selectedTabIndex)?.drawerLabel ?: "Partner Workspace",
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotifications) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(containerColor = BrandRed) {
                                            Text(unreadNotificationsCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.Black
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    navTabs.forEach { tab ->
                        val isSelected = selectedTabIndex == tab.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onTabSelected(tab.index) },
                            icon = {
                                if (tab.index == 1 && actionNeededCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = BrandRed) {
                                                Text(actionNeededCount.toString(), color = Color.White)
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.drawerLabel, modifier = Modifier.size(22.dp))
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.drawerLabel, modifier = Modifier.size(22.dp))
                                }
                            },
                            label = {
                                Text(
                                    text = tab.shortLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = BrandRed,
                                indicatorColor = BrandRed,
                                unselectedIconColor = Color(0xFF5F6368),
                                unselectedTextColor = Color(0xFF5F6368)
                            )
                        )
                    }
                }
            },
            containerColor = BackgroundSurface
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> PartnerOverviewTab(
                        profileState = profileState,
                        ordersState = ordersState,
                        engineersState = engineersState,
                        onManageOrdersClick = { onTabSelected(1) },
                        onFilterOrdersClick = onNavigateToOrdersFiltered,
                        onManageEngineersClick = { onTabSelected(2) },
                        onOrderClick = { order ->
                            onNavigateToOrderDetails(order.orderId)
                        },
                        onRetryOrders = {
                            onRetryProfile()
                            onRetryOrders()
                        }
                    )
                    1 -> PartnerOrdersTab(
                        ordersState = ordersState,
                        engineersState = engineersState,
                        targetFilter = targetOrderFilter,
                        targetSearch = targetOrderSearch,
                        onTargetConsumed = onConsumeOrderTarget,
                        onRetry = onRetryOrders,
                        onAssignEngineer = onAssignEngineer,
                        onUnassignEngineer = onUnassignEngineer,
                        onUpdateStatus = onUpdateOrderStatus,
                        onAddNote = onAddOrderNote
                    )
                    2 -> PartnerEngineersTab(
                        engineersState = engineersState,
                        onRetry = onRetryEngineers,
                        onCreateEngineer = onCreateEngineer,
                        onUpdateEngineer = onUpdateEngineer,
                        onToggleEngineerStatus = onToggleEngineerStatus
                    )
                    3 -> PartnerProductsPricingTab(
                        profileState = profileState,
                        selectedCityId = selectedCityId,
                        inventoryState = inventoryState,
                        pendingRequestsState = pendingRequestsState,
                        brands = brands,
                        categories = categories,
                        onSelectCity = onSelectCity,
                        onRetryInventory = onRetryInventory,
                        onRetryPendingRequests = onRetryPendingRequests,
                        onSubmitPricingRequest = onSubmitPricingRequest,
                        onUpdateDirectPricing = onUpdateDirectPricing,
                        onRequestNewProduct = onRequestNewProduct
                    )
                    4 -> PartnerInventoryTab(
                        profileState = profileState,
                        selectedCityId = selectedCityId,
                        inventoryState = inventoryState,
                        onSelectCity = onSelectCity,
                        onRetry = onRetryInventory,
                        onUpdateSingleStock = onUpdateSingleStock,
                        onBulkUpdateStock = onBulkUpdateStock
                    )
                }
            }
        }
    }
}
