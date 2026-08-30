package com.battery.mantra.ui.screens.engineer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.battery.mantra.ui.theme.BackgroundSurface
import com.battery.mantra.data.models.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerDashboardScreen(
    uiState: EngineerDashboardState,
    profile: UserResponse?,
    attendance: List<com.battery.mantra.data.models.AttendanceResponse>,
    leaves: List<com.battery.mantra.data.models.LeaveRequestResponse>,
    selectedTabIndex: Int,
    isOnDuty: Boolean,
    onTabSelected: (Int) -> Unit,
    onDutyChange: (Boolean) -> Unit,
    onNavigateToJobExecution: (String) -> Unit = {},
    onCallClick: (String, String) -> Unit = { _, _ -> },
    onCheckIn: () -> Unit = {},
    onCheckOut: () -> Unit = {},
    onApplyLeave: (String, String, String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Engineer Menu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    label = { Text("Home", color = Color.Black, fontSize = 16.sp) },
                    selected = false,
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    label = { Text("Profile", color = Color.Black, fontSize = 16.sp) },
                    selected = false,
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    onClick = {
                        scope.launch { drawerState.close() }
                        onTabSelected(4)
                    }
                )
                
                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    label = { Text("Logout", color = Color.Black, fontSize = 16.sp) },
                    selected = false,
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFFD32F2F))
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
                    icon = { Icon(Icons.Default.History, contentDescription = "History", modifier = Modifier.size(24.dp)) },
                    label = { Text("History") },
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
                    icon = { Icon(Icons.Default.Event, contentDescription = "Leaves", modifier = Modifier.size(24.dp)) },
                    label = { Text("Leaves") },
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
                        0 -> EngineerHomeTab(
                            attendance = attendance,
                            onCheckIn = onCheckIn,
                            onCheckOut = onCheckOut
                        )
                        1 -> EngineerActiveJobsTab(
                            activeJobs = state.activeJobs,
                            onNavigateToJobExecution = onNavigateToJobExecution,
                            onCallClick = onCallClick
                        )
                        2 -> EngineerHistoryTab(
                            historyJobs = state.historyJobs
                        )
                        3 -> LeaveManagementTab(
                            leaves = leaves,
                            onApplyLeaveClick = onApplyLeave
                        )
                        4 -> EngineerProfileTab(
                            profile = profile
                        )
                    }
                }
            }
        }
        }
    }
}
