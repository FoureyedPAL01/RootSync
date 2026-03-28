package com.project.rootsync.util

object UnitConverter {
    fun celsiusToFahrenheit(c: Double): Double = c * 9.0 / 5.0 + 32
    fun fahrenheitToCelsius(f: Double): Double = (f - 32) * 5.0 / 9.0
    fun litersToGallons(l: Double): Double = l * 0.264172
    fun mmToInches(mm: Double): Double = mm / 25.4
}
