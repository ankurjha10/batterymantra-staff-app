package com.battery.mantra.ui.screens.partner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.ui.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandRed
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { onTabSelected(0) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandRed,
                        selectedTextColor = BrandRed,
                        indicatorColor = BrandRed.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { onTabSelected(1) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Engineers") },
                    label = { Text("Engineers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandRed,
                        selectedTextColor = BrandRed,
                        indicatorColor = BrandRed.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { onTabSelected(2) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandRed,
                        selectedTextColor = BrandRed,
                        indicatorColor = BrandRed.copy(alpha = 0.1f)
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
                0 -> PartnerOrdersTab()
                1 -> PartnerEngineersTab()
                2 -> PartnerInventoryTab()
            }
        }
    }
}
