package com.battery.mantra.data.repository

import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.remote.BatteryMantraApi

class PartnerRepository(private val api: BatteryMantraApi) {
    suspend fun getOrders(): Result<List<OrderResponse>> {
        return try {
            val response = api.getPartnerOrders()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch partner orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEngineers(): Result<List<EngineerResponse>> {
        return try {
            val response = api.getPartnerEngineers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch partner engineers: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createEngineer(request: CreateEngineerRequest): Result<EngineerResponse> {
        return try {
            val response = api.createPartnerEngineer(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create partner engineer: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
