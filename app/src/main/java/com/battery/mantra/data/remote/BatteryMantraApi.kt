package com.battery.mantra.data.remote

import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.UserResponse
import com.battery.mantra.data.models.ProductResponse
import com.battery.mantra.data.models.PaginatedResponse
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.ProductDetailResponse
import com.battery.mantra.data.models.UpdateProductRequest
import com.battery.mantra.data.models.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BatteryMantraApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/orders")
    suspend fun getMyOrders(): Response<List<OrderResponse>>

    // --- Admin Endpoints ---
    @GET("api/admin/orders")
    suspend fun getAdminOrders(): Response<List<OrderResponse>>

    @PATCH("api/admin/orders/{orderId}/assign-partner")
    suspend fun assignPartner(@Path("orderId") orderId: String, @Query("partnerId") partnerId: String): Response<OrderResponse>

    @PATCH("api/admin/orders/{orderId}/assign-engineer")
    suspend fun assignEngineer(@Path("orderId") orderId: String, @Query("engineerId") engineerId: String): Response<OrderResponse>

    @PATCH("api/admin/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: com.battery.mantra.data.models.OrderStatusUpdateRequest
    ): Response<OrderResponse>

    @GET("api/admin/partners")
    suspend fun getAdminPartners(): Response<List<PartnerResponse>>

    @POST("api/admin/partners")
    suspend fun createAdminPartner(@Body request: com.battery.mantra.data.models.CreatePartnerRequest): Response<PartnerResponse>

    @GET("api/admin/engineers")
    suspend fun getAdminEngineers(): Response<List<EngineerResponse>>

    @POST("api/admin/engineers")
    suspend fun createAdminEngineer(@Body request: com.battery.mantra.data.models.CreateEngineerRequest): Response<EngineerResponse>

    @GET("api/admin/users")
    suspend fun getAdminUsers(): Response<List<UserResponse>>

    @GET("api/admin/cities")
    suspend fun getAdminCities(): Response<List<com.battery.mantra.data.models.CityResponse>>

    @GET("api/admin/coupons")
    suspend fun getAdminCoupons(): Response<List<com.battery.mantra.data.models.CouponResponse>>

    @POST("api/admin/coupons")
    suspend fun createAdminCoupon(@Body request: com.battery.mantra.data.models.CouponRequest): Response<com.battery.mantra.data.models.CouponResponse>

    @PUT("api/admin/coupons/{id}")
    suspend fun updateAdminCoupon(@Path("id") id: String, @Body request: com.battery.mantra.data.models.CouponRequest): Response<com.battery.mantra.data.models.CouponResponse>

    @retrofit2.http.DELETE("api/admin/coupons/{id}")
    suspend fun deleteAdminCoupon(@Path("id") id: String): Response<Unit>

    @GET("api/admin/callbacks")
    suspend fun getAllCallbacks(): Response<List<com.battery.mantra.data.models.CallbackResponse>>

    @PATCH("api/admin/callbacks/{callbackId}/status")
    suspend fun updateCallbackStatus(
        @Path("callbackId") callbackId: Long,
        @Body request: com.battery.mantra.data.models.UpdateCallbackStatusRequest
    ): Response<com.battery.mantra.data.models.CallbackResponse>

    @GET("api/admin/enquiries")
    suspend fun getAllEnquiries(@Query("type") type: String? = null): Response<List<com.battery.mantra.data.models.EnquiryResponse>>

    @PATCH("api/admin/enquiries/{id}/status")
    suspend fun updateEnquiryStatus(
        @Path("id") id: Long,
        @Body request: com.battery.mantra.data.models.UpdateEnquiryStatusRequest
    ): Response<com.battery.mantra.data.models.EnquiryResponse>

    @GET("api/products/filter")
    suspend fun getAdminProducts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("brandId") brandId: String?,
        @Query("keyword") keyword: String?,
        @Query("sortBy") sortBy: String? = "productName",
        @Query("sortDir") sortDir: String? = "asc"
    ): Response<PaginatedResponse<ProductResponse>>

    @GET("api/brands")
    suspend fun getAllBrands(): Response<List<BrandResponse>>

    @GET("api/products/id/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ProductDetailResponse>

    @PATCH("api/admin/products/id/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: UpdateProductRequest
    ): Response<ProductDetailResponse>

    @GET("api/categories")
    suspend fun getAllCategories(): Response<List<CategoryResponse>>

    // --- FCM Notifications ---
    @POST("api/user/fcm-token")
    suspend fun updateFcmToken(@Body request: com.battery.mantra.data.models.FcmTokenRequest): Response<Unit>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<com.battery.mantra.data.models.NotificationResponse>>

    @DELETE("api/notifications")
    suspend fun clearNotifications(): Response<Unit>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>

    // --- Profile & Shared ---
    @GET("api/engineer/profile")
    suspend fun getProfile(): Response<UserResponse>

    // --- Engineer Features ---
    @POST("api/engineer/orders/{orderId}/log-call")
    suspend fun logCall(@Path("orderId") orderId: String): Response<Unit>

    @GET("api/engineer/orders")
    suspend fun getEngineerOrders(@Query("type") type: String): Response<List<OrderResponse>>

    @POST("api/engineer/orders/{orderId}/send-otp")
    suspend fun sendCompletionOtp(@Path("orderId") orderId: String): Response<String>

    @POST("api/engineer/orders/{orderId}/complete")
    suspend fun completeJob(
        @Path("orderId") orderId: String,
        @Body request: com.battery.mantra.data.models.EngineerCompleteJobRequest
    ): Response<OrderResponse>

    // --- Engineer Attendance & Leaves ---
    @POST("api/engineer/attendance/check-in")
    suspend fun checkIn(): Response<com.battery.mantra.data.models.AttendanceResponse>

    @POST("api/engineer/attendance/check-out")
    suspend fun checkOut(): Response<com.battery.mantra.data.models.AttendanceResponse>

    @GET("api/engineer/attendance")
    suspend fun getAttendance(): Response<List<com.battery.mantra.data.models.AttendanceResponse>>

    @GET("api/engineer/leave-requests")
    suspend fun getMyLeaves(): Response<List<com.battery.mantra.data.models.LeaveRequestResponse>>

    @POST("api/engineer/leave-requests")
    suspend fun applyLeave(@Body request: com.battery.mantra.data.models.ApplyLeaveRequest): Response<com.battery.mantra.data.models.LeaveRequestResponse>

    // --- Admin Leave & Inventory ---
    @GET("api/admin/engineers/{engineerId}/attendance")
    suspend fun getEngineerAttendance(@Path("engineerId") engineerId: String): Response<List<com.battery.mantra.data.models.AttendanceResponse>>

    @PATCH("api/admin/leave-requests/{id}/status")
    suspend fun updateLeaveStatus(@Path("id") id: String, @Query("status") status: String): Response<com.battery.mantra.data.models.LeaveRequestResponse>

    @GET("api/admin/leave-requests")
    suspend fun getAllLeaveRequests(): Response<List<com.battery.mantra.data.models.LeaveRequestResponse>>

    @POST("api/admin/engineer-inventory/load")
    suspend fun loadInventory(@Body request: com.battery.mantra.data.models.LoadInventoryRequest): Response<Unit>

    @POST("api/admin/engineer-inventory/unload")
    suspend fun unloadInventory(@Body request: com.battery.mantra.data.models.LoadInventoryRequest): Response<Unit>

    // --- Admin Manual Order Creation ---
    @POST("api/admin/customers")
    suspend fun createCustomer(@Body request: com.battery.mantra.data.models.AdminCreateCustomerRequest): Response<UserResponse>

    @POST("api/admin/orders")
    suspend fun createAdminOrder(@Body request: com.battery.mantra.data.models.AdminCreateOrderRequest): Response<OrderResponse>

    @POST("api/admin/sub-admins")
    suspend fun createSubAdmin(@Body request: com.battery.mantra.data.models.AdminCreateSubAdminRequest): Response<UserResponse>

    @PUT("api/admin/sub-admins/{userId}")
    suspend fun updateSubAdmin(@Path("userId") userId: String, @Body request: com.battery.mantra.data.models.AdminUpdateSubAdminRequest): Response<UserResponse>

    @DELETE("api/admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>

    @PATCH("api/admin/users/{userId}/status")
    suspend fun toggleUserStatus(@Path("userId") userId: String, @Query("isActive") isActive: Boolean): Response<Unit>
}

