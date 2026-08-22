package com.battery.mantra.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.battery.mantra.BatteryMantraApp
import com.battery.mantra.ui.screens.admin.AdminDashboardScreen
import com.battery.mantra.ui.screens.admin.AdminViewModel
import com.battery.mantra.ui.screens.admin.AdminCallbacksScreen
import com.battery.mantra.ui.screens.admin.AdminEnquiriesScreen
import com.battery.mantra.ui.screens.admin.AdminCouponsScreen
import com.battery.mantra.ui.screens.admin.AdminProductsScreen
import com.battery.mantra.ui.screens.admin.AdminCallbacksViewModel
import com.battery.mantra.ui.screens.admin.AdminEnquiriesViewModel
import com.battery.mantra.ui.screens.admin.AdminCouponsViewModel
import com.battery.mantra.ui.screens.admin.AdminProductsViewModel
import com.battery.mantra.ui.screens.admin.EditProductScreen
import com.battery.mantra.ui.screens.admin.EditProductViewModel
import com.battery.mantra.ui.screens.auth.LoginScreen
import com.battery.mantra.ui.screens.auth.AuthViewModel
import com.battery.mantra.ui.screens.engineer.EngineerDashboardScreen
import com.battery.mantra.ui.screens.engineer.EngineerViewModel
import com.battery.mantra.ui.screens.engineer.JobExecutionScreen
import com.battery.mantra.ui.screens.partner.PartnerDashboardScreen
import com.battery.mantra.ui.screens.partner.PartnerViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as BatteryMantraApp).container
    
    val token by appContainer.tokenManager.jwtToken.collectAsState(initial = "LOADING")
    val role by appContainer.tokenManager.userRole.collectAsState(initial = "LOADING")
    val scope = rememberCoroutineScope()
    
    if (token == "LOADING" || role == "LOADING") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(token) {
        if (token.isNullOrEmpty() && token != "LOADING") {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    
    val startDest = if (token.isNullOrEmpty()) {
        Screen.Login.route
    } else {
        when {
            role.equals("ADMIN", ignoreCase = true) -> Screen.AdminDashboard.route
            role.equals("PARTNER", ignoreCase = true) -> Screen.PartnerDashboard.route
            role.equals("ENGINEER", ignoreCase = true) -> Screen.EngineerDashboard.route
            else -> Screen.AdminDashboard.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
    ) {
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.provideFactory(appContainer.authRepository)
            )
            val authState by authViewModel.authState.collectAsState()
            val loginUiState by authViewModel.loginUiState.collectAsState()

            LoginScreen(
                authState = authState,
                loginUiState = loginUiState,
                onIdentifierChange = { authViewModel.updateIdentifier(it) },
                onPasswordChange = { authViewModel.updatePassword(it) },
                onTogglePasswordVisibility = { authViewModel.togglePasswordVisibility() },
                onLoginClick = { authViewModel.login(loginUiState.identifier, loginUiState.password) },
                onLoginSuccess = { role ->
                    val route = when (role.uppercase()) {
                        "ADMIN" -> Screen.AdminDashboard.route
                        "ENGINEER" -> Screen.EngineerDashboard.route
                        "PARTNER" -> Screen.PartnerDashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onResetState = { authViewModel.resetState() }
            )
        }
        
        composable(Screen.AdminDashboard.route) {
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModel.provideFactory(appContainer.adminRepository, appContainer.tokenManager)
            )
            val ordersState by adminViewModel.ordersState.collectAsState()
            val partnersState by adminViewModel.partnersState.collectAsState()
            val engineersState by adminViewModel.engineersState.collectAsState()
            val usersState by adminViewModel.usersState.collectAsState()
            val citiesState by adminViewModel.citiesState.collectAsState()
            val selectedTabIndex by adminViewModel.selectedTabIndex.collectAsState()

            val targetSearchQuery by adminViewModel.targetOrderSearchQuery.collectAsState()
            val targetFilter by adminViewModel.targetOrderFilter.collectAsState()
            val notificationsState by adminViewModel.notificationsState.collectAsState()
            val unreadNotificationsCount = (notificationsState as? com.battery.mantra.ui.screens.admin.AdminDataState.Success)?.data?.size ?: 0

            AdminDashboardScreen(
                ordersState = ordersState,
                partnersState = partnersState,
                engineersState = engineersState,
                usersState = usersState,
                citiesState = citiesState,
                selectedTabIndex = selectedTabIndex,
                targetSearchQuery = targetSearchQuery,
                targetFilter = targetFilter,
                unreadNotificationsCount = unreadNotificationsCount,
                onTargetConsumed = { adminViewModel.clearTargetOrderState() },
                onTabSelected = { adminViewModel.onTabSelected(it) },
                onNavigateToUsers = { /* Optional */ },
                onNavigateToProducts = { navController.navigate("admin_products") },
                onNavigateToCoupons = { navController.navigate("admin_coupons") },
                onNavigateToCallbacks = { navController.navigate("admin_callbacks") },
                onNavigateToEnquiries = { navController.navigate("admin_enquiries") },
                onNavigateToNotifications = { navController.navigate("admin_notifications") },
                onNavigateToAssignEngineer = { orderId -> 
                    /* We'll handle order assignment later */ 
                },
                onCreatePartner = { request, onSuccess, onError ->
                    adminViewModel.createPartner(request, onSuccess, onError)
                },
                onCreateEngineer = { request, onSuccess, onError ->
                    adminViewModel.createEngineer(request, onSuccess, onError)
                },
                onLogout = {
                    scope.launch {
                        appContainer.tokenManager.clearTokens()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("admin_users") {
            PlaceholderScreen("Users Management")
        }
        
        composable("admin_products") {
            val viewModel: AdminProductsViewModel = viewModel(
                factory = AdminProductsViewModel.provideFactory(appContainer.adminRepository)
            )
            val uiState by viewModel.uiState.collectAsState()
            AdminProductsScreen(
                uiState = uiState, 
                onBackClick = { navController.navigateUp() },
                onBrandSelected = { brandId, brandName -> viewModel.onBrandSelected(brandId, brandName) },
                onLoadMore = { viewModel.loadMore() },
                onEditProduct = { product ->
                    navController.navigate("admin_edit_product/${product.id}")
                }
            )
        }

        composable("admin_edit_product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            val viewModel: EditProductViewModel = viewModel(
                factory = EditProductViewModel.provideFactory(appContainer.adminRepository, productId)
            )
            val uiState by viewModel.uiState.collectAsState()
            EditProductScreen(
                uiState = uiState,
                onBackClick = { navController.navigateUp() },
                onProductNameChange = viewModel::onProductNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onSellingPriceChange = viewModel::onSellingPriceChange,
                onOriginalPriceChange = viewModel::onOriginalPriceChange,
                onExchangeDiscountChange = viewModel::onExchangeDiscountChange,
                onStockQuantityChange = viewModel::onStockQuantityChange,
                onCapacityChange = viewModel::onCapacityChange,
                onBrandSelected = viewModel::onBrandSelected,
                onCategorySelected = viewModel::onCategorySelected,
                onAutoAssignChanged = viewModel::onAutoAssignChanged,
                onSeoSlugChange = viewModel::onSeoSlugChange,
                onSeoTitleChange = viewModel::onSeoTitleChange,
                onSeoKeywordsChange = viewModel::onSeoKeywordsChange,
                onSeoDescriptionChange = viewModel::onSeoDescriptionChange,
                onSeoTitleCityChange = viewModel::onSeoTitleCityChange,
                onSeoKeywordsCityChange = viewModel::onSeoKeywordsCityChange,
                onSeoDescriptionCityChange = viewModel::onSeoDescriptionCityChange,
                onOgTitleChange = viewModel::onOgTitleChange,
                onOgDescriptionChange = viewModel::onOgDescriptionChange,
                onOgTitleCityChange = viewModel::onOgTitleCityChange,
                onOgDescriptionCityChange = viewModel::onOgDescriptionCityChange,
                onSaveClick = viewModel::saveProduct,
                onSaveSuccessAck = viewModel::clearSaveSuccess,
                onInitialHtmlFormatted = viewModel::onInitialHtmlFormatted
            )
        }
        
        composable("admin_coupons") {
            val viewModel: AdminCouponsViewModel = viewModel(
                factory = AdminCouponsViewModel.provideFactory(appContainer.adminRepository)
            )
            AdminCouponsScreen(viewModel = viewModel, onBackClick = { navController.navigateUp() })
        }
        
        composable("admin_callbacks") {
            val viewModel: AdminCallbacksViewModel = viewModel(
                factory = AdminCallbacksViewModel.provideFactory(appContainer.adminRepository)
            )
            AdminCallbacksScreen(viewModel = viewModel, onBackClick = { navController.navigateUp() })
        }
        
        composable("admin_enquiries") {
            val viewModel: AdminEnquiriesViewModel = viewModel(
                factory = AdminEnquiriesViewModel.provideFactory(appContainer.adminRepository)
            )
            AdminEnquiriesScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable("admin_notifications") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.AdminDashboard.route)
            }
            val viewModel: AdminViewModel = viewModel(
                parentEntry,
                factory = AdminViewModel.provideFactory(appContainer.adminRepository, appContainer.tokenManager)
            )
            com.battery.mantra.ui.screens.admin.NotificationsScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Screen.PartnerDashboard.route) {
            val viewModel: PartnerViewModel = viewModel()
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
            
            PartnerDashboardScreen(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        }
        
        composable(Screen.EngineerDashboard.route) {
            val engineerViewModel: EngineerViewModel = viewModel(
                factory = EngineerViewModel.provideFactory(appContainer.engineerRepository)
            )
            val uiState by engineerViewModel.uiState.collectAsState()
            val selectedTabIndex by engineerViewModel.selectedTabIndex.collectAsState()
            val isOnDuty by engineerViewModel.isOnDuty.collectAsState()

            EngineerDashboardScreen(
                uiState = uiState,
                selectedTabIndex = selectedTabIndex,
                isOnDuty = isOnDuty,
                onTabSelected = { engineerViewModel.onTabSelected(it) },
                onDutyChange = { engineerViewModel.setDutyStatus(it) },
                onNavigateToJobExecution = { jobId ->
                    navController.navigate("${Screen.JobExecution.route}/$jobId")
                }
            )
        }
        
        composable(Screen.JobExecution.route) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobExecutionScreen(
                jobId = jobId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(title) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            Text("This screen is under construction.")
        }
    }
}
