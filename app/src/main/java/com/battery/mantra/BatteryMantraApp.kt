package com.battery.mantra

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.battery.mantra.di.AppContainer

class BatteryMantraApp : Application() {
    lateinit var container: AppContainer

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "battery_mantra_staff_orders_channel_v3"
        const val NOTIFICATION_CHANNEL_NAME = "Battery Mantra Orders & Alerts"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

                // Clean up previous silent/legacy channels so they don't confuse system settings
                try {
                    notificationManager.deleteNotificationChannel("default_notification_channel")
                    notificationManager.deleteNotificationChannel("battery_mantra_high_importance")
                } catch (_: Exception) {}

                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for orders and staff updates"
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    setSound(defaultSoundUri, audioAttributes)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel(this)
    }
}
