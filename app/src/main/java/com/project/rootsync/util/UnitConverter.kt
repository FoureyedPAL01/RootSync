package com.project.rootsync.util

/**
 * Unit conversion utilities.
 */
object UnitConverter {
    fun celsiusToFahrenheit(c: Double): Double = c * 9.0 / 5.0 + 32
    fun fahrenheitToCelsius(f: Double): Double = (f - 32) * 5.0 / 9.0
    fun litersToGallons(l: Double): Double = l * 0.264172
    fun mmToInches(mm: Double): Double = mm / 25.4

    /**
     * Formats volume based on user's preferred unit.
     */
    fun formatVolume(litres: Double, unit: String): String {
        return when (unit.lowercase()) {
            "gallons" -> {
                val gallons = litersToGallons(litres)
                "${gallons.toStringAsFixed(1)} gal"
            }
            else -> "${litres.toStringAsFixed(1)} L"
        }
    }
}

/**
 * Extension function to format Double with fixed decimal places.
 */
fun Double.toStringAsFixed(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
