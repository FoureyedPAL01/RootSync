package com.project.rootsync.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object DateHelpers {
    fun formatRelative(instant: Instant): String {
        val now = Clock.System.now()
        val diff = now - instant
        return when {
            diff < 1.minutes -> "Just now"
            diff < 1.hours -> "${diff.inWholeMinutes}m ago"
            diff < 1.days -> "${diff.inWholeHours}h ago"
            else -> instant.toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()
        }
    }

    fun formatTimestamp(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            formatRelative(instant)
        } catch (e: Exception) {
            timestamp
        }
    }
}
