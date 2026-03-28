package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val location: String? = null,
    @SerialName("is_online") val isOnline: Boolean
)
