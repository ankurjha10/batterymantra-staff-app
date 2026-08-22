package com.battery.mantra.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.R
import com.battery.mantra.ui.theme.BrandRed
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.ui.screens.admin.AdminViewModel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.LocalOffer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    ordersState: AdminDataState<List<com.battery.mantra.data.models.OrderResponse>>,
    partnersState: AdminDataState<List<com.battery.mantra.data.models.PartnerResponse>>,
    engineersState: AdminDataState<List<com.battery.mantra.data.models.EngineerResponse>>,
    usersState: AdminDataState<List<com.battery.mantra.data.models.UserResponse>>,
    citiesState: AdminDataState<List<com.battery.mantra.data.models.CityResponse>>,
    selectedTabIndex: Int,
    targetSearchQuery: String? = null,
    targetFilter: String? = null,
    unreadNotificationsCount: Int = 0,
    onTargetConsumed: () -> Unit = {},
    onTabSelected: (Int) -> Unit,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToCallbacks: () -> Unit = {},
    onNavigateToEnquiries: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onAssignPartner: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onAssignEngineer: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onCreatePartner: (com.battery.mantra.data.models.CreatePartnerRequest, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onCreateEngineer: (com.battery.mantra.data.models.CreateEngineerRequest, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFF8F9FA),
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.height(90.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "admin@batterymantra.com", 
                            fontSize = 14.sp,
                            color = Color(0xFF5F6368)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "OPERATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.PhoneCallback, contentDescription = null) },
                    label = { Text("Callbacks") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToCallbacks()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF424242),
                        unselectedTextColor = Color(0xFF212121)
                    )
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.QuestionAnswer, contentDescription = null) },
                    label = { Text("Enquiries") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToEnquiries()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF424242),
                        unselectedTextColor = Color(0xFF212121)
                    )
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                Text(
                    text = "MANAGEMENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.LocalOffer, contentDescription = null) },
                    label = { Text("Coupons") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToCoupons()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF424242),
                        unselectedTextColor = Color(0xFF212121)
                    )
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Inventory, contentDescription = null) },
                    label = { Text("Products Catalog") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToProducts()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF424242),
                        unselectedTextColor = Color(0xFF212121)
                    )
                )
                
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.ExitToApp, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).padding(bottom = 16.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Red,
                        unselectedTextColor = Color.Red
                    )
                )
            }
        }
    ) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Admin Hub", 
                        color = Color(0xFFD32F2F), 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ) 
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
                                    Badge(containerColor = Color.Red) {
                                        Text(unreadNotificationsCount.toString())
                                    }
                                } 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.Black
                            )
                        }            
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { onTabSelected(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Overview", modifier = Modifier.size(24.dp)) },
                    label = { Text("Home", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD32F2F),
                        indicatorColor = Color(0xFFD32F2F),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { onTabSelected(1) },
                    icon = { 
                        val pendingOrders = if (ordersState is AdminDataState.Success) {
                            ordersState.data.count { it.orderStatus == "PENDING" || it.orderStatus == "CONFIRMED" || it.orderStatus == "UNASSIGNED" }
                        } else 0
                        if (pendingOrders > 0) {
                            BadgedBox(badge = { Badge { Text(pendingOrders.toString()) } }) {
                                Icon(Icons.Default.List, contentDescription = "Orders", modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Icon(Icons.Default.List, contentDescription = "Orders", modifier = Modifier.size(24.dp))
                        }
                    },
                    label = { Text("Orders", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD32F2F),
                        indicatorColor = Color(0xFFD32F2F),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { onTabSelected(2) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Partners", modifier = Modifier.size(24.dp)) },
                    label = { Text("Partners", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD32F2F),
                        indicatorColor = Color(0xFFD32F2F),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { onTabSelected(3) },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = "Engineers", modifier = Modifier.size(24.dp)) },
                    label = { Text("Engineers", fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD32F2F),
                        indicatorColor = Color(0xFFD32F2F),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 4,
                    onClick = { onTabSelected(4) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users", modifier = Modifier.size(24.dp)) },
                    label = { Text("Users", fontWeight = if (selectedTabIndex == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD32F2F),
                        indicatorColor = Color(0xFFD32F2F),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    )
                )
            }
        },
        containerColor = BackgroundSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> AdminOverviewTab(ordersState, partnersState, engineersState, usersState)
                1 -> AdminOrdersTab(
                        ordersState = ordersState, 
                        partnersState = partnersState,
                        engineersState = engineersState,
                        targetSearchQuery = targetSearchQuery,
                        targetFilter = targetFilter,
                        onTargetConsumed = onTargetConsumed,
                        onAssignPartner = onAssignPartner,
                        onAssignEngineer = onAssignEngineer
                     )
                2 -> AdminPartnersTab(
                    partnersState = partnersState,
                    citiesState = citiesState,
                    onCreatePartner = onCreatePartner
                )
                3 -> AdminEngineersTab(
                    engineersState = engineersState,
                    partnersState = partnersState,
                    onCreateEngineer = onCreateEngineer
                )
                4 -> AdminUsersTab(usersState = usersState)
            }
        }
    }
}
}

