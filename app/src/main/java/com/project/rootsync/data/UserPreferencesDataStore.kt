package com.project.rootsync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Top-level extension — one DataStore per app
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * User preferences DataStore for caching local settings.
 * Maps to Supabase users and devices tables.
 */
class UserPreferencesDataStore(private val context: Context) {

    companion object {
        // Device & User
        val DEVICE_ID = stringPreferencesKey("device_id")
        val USER_ID = stringPreferencesKey("user_id")

        // Theme
        val THEME = stringPreferencesKey("theme") // "light" / "dark" / "system"

        // Units (matches users table columns)
        val TEMP_UNIT = stringPreferencesKey("temp_unit") // "celsius" / "fahrenheit"
        val VOLUME_UNIT = stringPreferencesKey("volume_unit") // "litres" / "gallons"
        val WIND_UNIT = stringPreferencesKey("wind_unit") // "km/h" / "m/s" / "mph" / "kn"
        val PRECIPITATION_UNIT = stringPreferencesKey("precipitation_unit") // "mm" / "inch"
        val AQI_TYPE = stringPreferencesKey("aqi_type") // "us" / "eu"

        // Location
        val LOCATION_LAT = stringPreferencesKey("location_lat")
        val LOCATION_LON = stringPreferencesKey("location_lon")
        val TIMEZONE = stringPreferencesKey("timezone")

        // Notification settings (matches users table boolean columns)
        val PUMP_ALERTS = booleanPreferencesKey("pump_alerts")
        val SOIL_MOISTURE_ALERTS = booleanPreferencesKey("soil_moisture_alerts")
        val WEATHER_ALERTS = booleanPreferencesKey("weather_alerts")
        val FERTIGATION_REMINDERS = booleanPreferencesKey("fertigation_reminders")
        val DEVICE_OFFLINE_ALERTS = booleanPreferencesKey("device_offline_alerts")
        val WEEKLY_SUMMARY = booleanPreferencesKey("weekly_summary")
    }

    // --- Flows (read) ---

    val deviceIdFlow: Flow<String> = context.dataStore.data
        .map { it[DEVICE_ID] ?: "" }

    val userIdFlow: Flow<String> = context.dataStore.data
        .map { it[USER_ID] ?: "" }

    val themeFlow: Flow<String> = context.dataStore.data
        .map { it[THEME] ?: "system" }

    val tempUnitFlow: Flow<String> = context.dataStore.data
        .map { it[TEMP_UNIT] ?: "celsius" }

    val volumeUnitFlow: Flow<String> = context.dataStore.data
        .map { it[VOLUME_UNIT] ?: "litres" }

    val windUnitFlow: Flow<String> = context.dataStore.data
        .map { it[WIND_UNIT] ?: "km/h" }

    val precipitationUnitFlow: Flow<String> = context.dataStore.data
        .map { it[PRECIPITATION_UNIT] ?: "mm" }

    val aqiTypeFlow: Flow<String> = context.dataStore.data
        .map { it[AQI_TYPE] ?: "us" }

    val locationLatFlow: Flow<String> = context.dataStore.data
        .map { it[LOCATION_LAT] ?: "19.0760" }

    val locationLonFlow: Flow<String> = context.dataStore.data
        .map { it[LOCATION_LON] ?: "72.8777" }

    val timezoneFlow: Flow<String> = context.dataStore.data
        .map { it[TIMEZONE] ?: "UTC" }

    val pumpAlertsFlow: Flow<Boolean> = context.dataStore.data
        .map { it[PUMP_ALERTS] ?: true }

    val soilMoistureAlertsFlow: Flow<Boolean> = context.dataStore.data
        .map { it[SOIL_MOISTURE_ALERTS] ?: true }

    val weatherAlertsFlow: Flow<Boolean> = context.dataStore.data
        .map { it[WEATHER_ALERTS] ?: true }

    val fertigationRemindersFlow: Flow<Boolean> = context.dataStore.data
        .map { it[FERTIGATION_REMINDERS] ?: true }

    val deviceOfflineAlertsFlow: Flow<Boolean> = context.dataStore.data
        .map { it[DEVICE_OFFLINE_ALERTS] ?: true }

    val weeklySummaryFlow: Flow<Boolean> = context.dataStore.data
        .map { it[WEEKLY_SUMMARY] ?: false }

    // --- Suspend functions (write) ---

    suspend fun saveDeviceId(id: String) =
        context.dataStore.edit { it[DEVICE_ID] = id }

    suspend fun saveUserId(id: String) =
        context.dataStore.edit { it[USER_ID] = id }

    suspend fun saveTheme(theme: String) =
        context.dataStore.edit { it[THEME] = theme }

    suspend fun saveTempUnit(unit: String) =
        context.dataStore.edit { it[TEMP_UNIT] = unit }

    suspend fun saveVolumeUnit(unit: String) =
        context.dataStore.edit { it[VOLUME_UNIT] = unit }

    suspend fun saveWindUnit(unit: String) =
        context.dataStore.edit { it[WIND_UNIT] = unit }

    suspend fun savePrecipitationUnit(unit: String) =
        context.dataStore.edit { it[PRECIPITATION_UNIT] = unit }

    suspend fun saveAqiType(type: String) =
        context.dataStore.edit { it[AQI_TYPE] = type }

    suspend fun saveLocation(lat: String, lon: String) =
        context.dataStore.edit {
            it[LOCATION_LAT] = lat
            it[LOCATION_LON] = lon
        }

    suspend fun saveTimezone(tz: String) =
        context.dataStore.edit { it[TIMEZONE] = tz }

    suspend fun savePumpAlerts(enabled: Boolean) =
        context.dataStore.edit { it[PUMP_ALERTS] = enabled }

    suspend fun saveSoilMoistureAlerts(enabled: Boolean) =
        context.dataStore.edit { it[SOIL_MOISTURE_ALERTS] = enabled }

    suspend fun saveWeatherAlerts(enabled: Boolean) =
        context.dataStore.edit { it[WEATHER_ALERTS] = enabled }

    suspend fun saveFertigationReminders(enabled: Boolean) =
        context.dataStore.edit { it[FERTIGATION_REMINDERS] = enabled }

    suspend fun saveDeviceOfflineAlerts(enabled: Boolean) =
        context.dataStore.edit { it[DEVICE_OFFLINE_ALERTS] = enabled }

    suspend fun saveWeeklySummary(enabled: Boolean) =
        context.dataStore.edit { it[WEEKLY_SUMMARY] = enabled }
}
