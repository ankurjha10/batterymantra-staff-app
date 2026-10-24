package com.battery.mantra.data.remote

import com.battery.mantra.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

object ApiClient {
    private const val BASE_URL = "https://api.batterymantra.com/"

    fun createBatteryMantraApi(tokenManager: TokenManager): BatteryMantraApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            val token = tokenManager.getCachedJwt()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            var response = chain.proceed(requestBuilder.build())
            
            if (response.code == 401 && !chain.request().url.encodedPath.contains("auth/refresh")) {
                android.util.Log.w("AuthInterceptor", "Received HTTP 401 for ${chain.request().method} ${chain.request().url}")
                val refreshToken = tokenManager.getCachedRefresh()
                android.util.Log.d("AuthInterceptor", "Refresh token present: ${!refreshToken.isNullOrEmpty()}")
                if (!refreshToken.isNullOrEmpty()) {
                    synchronized(ApiClient::class.java) {
                        val currentToken = tokenManager.getCachedJwt()
                        if (currentToken != null && currentToken != token) {
                            // Token already refreshed by another thread
                            android.util.Log.d("AuthInterceptor", "Token already refreshed by another thread, retrying with current token")
                            response.close()
                            return@Interceptor chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $currentToken").build())
                        }

                        android.util.Log.d("AuthInterceptor", "Attempting to refresh token...")
                        val refreshClient = OkHttpClient()
                        val mediaType = "application/json".toMediaTypeOrNull()
                        val body = "{\"refreshToken\":\"$refreshToken\"}".toRequestBody(mediaType)
                        val refreshReq = okhttp3.Request.Builder()
                            .url("${BASE_URL}api/auth/refresh")
                            .post(body)
                            .build()

                        try {
                            val refreshRes = refreshClient.newCall(refreshReq).execute()
                            android.util.Log.d("AuthInterceptor", "Refresh response — HTTP ${refreshRes.code}")
                            if (refreshRes.isSuccessful) {
                                val resBody = refreshRes.body?.string()
                                android.util.Log.d("AuthInterceptor", "Refresh response body present: ${resBody != null}")
                                if (resBody != null) {
                                    android.util.Log.d("AuthInterceptor", "Refresh response raw body: $resBody")
                                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                    val map: Map<String, String> = com.google.gson.Gson().fromJson(resBody, type)
                                    android.util.Log.d("AuthInterceptor", "JSON parsed successfully — keys: ${map.keys}")
                                    val newAccessToken = map["accessToken"]
                                    val newRefreshToken = map["refreshToken"]
                                    android.util.Log.d("AuthInterceptor", "accessToken present: ${newAccessToken != null}, refreshToken present: ${newRefreshToken != null}")
                                    if (newAccessToken != null && newRefreshToken != null) {
                                        kotlinx.coroutines.runBlocking {
                                            tokenManager.saveTokens(newAccessToken, newRefreshToken, tokenManager.getCachedRole() ?: "", tokenManager.getCachedPermissions().toList())
                                        }
                                        response.close()
                                        android.util.Log.d("AuthInterceptor", "Token refreshed successfully, retrying request...")
                                        return@Interceptor chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $newAccessToken").build())
                                    }
                                    android.util.Log.e("AuthInterceptor", "BRANCH 4d/4e: Refresh succeeded (HTTP ${refreshRes.code}) but response missing required keys — accessToken: ${newAccessToken != null}, refreshToken: ${newRefreshToken != null}, raw body: $resBody")
                                } else {
                                    android.util.Log.e("AuthInterceptor", "BRANCH 4c: Refresh succeeded (HTTP ${refreshRes.code}) but response body was null")
                                }
                            } else {
                                val errorBody = try { refreshRes.body?.string() } catch (_: Exception) { "<unreadable>" }
                                android.util.Log.e("AuthInterceptor", "BRANCH 4b: Refresh endpoint returned HTTP ${refreshRes.code}, body: $errorBody")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AuthInterceptor", "BRANCH 4f: Exception during refresh call", e)
                        }
                    }
                } else {
                    android.util.Log.e("AuthInterceptor", "BRANCH 4a: No refresh token available (null or empty), cannot attempt refresh")
                }
                
                android.util.Log.e("AuthInterceptor", "Token refresh failed or not available for URL: ${chain.request().url}. Clearing tokens...")
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    tokenManager.clearTokens()
                }
            }
            
            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BatteryMantraApi::class.java)
    }
}
