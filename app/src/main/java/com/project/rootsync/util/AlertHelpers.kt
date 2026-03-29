package com.project.rootsync.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Alert impact levels for classification.
 */
enum class Impact {
    CRITICAL, WARNING, INFO
}

/**
 * Classifies an alert type into an impact level.
 */
fun classifyImpact(alertType: String): Impact {
    val t = alertType.lowercase()
    return when {
        t.contains("sensor_stuck") || t.contains("no_flow") || t.contains("fault") -> Impact.CRITICAL
        t.contains("pump") || t.contains("wifi") || t.contains("offline") || t.contains("reconnect") -> Impact.WARNING
        else -> Impact.INFO
    }
}

/**
 * Date/time helper for alert timestamps.
 */
object AlertDateHelpers {
    fun timeAgoShort(isoString: String): String {
        return try {
            val instant = Instant.parse(isoString)
            val now = Clock.System.now()
            val diff = now - instant

            val minutes = diff.inWholeMinutes
            val hours = diff.inWholeHours
            val days = diff.inWholeDays

            when {
                minutes < 1 -> "Just now"
                hours < 1 -> "${minutes}m ago"
                days < 1 -> "${hours}h ago"
                else -> {
                    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    "${date.month.name.take(3)} ${date.dayOfMonth}"
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
