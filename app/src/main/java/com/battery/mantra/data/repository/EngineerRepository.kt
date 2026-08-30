package com.battery.mantra.data.repository

import com.battery.mantra.data.remote.BatteryMantraApi
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.UserResponse

class EngineerRepository(
    private val api: BatteryMantraApi
) {
    suspend fun getActiveJobs(): Result<List<OrderResponse>> {
        return try {
            val response = api.getEngineerOrders("active")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch jobs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistoryJobs(): Result<List<OrderResponse>> {
        return try {
            val response = api.getEngineerOrders("history")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch jobs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logCall(orderId: String): Result<Unit> {
        return try {
            val response = api.logCall(orderId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to log call: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserResponse> = try {
        val response = api.getProfile()
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Failed to fetch profile"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateDutyStatus(isOnDuty: Boolean): Result<UserResponse> = try {
        val response = api.updateDutyStatus(isOnDuty)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Failed to update duty status"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun checkIn(): Result<com.battery.mantra.data.models.AttendanceResponse> = try {
        val response = api.checkIn()
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Check-in failed"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun checkOut(): Result<com.battery.mantra.data.models.AttendanceResponse> = try {
        val response = api.checkOut()
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Check-out failed"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getAttendance(): Result<List<com.battery.mantra.data.models.AttendanceResponse>> = try {
        val response = api.getAttendance()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to fetch attendance"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun applyLeave(startDate: String, endDate: String, reason: String): Result<com.battery.mantra.data.models.LeaveRequestResponse> = try {
        val response = api.applyLeave(com.battery.mantra.data.models.ApplyLeaveRequest(startDate, endDate, reason))
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Failed to apply leave"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getMyLeaves(): Result<List<com.battery.mantra.data.models.LeaveRequestResponse>> = try {
        val response = api.getMyLeaves()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to fetch leaves"))
    } catch (e: Exception) { Result.failure(e) }

}
