# RootSync — Flutter to Kotlin/Compose Migration Plan
### Complete 1-to-1 Mapping from SISSv2 Flutter Architecture

---

## Table of Contents
1. [Project Overview & Strategy](#overview)
2. [New Android Project Structure](#structure)
3. [Dependency Mapping (pubspec → build.gradle)](#dependencies)
4. [State Management: Provider → ViewModel + StateFlow](#state)
5. [Navigation: GoRouter → Navigation Compose](#navigation)
6. [Screen-by-Screen Migration (15 Screens)](#screens)
7. [Widget-by-Widget Migration (8 Widgets)](#widgets)
8. [Service Migration (MQTT + Notifications)](#services)
9. [Utils Migration (3 Helpers)](#utils)
10. [Data Layer: Supabase SDK Migration](#data)
11. [Realtime: Flutter Streams → Kotlin Flows](#realtime)
12. [Assets Migration (Lottie, Fonts, Icons)](#assets)
13. [Supabase Backend (Schema + Edge Functions)](#backend)
14. [ESP32 — No Changes Required](#esp32)
15. [Build & Deployment](#build)
16. [Phase-by-Phase Execution Checklist](#checklist)

---

## 1. Project Overview & Strategy <a name="overview"></a>

You are migrating from a **Flutter (Dart) cross-platform app** to a **native Android (Kotlin + Jetpack Compose)** app. The backend (Supabase), MQTT broker (HiveMQ), and ESP32 firmware stay **identical** — only the mobile client changes.

### Key Principle: Everything Has a Direct Equivalent

| Flutter Concept | Kotlin/Android Equivalent |
|---|---|
| `Provider` / `ChangeNotifier` | `ViewModel` + `StateFlow` |
| `GoRouter` | Navigation Compose + `NavHost` |
| `StatelessWidget` | `@Composable` function |
| `StatefulWidget` | `@Composable` + `remember` / ViewModel |
| `StreamBuilder` | `collectAsState()` |
| `Future` / `async-await` | Kotlin `suspend` + Coroutines |
| `Stream<T>` | `Flow<T>` |
| `SharedPreferences` | `DataStore<Preferences>` |
| `BuildContext` | Hilt `@HiltViewModel` injection |
| `pubspec.yaml` | `build.gradle.kts` |
| `dart:convert` | `kotlinx.serialization` |

---

## 2. New Android Project Structure <a name="structure"></a>

### Create a new Android project in Android Studio
- **Template:** Empty Activity (Compose)
- **Package:** `com.yourdomain.rootsync`
- **Language:** Kotlin
- **Min SDK:** API 26 (Android 8.0) — covers 95%+ of active devices
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`)

### Target Directory Structure

```
com.yourdomain.rootsync/
│
├── RootSyncApp.kt               # Application class (Hilt entry point)
├── MainActivity.kt              # Single activity host
│
├── di/                          # Hilt dependency injection modules
│   ├── SupabaseModule.kt        # Provides SupabaseClient instance
│   ├── MqttModule.kt            # Provides MQTT client
│   └── RepositoryModule.kt      # Binds repository interfaces
│
├── data/
│   ├── model/                   # @Serializable data classes (1-to-1 with DB tables)
│   │   ├── UserProfile.kt
│   │   ├── Device.kt
│   │   ├── SensorReading.kt
│   │   ├── PumpLog.kt
│   │   ├── CropProfile.kt
│   │   ├── SystemAlert.kt
│   │   └── DeviceToken.kt
│   │
│   ├── repository/              # Data access logic (replaces Provider data methods)
│   │   ├── AuthRepository.kt
│   │   ├── DeviceRepository.kt
│   │   ├── SensorRepository.kt
│   │   ├── PumpRepository.kt
│   │   ├── CropProfileRepository.kt
│   │   ├── AlertRepository.kt
│   │   └── WeatherRepository.kt
│   │
│   └── remote/
│       └── OpenMeteoApi.kt      # Retrofit service (replaces weather_client.dart)
│
├── domain/                      # Pure business logic (no Android dependencies)
│   ├── AutoIrrigationLogic.kt   # Decision logic from ESP32 mirrored for UI
│   └── WaterUsageCalculator.kt  # Efficiency score calculations
│
├── ui/
│   ├── navigation/
│   │   └── RootSyncNavGraph.kt  # Replaces router.dart
│   │
│   ├── theme/
│   │   ├── Color.kt             # Replaces theme.dart colors
│   │   ├── Type.kt              # Manrope font + typography
│   │   └── Theme.kt             # MaterialTheme setup
│   │
│   ├── screens/                 # 15 screens — see Section 6
│   │   ├── login/
│   │   ├── dashboard/
│   │   ├── device/
│   │   ├── crop/
│   │   ├── irrigation/
│   │   ├── waterusage/
│   │   ├── weather/
│   │   ├── alerts/
│   │   ├── fertigation/
│   │   ├── settings/
│   │   └── more/
│   │
│   └── components/              # Replaces widgets/ folder (8 widgets → Composables)
│       ├── DeviceHealthTile.kt
│       ├── ToggleSettingTile.kt
│       ├── DropdownSettingTile.kt
│       ├── EditableTextTile.kt
│       ├── SettingsSection.kt
│       ├── ReadOnlyTile.kt
│       ├── InlinePasswordTile.kt
│       └── DeleteAccountButton.kt
│
└── util/
    ├── DateHelpers.kt           # Replaces date_helpers.dart
    ├── UnitConverter.kt         # Replaces unit_converter.dart
    └── Enums.kt                 # Replaces enums.dart
```

---

## 3. Dependency Mapping <a name="dependencies"></a>

### `pubspec.yaml` → `build.gradle.kts`

| Flutter Package | Kotlin/Android Equivalent | Purpose |
|---|---|---|
| `supabase_flutter` | `io.github.jan-tennert.supabase:bom` | Backend SDK |
| `provider` | `androidx.lifecycle:lifecycle-viewmodel-compose` | State management |
| `go_router` | `androidx.navigation:navigation-compose` | Navigation |
| `mqtt_client` (MQTTtx) | `org.eclipse.paho:org.eclipse.paho.client.mqttv3` | MQTT |
| `firebase_messaging` | `com.google.firebase:firebase-messaging-ktx` | Push notifications |
| `fl_chart` | `com.patrykandpatrick.vico:compose` | Charts (Vico) |
| `shared_preferences` | `androidx.datastore:datastore-preferences` | Key-value storage |
| `lottie` | `com.airbnb.android:lottie-compose` | Lottie animations |
| `intl` | `java.time` / `kotlinx-datetime` | Date formatting |
| `qr_code_scanner` (CameraX) | `androidx.camera:camera-camera2` + `com.google.mlkit:barcode-scanning` | QR scanning |
| `google_fonts` | Bundle Manrope TTF in `assets/fonts/` | Custom fonts |
| `http` | `io.ktor:ktor-client-android` OR Retrofit | HTTP client |
| `image_picker` | `androidx.activity:activity-compose` (photo picker) | Image selection |

### Complete `build.gradle.kts` (app module)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.53.1")
    ksp("com.google.dagger:hilt-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Supabase BOM
    val supabaseBom = platform("io.github.jan-tennert.supabase:bom:3.0.3")
    implementation(supabaseBom)
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")

    // Ktor engine for Supabase
    implementation("io.ktor:ktor-client-android:3.0.2")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Charts
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.28")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // CameraX + Barcode (QR scanner)
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Coil (image loading, replaces Flutter's Image widgets)
    implementation("io.coil-kt:coil-compose:2.7.0")
}
```

---

## 4. State Management: Provider → ViewModel + StateFlow <a name="state"></a>

### Flutter `app_state_provider.dart` (564 lines) → Split into focused ViewModels

The monolithic `AppStateProvider` should be **split** into focused ViewModels. Each screen gets its own ViewModel, all backed by repositories.

| Flutter Provider Feature | Kotlin ViewModel | File |
|---|---|---|
| `init()` + device fetch | `DeviceViewModel.init()` | `DeviceViewModel.kt` |
| `refreshSensorData()` | `SensorViewModel` + StateFlow | `SensorViewModel.kt` |
| `sendMqttCommand()` | `DashboardViewModel.sendPumpCommand()` | `DashboardViewModel.kt` |
| Crop profile management | `CropProfileViewModel` | `CropProfileViewModel.kt` |
| Alert handling | `AlertViewModel` | `AlertViewModel.kt` |
| User profile & settings | `ProfileViewModel` | `ProfileViewModel.kt` |
| Auth state | `AuthViewModel` | `AuthViewModel.kt` |

### Pattern: How to Replace `ChangeNotifier`

**Flutter (before):**
```dart
class AppStateProvider extends ChangeNotifier {
  List<SensorReading> _readings = [];
  
  Future<void> refreshSensorData() async {
    _readings = await supabase.from('sensor_readings').select();
    notifyListeners();
  }
}
```

**Kotlin (after):**
```kotlin
@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepo: SensorRepository
) : ViewModel() {

    private val _readings = MutableStateFlow<List<SensorReading>>(emptyList())
    val readings: StateFlow<List<SensorReading>> = _readings.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _readings.value = sensorRepo.getLatestReadings()
        }
    }
}

// In Composable:
val readings by viewModel.readings.collectAsState()
```

### Hilt Module: Replacing `main.dart` Supabase Init

**Flutter `main.dart`:**
```dart
await Supabase.initialize(url: supabaseUrl, anonKey: supabaseAnonKey);
```

**Kotlin `SupabaseModule.kt` (Hilt):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
```

Store secrets in `local.properties` and expose via `BuildConfig` — equivalent to Flutter's `.env`.

---

## 5. Navigation: GoRouter → Navigation Compose <a name="navigation"></a>

### Route Mapping

| Flutter GoRouter Route | Android NavGraph Route |
|---|---|
| `/login` | `Screen.Login` |
| `/dashboard` | `Screen.Dashboard` |
| `/devices` | `Screen.DeviceManagement` |
| `/devices/link` | `Screen.LinkDevice` |
| `/devices/choice` | `Screen.DeviceChoice` |
| `/crop-profiles` | `Screen.CropProfiles` |
| `/irrigation` | `Screen.Irrigation` |
| `/water-usage` | `Screen.WaterUsage` |
| `/weather` | `Screen.Weather` |
| `/alerts` | `Screen.Alerts` |
| `/fertigation` | `Screen.Fertigation` |
| `/profile` | `Screen.Profile` |
| `/settings` | `Screen.Settings` |
| `/preferences` | `Screen.Preferences` |
| `/more` | `Screen.More` |

### `RootSyncNavGraph.kt` (replaces `router.dart`)

```kotlin
// Sealed class for type-safe routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object DeviceManagement : Screen("devices")
    object LinkDevice : Screen("devices/link")
    object DeviceChoice : Screen("devices/choice")
    object CropProfiles : Screen("crop-profiles")
    object Irrigation : Screen("irrigation")
    object WaterUsage : Screen("water-usage")
    object Weather : Screen("weather")
    object Alerts : Screen("alerts")
    object Fertigation : Screen("fertigation")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Preferences : Screen("preferences")
    object More : Screen("more")
}

@Composable
fun RootSyncNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.DeviceManagement.route) { DeviceManagementScreen(navController) }
        // ... all 15 routes
    }
}
```

### Auth Guard (replaces GoRouter `redirect`)

```kotlin
// In MainActivity or NavGraph
val authState by authViewModel.sessionState.collectAsState()

val startDestination = if (authState is SessionState.Authenticated)
    Screen.Dashboard.route
else
    Screen.Login.route
```

### Bottom Navigation

Flutter's bottom nav was likely in `router.dart`. Replicate with `NavigationBar` (Material 3):

```kotlin
NavigationBar {
    NavigationBarItem(icon = { Icon(Icons.Rounded.Dashboard) }, label = { Text("Dashboard") }, ...)
    NavigationBarItem(icon = { Icon(Icons.Rounded.Sensors) }, label = { Text("Sensors") }, ...)
    NavigationBarItem(icon = { Icon(Icons.Rounded.WaterDrop) }, label = { Text("Irrigation") }, ...)
    NavigationBarItem(icon = { Icon(Icons.Rounded.MoreHoriz) }, label = { Text("More") }, ...)
}
```

---

## 6. Screen-by-Screen Migration (15 Screens) <a name="screens"></a>

Each Flutter screen maps to a **Composable screen function + ViewModel** pair.

---

### Screen 1: `login_screen.dart` → `LoginScreen.kt`

**Flutter approach:** Supabase Flutter `signIn()`, `onAuthStateChange` stream.

**Kotlin approach:**
```kotlin
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // Email + Password TextFields
    // Button calls viewModel.signIn(email, password)
    // LaunchedEffect observes uiState.isAuthenticated → navigate to Dashboard
}

// AuthViewModel.kt
fun signIn(email: String, password: String) {
    viewModelScope.launch {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }
}
```

Deep link for `rootsync://login-callback` — configure in `AndroidManifest.xml`:
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <data android:scheme="rootsync" android:host="login-callback" />
</intent-filter>
```

---

### Screen 2: `dashboard_screen.dart` → `DashboardScreen.kt`

**Flutter approach:** `StreamBuilder` on `AppStateProvider`'s sensor stream, pump button calls `sendMqttCommand()`.

**Kotlin approach:**
```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val sensorState by viewModel.latestReading.collectAsState()
    val pumpState by viewModel.pumpState.collectAsState()

    // Moisture dial, temp card, humidity card — update automatically via StateFlow
    SensorDial(value = sensorState?.soilMoisture ?: 0f)
    PumpToggleButton(
        isOn = pumpState.isRunning,
        onClick = { viewModel.togglePump() }
    )
}
```

`DashboardViewModel` holds:
- `latestReading: StateFlow<SensorReading?>` — fed by Supabase Realtime (see Section 11)
- `pumpState: StateFlow<PumpState>` — updated after MQTT publish

---

### Screen 3: `device_management_screen.dart` → `DeviceManagementScreen.kt`

**Flutter approach:** Lists devices from `AppStateProvider`, delete/unlink calls.

**Kotlin:**
- `DeviceViewModel` exposes `devices: StateFlow<List<Device>>`
- `LazyColumn` renders device cards using `DeviceHealthTile` composable
- Swipe-to-delete with `SwipeToDismiss`

---

### Screen 4: `link_device_screen.dart` → `LinkDeviceScreen.kt`

**Flutter approach:** Manual UUID entry or QR scan → insert into `devices` table.

**Kotlin approach:** Use CameraX + ML Kit for QR scan (replaces `qr_code_scanner` package):
```kotlin
// QR value decoded → viewModel.linkDevice(uuid, userId)
// On success → navigate back to DeviceManagement
```

---

### Screen 5: `device_choice_screen.dart` → `DeviceChoiceScreen.kt`

**Flutter approach:** Radio group or dropdown to select active device for the session.

**Kotlin approach:**
- `DeviceChoiceScreen` renders a list with `RadioButton` per device
- Selection saved to `DataStore<Preferences>` (replaces `SharedPreferences`)

---

### Screen 6: `crop_profiles_screen.dart` → `CropProfilesScreen.kt`

**Flutter approach:** CRUD on `crop_profiles` table. Form with sliders/text fields for thresholds.

**Kotlin approach:**
- `CropProfileViewModel` with `profiles: StateFlow<List<CropProfile>>`
- `Slider` composable for moisture/rain thresholds (direct equivalent of Flutter `Slider`)
- `TextField` for profile name
- Perenual lookup via `perenual-lookup` Edge Function (same as Flutter)

---

### Screen 7: `irrigation_screen.dart` → `IrrigationScreen.kt`

**Flutter approach:** List of `pump_logs` with duration and water volume.

**Kotlin approach:**
- `PumpRepository.getPumpLogs()` queries `pump_logs` table with PostgREST
- `LazyColumn` with `PumpLogCard` composable
- Date filter chips (Today / This Week / All)

---

### Screen 8: `water_usage_screen.dart` → `WaterUsageScreen.kt`

**Flutter approach:** `FL Chart` bar/line chart of aggregated water use.

**Kotlin approach:** Use **Vico** library (direct Compose-native chart library):
```kotlin
// Vico ColumnChart — equivalent to FL Chart's BarChart
Chart(
    chart = columnChart(),
    model = entryModelOf(*weeklyUsageData.toTypedArray()),
    startAxis = rememberStartAxis(),
    bottomAxis = rememberBottomAxis()
)
```

SQL query (same as Flutter — use PostgREST `.select()` with aggregate):
```kotlin
supabase.from("pump_logs")
    .select(columns = Columns.raw("date_trunc('day', started_at), sum(water_used_liters)"))
    .filter("device_id", FilterOperator.EQ, deviceId)
```

---

### Screen 9: `weather_screen.dart` → `WeatherScreen.kt`

**Flutter approach:** Open-Meteo API fetch, display hourly/daily forecast. Lottie animations for weather icons.

**Kotlin approach:**
- `WeatherRepository` uses Retrofit + `OpenMeteoApi` interface (replaces `weather_client.dart` logic in UI)
- Lottie animations: use `lottie-compose` — **direct port** of Flutter's `lottie` package
  ```kotlin
  val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/phone_portrait/sunny.json"))
  LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)
  ```
- The same `lottie/` folder from Flutter assets can be copied into `app/src/main/assets/lottie/`

---

### Screen 10: `alerts_screen.dart` → `AlertsScreen.kt`

**Flutter approach:** Reads `system_alerts` table, mark-as-read.

**Kotlin approach:**
- `AlertViewModel` exposes `alerts: StateFlow<List<SystemAlert>>`
- `LazyColumn` with `AlertCard` composable
- `SwipeToDismiss` to mark alert read → `alertRepo.markRead(alertId)`
- Real-time new alerts via Supabase Realtime channel on `system_alerts`

---

### Screen 11: `fertigation_screen.dart` → `FertigationScreen.kt`

**Flutter approach:** Plant management using Perenual API data. Displays plant care schedules.

**Kotlin approach:**
- Call `perenual-lookup` Edge Function via Supabase:
  ```kotlin
  supabase.functions.invoke("perenual-lookup", body = buildJsonObject { put("query", plantName) })
  ```
- Display care data (watering schedule, sunlight, etc.)
- Data stored in `crop_profiles` table (same as Flutter)

---

### Screen 12: `settings_screen.dart` → `SettingsScreen.kt`

**Flutter approach:** Uses `ToggleSettingTile`, `DropdownSettingTile`, `EditableTextTile` widgets for settings groups.

**Kotlin approach:**
- Each Flutter widget → matching Composable (see Section 7)
- Settings persisted in `DataStore<Preferences>` (replaces `SharedPreferences`)
- User-specific settings (units, timezone) synced to `user_profiles` table

---

### Screen 13: `preferences_screen.dart` → `PreferencesScreen.kt`

**Flutter approach:** User preferences (notification toggles, units).

**Kotlin approach:**
- Unit preferences (metric/imperial) → `DataStore`
- Notification preferences → `DataStore` + update `device_tokens` in Supabase
- Migrations `007_unit_preferences.sql` and `006_user_location.sql` are already in your schema — just read/write to `user_profiles`

---

### Screen 14: `profile_screen.dart` → `ProfileScreen.kt`

**Flutter approach:** Display/edit user profile, password change, account deletion.

**Kotlin approach:**
- `ProfileViewModel` wraps `supabase.auth.updateUser { }` for password change
- `supabase.auth.signOut()` for logout
- `DeleteAccountButton` composable handles account deletion (see Section 7)

---

### Screen 15: `more_screen.dart` → `MoreScreen.kt`

**Flutter approach:** Extra links/options (likely navigation hub for less-used screens).

**Kotlin approach:**
- Simple `Column` of `NavigationListItem` composables
- Tap → `navController.navigate(Screen.X.route)`

---

## 7. Widget-by-Widget Migration (8 Widgets → Composables) <a name="widgets"></a>

All Flutter widgets in `widgets/` map directly to `@Composable` functions in `ui/components/`.

---

### Widget 1: `device_health_tile.dart` → `DeviceHealthTile.kt`

```kotlin
@Composable
fun DeviceHealthTile(device: Device, onTap: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Online/offline indicator dot
            Box(modifier = Modifier.size(10.dp).background(
                color = if (device.isOnline) Color.Green else Color.Red,
                shape = CircleShape
            ))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(device.name, style = MaterialTheme.typography.titleMedium)
                Text(device.location ?: "No location", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
```

---

### Widget 2: `toggle_setting_tile.dart` → `ToggleSettingTile.kt`

```kotlin
@Composable
fun ToggleSettingTile(title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}
```

---

### Widget 3: `dropdown_setting_tile.dart` → `DropdownSettingTile.kt`

```kotlin
@Composable
fun DropdownSettingTile(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                Text(selected, modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
                    }
                }
            }
        }
    )
}
```

---

### Widget 4: `editable_text_tile.dart` → `EditableTextTile.kt`

```kotlin
@Composable
fun EditableTextTile(title: String, value: String, onSave: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            if (editing) TextField(value = text, onValueChange = { text = it },
                keyboardActions = KeyboardActions(onDone = { onSave(text); editing = false }))
            else Text(value)
        },
        trailingContent = { IconButton(onClick = { editing = !editing }) { Icon(Icons.Rounded.Edit, null) } }
    )
}
```

---

### Widget 5: `settings_section.dart` → `SettingsSection.kt`

```kotlin
@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.primary)
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Column(content = content)
        }
    }
}
```

---

### Widget 6: `read_only_tile.dart` → `ReadOnlyTile.kt`

```kotlin
@Composable
fun ReadOnlyTile(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    )
}
```

---

### Widget 7: `inline_password_tile.dart` → `InlinePasswordTile.kt`

```kotlin
@Composable
fun InlinePasswordTile(label: String, onSave: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = {
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null)
                }}
            )
        },
        trailingContent = { TextButton(onClick = { onSave(password) }) { Text("Save") } }
    )
}
```

---

### Widget 8: `delete_account_button.dart` → `DeleteAccountButton.kt`

```kotlin
@Composable
fun DeleteAccountButton(onConfirm: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) { Text("Delete Account") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This action is permanent and cannot be undone.") },
            confirmButton = { TextButton(onClick = { onConfirm(); showDialog = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
```

---

## 8. Service Migration <a name="services"></a>

### 8.1 `mqtt_service.dart` → `MqttService.kt`

**Flutter (before):**
```dart
class MqttService {
  final MqttServerClient client;
  void publishPumpCommand(String deviceId, bool on) {
    client.publishMessage("rootsync/$deviceId/pump", MqttQos.atLeastOnce, ...);
  }
}
```

**Kotlin (after) — Eclipse Paho:**
```kotlin
@Singleton
class MqttService @Inject constructor() {

    private lateinit var client: MqttClient

    fun connect(host: String, port: Int, username: String, password: String) {
        client = MqttClient("ssl://$host:$port", MqttClient.generateClientId(), MemoryPersistence())
        val options = MqttConnectOptions().apply {
            this.userName = username
            this.password = password.toCharArray()
            isCleanSession = true
        }
        client.connect(options)
        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String, message: MqttMessage) { /* handle */ }
            override fun connectionLost(cause: Throwable?) { /* reconnect logic */ }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }

    fun publishPumpCommand(deviceId: String, on: Boolean) {
        val payload = if (on) "pump_on" else "pump_off"
        client.publish("rootsync/$deviceId/pump", MqttMessage(payload.toByteArray()))
    }
}
```

MQTT topics, host, and credentials stay **identical** to `config.h` — no ESP32 changes needed.

---

### 8.2 `notification_service.dart` → `NotificationService.kt` + `MyFirebaseMessagingService.kt`

**Flutter (before):**
```dart
class NotificationService {
  Future<void> init() async {
    final token = await FirebaseMessaging.instance.getToken();
    // save token to device_tokens table
  }
}
```

**Kotlin (after):**

**`MyFirebaseMessagingService.kt`** (register in `AndroidManifest.xml`):
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Save to Supabase device_tokens table
        CoroutineScope(Dispatchers.IO).launch {
            supabase.from("device_tokens").upsert(mapOf("token" to token, "user_id" to userId))
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Show notification using NotificationManager
        // Navigate to AlertsScreen if tapped (same as Flutter notification click handling)
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .build()
        NotificationManagerCompat.from(this).notify(notif.hashCode(), notif)
    }
}
```

Notification channel creation in `Application.onCreate()` (replaces Flutter's `notification_service.dart` init):
```kotlin
NotificationChannel(CHANNEL_ID, "RootSync Alerts", NotificationManager.IMPORTANCE_HIGH)
```

---

## 9. Utils Migration <a name="utils"></a>

### `date_helpers.dart` → `DateHelpers.kt`

```kotlin
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
}
```

### `unit_converter.dart` → `UnitConverter.kt`

```kotlin
object UnitConverter {
    fun celsiusToFahrenheit(c: Double) = c * 9.0 / 5.0 + 32
    fun fahrenheitToCelsius(f: Double) = (f - 32) * 5.0 / 9.0
    fun litersToGallons(l: Double) = l * 0.264172
    fun mmToInches(mm: Double) = mm / 25.4
}
```

### `enums.dart` → `Enums.kt`

```kotlin
enum class PumpStatus { IDLE, RUNNING, ERROR }
enum class SensorStatus { ONLINE, OFFLINE, WARNING }
enum class IrrigationMode { AUTO, MANUAL, DISABLED }
enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class VolumeUnit { LITERS, GALLONS }
```

---

## 10. Data Layer: Supabase SDK <a name="data"></a>

### Data Models (`@Serializable` — replaces Dart classes)

```kotlin
// Replaces dart class SensorReading in AppStateProvider
@Serializable
data class SensorReading(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("soil_moisture") val soilMoisture: Float,
    val temperature: Float,
    val humidity: Float,
    @SerialName("is_raining") val isRaining: Boolean,
    @SerialName("flow_rate") val flowRate: Float?,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Device(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val location: String?,
    @SerialName("is_online") val isOnline: Boolean
)

@Serializable
data class CropProfile(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    val name: String,
    @SerialName("moisture_threshold") val moistureThreshold: Int,
    @SerialName("rain_probability_threshold") val rainProbabilityThreshold: Int
)

@Serializable
data class PumpLog(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String?,
    @SerialName("water_used_liters") val waterUsedLiters: Float?,
    val duration: Int?
)

@Serializable
data class SystemAlert(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    val type: String,
    val message: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String
)
```

### Repository Pattern

```kotlin
// SensorRepository.kt
class SensorRepository @Inject constructor(private val supabase: SupabaseClient) {

    suspend fun getLatestReading(deviceId: String): SensorReading? =
        supabase.from("sensor_readings")
            .select()
            .eq("device_id", deviceId)
            .order("created_at", Order.DESCENDING)
            .limit(1)
            .decodeSingleOrNull()

    suspend fun getReadingsForRange(deviceId: String, from: String, to: String): List<SensorReading> =
        supabase.from("sensor_readings")
            .select()
            .eq("device_id", deviceId)
            .gte("created_at", from)
            .lte("created_at", to)
            .decodeList()
}
```

---

## 11. Realtime: Flutter Streams → Kotlin Flows <a name="realtime"></a>

This is the most important migration step — the equivalent of Flutter's `Supabase.instance.client.from('sensor_readings').stream()`.

### Flutter (before):
```dart
supabase
  .from('sensor_readings')
  .stream(primaryKey: ['id'])
  .eq('device_id', deviceId)
  .listen((data) { setState(() => _readings = data); });
```

### Kotlin (after):
```kotlin
// In SensorRepository.kt
fun sensorReadingFlow(deviceId: String): Flow<SensorReading> {
    val channel = supabase.realtime.createChannel("sensor:$deviceId")
    return channel
        .postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "sensor_readings"
            filter = "device_id=eq.$deviceId"
        }
        .map { it.decodeRecord<SensorReading>() }
        .onStart { channel.subscribe() }
        .onCompletion { supabase.realtime.removeChannel(channel) }
}

// In SensorViewModel.kt
val liveReading: StateFlow<SensorReading?> = sensorRepo
    .sensorReadingFlow(selectedDeviceId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

// In DashboardScreen.kt
val reading by viewModel.liveReading.collectAsState()
```

Realtime also needed for `system_alerts` (same pattern, different table).

---

## 12. Assets Migration <a name="assets"></a>

### Fonts (Manrope)
- Copy `assets/fonts/Manrope-VariableFont_wght.ttf` → `app/src/main/res/font/manrope.ttf`
- In `ui/theme/Type.kt`:
```kotlin
val ManropeFont = FontFamily(Font(R.font.manrope))
val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = ManropeFont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    // ... all text styles
)
```

### Lottie Animations
- Copy entire `assets/lottie/` folder → `app/src/main/assets/lottie/`
- **No changes needed** — `lottie-compose` reads from `assets/` using the same path strings as Flutter's `lottie` package
- Use `phone_portrait/` variants as default on phones, `tablet_portrait/` on tablets (detect with `LocalConfiguration.current.screenWidthDp`)

### Weather Icons
- Copy `assets/set-3/` through `assets/set-6/` (SVG sets) → `app/src/main/assets/weather_icons/`
- Load with Coil: `AsyncImage(model = "file:///android_asset/weather_icons/...")` 
- OR convert SVGs to VectorDrawable using Android Studio's SVG import tool

### App Icons
- Copy `assets/icon/` → use Android Studio **Image Asset Studio** to generate proper `mipmap-*` densities

---

## 13. Supabase Backend <a name="backend"></a>

### Schema — No Changes Needed
All 9 tables stay identical:
- `user_profiles`, `devices`, `device_tokens`, `sensor_readings`, `pump_logs`, `crop_profiles`, `system_alerts`, `device_commands`
- All 3 migrations (`006`, `007`, `008`) already applied
- **If migrating to a new Supabase account:** re-run all migrations in the new SQL Editor

### RLS Policies — No Changes
- Users see only their own devices ✓
- Devices write only to their own rows ✓
- FCM service account key stored as Supabase secret ✓

### Edge Functions — No Changes
All 4 functions deploy identically:
```bash
supabase functions deploy perenual-lookup
supabase functions deploy weekly-summary
supabase functions deploy purge-old-logs
supabase functions deploy send-alert-notification
```

The `send-alert-notification` function uses FCM HTTP v1 API. The Android app is an FCM client the same way the Flutter app was — no function changes needed.

---

## 14. ESP32 — No Changes Required <a name="esp32"></a>

The ESP32 communicates only with:
1. **Supabase REST API** — same URL and anon key
2. **HiveMQ MQTT** — same broker, same topics
3. **Open-Meteo** — no auth, no changes

**Only change:** If migrating to a new Supabase project, update `config.h`:
```cpp
// config.h — only update if moving to a new Supabase project
#define SUPABASE_URL "https://NEW_PROJECT_ID.supabase.co"
#define SUPABASE_ANON_KEY "new_anon_key_here"
```

All other firmware (`esp32.ino`, `supabase_client.h`, `weather_client.h`) remains **untouched**.

---

## 15. Build & Deployment <a name="build"></a>

### Secrets Setup
Create `local.properties`:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_anon_key
HIVEMQ_HOST=your-cluster.hivemq.cloud
HIVEMQ_USERNAME=your_username
HIVEMQ_PASSWORD=your_password
```

Expose via `build.gradle.kts`:
```kotlin
val localProps = Properties().apply { load(rootProject.file("local.properties").inputStream()) }
buildConfigField("String", "SUPABASE_URL", "\"${localProps["SUPABASE_URL"]}\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps["SUPABASE_ANON_KEY"]}\"")
```

### Firebase Setup
1. Create Android app in Firebase Console (package: `com.yourdomain.rootsync`)
2. Download `google-services.json` → place in `app/`
3. Add `com.google.gms.google-services` plugin to `build.gradle.kts`

### Build Commands
```bash
# Debug build (equivalent to flutter run)
./gradlew assembleDebug

# Release APK (equivalent to flutter build apk --release)
./gradlew assembleRelease

# Release AAB for Play Store (no Flutter equivalent)
./gradlew bundleRelease
```

---

## 16. Phase-by-Phase Execution Checklist <a name="checklist"></a>

Work through these phases in order. Each phase is independently testable.

---

### Phase 1 — Project Bootstrap (Day 1–2)
- [ ] Create new Android project (Kotlin + Compose, min API 26)
- [ ] Add all dependencies to `build.gradle.kts`
- [ ] Setup `local.properties` with Supabase and HiveMQ credentials
- [ ] Create `RootSyncApp.kt` with `@HiltAndroidApp`
- [ ] Create `SupabaseModule.kt` — verify Supabase connection in a test Composable

### Phase 2 — Auth & Navigation (Day 3–4)
- [ ] Create `Screen.kt` sealed class with all 15 routes
- [ ] Build `RootSyncNavGraph.kt`
- [ ] Implement `AuthViewModel.kt` + `AuthRepository.kt`
- [ ] Build `LoginScreen.kt` — verify sign-in works and navigates to Dashboard
- [ ] Add auth guard (session check on startup)

### Phase 3 — Core Data Models & Repositories (Day 5–6)
- [ ] Create all 7 `@Serializable` data classes in `data/model/`
- [ ] Implement all repository classes with basic `select()` queries
- [ ] Write a simple test: fetch latest sensor reading and print to Logcat

### Phase 4 — Realtime & Dashboard (Day 7–9)
- [ ] Implement `SensorRepository.sensorReadingFlow()`
- [ ] Build `DashboardViewModel.kt` with `liveReading` StateFlow
- [ ] Build `DashboardScreen.kt` — verify live sensor data updates on screen
- [ ] Integrate MQTT service — verify pump command is received by ESP32

### Phase 5 — All 15 Screens (Day 10–18, ~1 screen/day)
- [ ] `DeviceManagementScreen` + `DeviceViewModel`
- [ ] `LinkDeviceScreen` (CameraX QR scanner)
- [ ] `DeviceChoiceScreen`
- [ ] `CropProfilesScreen` + `CropProfileViewModel`
- [ ] `IrrigationScreen` + `PumpRepository`
- [ ] `WaterUsageScreen` (Vico charts)
- [ ] `WeatherScreen` (Retrofit + Lottie)
- [ ] `AlertsScreen` + Realtime alerts
- [ ] `FertigationScreen` (Perenual Edge Function)
- [ ] `SettingsScreen`
- [ ] `PreferencesScreen`
- [ ] `ProfileScreen`
- [ ] `MoreScreen`

### Phase 6 — All 8 Composable Widgets (Day 19–20)
- [ ] Port all 8 widgets from `widgets/` → `ui/components/`
- [ ] Refactor screens to use shared components

### Phase 7 — Notifications (Day 21)
- [ ] Setup Firebase project + `google-services.json`
- [ ] Implement `MyFirebaseMessagingService`
- [ ] Verify FCM token saved to `device_tokens` table
- [ ] Trigger test alert from ESP32 → verify push notification received

### Phase 8 — Assets & Theme (Day 22–23)
- [ ] Port `theme.dart` colors → `Color.kt` + `Theme.kt`
- [ ] Configure Manrope font in `Type.kt`
- [ ] Copy Lottie animations to `assets/lottie/`
- [ ] Copy weather icon sets to `assets/weather_icons/`
- [ ] Generate app icon with Image Asset Studio

### Phase 9 — Polish & Testing (Day 24–25)
- [ ] End-to-end test: ESP32 → Supabase → Realtime → Android UI
- [ ] End-to-end test: Android UI → MQTT → ESP32 pump toggle
- [ ] End-to-end test: ESP32 alert → Edge Function → FCM → Android notification
- [ ] Add `indexes` on `device_id` + `created_at` in Supabase for query performance
- [ ] Final release build: `./gradlew bundleRelease`

---

*Migration plan generated from SISSv2 Flutter architecture → RootSync Android (Kotlin/Compose)*
