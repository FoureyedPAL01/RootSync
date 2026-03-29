package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Fertigation log data class representing a fertilizer application record.
 */
@Serializable
data class FertigationLog(
    val id: Long? = null,
    @SerialName("device_id") val deviceId: String,
    @SerialName("crop_profile_id") val cropProfileId: Int? = null,
    @SerialName("fertilized_at") val fertilizedAt: String,
    val notes: String? = null
)

/**
 * Crop profile with Perenual data for fertigation display.
 */
@Serializable
data class CropProfileWithPerenual(
    val id: Int,
    val name: String,
    @SerialName("plant_name") val plantName: String? = null,
    @SerialName("min_moisture") val minMoisture: Int,
    @SerialName("perenual_data") val perenualData: JsonElement? = null,
    @SerialName("perenual_care_data") val perenualCareData: JsonElement? = null,
    @SerialName("perenual_cached_at") val perenualCachedAt: String? = null
)
