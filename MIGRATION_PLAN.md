# RootSync Migration Plan: Supabase to Supabase (Native Android)

## Current State
- **Supabase Project**: No user tables (only auth, storage system tables)
- **Local Project**: Android Kotlin/Compose app with Hilt, Navigation, Vico charts
- **Architecture**: MVVM with repositories, data models for SensorReading, Alert, PumpLog

---

### **Phase 1: New Supabase Infrastructure Setup**
Instead of switching to NoSQL, we are deploying a "Clean Slate" relational schema on your new account.

#### **1.1. Project Initialization**
* Create a new project in the **Supabase Dashboard**.
* **Auth:** Enable Email/Password. Configure the Redirect URL for your Android app (e.g., `rootsync://login-callback`).
* **Database:** Enable **Realtime** on the tables you need to stream to the Android UI (`sensor_readings`, `system_alerts`).
* **Storage:** Create a bucket for `plant_images` if you plan to store user-captured photos.

#### **1.2. Relational Schema Migration (SQL)**
Execute your DDL (Data Definition Language) in the new SQL Editor. Maintain these core tables:
* `user_profiles`: Linked via `uuid` to `auth.users`.
* `devices`: Metadata for ESP32 (UUID, name, location).
* `sensor_readings`: Time-series data (Foreign key to `devices`).
* `pump_logs`: Logs of irrigation events.
* `crop_profiles`: Thresholds for specific plants.

#### **1.3. Edge Functions (Deno/TypeScript)**
Redeploy your logic to the new account using the Supabase CLI:
* `perenual-lookup`: Fetch and cache plant care API data.
* `weekly-summary`: A cron-scheduled function (using `pg_net` or Supabase Cron) to email users.
* `purge-old-logs`: Clean up old data to keep the database lean.
* `send-alert-notification`: Triggered by a database webhook to send push notifications via FCM (using a service account key).

---

### **Phase 2: Android Project Setup (Kotlin/Compose)**
We will use the **Supabase Kotlin SDK**, which provides a very "Kotlin-first" way to handle PostgREST and Auth.

#### **2.1. Dependency Configuration**
* Add the Supabase BOM to your `build.gradle.kts`:
    * `postgrest-kt` (Database access)
    * `gotrue-kt` (Authentication)
    * `realtime-kt` (Websockets for live data)
    * `storage-kt` (File uploads)
* Add standard Android architecture components: **Hilt**, **Navigation Compose**, and **Retrofit** (for Open-Meteo).

#### **2.2. Directory Structure**
The structure remains "The Android Way," but the data layer now targets Supabase:
```
com.project.rootsync/
├── di/                    # Hilt Modules (Provides SupabaseClient, PostgREST instance)
├── data/
│   ├── model/             # Kotlin Data Classes with @Serializable (matches SQL tables)
│   ├── repository/        # Logic using supabase-kt (e.g., supabase.from("table").select())
│   └── remote/            # Open-Meteo API
├── domain/                # Calculation logic (ET, Efficiency Score)
├── ui/
│   ├── components/        # Reusable Compose components (ArcGauge, SensorCard)
│   ├── screens/           # Screen composables (Dashboard, Alerts, Weather)
│   └── theme/             # Theme files (Color, Type, Theme)
├── viewmodel/             # ViewModels for state management
└── util/                  # Helpers
```

---

### **Phase 3: Data Layer & Realtime State**
Replacing the Flutter logic with Kotlin Coroutines and the Supabase SDK.

#### **3.1. Authentication Repository**
* Implement `AuthRepository.kt` using `supabase.auth.signInWith(Email)`. Use `sessionStatus` Flow to reactively update the UI when a user logs in or out.

#### **3.2. Realtime Sensor Streams**
* In `SensorRepository.kt`, use the Supabase Realtime SDK:
  ```kotlin
  val channel = supabase.realtime.createChannel("sensor_changes")
  val flow = channel.postgresChangeFlow<SensorReading>(schema = "public") {
      table = "sensor_readings"
  }
  ```
* This creates a `Flow<SensorReading>` that feeds your ViewModels, replacing the old Flutter provider logic.

#### **3.3. Manual Overrides (MQTT)**
* Keep the **Eclipse Paho MQTT** client for low-latency manual pump control. While Supabase can handle this, HiveMQ is usually faster for direct hardware triggers.

---

### **Phase 4: UI Implementation (Jetpack Compose)**
#### **4.1. Dashboard & Analytics**
* **`DashboardScreen`:** Collect the Realtime `Flow`. When a new reading hits the database, Compose will automatically recompose the dials for moisture and temperature.
* **`WaterUsageScreen`:** Use **Vico** to graph data fetched via PostgREST. Since it's SQL, you can use complex `group by` queries directly through the SDK.

#### **4.2. Device Onboarding**
* **`DeviceChoiceScreen`:** Use a QR code scanner (via CameraX) to scan the ESP32 UUID and insert it into the `devices` table with the current user's `owner_id`.

---

### **Phase 5: Hardware & Notification Sync**
#### **5.1. ESP32 Update**
* Update your `config.h` with the **New Supabase URL** and **New Anon Key**.
* If you were using a custom Supabase library on ESP32, ensure it supports the new project's API version. The ESP32 will continue to `POST` JSON to your `sensor_readings` table.

#### **5.2. Push Notifications**
* Since Supabase doesn't have a built-in "Push" UI like Firebase, you will use a **Database Webhook**.
* **Flow:** ESP32 inserts "Low Water" alert → Supabase Webhook triggers Edge Function → Edge Function sends FCM message to the Android app.

---

### **Phase 6: Testing & Validation**
#### **6.1. Migration Verification**
* Use the Supabase Dashboard "Table Editor" to manually insert a row into `sensor_readings`.
* Verify that your Android app (running in the emulator or on a device) updates the UI instantly without a refresh.

#### **6.2. SQL Performance**
* Ensure you have added **Indexes** to your new database on `device_id` and `created_at` to keep the Kotlin app's queries fast as your time-series data grows.

---

**Would you like me to draft the SQL schema for your new Supabase tables, or would you prefer the Hilt Module code to initialize the Supabase client in Kotlin?**