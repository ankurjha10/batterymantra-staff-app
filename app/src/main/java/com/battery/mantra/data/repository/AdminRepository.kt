package com.battery.mantra.data.repository

import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.UserResponse
import com.battery.mantra.data.models.ProductResponse
import com.battery.mantra.data.models.PaginatedResponse
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.ProductDetailResponse
import com.battery.mantra.data.models.UpdateProductRequest
import com.battery.mantra.data.models.CategoryResponse
import com.battery.mantra.data.remote.BatteryMantraApi
import kotlin.coroutines.cancellation.CancellationException

class AdminRepository(private val api: BatteryMantraApi) {

    suspend fun getOrders(): Result<List<OrderResponse>> {
        return try {
            val response = api.getAdminOrders()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch admin orders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignPartner(orderId: String, partnerId: String): Result<OrderResponse> {
        return try {
            val response = api.assignPartner(orderId, partnerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to assign partner: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignEngineer(orderId: String, engineerId: String): Result<OrderResponse> {
        return try {
            val response = api.assignEngineer(orderId, engineerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to assign engineer: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<OrderResponse> {
        return try {
            val response = api.updateOrderStatus(orderId, com.battery.mantra.data.models.OrderStatusUpdateRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update status: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartners(): Result<List<PartnerResponse>> {
        return try {
            val response = api.getAdminPartners()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch admin partners: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPartner(request: com.battery.mantra.data.models.CreatePartnerRequest): Result<PartnerResponse> {
        return try {
            val response = api.createAdminPartner(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to create partner: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePartner(id: String, request: com.battery.mantra.data.models.CreatePartnerRequest): Result<PartnerResponse> {
        return try {
            val response = api.updateAdminPartner(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update partner: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun togglePartnerStatus(partner: PartnerResponse, isActive: Boolean): Result<Unit> {
        return try {
            var updated = false
            try {
                val req = com.battery.mantra.data.models.CreatePartnerRequest(
                    businessName = partner.businessName ?: "",
                    contactPerson = partner.contactPerson ?: "",
                    email = partner.email ?: "",
                    phoneNumber = partner.phoneNumber ?: "",
                    alternatePhone = partner.alternatePhone,
                    address = partner.address,
                    operatingCityIds = partner.operatingCities?.map { it.cityId } ?: emptyList(),
                    isActive = isActive
                )
                val putRes = api.updateAdminPartner(partner.id, req)
                if (putRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            val userId = partner.userId
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRes = api.toggleUserStatus(userId, isActive)
                    if (userRes.isSuccessful) updated = true
                } catch (e: Exception) {
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                }
            }

            try {
                val patchRes = api.togglePartnerStatus(partner.id, isActive)
                if (patchRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            if (updated) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update partner status"))
            }
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun togglePartnerStatus(id: String, userId: String?, isActive: Boolean): Result<Unit> {
        return try {
            var updated = false
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRes = api.toggleUserStatus(userId, isActive)
                    if (userRes.isSuccessful) updated = true
                } catch (e: Exception) {
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                }
            }
            try {
                val patchRes = api.togglePartnerStatus(id, isActive)
                if (patchRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            if (updated) Result.success(Unit) else Result.failure(Exception("Failed to update partner status"))
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getEngineers(): Result<List<EngineerResponse>> {
        return try {
            val response = api.getAdminEngineers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch admin engineers: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createEngineer(request: com.battery.mantra.data.models.CreateEngineerRequest): Result<EngineerResponse> {
        return try {
            val response = api.createAdminEngineer(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to create engineer: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEngineer(id: String, request: com.battery.mantra.data.models.CreateEngineerRequest): Result<EngineerResponse> {
        return try {
            val response = api.updateAdminEngineer(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update engineer: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleEngineerStatus(engineer: EngineerResponse, isActive: Boolean): Result<Unit> {
        return try {
            var updated = false
            try {
                val req = com.battery.mantra.data.models.CreateEngineerRequest(
                    firstName = engineer.firstName ?: "",
                    lastName = engineer.lastName ?: "",
                    email = engineer.email ?: "",
                    phoneNumber = engineer.phoneNumber ?: "",
                    alternatePhone = engineer.alternatePhone,
                    address = engineer.address,
                    city = engineer.city,
                    partnerId = engineer.partnerId,
                    isActive = isActive
                )
                val putRes = api.updateAdminEngineer(engineer.id, req)
                if (putRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            val userId = engineer.userId
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRes = api.toggleUserStatus(userId, isActive)
                    if (userRes.isSuccessful) updated = true
                } catch (e: Exception) {
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                }
            }

            try {
                val patchRes = api.toggleEngineerStatus(engineer.id, isActive)
                if (patchRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            if (updated) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update engineer status"))
            }
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun toggleEngineerStatus(id: String, userId: String?, isActive: Boolean): Result<Unit> {
        return try {
            var updated = false
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRes = api.toggleUserStatus(userId, isActive)
                    if (userRes.isSuccessful) updated = true
                } catch (e: Exception) {
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                }
            }
            try {
                val patchRes = api.toggleEngineerStatus(id, isActive)
                if (patchRes.isSuccessful) updated = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            }

            if (updated) Result.success(Unit) else Result.failure(Exception("Failed to update engineer status"))
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<UserResponse>> {
        return try {
            val response = api.getAdminUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch admin users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSubAdmin(request: com.battery.mantra.data.models.AdminCreateSubAdminRequest): Result<Unit> {
        return try {
            val response = api.createSubAdmin(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                val errMsg = parseUserFriendlyError(response.code(), errStr, "Create Sub-Admin")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val friendly = if (e is java.io.IOException) "Unable to connect to server. Please check your internet connection." else (e.message ?: "Failed to create sub-admin")
            Result.failure(Exception(friendly))
        }
    }

    suspend fun updateSubAdmin(userId: String, request: com.battery.mantra.data.models.AdminUpdateSubAdminRequest): Result<Unit> {
        return try {
            val response = api.updateSubAdmin(userId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                val errMsg = parseUserFriendlyError(response.code(), errStr, "Update Sub-Admin")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val friendly = if (e is java.io.IOException) "Unable to connect to server. Please check your internet connection." else (e.message ?: "Failed to update sub-admin")
            Result.failure(Exception(friendly))
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val response = api.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                val errMsg = parseUserFriendlyError(response.code(), errStr, "Delete user")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val friendly = if (e is java.io.IOException) "Unable to connect to server. Please check your internet connection." else (e.message ?: "Failed to delete user")
            Result.failure(Exception(friendly))
        }
    }

    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            val response = api.toggleUserStatus(userId, isActive)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                val errMsg = parseUserFriendlyError(response.code(), errStr, "Update status")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val friendly = if (e is java.io.IOException) "Unable to connect to server. Please check your internet connection." else (e.message ?: "Failed to update user status")
            Result.failure(Exception(friendly))
        }
    }

    private fun parseUserFriendlyError(responseCode: Int, errorBodyString: String?, defaultAction: String): String {
        if (errorBodyString.isNullOrBlank()) {
            return when (responseCode) {
                400 -> "Invalid request. Please verify the entered details."
                401, 403 -> "You don't have permission to perform this action."
                404 -> "The requested record was not found."
                409 -> "A conflicting record already exists with these details."
                in 500..599 -> "Server is temporarily unavailable. Please try again shortly."
                else -> "$defaultAction failed (Error $responseCode)."
            }
        }

        var message = ""
        try {
            val json = org.json.JSONObject(errorBodyString)
            if (json.has("message") && !json.isNull("message")) {
                message = json.getString("message")
            } else if (json.has("error") && !json.isNull("error")) {
                message = json.getString("error")
            } else if (json.has("errors") && !json.isNull("errors")) {
                val errorsArray = json.optJSONArray("errors")
                if (errorsArray != null && errorsArray.length() > 0) {
                    val first = errorsArray.optJSONObject(0)
                    message = first?.optString("defaultMessage") ?: errorsArray.getString(0)
                }
            }
        } catch (_: Exception) {}

        val raw = if (message.isNotBlank()) message else errorBodyString
        val lower = raw.lowercase()

        return when {
            lower.contains("users_role_check") || lower.contains("sub_admin is not permitted") ->
                "The SUB_ADMIN role is restricted by database check constraint. Please update 'users_role_check' in PostgreSQL."
            lower.contains("email is already taken") || (lower.contains("email") && (lower.contains("already") || lower.contains("unique") || lower.contains("duplicate"))) ->
                "This email address is already registered. Please use a different email."
            lower.contains("phone number is already taken") || lower.contains("phone number is already in use") || (lower.contains("phone") && (lower.contains("already") || lower.contains("unique") || lower.contains("duplicate"))) ->
                "This phone number is already registered. Please use a different phone number."
            lower.contains("name/username is already") || lower.contains("username is already taken") || (lower.contains("username") && (lower.contains("already") || lower.contains("unique") || lower.contains("duplicate"))) ->
                "This name/username is already in use. Please enter a different name."
            lower.contains("cannot be modified or deleted because it is linked") ->
                "This record is linked to other data (orders/tasks) and cannot be altered or removed."
            lower.contains("text you entered is too long") ->
                "The text you entered is too long for one of the fields. Please shorten it."
            lower.contains("required field is missing") ->
                "Please fill in all mandatory fields before submitting."
            lower.contains("invalid email") ->
                "Please enter a valid email address."
            message.isNotBlank() ->
                message
            responseCode in 500..599 ->
                "Server error occurred. Please try again later."
            else ->
                "$defaultAction failed (Error $responseCode)."
        }
    }

    suspend fun getCities(): Result<List<com.battery.mantra.data.models.CityResponse>> {
        return try {
            val response = api.getAdminCities()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch cities: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminCoupons(): Result<List<com.battery.mantra.data.models.CouponResponse>> {
        return try {
            val response = api.getAdminCoupons()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch coupons: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminCoupon(request: com.battery.mantra.data.models.CouponRequest): Result<com.battery.mantra.data.models.CouponResponse> {
        return try {
            val response = api.createAdminCoupon(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to create coupon (${response.code()}): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAdminCoupon(id: String, request: com.battery.mantra.data.models.CouponRequest): Result<com.battery.mantra.data.models.CouponResponse> {
        return try {
            val response = api.updateAdminCoupon(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update coupon (${response.code()}): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAdminCoupon(id: String): Result<Unit> {
        return try {
            val response = api.deleteAdminCoupon(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to delete coupon (${response.code()}): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(page: Int, size: Int, brandId: String?, keyword: String?): Result<PaginatedResponse<ProductResponse>> {
        return try {
            val response = api.getAdminProducts(page, size, brandId, keyword)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCallbacks(): Result<List<com.battery.mantra.data.models.CallbackResponse>> {
        return try {
            val response = api.getAllCallbacks()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to fetch callbacks: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCallbackStatus(callbackId: Long, status: String): Result<com.battery.mantra.data.models.CallbackResponse> {
        return try {
            val request = com.battery.mantra.data.models.UpdateCallbackStatusRequest(status)
            val response = api.updateCallbackStatus(callbackId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update callback status: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEnquiries(type: String? = null): Result<List<com.battery.mantra.data.models.EnquiryResponse>> {
        return try {
            val response = api.getAllEnquiries(type)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to fetch enquiries: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEnquiryStatus(id: Long, status: String): Result<com.battery.mantra.data.models.EnquiryResponse> {
        return try {
            val request = com.battery.mantra.data.models.UpdateEnquiryStatusRequest(status)
            val response = api.updateEnquiryStatus(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Failed to update enquiry status: ${response.code()} $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBrands(): Result<List<BrandResponse>> {
        return try {
            val response = api.getAllBrands()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch brands: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(id: String): Result<ProductDetailResponse> {
        return try {
            val response = api.getProductById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch product: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: String, request: UpdateProductRequest): Result<ProductDetailResponse> {
        return try {
            val response = api.updateProduct(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to update product: ${response.code()} $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoryResponse>> {
        return try {
            val response = api.getAllCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(fcmToken: String): Result<Unit> {
        return try {
            val response = api.updateFcmToken(com.battery.mantra.data.models.FcmTokenRequest(fcmToken))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update FCM token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): Result<List<com.battery.mantra.data.models.NotificationResponse>> {
        return try {
            val response = api.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch notifications: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLeaveRequests(): Result<List<com.battery.mantra.data.models.LeaveRequestResponse>> = try {
        val response = api.getAllLeaveRequests()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to fetch leave requests"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateLeaveStatus(id: String, status: String): Result<com.battery.mantra.data.models.LeaveRequestResponse> = try {
        val response = api.updateLeaveStatus(id, status)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Failed to update leave status"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun clearNotifications(): Result<Unit> {
        return try {
            val response = api.clearNotifications()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to clear notifications: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadInventory(engineerId: String, productId: String, quantity: Int): Result<Unit> = try {
        val response = api.loadInventory(com.battery.mantra.data.models.LoadInventoryRequest(engineerId, productId, quantity))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Failed to load inventory"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun unloadInventory(engineerId: String, productId: String, quantity: Int): Result<Unit> = try {
        val response = api.unloadInventory(com.battery.mantra.data.models.LoadInventoryRequest(engineerId, productId, quantity))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Failed to unload inventory"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            val response = api.deleteNotification(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete notification: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCustomer(request: com.battery.mantra.data.models.AdminCreateCustomerRequest): Result<com.battery.mantra.data.models.UserResponse> {
        return try {
            val response = api.createCustomer(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create customer: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminOrder(request: com.battery.mantra.data.models.AdminCreateOrderRequest): Result<com.battery.mantra.data.models.OrderResponse> {
        return try {
            val response = api.createAdminOrder(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

