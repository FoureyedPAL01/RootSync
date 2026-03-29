package com.project.rootsync.data.repository

import com.project.rootsync.data.model.PumpLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for pump-related operations.
 */
@Singleton
class PumpRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getPumpLogs(deviceId: String, limit: Long = 50): List<PumpLog> =
        supabase.postgrest["pump_logs"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("pump_on_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<PumpLog>()

    suspend fun getPumpLogsSince(deviceId: String, sinceDate: String): List<PumpLog> =
        supabase.postgrest["pump_logs"]
            .select {
                filter {
                    eq("device_id", deviceId)
                    gte("pump_on_at", sinceDate)
                }
                order("pump_on_at", Order.ASCENDING)
            }
            .decodeList<PumpLog>()

    suspend fun getPumpLogsForRange(
        deviceId: String,
        from: String,
        to: String
    ): List<PumpLog> =
        supabase.postgrest["pump_logs"]
            .select {
                filter {
                    eq("device_id", deviceId)
                    gte("pump_on_at", from)
                    lte("pump_on_at", to)
                }
                order("pump_on_at", Order.DESCENDING)
            }
            .decodeList<PumpLog>()

    suspend fun getTotalWaterUsed(deviceId: String, from: String, to: String): Double =
        getPumpLogsForRange(deviceId, from, to)
            .sumOf { it.waterUsedLitres ?: 0.0 }

    suspend fun insertPumpLog(pumpLog: PumpLog): PumpLog =
        supabase.postgrest["pump_logs"]
            .insert(pumpLog) {
                select()
            }
            .decodeSingle()
}
