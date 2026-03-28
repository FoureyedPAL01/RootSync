package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    val email: String,
    val name: String? = null,
    @SerialName("preferred_unit") val preferredUnit: String = "metric",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)
