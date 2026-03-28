package com.project.rootsync.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var supabaseClient: io.github.jan.supabase.SupabaseClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseMsgService", "New FCM token: $token")

        serviceScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    supabaseClient.from("device_tokens").upsert(
                        mapOf(
                            "user_id" to userId,
                            "token" to token,
                            "platform" to "android"
                        )
                    )
                    Log.d("FirebaseMsgService", "Token saved to Supabase")
                }
            } catch (e: Exception) {
                Log.e("FirebaseMsgService", "Failed to save token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FirebaseMsgService", "Message received from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "RootSync"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        notificationService.showNotification(title, body)
    }
}
