package com.project.rootsync.data.repository

import com.project.rootsync.data.model.Device
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getDevicesForUser(userId: String): List<Device> =
        supabase.postgrest["devices"]
            .select {
                filter {
                    eq("owner_id", userId)
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
            ownerId = userId,
            name = name,
            location = location,
            isOnline = false
        )
        return insertDevice(device)
    }
}
