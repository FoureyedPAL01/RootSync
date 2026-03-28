package com.project.rootsync.domain

import kotlin.math.max
import kotlin.math.min

/**
 * Domain logic for irrigation calculations
 */
object IrrigationCalculator {

    /**
     * Calculate water efficiency score based on pump usage and sensor readings
     * @param waterUsedLiters Total water used in liters
     * @param targetMoisture Desired soil moisture percentage
     * @param actualMoisture Actual soil moisture percentage after irrigation
     * @return Efficiency score from 0-100
     */
    fun calculateEfficiencyScore(
        waterUsedLiters: Double,
        targetMoisture: Double,
        actualMoisture: Double
    ): Double {
        if (waterUsedLiters <= 0) return 0.0

        val moistureEfficiency = if (targetMoisture > 0) {
            min(actualMoisture / targetMoisture, 1.0)
        } else {
            0.0
        }

        return (moistureEfficiency * 100).coerceIn(0.0, 100.0)
    }

    /**
     * Calculate irrigation duration based on evapotranspiration and crop coefficient
     * @param et0 Reference evapotranspiration (mm/day)
     * @param kc Crop coefficient
     * @param area Square meters to irrigate
     * @param flowRate Liters per minute
     * @return Duration in minutes
     */
    fun calculateIrrigationDuration(
        et0: Double,
        kc: Double,
        area: Double,
        flowRate: Double
    ): Double {
        if (flowRate <= 0 || area <= 0) return 0.0

        val waterNeed = et0 * kc * area // liters needed
        return waterNeed / flowRate
    }

    /**
     * Determine if irrigation is needed based on current moisture and forecast
     * @param currentMoisture Current soil moisture percentage
     * @param threshold Minimum moisture threshold
     * @param forecastPrecipitation Predicted precipitation in next 24h (mm)
     * @return True if irrigation is recommended
     */
    fun isIrrigationNeeded(
        currentMoisture: Double,
        threshold: Double,
        forecastPrecipitation: Double
    ): Boolean {
        return currentMoisture < threshold && forecastPrecipitation < 5.0
    }
}
