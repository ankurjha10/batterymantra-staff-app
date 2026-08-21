package com.battery.mantra.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.battery.mantra.MainActivity
import com.battery.mantra.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this token to the backend server via ApiClient once the API is ready
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
    }

    private fun showNotification(title: String, body: String, type: String? = null) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Using a hardcoded string here temporarily, will add to strings.xml later
        val channelId = "default_notification_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val isCustomUi = type == "custom_ui"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if (isCustomUi) {
            val remoteViews = android.widget.RemoteViews(packageName, R.layout.layout_custom_notification)
            remoteViews.setTextViewText(R.id.notification_title, title)
            remoteViews.setTextViewText(R.id.notification_message, body)
            notificationBuilder.setStyle(androidx.core.app.NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(remoteViews)
        } else {
            notificationBuilder.setContentTitle(title)
            notificationBuilder.setContentText(body)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Battery Mantra Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
