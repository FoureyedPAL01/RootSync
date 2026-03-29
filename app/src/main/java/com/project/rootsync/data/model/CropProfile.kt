package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Crop profile data class representing a plant configuration.
 * Matches Supabase crop_profiles table schema.
 */
@Serializable
data class CropProfile(
    val id: Long,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("moisture_threshold_low") val moistureThresholdLow: Int = 30,
    @SerialName("moisture_threshold_high") val moistureThresholdHigh: Int = 70,
    @SerialName("irrigation_duration_s") val irrigationDurationS: Int = 60,
    @SerialName("weather_sensitivity") val weatherSensitivity: Int = 60,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("plant_name") val plantName: String? = null,
    @SerialName("min_moisture") val minMoisture: Int = 30,
    @SerialName("perenual_species_id") val perenualSpeciesId: Int? = null,
    @SerialName("perenual_data") val perenualData: JsonElement? = null,
    @SerialName("perenual_cached_at") val perenualCachedAt: String? = null,
    @SerialName("perenual_care_data") val perenualCareData: JsonElement? = null
)
