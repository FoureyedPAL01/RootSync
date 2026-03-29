package com.project.rootsync.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.project.rootsync.MainActivity
import com.project.rootsync.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "rootsync_alerts"
        const val CHANNEL_NAME = "RootSync Alerts"
        const val CHANNEL_DESCRIPTION = "Notifications for system alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, body: String, notificationId: Int = 0) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun showAlertNotification(alertType: String, message: String) {
        showNotification(
            title = "RootSync Alert: $alertType",
            body = message,
            notificationId = alertType.hashCode()
        )
    }

    /**
     * Called after successful login/signup to initialize notifications.
     * Equivalent to Flutter's NotificationService.onUserLogin()
     */
    suspend fun onUserLogin() {
        // Initialize FCM token registration or other notification setup
        // This can be expanded to register device token with backend
    }
}
