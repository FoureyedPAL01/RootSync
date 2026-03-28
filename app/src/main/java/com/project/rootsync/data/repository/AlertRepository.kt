package com.project.rootsync.data.repository

import com.project.rootsync.data.model.Alert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun getAlertsForDevice(deviceId: String, limit: Long = 50): List<Alert> =
        supabase.postgrest["system_alerts"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<Alert>()

    suspend fun getUnreadAlerts(deviceId: String): List<Alert> =
        supabase.postgrest["system_alerts"]
            .select {
                filter {
                    eq("device_id", deviceId)
                    eq("is_read", false)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Alert>()

    suspend fun markAlertAsRead(alertId: String) {
        supabase.postgrest["system_alerts"]
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("id", alertId)
                }
            }
    }

    suspend fun markAllAlertsAsRead(deviceId: String) {
        supabase.postgrest["system_alerts"]
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("device_id", deviceId)
                    eq("is_read", false)
                }
            }
    }

    suspend fun getAlertCount(deviceId: String): Int =
        getUnreadAlerts(deviceId).size
}
