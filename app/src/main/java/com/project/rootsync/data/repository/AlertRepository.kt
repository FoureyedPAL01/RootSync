package com.project.rootsync.data.repository

import com.project.rootsync.data.model.Alert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for alert-related operations.
 */
@Singleton
class AlertRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    suspend fun fetchAlerts(deviceId: String): List<Alert> {
        return supabase.postgrest["alerts"]
            .select {
                filter {
                    eq("device_id", deviceId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Alert>()
    }

    suspend fun getUnreadAlerts(deviceId: String): List<Alert> =
        supabase.postgrest["alerts"]
            .select {
                filter {
                    eq("device_id", deviceId)
                    eq("resolved", false)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Alert>()

    suspend fun resolveAlert(id: Long) {
        supabase.postgrest["alerts"]
            .update(mapOf("resolved" to true)) {
                filter {
                    eq("id", id)
                }
            }
    }

    suspend fun markAllAsResolved(deviceId: String) {
        supabase.postgrest["alerts"]
            .update(mapOf("resolved" to true)) {
                filter {
                    eq("device_id", deviceId)
                    eq("resolved", false)
                }
            }
    }

    suspend fun deleteAllAlerts(deviceId: String) {
        supabase.postgrest["alerts"]
            .delete {
                filter {
                    eq("device_id", deviceId)
                }
            }
    }

    suspend fun getAlertCount(deviceId: String): Int =
        getUnreadAlerts(deviceId).size
}
