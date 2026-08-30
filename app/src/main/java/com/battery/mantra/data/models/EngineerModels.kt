package com.battery.mantra.data.models

data class AttendanceResponse(
    val id: String,
    val date: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val status: String // PRESENT, ABSENT, LEAVE
)

data class LeaveRequestResponse(
    val id: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String // PENDING, APPROVED, REJECTED
)

data class ApplyLeaveRequest(
    val startDate: String,
    val endDate: String,
    val reason: String
)

data class LoadInventoryRequest(
    val engineerId: String,
    val productId: String,
    val quantity: Int
)

data class EngineerCompleteJobRequest(
    val otp: String,
    val serialNumber: String,
    val oldBatteryCollected: Boolean,
    val paymentMode: String
)
