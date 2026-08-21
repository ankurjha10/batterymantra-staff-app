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
}

