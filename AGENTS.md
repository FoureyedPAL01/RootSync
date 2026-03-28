# AGENTS.md - RootSync Android Project

## Project Overview

RootSync is an Android application built with Jetpack Compose that interfaces with Supabase backend for IoT sensor monitoring and control.

- **Language**: Kotlin 2.0.21
- **Min SDK**: 28
- **Target SDK**: 36
- **Build Tool**: Gradle (Kotlin DSL)
- **UI Framework**: Jetpack Compose with Material3

---

## Build & Test Commands

### Build Commands
```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease     # Build release APK
./gradlew build               # Build all variants
./gradlew installDebug        # Install debug APK to connected device
```

### Test Commands
```bash
./gradlew test                # Run unit tests
./gradlew testDebugUnitTest    # Run unit tests for debug variant
./gradlew connectedAndroidTest # Run instrumented tests on device/emulator

# Run a single test class
./gradlew test --tests "com.project.rootsync.ExampleUnitTest"

# Run a single test method
./gradlew test --tests "com.project.rootsync.ExampleUnitTest.addition_isCorrect"

# Run a single instrumented test class
./gradlew connectedAndroidTest --tests "com.project.rootsync.ExampleInstrumentedTest"
```

### Other Commands
```bash
./gradlew clean               # Clean build artifacts
./gradlew dependencies        # Show project dependencies
./gradlew :app:dependencies  # Show app module dependencies
./gradlew tasks               # List all available tasks
./gradlew lint                # Run lint analysis
./gradlew ktlintCheck         # Run Kotlin linting (if configured)
```

**Always run `./gradlew ktlintCheck` (or `./gradlew lint`) before declaring a task done.**

---

## Code Style Guidelines

### Language & Style
- Use idiomatic Kotlin: data classes, sealed classes, extension functions.
- **Prefer `val` over `var`** for immutability.
- **Never use `!!`** — use `?.let`, `?: return`, or `requireNotNull`.
- Use coroutines (`suspend fun`, `Flow`) for async. **Never use raw threads.**
- All **public APIs must have KDoc**. Internal code needs inline comments only when non-obvious.
- Use explicit types for public properties and function return types.

### Project Structure
```
app/src/main/java/com/project/rootsync/
├── MainActivity.kt
├── data/
│   ├── model/           # Data classes (SensorReading, Alert, PumpLog)
│   └── repository/      # Repository classes for data access
├── ui/
│   ├── components/      # Reusable Compose components (ArcGauge, SensorCard)
│   ├── screens/         # Screen composables (Dashboard, Alerts, Weather)
│   └── theme/           # Theme files (Color, Type, Theme)
└── viewmodel/           # ViewModels for state management
```

### Architecture
- Domain layer must have **zero Android/framework imports**.
- Use **constructor injection** — no service locators.
- [TODO: Describe your layers — Clean Architecture / MVVM / Hexagonal. Fill in before use.]

### Naming Conventions
- **Classes/Objects/Interfaces**: PascalCase (`SensorReading`, `DashboardViewModel`)
- **Functions/Properties/Variables**: camelCase (`sensorReadings`, `fetchData()`)
- **Constants**: SCREAMING_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Package names**: lowercase with dots (`com.project.rootsync.data.model`)
- **Composable functions**: PascalCase, noun-based (`SensorCard`, not `sensorCard`)

### File Naming
- Kotlin files: Match the primary class/interface they contain
- Single-function files: Use the function name with descriptive noun

### Code Formatting
- Indent: 4 spaces (no tabs)
- Line length: No strict limit, but prefer 100-120 characters
- Use trailing commas in multi-line structures
- Each statement on its own line

### Import Organization
1. Android framework imports
2. Kotlin standard library
3. Third-party libraries (Compose, Supabase, Ktor, etc.)
4. Project imports
5. Blank line between groups
6. Static imports at the end

### Types & Type Annotations
- Use `nullable types` (`T?`) when a value can be absent
- Use `data class` for simple data containers
- Use `sealed class` or `sealed interface` for restricted hierarchies

```kotlin
data class SensorReading(
    val id: String,
    val sensorId: String,
    val value: Double,
    val timestamp: Long,
    val unit: String
)
```

### Compose Guidelines
- Mark all UI functions with `@Composable`
- Use `remember` for state that should survive recomposition
- Use `mutableStateOf` for UI state
- Pass `Modifier` as last optional parameter
- Use `Preview` annotation with `showBackground = true`

```kotlin
@Composable
fun SensorCard(
    reading: SensorReading,
    modifier: Modifier = Modifier
) {
    val isAlert by remember { mutableStateOf(false) }
    // ...
}

@Preview(showBackground = true)
@Composable
fun SensorCardPreview() {
    SensorCard(reading = SensorReading(...))
}
```

- Use Material3 color scheme (`MaterialTheme.colorScheme.primary`)
- Use Typography from theme (`MaterialTheme.typography.bodyLarge`)
- Support both light and dark themes
- Enable dynamic colors on Android 12+

### Error Handling
- Use `try-catch` for recoverable errors
- Return `Result<T>` or sealed results for operations that may fail
- Log errors appropriately with context
- Never expose sensitive information in error messages

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
}
```

### Coroutines & Async
- Use `viewModelScope` for ViewModel coroutines
- Use `Dispatchers.IO` for network/disk operations
- Cancel coroutines when no longer needed
- Use `StateFlow` or `MutableStateFlow` for reactive state

```kotlin
class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}
```

### Android Specific
- Use `Context` sparingly; prefer `LocalContext` in Compose
- Release resources in `onDestroy` or use lifecycle-aware components
- Handle configuration changes with `rememberSaveable` or ViewModel
- Use `enableEdgeToEdge()` for modern edge-to-edge UI

---

## Testing Rules

- **Every new class needs a corresponding test file** in `app/src/test/java/com/project/rootsync/`.
- Use **JUnit 4 + AndroidX test** (matches current project dependencies). No Mockito.
- Tests must cover the **happy path AND at least one error/null case**.
- **Never mock data classes** — use real instances with test fixtures.
- Never use `Thread.sleep()` in tests — use `runTest {}` with virtual time.

---

## Dependencies

### Key Libraries
- **Jetpack Compose BOM**: 2024.02.00
- **Material3**: Latest from Compose BOM
- **Navigation**: navigation-compose 2.7.6
- **Lifecycle**: lifecycle-viewmodel-compose 2.7.0
- **Supabase**: postgrest-kt, realtime-kt, gotrue-kt (BOM 2.3.1)
- **Ktor**: ktor-client-android 2.3.8
- **Coroutines**: kotlinx-coroutines-android 1.7.3
- **Coil**: coil-compose 2.5.0 (image loading)
- **DataStore**: datastore-preferences 1.0.0

### Testing Dependencies
- JUnit 4 (unit tests)
- androidx.test.ext:junit (instrumented tests)
- espresso-core (UI testing)
- androidx.compose.ui:ui-test-junit4

---

## What NOT to Do

1. **Never use `!!`** — use `?.let`, `?: return`, or `requireNotNull`.
2. **Never use `Thread.sleep()`** in tests — use `runTest {}` with virtual time.
3. **Never use raw threads** — use coroutines.
4. **Don't create new dependencies** without checking if one already exists in `build.gradle.kts`.
5. **Never delete files** — mark them deprecated first and ask.
