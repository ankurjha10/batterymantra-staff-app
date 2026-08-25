package com.battery.mantra.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    
    object AdminDashboard : Screen("admin_dashboard")
    object AdminOrders : Screen("admin_orders")
    object AdminPartners : Screen("admin_partners")
    object AdminApprovals : Screen("admin_approvals")
    object AdminLeaveRequests : Screen("admin_leave_requests")
    
    object PartnerDashboard : Screen("partner_dashboard")
    object PartnerOrders : Screen("partner_orders")
    object PartnerEngineers : Screen("partner_engineers")
    object PartnerInventory : Screen("partner_inventory")
    
    object EngineerDashboard : Screen("engineer_dashboard")
    object EngineerJobs : Screen("engineer_jobs")
    object EngineerHistory : Screen("engineer_history")
    
    object JobExecution : Screen("job_execution/{jobId}") {
        fun createRoute(jobId: String) = "job_execution/$jobId"
    }
}
