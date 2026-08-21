package com.battery.mantra

import android.app.Application
import com.battery.mantra.di.AppContainer

class BatteryMantraApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
