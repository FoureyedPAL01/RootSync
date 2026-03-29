package com.project.rootsync.data.repository

import com.project.rootsync.data.model.SensorReading
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
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
                order("recorded_at", Order.DESCENDING)
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
                    gte("recorded_at", from)
                    lte("recorded_at", to)
                }
                order("recorded_at", Order.ASCENDING)
            }
            .decodeList<SensorReading>()

    suspend fun getRecentReadings(deviceId: String, limit: Long = 24): List<SensorReading> =
        supabase.postgrest["sensor_readings"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("recorded_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<SensorReading>()

    fun sensorReadingFlow(deviceId: String): Flow<SensorReading> {
        val channel = supabase.channel("sensor:$deviceId")
        return channel
            .postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "sensor_readings"
                filter = "device_id=eq.$deviceId"
            }
            .map { action ->
                Json.decodeFromJsonElement(
                    SensorReading.serializer(),
                    action.record
                )
            }
            .onStart { channel.subscribe() }
            .onCompletion { supabase.removeChannel(channel) }
    }

    fun alertFlow(deviceId: String): Flow<PostgresAction> {
        val channel = supabase.channel("alerts:$deviceId")
        return channel
            .postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "alerts"
                filter = "device_id=eq.$deviceId"
            }
            .onStart { channel.subscribe() }
            .onCompletion { supabase.removeChannel(channel) }
    }
}
