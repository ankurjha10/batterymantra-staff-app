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

    suspend fun createSubAdmin(request: com.battery.mantra.data.models.AdminCreateSubAdminRequest): Result<UserResponse> {
        return try {
            val response = api.createSubAdmin(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                var errMsg = "Failed to create sub-admin: ${response.code()}"
                try {
                    val json = org.json.JSONObject(errStr)
                    if (json.has("message")) {
                        errMsg = json.getString("message")
                    }
                } catch (e: Exception) {}
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubAdmin(userId: String, request: com.battery.mantra.data.models.AdminUpdateSubAdminRequest): Result<com.battery.mantra.data.models.UserResponse> {
        return try {
            val response = api.updateSubAdmin(userId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                var errMsg = "Failed to update sub-admin: ${response.code()}"
                try {
                    val json = org.json.JSONObject(errStr)
                    if (json.has("message")) errMsg = json.getString("message")
                } catch (e: Exception) {}
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val response = api.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                var errMsg = "Failed to delete user: ${response.code()}"
                try {
                    val json = org.json.JSONObject(errStr)
                    if (json.has("message")) errMsg = json.getString("message")
                } catch (e: Exception) {}
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            val response = api.toggleUserStatus(userId, isActive)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errStr = response.errorBody()?.string() ?: ""
                var errMsg = "Failed to update status: ${response.code()}"
                try {
                    val json = org.json.JSONObject(errStr)
                    if (json.has("message")) errMsg = json.getString("message")
                } catch (e: Exception) {}
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

