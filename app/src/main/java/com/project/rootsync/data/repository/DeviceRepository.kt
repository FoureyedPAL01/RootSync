package com.project.rootsync.data.repository

import com.project.rootsync.data.model.Device
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for device-related operations.
 */
@Singleton
class DeviceRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getDevicesForUser(userId: String): List<Device> =
        supabase.postgrest["devices"]
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<Device>()

    suspend fun getDevice(deviceId: String): Device? =
        supabase.postgrest["devices"]
            .select {
                filter {
                    eq("id", deviceId)
                }
                limit(1)
            }
            .decodeList<Device>()
            .firstOrNull()

    suspend fun insertDevice(device: Device): Device =
        supabase.postgrest["devices"]
            .insert(device) {
                select()
            }
            .decodeSingle()

    suspend fun updateDevice(deviceId: String, updates: Map<String, Any>) {
        supabase.postgrest["devices"]
            .update(updates) {
                filter {
                    eq("id", deviceId)
                }
            }
    }

    suspend fun deleteDevice(deviceId: String) {
        supabase.postgrest["devices"]
            .delete {
                filter {
                    eq("id", deviceId)
                }
            }
    }

    suspend fun linkDevice(deviceId: String, userId: String, name: String, location: String? = null): Device {
        val device = Device(
            id = deviceId,
            userId = userId,
            name = name,
            status = "offline"
        )
        return insertDevice(device)
    }

    /**
     * Attempts to claim a device by updating its user_id.
     * Returns the updated Device if successful, or null if UUID doesn't exist.
     */
    suspend fun claimDevice(deviceUuid: String, userId: String, name: String? = null): Device? {
        val now = Clock.System.now().toString()
        val updates = mutableMapOf<String, Any>(
            "user_id" to userId,
            "claimed_at" to now
        )
        if (name != null) {
            updates["name"] = name
        }

        return try {
            supabase.postgrest["devices"]
                .update(updates) {
                    filter {
                        eq("id", deviceUuid)
                    }
                    select()
                }
                .decodeSingleOrNull<Device>()
        } catch (e: Exception) {
            null
        }
    }
}
