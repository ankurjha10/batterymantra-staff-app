package com.battery.mantra.data.repository

import com.battery.mantra.data.remote.BatteryMantraApi
import com.battery.mantra.data.models.OrderResponse

data class EngineerTask(
    val id: String,
    val customerName: String,
    val address: String,
    val status: String,
    val price: String
)

class EngineerRepository(
    private val api: BatteryMantraApi
) {
    suspend fun getActiveJobs(): Result<List<EngineerTask>> {
        return try {
            val response = api.getMyOrders()
            if (response.isSuccessful && response.body() != null) {
                // Filter active jobs (e.g. ASSIGNED, DISPATCHED)
                val activeStatuses = listOf("ASSIGNED", "DISPATCHED", "PENDING", "ACCEPTED")
                val activeOrders = response.body()!!.filter {
                    activeStatuses.contains(it.orderStatus?.uppercase())
                }.map { it.toEngineerTask() }
                Result.success(activeOrders)
            } else {
                Result.failure(Exception("Failed to fetch jobs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistoryJobs(): Result<List<EngineerTask>> {
        return try {
            val response = api.getMyOrders()
            if (response.isSuccessful && response.body() != null) {
                // Filter history jobs (e.g. COMPLETED, CANCELLED, FAILED)
                val historyStatuses = listOf("COMPLETED", "DELIVERED", "CANCELLED", "FAILED")
                val historyOrders = response.body()!!.filter {
                    historyStatuses.contains(it.orderStatus?.uppercase())
                }.map { it.toEngineerTask() }
                Result.success(historyOrders)
            } else {
                Result.failure(Exception("Failed to fetch jobs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OrderResponse.toEngineerTask(): EngineerTask {
        return EngineerTask(
            id = this.orderId,
            customerName = this.customerName ?: "Unknown",
            address = this.shippingAddress ?: "No address provided",
            status = this.orderStatus ?: "UNKNOWN",
            price = "₹${this.totalAmount ?: 0.0}"
        )
    }
}
