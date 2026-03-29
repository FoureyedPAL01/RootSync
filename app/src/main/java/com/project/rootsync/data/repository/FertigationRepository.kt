package com.project.rootsync.data.repository

import com.project.rootsync.data.model.CropProfileWithPerenual
import com.project.rootsync.data.model.FertigationLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fertigation-related operations.
 */
@Singleton
class FertigationRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    /**
     * Gets the active crop profile for a device.
     */
    suspend fun getActiveCropProfile(deviceId: String): CropProfileWithPerenual? {
        // First get device's crop_profile_id
        val device = supabase.postgrest["devices"]
            .select {
                filter {
                    eq("id", deviceId)
                }
                limit(1)
            }
            .decodeSingleOrNull<Map<String, JsonElement>>()

        val profileId = device?.get("crop_profile_id")?.jsonPrimitive?.content?.toIntOrNull()
            ?: return null

        return supabase.postgrest["crop_profiles"]
            .select {
                filter {
                    eq("id", profileId)
                }
                limit(1)
            }
            .decodeList<CropProfileWithPerenual>()
            .firstOrNull()
    }

    /**
     * Gets fertigation logs for a device.
     */
    suspend fun getFertigationLogs(deviceId: String, limit: Int = 20): List<FertigationLog> {
        return supabase.postgrest["fertigation_logs"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("fertilized_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<FertigationLog>()
    }

    /**
     * Logs a fertilizer application.
     */
    suspend fun logFertilizerApplication(
        deviceId: String,
        profileId: Int?,
        notes: String?
    ) {
        supabase.postgrest["fertigation_logs"]
            .insert(
                mapOf(
                    "device_id" to deviceId,
                    "crop_profile_id" to profileId,
                    "fertilized_at" to kotlinx.datetime.Clock.System.now().toString(),
                    "notes" to notes?.ifEmpty { null }
                )
            )
    }

    /**
     * Fetches plant data from Perenual API via edge function.
     */
    suspend fun fetchPlantData(profileId: Int, plantName: String): JsonElement? {
        return try {
            supabase.functions.invoke(
                function = "perenual-lookup",
                body = buildJsonObject {
                    put("profile_id", profileId)
                    put("plant_name", plantName)
                }
            )
        } catch (e: Exception) {
            null
        }
    }
}
