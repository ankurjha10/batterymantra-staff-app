package com.battery.mantra.data.remote

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.battery.mantra.BatteryMantraApp
import com.battery.mantra.MainActivity
import com.battery.mantra.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "New Token Generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        android.util.Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload (sent from Firebase Console mostly)
        remoteMessage.notification?.let {
            val title = it.title ?: "Battery Mantra"
            val body = it.body ?: ""
            showNotification(title, body)
        }

        // Check if message contains a data payload (sent from our Backend API)
        if (remoteMessage.data.isNotEmpty()) {
            // In data messages, we usually construct the notification manually
            val title = remoteMessage.data["title"] ?: "New Update"
            val body = remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: ""
            val type = remoteMessage.data["type"]
            
            // Only show if the notification block didn't already show one
            if (remoteMessage.notification == null) {
                showNotification(title, body, type)
            }
        }
        
        // Notify the app that new data might be available
        com.battery.mantra.util.AppEventBus.emitRefreshEvent()
    }

    private fun showNotification(title: String, body: String, type: String? = null) {
        // Ensure channel with sound & vibration exists
        BatteryMantraApp.createNotificationChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = BatteryMantraApp.NOTIFICATION_CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val isCustomUi = type == "custom_ui"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)

        if (isCustomUi) {
            val remoteViews = android.widget.RemoteViews(packageName, R.layout.layout_custom_notification)
            remoteViews.setTextViewText(R.id.notification_title, title)
            remoteViews.setTextViewText(R.id.notification_message, body)
            
            // Set dynamic icon based on title
            val iconResId = when {
                title.contains("Cancel", ignoreCase = true) -> R.drawable.ic_cancel
                else -> R.drawable.ic_shopping_bag
            }
            remoteViews.setImageViewResource(R.id.notification_icon, iconResId)
            
            notificationBuilder.setStyle(androidx.core.app.NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(remoteViews)
        } else {
            notificationBuilder.setContentTitle(title)
            notificationBuilder.setContentText(body)
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Explicitly trigger sound playback if the phone is not in silent or vibrate mode
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null || audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                val ringtone = RingtoneManager.getRingtone(applicationContext, defaultSoundUri)
                ringtone?.play()
            }
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Error playing ringtone", e)
        }
    }
}
