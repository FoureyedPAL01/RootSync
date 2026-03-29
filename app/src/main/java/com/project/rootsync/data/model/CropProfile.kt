package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Crop profile data class representing a plant configuration.
 */
@Serializable
data class CropProfile(
    val id: Int,
    val name: String,
    @SerialName("plant_name") val plantName: String,
    @SerialName("min_moisture") val minMoisture: Int,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("perenual_cached_at") val perenualCachedAt: String? = null,
    @SerialName("perenual_data") val perenualData: JsonElement? = null,
    @SerialName("perenual_species_id") val perenualSpeciesId: Int? = null,
    @SerialName("created_at") val createdAt: String? = null
)
