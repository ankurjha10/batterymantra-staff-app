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
    private const val BASE_URL = "https://200.97.165.54.nip.io/"

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
            
            if ((response.code == 401 || response.code == 403) && !chain.request().url.encodedPath.contains("auth/refresh")) {
                val refreshToken = tokenManager.getCachedRefresh()
                if (!refreshToken.isNullOrEmpty()) {
                    synchronized(ApiClient::class.java) {
                        val currentToken = tokenManager.getCachedJwt()
                        if (currentToken != null && currentToken != token) {
                            // Token already refreshed by another thread
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
                            if (refreshRes.isSuccessful) {
                                val resBody = refreshRes.body?.string()
                                if (resBody != null) {
                                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                    val map: Map<String, String> = com.google.gson.Gson().fromJson(resBody, type)
                                    val newAccessToken = map["accessToken"]
                                    val newRefreshToken = map["refreshToken"]
                                    if (newAccessToken != null && newRefreshToken != null) {
                                        kotlinx.coroutines.runBlocking {
                                            tokenManager.saveTokens(newAccessToken, newRefreshToken, tokenManager.getCachedRole() ?: "", tokenManager.getCachedPermissions().toList())
                                        }
                                        response.close()
                                        android.util.Log.d("AuthInterceptor", "Token refreshed successfully, retrying request...")
                                        return@Interceptor chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $newAccessToken").build())
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AuthInterceptor", "Failed to refresh token", e)
                        }
                    }
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
