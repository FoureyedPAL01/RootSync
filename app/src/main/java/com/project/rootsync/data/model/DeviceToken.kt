package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device token data class for FCM push notifications.
 * Matches Supabase device_tokens table schema.
 */
@Serializable
data class DeviceToken(
    @SerialName("user_id") val userId: String,
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("updated_at") val updatedAt: String? = null
)
