package com.battery.mantra.data.repository

import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CategoryResponse
import com.battery.mantra.data.models.CityInventoryBulkUpdateResponse
import com.battery.mantra.data.models.CityPricingRequestDto
import com.battery.mantra.data.models.CityPricingRequestResponse
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.FcmTokenRequest
import com.battery.mantra.data.models.NotificationResponse
import com.battery.mantra.data.models.OrderNoteRequest
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerBulkInventoryUpdateRequest
import com.battery.mantra.data.models.PartnerCityPricingUpdateDto
import com.battery.mantra.data.models.PartnerCreateProductRequest
import com.battery.mantra.data.models.PartnerInventoryItemResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.ProductDetailResponse
import com.battery.mantra.data.remote.BatteryMantraApi
import org.json.JSONObject
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

class PartnerRepository(private val api: BatteryMantraApi) {

    private fun extractErrorMessage(response: Response<*>, fallback: String): String {
        val rawError = try {
            response.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }
        val parsedMessage = if (rawError.isNotBlank()) {
            try {
                JSONObject(rawError).optString("message", "").takeIf { it.isNotBlank() }
                    ?: rawError
            } catch (_: Exception) {
                rawError
            }
        } else {
            ""
        }
        return parsedMessage.ifBlank { "$fallback (${response.code()})" }
    }

    private suspend inline fun <T> safeCall(
        fallbackError: String,
        crossinline call: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(extractErrorMessage(response, fallbackError)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend inline fun safeUnitCall(
        fallbackError: String,
        crossinline call: suspend () -> Response<Unit>
    ): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, fallbackError)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Profile ---
    suspend fun getProfile(): Result<PartnerResponse> =
        safeCall("Failed to fetch partner profile") { api.getPartnerProfile() }

    // --- Orders ---
    suspend fun getOrders(): Result<List<OrderResponse>> =
        safeCall("Failed to fetch partner orders") { api.getPartnerOrders() }

    suspend fun assignEngineer(orderId: String, engineerId: String): Result<OrderResponse> =
        safeCall("Failed to assign engineer") { api.assignPartnerEngineer(orderId, engineerId) }

    suspend fun unassignEngineer(orderId: String): Result<OrderResponse> =
        safeCall("Failed to unassign engineer") { api.unassignPartnerEngineer(orderId) }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        cancellationReason: String? = null
    ): Result<OrderResponse> = safeCall("Failed to update order status") {
        api.updatePartnerOrderStatus(orderId, newStatus, cancellationReason)
    }

    suspend fun addOrderNote(orderId: String, note: String): Result<OrderResponse> =
        safeCall("Failed to add order note") { api.addPartnerOrderNote(orderId, OrderNoteRequest(note)) }

    // --- Engineers ---
    suspend fun getEngineers(): Result<List<EngineerResponse>> =
        safeCall("Failed to fetch partner engineers") { api.getPartnerEngineers() }

    suspend fun getAvailableEngineers(): Result<List<EngineerResponse>> =
        safeCall("Failed to fetch available engineers") { api.getAvailablePartnerEngineers() }

    suspend fun createEngineer(request: CreateEngineerRequest): Result<EngineerResponse> =
        safeCall("Failed to create engineer") { api.createPartnerEngineer(request) }

    suspend fun updateEngineer(id: String, request: CreateEngineerRequest): Result<EngineerResponse> =
        safeCall("Failed to update engineer") { api.updatePartnerEngineer(id, request) }

    suspend fun deactivateEngineer(id: String): Result<Unit> =
        safeUnitCall("Failed to deactivate engineer") { api.deletePartnerEngineer(id) }

    // --- Branch Inventory ---
    suspend fun getInventory(cityId: String): Result<List<PartnerInventoryItemResponse>> =
        safeCall("Failed to fetch branch inventory") { api.getPartnerInventory(cityId) }

    suspend fun updateSingleStock(
        productId: String,
        cityId: String,
        stock: Int
    ): Result<PartnerInventoryItemResponse> = safeCall("Failed to update stock") {
        api.updatePartnerSingleStock(productId, cityId, stock)
    }

    suspend fun bulkUpdateInventory(
        request: PartnerBulkInventoryUpdateRequest
    ): Result<CityInventoryBulkUpdateResponse> = safeCall("Failed to update branch inventory") {
        api.bulkUpdatePartnerInventory(request)
    }

    // --- Products & Pricing ---
    suspend fun submitCityPricingRequest(
        productId: String,
        request: CityPricingRequestDto
    ): Result<CityPricingRequestResponse> = safeCall("Failed to submit price change request") {
        api.submitPartnerCityPricingRequest(productId, request)
    }

    suspend fun getPendingPricingRequests(): Result<List<CityPricingRequestResponse>> =
        safeCall("Failed to fetch pricing requests") { api.getPartnerPendingPricingRequests() }

    suspend fun updateDirectCityPricing(
        productId: String,
        request: PartnerCityPricingUpdateDto
    ): Result<ProductDetailResponse> = safeCall("Failed to update city pricing") {
        api.updatePartnerCityPricing(productId, request)
    }

    suspend fun requestNewProduct(request: PartnerCreateProductRequest): Result<ProductDetailResponse> =
        safeCall("Failed to submit new product request") { api.createPartnerProduct(request) }

    suspend fun getBrands(): Result<List<BrandResponse>> =
        safeCall("Failed to fetch brands") { api.getAllBrands() }

    suspend fun getCategories(): Result<List<CategoryResponse>> =
        safeCall("Failed to fetch categories") { api.getAllCategories() }

    // --- Notifications & FCM ---
    suspend fun getNotifications(): Result<List<NotificationResponse>> =
        safeCall("Failed to fetch notifications") { api.getNotifications() }

    suspend fun clearNotifications(): Result<Unit> =
        safeUnitCall("Failed to clear notifications") { api.clearNotifications() }

    suspend fun deleteNotification(id: String): Result<Unit> =
        safeUnitCall("Failed to delete notification") { api.deleteNotification(id) }

    suspend fun updateFcmToken(fcmToken: String): Result<Unit> =
        safeUnitCall("Failed to update FCM token") { api.updateFcmToken(FcmTokenRequest(fcmToken)) }
}
