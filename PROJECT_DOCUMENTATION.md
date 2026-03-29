# RootSync — Project Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Directory Structure](#directory-structure)
4. [File Descriptions](#file-descriptions)
5. [Supabase Backend](#supabase-backend)
6. [Development Timeline](#development-timeline)
7. [Placeholders & TODOs](#placeholders--todos)
8. [Known Issues](#known-issues)

---

## Project Overview

RootSync is an Android IoT sensor monitoring and control application. It interfaces with a Supabase backend and ESP32 hardware devices to provide real-time soil moisture monitoring, automated irrigation, weather tracking, and fertigation management.

**GitHub Repository:** https://github.com/FoureyedPAL01/RootSync
**Branch:** main
**Last Updated:** March 29, 2026

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.0.21 |
| UI Framework | Jetpack Compose with Material3 |
| Build Tool | Gradle (Kotlin DSL) |
| Min SDK | 28 |
| Target SDK | 36 |
| DI Framework | Hilt |
| Backend | Supabase (PostgreSQL, Auth, Realtime, Edge Functions) |
| Charts | Vico (line/column charts) |
| Image Loading | Coil |
| MQTT Client | HiveMQ for ESP32 communication |
| Notifications | Firebase Cloud Messaging (FCM) |
| HTTP Client | Ktor (weather API), Retrofit |

---

## Directory Structure

```
RootSync/
├── .git/
├── .gitignore
├── AGENTS.md                          # Build commands and coding standards
├── MIGRATION_PLAN.md                  # Flutter-to-Kotlin migration guide
├── PROJECT_DOCUMENTATION.md           # This file
├── README.md
├── build.gradle.kts                   # Root build configuration
├── settings.gradle.kts                # Gradle settings
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties                   # Local secrets (gitignored)
│
├── app/
│   ├── build.gradle.kts               # App module dependencies
│   ├── google-services.json           # Firebase config (gitignored)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   ├── lottie/            # Weather animations (80+ JSON files)
│       │   │   │   ├── phone_portrait/
│       │   │   │   ├── phone_portrait_dark/
│       │   │   │   ├── phone_square/
│       │   │   │   ├── phone_square_dark/
│       │   │   │   ├── tablet_portrait/
│       │   │   │   ├── tablet_portrait_dark/
│       │   │   │   ├── tablet_square/
│       │   │   │   └── tablet_square_dark/
│       │   │   └── weather_icons/     # SVG weather icons (30+ files)
│       │   ├── res/
│       │   │   ├── drawable/          # App icons (foreground/background)
│       │   │   ├── font/              # Manrope-VariableFont_wght.ttf
│       │   │   ├── mipmap-*/          # App launcher icons
│       │   │   ├── values/
│       │   │   └── xml/
│       │   └── java/com/project/rootsync/
│       │       ├── MainActivity.kt
│       │       ├── RootSyncApp.kt
│       │       ├── data/
│       │       │   ├── model/         # @Serializable data classes
│       │       │   ├── remote/        # API interfaces (OpenMeteoApi)
│       │       │   └── repository/    # Supabase data access
│       │       ├── di/                # Hilt modules
│       │       ├── domain/            # Business logic
│       │       ├── service/           # MQTT, Notifications, FCM
│       │       ├── ui/
│       │       │   ├── components/    # Reusable composables
│       │       │   ├── navigation/    # NavGraph, Screen routes
│       │       │   ├── screens/       # 15 screen folders
│       │       │   └── theme/         # Color, Type, Theme
│       │       ├── util/              # Helpers, Enums
│       │       └── viewmodel/         # ViewModels (12 total)
│       ├── test/                      # Unit tests
│       └── androidTest/               # Instrumented tests
│
├── esp32/                             # ESP32 firmware
│   ├── .gitignore
│   ├── config.h                       # Supabase credentials
│   ├── config.h.example               # Template
│   ├── esp32.ino                      # Main Arduino sketch
│   ├── supabase_client.h              # Supabase HTTP client
│   └── weather_client.h               # Weather API client
│
├── gradle/
│   └── libs.versions.toml             # Version catalog
│
└── supabase/
    ├── .gitignore
    ├── .temp/
    ├── config.toml
    ├── migrations/                    # Database migrations
    └── seed.sql
```

---

## File Descriptions

### Root Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Root build file with plugin declarations (Kotlin 2.0.21, Hilt, KSP, Google Services) |
| `settings.gradle.kts` | Gradle settings, includes app module and version catalogs |
| `MIGRATION_PLAN.md` | 1247-line Flutter-to-Kotlin migration blueprint |
| `AGENTS.md` | Build commands, test commands, and coding standards |

### App Module — Core

| File | Purpose |
|------|---------|
| `RootSyncApp.kt` | Application class with `@HiltAndroidApp` |
| `MainActivity.kt` | Main activity with auth guard and navigation |
| `AndroidManifest.xml` | App permissions, activities, Firebase messaging service |

### App Module — Data Models (`data/model/`)

| File | Fields | Purpose |
|------|--------|---------|
| `Alert.kt` | id, deviceId, alertType, message, resolved, createdAt | System alerts |
| `CropProfile.kt` | id, name, plantName, minMoisture, perenualData | Plant profiles |
| `Device.kt` | id, userId, name, isOnline, lastSeen | ESP32 devices |
| `DeviceToken.kt` | userId, fcmToken | FCM tokens |
| `FertigationLog.kt` | id, deviceId, cropProfileId, fertilizedAt, notes | Fertilizer logs |
| `PumpLog.kt` | id, deviceId, pumpOnAt, durationSeconds, waterUsedLitres, moistureBefore, moistureAfter, rainDetected | Pump operations |
| `SensorReading.kt` | id, deviceId, soilMoisture, temperature, humidity, isRaining, flowRate, createdAt | Sensor data |
| `UserProfile.kt` | id, username, email, tempUnit, volumeUnit, timezone | User settings |
| `WeatherModels.kt` | WeatherResponse, CurrentWeather, HourlyWeather, DailyWeather, AirQualityResponse, WeatherData, DayForecast, WeatherUnits | Weather API models |

### App Module — Remote APIs (`data/remote/`)

| File | Purpose |
|------|---------|
| `OpenMeteoApi.kt` | Retrofit interfaces for Open-Meteo forecast and air quality APIs, plus helper objects (WeatherUnitConverter, WmoWeatherHelper, UvHelper, AqiHelper, WindDirectionHelper, WeatherDateHelper, DewPointCalculator) |

### App Module — Repositories (`data/repository/`)

| File | Purpose |
|------|---------|
| `AlertRepository.kt` | fetchAlerts, resolveAlert, deleteAllAlerts |
| `AuthRepository.kt` | signIn, signUp, signOut, getCurrentUserId, getEmailByUsername, isUsernameTaken |
| `CropProfileRepository.kt` | getProfilesForUser, setActiveProfile, fetchPerenualData |
| `DeviceRepository.kt` | getDevicesForUser, claimDevice, linkDevice, deleteDevice |
| `FertigationRepository.kt` | getActiveCropProfile, getFertigationLogs, logFertilizerApplication |
| `PumpRepository.kt` | getPumpLogs, getPumpLogsSince, getTotalWaterUsed |
| `SensorRepository.kt` | getLatestReading, getReadingsForRange, sensorReadingFlow, alertFlow |
| `WeatherRepository.kt` | fetchWeatherData (forecast + air quality) |

### App Module — Dependency Injection (`di/`)

| File | Purpose |
|------|---------|
| `MqttModule.kt` | Provides MqttClient instance |
| `NetworkModule.kt` | Provides WeatherApiService and AirQualityApiService via Retrofit |
| `SupabaseModule.kt` | Provides SupabaseClient instance |

### App Module — Services (`service/`)

| File | Purpose |
|------|---------|
| `MqttService.kt` | Connects to HiveMQ broker, publishes pump commands |
| `MyFirebaseMessagingService.kt` | Handles FCM push notifications |
| `NotificationService.kt` | Shows local notifications |

### App Module — UI Components (`ui/components/`)

| File | Purpose |
|------|---------|
| `ArcGauge.kt` | Circular gauge component |
| `BottomNavBar.kt` | Bottom navigation bar |
| `DeleteAccountButton.kt` | Button with confirmation dialog and loading state |
| `DeviceHealthTile.kt` | Device status tile |
| `DropdownSettingTile.kt` | Settings dropdown |
| `EditableTextTile.kt` | Inline editable text |
| `InlinePasswordTile.kt` | Expandable password change form (current + new) |
| `ReadOnlyTile.kt` | Read-only settings tile with icon |
| `SensorCard.kt` | Sensor value display card |
| `SettingsSection.kt` | Section wrapper with title and optional leading icon |
| `ThemeRadioGroup.kt` | Light/Dark/System theme selector |
| `ToggleSettingTile.kt` | Toggle switch with icon and save status |
| `WeatherAnimation.kt` | Lottie animation wrapper |

### App Module — Navigation (`ui/navigation/`)

| File | Purpose |
|------|---------|
| `Screen.kt` | Sealed class defining 15 navigation routes |
| `RootSyncNavGraph.kt` | NavHost with all screen routes |

### App Module — Screens (`ui/screens/`)

| Folder/File | Purpose |
|------------|---------|
| `login/LoginScreen.kt` | Tab-based sign in/sign up with username support |
| `login/LoginComponents.kt` | LoginTextField, ErrorBox, PrimaryButton |
| `dashboard/DashboardScreen.kt` | Sensor cards, pump control, rain status |
| `alerts/AlertsScreen.kt` | Alert list with filter chips, impact classification |
| `WeatherScreen.kt` | Hero card, hourly strip, daily forecast, condition tiles |
| `crop/CropProfilesScreen.kt` | Profile cards, Perenual data, bottom sheet editor |
| `device/DeviceManagementScreen.kt` | Device header, editable name, connectivity |
| `device/DeviceChoiceScreen.kt` | Continue with existing or relink device |
| `device/LinkDeviceScreen.kt` | UUID input with validation |
| `fertigation/FertigationScreen.kt` | Care data, application history |
| `irrigation/IrrigationScreen.kt` | 7-day soil moisture trend chart |
| `waterusage/WaterUsageScreen.kt` | Charts, stat cards, daily log table |
| `more/MoreScreen.kt` | Menu list with navigation items |
| `profile/ProfileScreen.kt` | Avatar, username edit, password change, sign out |
| `settings/SettingsScreen.kt` | Theme selection, notification toggles |
| `preferences/PreferencesScreen.kt` | Unit preferences, location editor |

### App Module — ViewModels (`viewmodel/`)

| File | Purpose |
|------|---------|
| `AlertViewModel.kt` | Load/filter/resolve alerts |
| `AuthViewModel.kt` | Sign in/up/out, session state |
| `CropProfileViewModel.kt` | Load/save/delete profiles, Perenual fetch |
| `DashboardViewModel.kt` | Sensor data, pump control, realtime |
| `DeviceChoiceViewModel.kt` | Load device ID for choice screen |
| `DeviceViewModel.kt` | Device list management |
| `FertigationViewModel.kt` | Fertilizer logs, care data |
| `IrrigationViewModel.kt` | 7-day soil moisture history |
| `LinkDeviceViewModel.kt` | UUID validation, device claiming |
| `PreferencesViewModel.kt` | Unit/location preferences |
| `ProfileViewModel.kt` | Username, password, sign out, delete account |
| `SettingsViewModel.kt` | Theme mode, notification toggles |
| `WaterUsageViewModel.kt` | Water usage aggregation, efficiency calculation |
| `WeatherViewModel.kt` | Weather data caching |

### App Module — Utilities (`util/`)

| File | Purpose |
|------|---------|
| `AlertHelpers.kt` | Impact enum, classifyImpact(), AlertDateHelpers |
| `DateHelpers.kt` | formatRelative(), formatTimestamp() |
| `Enums.kt` | PumpStatus, SensorStatus, IrrigationMode, TemperatureUnit, VolumeUnit, SaveStatus |
| `UnitConverter.kt` | Temperature, volume, wind conversions, formatVolume() |

### ESP32 Firmware

| File | Purpose |
|------|---------|
| `config.h` | Supabase URL, anon key, WiFi credentials |
| `esp32.ino` | Main Arduino sketch |
| `supabase_client.h` | HTTP client for Supabase REST API |
| `weather_client.h` | HTTP client for Open-Meteo API |

---

## Supabase Backend

### Database Tables

| Table | Purpose | RLS |
|-------|---------|-----|
| `users` | User profiles and preferences | Owner-based |
| `devices` | ESP32 device registry | Owner + ESP32 |
| `sensor_readings` | Sensor telemetry | Owner + ESP32 |
| `crop_profiles` | Plant profiles | Owner + ESP32 |
| `pump_logs` | Pump operation records | Owner + ESP32 |
| `alerts` | System alerts | Owner + ESP32 |
| `fertigation_logs` | Fertilizer application records | Owner-based |
| `device_commands` | Commands sent to devices | Owner + ESP32 |
| `device_tokens` | FCM push tokens | Owner-based |

### Edge Functions

| Function | Purpose | Required Env Vars |
|----------|---------|-------------------|
| `send-alert-notification` | Push notifications via FCM | `FCM_SERVICE_ACCOUNT` |
| `weekly-summary` | Email digest of water usage | - |
| `perenual-lookup` | Fetch plant care data | `PERENUAL_API_KEY` |
| `purge-old-logs` | Delete pump_logs older than 14 days | - |

**Note:** `weekly-summary` has a bug — references `user_profiles` table instead of `users`

---

## Development Timeline

### Phase 1: Project Setup & Repository Creation
1. Created GitHub repository "RootSync" with Kotlin .gitignore and MIT license
2. Created `MIGRATION_PLAN.md` — 1247-line Flutter-to-Kotlin migration blueprint
3. Connected local Android project to GitHub and pushed initial files
4. Created `AGENTS.md` with build/test commands and coding standards

### Phase 2: Project Bootstrap
1. Configured `build.gradle.kts` with Hilt, Supabase SDK, Compose BOM, Firebase dependencies
2. Created `RootSyncApp.kt` — Application class with `@HiltAndroidApp`
3. Created `SupabaseModule.kt` — Hilt DI for SupabaseClient
4. Created `MqttModule.kt` — Hilt DI for MqttClient

### Phase 3: Auth & Navigation
1. Created `Screen.kt` — Sealed class defining 15 navigation routes
2. Created `RootSyncNavGraph.kt` — NavHost with auth guard
3. Created `AuthRepository.kt` — Sign in/up/out with Supabase
4. Created `AuthViewModel.kt` — Session state management
5. Created initial `LoginScreen.kt` — Email/password sign in

### Phase 4: Data Models & Repositories
1. Created `SensorReading.kt` — Soil moisture, temperature, humidity, rain, flow rate
2. Created `Device.kt` — ESP32 device with online status
3. Created `CropProfile.kt` — Plant profiles with moisture thresholds
4. Created `PumpLog.kt` — Pump operation records
5. Created `Alert.kt` — System alerts
6. Created `UserProfile.kt` — User settings
7. Created all corresponding Repository classes

### Phase 5: Realtime & Dashboard
1. Implemented `sensorReadingFlow()` in SensorRepository for Supabase Realtime
2. Created `MqttService.kt` for ESP32 communication via HiveMQ
3. Created initial `DashboardViewModel.kt` and `DashboardScreen.kt`

### Phase 6: Initial Screen Implementation
1. Created all 15 screens in basic form with navigation
2. Created 8 basic composable widgets (SettingsSection, ToggleSettingTile, etc.)

### Phase 7: Notifications
1. Created `NotificationService.kt` for local notifications
2. Created `MyFirebaseMessagingService.kt` for FCM

### Phase 8: Assets & Theme
1. Added Lottie weather animations (80+ JSON files)
2. Added weather SVG icons (30+ files)
3. Added Manrope font (VariableFont_wght.ttf)
4. Added adaptive app icons
5. Created `BottomNavBar.kt`

### Phase 9: Supabase Backend Migration
1. Verified Supabase schema — 9 tables with RLS
2. Verified RLS policies — 35 policies
3. Verified Edge Functions — 4 functions
4. Identified bug in `weekly-summary` function

### Phase 10: ESP32 Configuration
1. Updated `config.h` with new Supabase credentials
2. Verified ESP32 firmware files

### Phase 11: Weather Screen Enhancement
1. Created `WeatherModels.kt` — Full weather data classes
2. Created `WeatherViewModel.kt` — Caching, unit preferences
3. Created `WeatherComponents.kt` — HeroCard, HourlyStrip, DayRow, condition tiles
4. Rewrote `WeatherScreen.kt` — Pull-to-refresh, scrollable layout

### Phase 12: More Screen Enhancement
1. Rewrote `MoreScreen.kt` — Header with avatar, styled menu items

### Phase 13: Device Management Screen Enhancement
1. Created `DeviceManagementScreen.kt` — Device header, editable name, connectivity, danger zone
2. Updated `DeviceRepository.kt` — Added claimDevice(), fixed user_id field
3. Updated `Device.kt` — Added lastSeen, userId fields

### Phase 14: Crop Profiles Screen Enhancement
1. Updated `CropProfile.kt` — Added plantName, perenualData, userId
2. Updated `CropProfileRepository.kt` — Added fetchPerenualData(), setActiveProfile()
3. Updated `CropProfileViewModel.kt` — Active profile, fetching state
4. Rewrote `CropProfilesScreen.kt` — Bottom sheet, profile cards, plant data panel

### Phase 15: Water Usage Screen Enhancement
1. Updated `PumpLog.kt` — Fixed field names (pumpOnAt, waterUsedLitres)
2. Updated `PumpRepository.kt` — Added getPumpLogsSince()
3. Updated `UnitConverter.kt` — Added formatVolume()
4. Created `WaterUsageViewModel.kt` — Log aggregation, efficiency calculation
5. Rewrote `WaterUsageScreen.kt` — Charts, stat cards, daily log table

### Phase 16: Fertigation Screen Enhancement
1. Created `FertigationLog.kt` — Fertilizer application records
2. Created `FertigationRepository.kt` — Active profile, logs, Perenual fetch
3. Created `FertigationViewModel.kt` — Care data, application logging
4. Rewrote `FertigationScreen.kt` — Plant hero, care guide, history

### Phase 17: Login Screen Enhancement
1. Created `LoginComponents.kt` — Styled text fields, error box, primary button
2. Rewrote `LoginScreen.kt` — Tab-based sign in/up, username support
3. Updated `AuthRepository.kt` — Added getEmailByUsername(), isUsernameTaken(), updateUsername()
4. Updated `AuthViewModel.kt` — Username support, obscure toggles
5. Added `onUserLogin()` to NotificationService

### Phase 18: Dashboard Screen Enhancement
1. Rewrote `DashboardScreen.kt` — Sensor cards with gauges, pump control, rain card
2. Updated `DashboardViewModel.kt` — Sensor history, chart data

### Phase 19: Link Device Screen Enhancement
1. Created `LinkDeviceViewModel.kt` — UUID validation, device claiming
2. Rewrote `LinkDeviceScreen.kt` — Sensor icon, name input, UUID format validation

### Phase 20: Device Choice Screen Enhancement
1. Created `DeviceChoiceViewModel.kt` — Load existing device
2. Rewrote `DeviceChoiceScreen.kt` — Animations, UUID card, copy button

### Phase 21: Alerts Screen Enhancement
1. Created `AlertHelpers.kt` — Impact enum, classifyImpact(), timeAgoShort()
2. Created `AlertViewModel.kt` — Filter, resolve, optimistic updates
3. Updated `Alert.kt` — Changed to alertType, resolved fields
4. Updated `AlertRepository.kt` — Changed to alerts table, resolved field
5. Rewrote `AlertsScreen.kt` — Filter chips, impact colors, resolve button

### Phase 22: Irrigation Screen Enhancement
1. Created `IrrigationViewModel.kt` — 7-day soil moisture history
2. Rewrote `IrrigationScreen.kt` — Vico line chart, pull-to-refresh

### Phase 23: Preferences Screen Enhancement
1. Created `PreferencesViewModel.kt` — Unit/location preferences
2. Rewrote `PreferencesScreen.kt` — Radio groups, location editor

### Phase 24: Settings Screen Enhancement
1. Created `SettingsViewModel.kt` — Theme, notification toggles
2. Created `ThemeRadioGroup.kt` — Light/Dark/System selector
3. Updated `ToggleSettingTile.kt` — Added icon, saveStatus
4. Updated `SettingsSection.kt` — Added leadingIcon parameter
5. Added `SaveStatus` enum to Enums.kt
6. Rewrote `SettingsScreen.kt` — Theme selection, 6 notification toggles

### Phase 25: Profile Screen Enhancement
1. Created `ProfileViewModel.kt` — Username, password, sign out, delete account
2. Updated `InlinePasswordTile.kt` — Expand/collapse, current + new password
3. Updated `DeleteAccountButton.kt` — Loading state
4. Updated `ReadOnlyTile.kt` — Icon support
5. Rewrote `ProfileScreen.kt` — Avatar, editable username, password change

### Phase 26: Compilation Fix & Final Push
1. Fixed Kotlin version mismatch (unified to 2.0.21)
2. Added kotlin-compose plugin
3. Updated build.gradle.kts with correct dependencies
4. Verified .gitignore files
5. Pushed all changes to GitHub

---

## Placeholders & TODOs

### Build Secrets (`local.properties`)
```
SUPABASE_URL=https://nkargxiljgngngzgfmkx.supabase.co     [FILLED]
SUPABASE_ANON_KEY=eyJhbGci...                              [FILLED]
HIVEMQ_HOST=your-cluster.hivemq.cloud                      [TODO]
HIVEMQ_USERNAME=your_username                               [TODO]
HIVEMQ_PASSWORD=your_password                               [TODO]
```

### Edge Function Env Vars (Supabase Dashboard)
```
FCM_SERVICE_ACCOUNT = <Firebase service account JSON>       [TODO]
PERENUAL_API_KEY = <API key from perenual.com>              [TODO]
```

### Kotlin TODOs

| File | Line | Issue |
|------|------|-------|
| `DashboardScreen.kt` | - | Get deviceId from DataStore |
| `DeviceChoiceScreen.kt` | - | Get userId from auth, save to DataStore |
| `DeviceManagementScreen.kt` | - | Get userId from auth |
| `LinkDeviceScreen.kt` | - | Get userId from auth |
| `CropProfilesScreen.kt` | - | Get deviceId from DataStore |
| `IrrigationScreen.kt` | - | Get deviceId from DataStore |
| `WaterUsageScreen.kt` | - | Get deviceId from DataStore |
| `WeatherScreen.kt` | - | Get location from user profile |
| `AlertsScreen.kt` | - | Get deviceId from DataStore |
| `FertigationScreen.kt` | - | Get deviceId from DataStore |
| `ProfileScreen.kt` | - | Full implementation |
| `PreferencesScreen.kt` | - | DataStore persistence |
| `SettingsScreen.kt` | - | DataStore persistence |

---

## Known Issues

1. **Gradle Build Failure** — "Cannot add extension with name 'kotlin'" error. Needs `gradle --stop` and `.gradle` cache clearing.

2. **Edge Function Bug** — `weekly-summary` references `user_profiles` table but actual table is `users`.

3. **Missing DataStore** — Preferences/Settings not persisted. Need to implement DataStore<Preferences> for user preferences.

4. **Device ID Everywhere** — Many screens use TODO placeholder for deviceId. Need to implement proper device selection via DataStore.

5. **Location for Weather** — WeatherScreen uses 0.0, 0.0 coordinates. Need to fetch from user profile or DataStore.

---

## Build Commands

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK
./gradlew build                # Build all variants
./gradlew installDebug         # Install debug APK
./gradlew test                 # Run unit tests
./gradlew lint                 # Run lint analysis
./gradlew clean                # Clean build artifacts
```

---

## Git Commit History

| Commit | Date | Message |
|--------|------|---------|
| 36c9f45 | Mar 29, 2026 | Complete migration: Kotlin 2.0.21, assets, ESP32 config, new Supabase |
| 8da3b1d | Mar 29, 2026 | Complete Flutter-to-Kotlin migration: all screens, ViewModels, and components |
