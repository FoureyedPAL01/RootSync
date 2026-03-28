package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceToken(
    val id: String,
    @SerialName("user_id") val userId: String,
    val token: String,
    val platform: String = "android",
    @SerialName("created_at") val createdAt: String
)
