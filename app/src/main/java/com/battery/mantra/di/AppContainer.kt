package com.battery.mantra.di

import android.content.Context
import com.battery.mantra.data.local.TokenManager
import com.battery.mantra.data.remote.ApiClient
import com.battery.mantra.data.remote.BatteryMantraApi
import com.battery.mantra.data.repository.AdminRepository
import com.battery.mantra.data.repository.AuthRepository
import com.battery.mantra.data.repository.EngineerRepository

class AppContainer(private val context: Context) {
    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val batteryMantraApi: BatteryMantraApi by lazy {
        ApiClient.createBatteryMantraApi(tokenManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(batteryMantraApi, tokenManager)
    }

    val engineerRepository: EngineerRepository by lazy {
        EngineerRepository(batteryMantraApi)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository(batteryMantraApi)
    }
}
