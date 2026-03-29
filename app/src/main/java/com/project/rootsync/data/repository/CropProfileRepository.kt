package com.project.rootsync.data.repository

import com.project.rootsync.data.model.CropProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for crop profile operations.
 */
@Singleton
class CropProfileRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getProfilesForUser(userId: String): List<CropProfile> =
        supabase.postgrest["crop_profiles"]
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<CropProfile>()

    suspend fun getActiveProfileId(deviceId: String): Int? {
        return try {
            val response = supabase.postgrest["devices"]
                .select {
                    filter {
                        eq("id", deviceId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<Map<String, kotlinx.serialization.json.JsonElement>>()

            response?.get("crop_profile_id")?.let {
                kotlinx.serialization.json.jsonPrimitive(it).content.toIntOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setActiveProfile(deviceId: String, profileId: Int) {
        supabase.postgrest["devices"]
            .update(mapOf("crop_profile_id" to profileId)) {
                filter {
                    eq("id", deviceId)
                }
            }
    }

    suspend fun clearActiveProfile(deviceId: String) {
        supabase.postgrest["devices"]
            .update(mapOf("crop_profile_id" to null)) {
                filter {
                    eq("id", deviceId)
                }
            }
    }

    suspend fun getProfile(profileId: Int): CropProfile? =
        supabase.postgrest["crop_profiles"]
            .select {
                filter {
                    eq("id", profileId)
                }
                limit(1)
            }
            .decodeList<CropProfile>()
            .firstOrNull()

    suspend fun insertProfile(profile: Map<String, Any?>): CropProfile =
        supabase.postgrest["crop_profiles"]
            .insert(profile) {
                select()
            }
            .decodeSingle()

    suspend fun updateProfile(profileId: Int, updates: Map<String, Any?>) {
        supabase.postgrest["crop_profiles"]
            .update(updates) {
                filter {
                    eq("id", profileId)
                }
            }
    }

    suspend fun deleteProfile(profileId: Int) {
        supabase.postgrest["crop_profiles"]
            .delete {
                filter {
                    eq("id", profileId)
                }
            }
    }

    /**
     * Fetches plant data from Perenual API via edge function.
     */
    suspend fun fetchPerenualData(profileId: Int, plantName: String) {
        supabase.functions.invoke(
            function = "perenual-lookup",
            body = buildJsonObject {
                put("profile_id", profileId)
                put("plant_name", plantName)
            }
        )
    }
}
