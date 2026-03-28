package com.project.rootsync.data.repository

import com.project.rootsync.data.model.SensorReading
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getLatestReading(deviceId: String): SensorReading? =
        supabase.postgrest["sensor_readings"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<SensorReading>()
            .firstOrNull()

    suspend fun getReadingsForRange(
        deviceId: String,
        from: String,
        to: String
    ): List<SensorReading> =
        supabase.postgrest["sensor_readings"]
            .select {
                filter {
                    eq("device_id", deviceId)
                    gte("created_at", from)
                    lte("created_at", to)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<SensorReading>()

    suspend fun getRecentReadings(deviceId: String, limit: Long = 24): List<SensorReading> =
        supabase.postgrest["sensor_readings"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<SensorReading>()
}
