package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.data.repository.EngineerTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerDashboardScreen(
    uiState: EngineerDashboardState,
    selectedTabIndex: Int,
    isOnDuty: Boolean,
    onTabSelected: (Int) -> Unit,
    onDutyChange: (Boolean) -> Unit,
    onNavigateToJobExecution: (String) -> Unit = {}
) {
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "My Active Task", 
                        color = Color(0xFFD32F2F), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFFD32F2F))
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            text = if (isOnDuty) "DUTY ON" else "DUTY OFF", 
                            color = Color(0xFFD32F2F), 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isOnDuty,
                            onCheckedChange = onDutyChange,
                            modifier = Modifier.height(24.dp), // Adjust size if needed
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981), // Green
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                    label = { Text("Home") },
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
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "My Tasks", modifier = Modifier.size(24.dp)) },
                    label = { Text("My Tasks") },
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
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                    label = { Text("Profile") },
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
        containerColor = Color(0xFFF8F9FA) // Light gray background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is EngineerDashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD32F2F))
                    }
                }
                is EngineerDashboardState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (uiState as EngineerDashboardState.Error).message, color = Color.Red)
                    }
                }
                is EngineerDashboardState.Success -> {
                    val state = uiState as EngineerDashboardState.Success
                    when (selectedTabIndex) {
                        0 -> { /* Home Tab Content */ }
                        1 -> EngineerActiveJobsTab(
                            activeJobs = state.activeJobs,
                            onNavigateToJobExecution = onNavigateToJobExecution
                        )
                        2 -> EngineerHistoryTab(
                            historyJobs = state.historyJobs
                        )
                    }
                }
            }
        }
    }
}
