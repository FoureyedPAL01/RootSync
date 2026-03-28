package com.project.rootsync.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for common operations
 */

/**
 * Format timestamp to readable date string
 */
fun Long.toDateString(pattern: String = "MMM dd, yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Format timestamp to relative time (e.g., "2 hours ago")
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> toDateString("MMM dd")
    }
}

/**
 * Round double to specified decimal places
 */
fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

/**
 * Convert Celsius to Fahrenheit
 */
fun Double.toFahrenheit(): Double = (this * 9 / 5) + 32

/**
 * Check if value is within range
 */
fun Double.isInRange(min: Double, max: Double): Boolean = this in min..max
