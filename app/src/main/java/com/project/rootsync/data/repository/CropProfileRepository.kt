package com.project.rootsync.data.repository

import com.project.rootsync.data.model.CropProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropProfileRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getProfilesForDevice(deviceId: String): List<CropProfile> =
        supabase.postgrest["crop_profiles"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<CropProfile>()

    suspend fun getProfile(profileId: String): CropProfile? =
        supabase.postgrest["crop_profiles"]
            .select {
                filter {
                    eq("id", profileId)
                }
                limit(1)
            }
            .decodeList<CropProfile>()
            .firstOrNull()

    suspend fun insertProfile(profile: CropProfile): CropProfile =
        supabase.postgrest["crop_profiles"]
            .insert(profile) {
                select()
            }
            .decodeSingle()

    suspend fun updateProfile(profileId: String, updates: Map<String, Any>) {
        supabase.postgrest["crop_profiles"]
            .update(updates) {
                filter {
                    eq("id", profileId)
                }
            }
    }

    suspend fun deleteProfile(profileId: String) {
        supabase.postgrest["crop_profiles"]
            .delete {
                filter {
                    eq("id", profileId)
                }
            }
    }
}
