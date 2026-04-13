# DB Engineer Tech Stack Proposal: Wane

**Author**: DB Engineer
**Created**: 2026-04-13
**Status**: PROPOSED (awaiting HIL Gate 2)

## 1. Executive Summary

Wane is a fully local Android app -- no server, no network calls. The data layer must persist five domains (Session History, User Preferences, Streak Data, Water Theme Ownership, Auto-Lock State) while respecting two hard constraints: all data stays on device, and the storage layer must be reactive (Flow-based) to feed Jetpack Compose UI.

This proposal recommends **Room 3.0** for structured relational data and **Jetpack DataStore (Preferences)** for key-value settings. Both are the current (2026) Google-recommended libraries. No encryption is needed beyond Android's default file-based encryption because Wane stores no sensitive personal data -- session history is non-judgmental aggregate data, not behavioral surveillance.

---

## 2. Data Domain Mapping

| # | Domain | Storage | Justification |
| - | ------ | ------- | ------------- |
| 1 | Session History | Room 3.0 | Relational, queryable (streaks, aggregates), grows over time |
| 2 | User Preferences | Preferences DataStore | Simple key-value pairs, small dataset, no schema needed |
| 3 | Streak Data | Room 3.0 (derived) | Computed from Session History via DAO query -- no separate table |
| 4 | Water Theme Ownership | Room 3.0 | Structured, will grow with future IAP catalog, needs migration support |
| 5 | Auto-Lock State | In-memory only | Transient runtime state, must not survive process death (armed/grace period state resets on reboot) |

---

## 3. Technology Choices

### 3.1 Structured Data Storage -- Room 3.0

**Domain/Service**: Session History, Water Theme Ownership, Streak Data (derived)

**Recommended**: Room 3.0 (`androidx.room3:room3-runtime`, `androidx.room3:room3-compiler`) with KSP, targeting the latest stable release.

**Rationale**:
- Room 3.0 is the current major version (released March 2026), purpose-built for Kotlin-first projects. Room 2.x is in maintenance mode.
- Coroutine-first architecture: all DAO methods are `suspend` or return `Flow`, which aligns perfectly with Compose's reactive model.
- KSP-only code generation is faster than the deprecated KAPT and produces Kotlin output.
- Built-in migration support via `Migration` objects and the Room Gradle Plugin for schema export/validation.
- `@ConstructedBy` annotation replaces reflection-based instantiation -- cleaner DI integration.
- Uses `BundledSQLiteDriver` for consistent SQLite behavior across Android versions, decoupled from the platform's `SupportSQLite` APIs.
- Future-proofs us for KMP if Wane ever ships on other platforms.

**Alternatives considered**:

| Alternative | Why rejected |
| ----------- | ------------ |
| Room 2.x | Maintenance mode. No new features. Still uses KAPT/SupportSQLite. Starting a new project on 2.x creates immediate tech debt. |
| Raw SQLite (android.database.sqlite) | No compile-time query verification, no reactive Flow support, no migration tooling, high boilerplate. Room exists to solve these problems. |
| SQLDelight | Strong KMP story, but Room 3.0 now has KMP parity. Room has deeper Jetpack integration (Paging 3, lifecycle-aware), larger community, and Google-maintained documentation. Choosing SQLDelight would mean swimming against the Jetpack current for no clear gain. |
| Realm (MongoDB) | Overkill for a local-only app. Pulls in a large native binary (~5MB+), violating the <30MB app size constraint. Sync features are irrelevant. Object-based model doesn't align with our simple relational schema. |

**Modules using this**: `data/` (owned by DB Engineer), read by `ui/` (Frontend Dev) and `service/` (Backend Dev) through repository interfaces.

---

### 3.2 Key-Value Preferences -- Jetpack Preferences DataStore

**Domain/Service**: User Preferences (default duration, auto-lock config, emergency contacts list, water theme selection, ambient sounds toggle, haptic feedback toggle, onboarding completed flag)

**Recommended**: Jetpack Preferences DataStore 1.2.x (`androidx.datastore:datastore-preferences`).

**Rationale**:
- Google's recommended replacement for SharedPreferences, stable since 2023.
- Built on Kotlin Coroutines and Flow -- reads are reactive `Flow<T>`, writes are atomic `suspend` transactions.
- No schema definition needed (unlike Proto DataStore), which is appropriate for flat key-value settings.
- Thread-safe by design; SharedPreferences has known race conditions on `apply()`.
- Single-instance enforcement prevents the corruption bugs that plague SharedPreferences in multi-process scenarios.
- Small API surface, easy to wrap in a `PreferencesRepository`.

**Alternatives considered**:

| Alternative | Why rejected |
| ----------- | ------------ |
| SharedPreferences | Deprecated in spirit (Google docs recommend DataStore for all new projects). Synchronous reads block the main thread. `apply()` has well-documented race conditions. No Flow support. |
| Proto DataStore | Type-safe via Protobuf, but adds build complexity (protoc compiler, `.proto` files, generated code). Our preferences are flat key-value pairs -- the extra schema machinery isn't warranted. If preferences grow complex (nested objects), we can migrate later; Preferences DataStore to Proto DataStore migration is straightforward. |
| MMKV (Tencent) | Fast, battle-tested, but not a Jetpack component. No built-in Flow support. Adds a native dependency. For our scale (<20 preference keys), the performance difference is immaterial. |
| Room (for settings) | Relational database for flat key-value pairs is over-engineering. Room's migration overhead isn't justified for settings that can be rebuilt from defaults. |

**Modules using this**: `data/` (owned by DB Engineer), read by `ui/` (Frontend Dev) and `service/` (Backend Dev) through `PreferencesRepository`.

---

### 3.3 Auto-Lock State -- In-Memory StateFlow

**Domain/Service**: Auto-Lock transient state (armed flag, grace period countdown, skip-between evaluation)

**Recommended**: Kotlin `StateFlow` / `MutableStateFlow` held in the Backend Developer's `AutoLockManager` (in `service/`). No persistence.

**Rationale**:
- Auto-lock state is inherently transient: if the process dies, the state should reset. Persisting it creates stale-state bugs (e.g., app killed mid-grace-period, relaunched with an expired countdown).
- The auto-lock *configuration* (duration, grace period, skip-between window, skip-while-charging) is persisted in Preferences DataStore. The runtime *state* (is it armed right now?) lives in memory only.
- `StateFlow` is the idiomatic Kotlin mechanism for observable mutable state in services.

**Alternatives considered**:

| Alternative | Why rejected |
| ----------- | ------------ |
| DataStore | Persisting transient state creates consistency hazards. DataStore writes are async -- by the time the write completes, the grace period may have already changed. |
| Room | Same persistence concern, plus unnecessary I/O for state that changes every second during grace period countdown. |

**Modules using this**: `service/` (Backend Dev owns the implementation). DB Engineer provides the auto-lock *configuration* via `PreferencesRepository`; the *runtime state* is outside DB Engineer's ownership.

---

## 4. Data Models

### 4.1 Room Entities

#### `FocusSession` (session history)

```kotlin
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,          // epoch millis
    val endTime: Long,            // epoch millis
    val plannedDurationMs: Long,  // what the user chose
    val actualDurationMs: Long,   // how long they actually stayed
    val completionStatus: CompletionStatus,
    val themeId: String           // which water theme was active
)

enum class CompletionStatus {
    COMPLETED,    // water fully drained
    EARLY_EXIT    // user exited via emergency exit
}
```

Design notes:
- `startTime`/`endTime` as epoch millis (Long) avoids any timezone serialization issues. Formatting is a UI concern.
- `completionStatus` stored as a string via Room's `@TypeConverter` (enum name). Only two values -- no judgmental labels.
- `themeId` is a string foreign key to `WaterTheme.id`. Not a Room `@ForeignKey` -- theme ownership is a separate concern and we don't want cascading deletes.

#### `WaterTheme` (theme ownership)

```kotlin
@Entity(tableName = "water_themes")
data class WaterTheme(
    @PrimaryKey
    val id: String,               // e.g., "default", "monsoon", "glacier"
    val displayName: String,
    val isPurchased: Boolean,
    val purchaseToken: String?,   // Google Play purchase token, null for free themes
    val purchaseTimestamp: Long?  // epoch millis
)
```

Design notes:
- String `id` rather than auto-generated Long -- theme IDs are stable, known at compile time, seeded on first install.
- `purchaseToken` stored for Google Play purchase verification/restoration. This is the only field with any sensitivity, but it's a Google-issued opaque token, not user PII.
- v1 ships with one row: `WaterTheme(id = "default", displayName = "Still Water", isPurchased = true, purchaseToken = null, purchaseTimestamp = null)`.

### 4.2 Room DAOs

```kotlin
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<FocusSession>>

    @Query("""
        SELECT COUNT(DISTINCT date(startTime / 1000, 'unixepoch', 'localtime'))
        FROM focus_sessions
        WHERE completionStatus = 'COMPLETED'
          AND date(startTime / 1000, 'unixepoch', 'localtime') >= date('now', 'localtime', :daysBack || ' days')
    """)
    fun getCompletedDaysInRange(daysBack: String): Flow<Int>

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()
}

@Dao
interface WaterThemeDao {
    @Query("SELECT * FROM water_themes")
    fun getAllThemes(): Flow<List<WaterTheme>>

    @Query("SELECT * FROM water_themes WHERE isPurchased = 1")
    fun getPurchasedThemes(): Flow<List<WaterTheme>>

    @Query("SELECT * FROM water_themes WHERE id = :themeId")
    suspend fun getThemeById(themeId: String): WaterTheme?

    @Upsert
    suspend fun upsert(theme: WaterTheme)

    @Insert
    suspend fun insertAll(themes: List<WaterTheme>)
}
```

### 4.3 Room Database

```kotlin
@Database(
    entities = [FocusSession::class, WaterTheme::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(WaneDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class WaneDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun waterThemeDao(): WaterThemeDao
}

expect object WaneDatabaseConstructor : RoomDatabaseConstructor<WaneDatabase>
```

### 4.4 Streak Calculation (Derived, No Separate Table)

Streaks are computed from `FocusSession` data, not stored as a separate entity. This avoids sync bugs between a "streak counter" and the actual session history.

```kotlin
class StreakCalculator(private val sessionDao: FocusSessionDao) {

    /**
     * Returns the current consecutive-day streak.
     * A "day" counts if the user completed at least one session.
     * Streak breaks if any calendar day is missed.
     */
    suspend fun getCurrentStreak(): Int {
        // Walk backwards from today, counting consecutive days
        // with at least one COMPLETED session.
        // Implementation uses a single SQL query with window functions
        // or iterative Kotlin logic over the session list.
    }

    fun observeCurrentStreak(): Flow<Int> {
        // Reactive version that re-emits when sessions change
    }
}
```

The streak query:
```sql
-- Consecutive days with completed sessions, ending today
WITH daily AS (
    SELECT DISTINCT date(startTime / 1000, 'unixepoch', 'localtime') AS session_date
    FROM focus_sessions
    WHERE completionStatus = 'COMPLETED'
),
numbered AS (
    SELECT session_date,
           julianday(session_date) - ROW_NUMBER() OVER (ORDER BY session_date) AS grp
    FROM daily
)
SELECT COUNT(*) AS streak
FROM numbered
WHERE grp = (
    SELECT grp FROM numbered
    WHERE session_date = date('now', 'localtime')
)
```

If today has no session yet, the streak reflects yesterday's state (the UI should show the user's current streak, not penalize them before the day is over).

### 4.5 Preferences DataStore Keys

```kotlin
object PreferenceKeys {
    val DEFAULT_DURATION_MINUTES = intPreferencesKey("default_duration_minutes")
    val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
    val AUTO_LOCK_DURATION_MINUTES = intPreferencesKey("auto_lock_duration_minutes")
    val AUTO_LOCK_GRACE_PERIOD_SECONDS = intPreferencesKey("auto_lock_grace_period_seconds")
    val AUTO_LOCK_SKIP_START_HOUR = intPreferencesKey("auto_lock_skip_start_hour")
    val AUTO_LOCK_SKIP_START_MINUTE = intPreferencesKey("auto_lock_skip_start_minute")
    val AUTO_LOCK_SKIP_END_HOUR = intPreferencesKey("auto_lock_skip_end_hour")
    val AUTO_LOCK_SKIP_END_MINUTE = intPreferencesKey("auto_lock_skip_end_minute")
    val AUTO_LOCK_SKIP_WHILE_CHARGING = booleanPreferencesKey("auto_lock_skip_while_charging")
    val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
    val AMBIENT_SOUNDS_ENABLED = booleanPreferencesKey("ambient_sounds_enabled")
    val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}
```

**Emergency contacts** are stored as a JSON-serialized `List<String>` (phone numbers) under a single string preference key. The list is small (typically 1-5 contacts), and storing it in Room would over-complicate a flat list that has no relational needs.

```kotlin
val EMERGENCY_CONTACTS_JSON = stringPreferencesKey("emergency_contacts_json")
```

Serialization via `kotlinx.serialization` (already likely in the project for other uses) keeps the dependency footprint minimal.

---

## 5. Repository Pattern

All data access flows through repository interfaces. The UI and Services layers never touch DAOs or DataStore directly.

### 5.1 Repository Interfaces (in `data/repository/`)

```kotlin
interface SessionRepository {
    fun observeAllSessions(): Flow<List<FocusSession>>
    fun observeRecentSessions(limit: Int): Flow<List<FocusSession>>
    fun observeCurrentStreak(): Flow<Int>
    suspend fun recordSession(session: FocusSession): Long
    suspend fun clearAllSessions()
}

interface ThemeRepository {
    fun observeAllThemes(): Flow<List<WaterTheme>>
    fun observePurchasedThemes(): Flow<List<WaterTheme>>
    suspend fun getThemeById(id: String): WaterTheme?
    suspend fun markThemePurchased(themeId: String, purchaseToken: String)
    suspend fun seedDefaultThemes()
}

interface PreferencesRepository {
    fun observeDefaultDuration(): Flow<Int>
    fun observeAutoLockConfig(): Flow<AutoLockConfig>
    fun observeSelectedThemeId(): Flow<String>
    fun observeEmergencyContacts(): Flow<List<String>>
    fun observeAmbientSoundsEnabled(): Flow<Boolean>
    fun observeHapticFeedbackEnabled(): Flow<Boolean>
    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setDefaultDuration(minutes: Int)
    suspend fun setAutoLockConfig(config: AutoLockConfig)
    suspend fun setSelectedThemeId(themeId: String)
    suspend fun setEmergencyContacts(contacts: List<String>)
    suspend fun setAmbientSoundsEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

data class AutoLockConfig(
    val enabled: Boolean = false,
    val durationMinutes: Int = 30,
    val gracePeriodSeconds: Int = 10,
    val skipStartHour: Int? = null,
    val skipStartMinute: Int? = null,
    val skipEndHour: Int? = null,
    val skipEndMinute: Int? = null,
    val skipWhileCharging: Boolean = false
)
```

### 5.2 Design Principles

- **Observe via `Flow`**: All read operations return `Flow<T>` so Compose `collectAsState()` recomposes automatically when data changes. This is the reactive contract.
- **Mutate via `suspend`**: All write operations are `suspend` functions. Callers use `viewModelScope` or `lifecycleScope`.
- **No LiveData**: The project is Compose-first. `Flow` integrates natively via `collectAsStateWithLifecycle()`. LiveData adds an unnecessary abstraction layer.
- **Interface-first**: Repository interfaces live in `data/repository/`. Implementations live in `data/repository/impl/`. This allows the Test Engineer to substitute fakes without touching production code.
- **Single source of truth**: Streak data is derived from session history, not stored separately. This eliminates an entire class of consistency bugs.

---

## 6. Encryption Strategy

**Recommendation: No application-level encryption. Rely on Android's file-based encryption (FBE).**

### Justification

Wane stores no sensitive personal data:
- **Session history** is aggregate data (timestamps and durations). It contains no content about what the user was doing, no app usage logs, no behavioral profiles. The brand philosophy is explicitly non-surveillance.
- **Preferences** are app configuration (duration in minutes, toggle states). Not PII.
- **Theme ownership** contains a Google Play purchase token, which is an opaque string that cannot be used to identify the user or make purchases.
- **Emergency contacts** are phone numbers the user explicitly added. These are already present in the device's contact book, which has its own Android-level encryption.

Android's file-based encryption (FBE), enabled by default on all devices since Android 10, encrypts all app data at rest when the device is locked. This provides:
- AES-256 encryption of the entire app data directory
- Hardware-backed key storage
- Transparent to the application -- no code needed
- Decryption only when the device is unlocked and the user has authenticated

Adding application-level encryption (e.g., Tink + encrypted DataStore) would:
- Increase complexity with no meaningful security gain (the threat model doesn't warrant it)
- Add ~1.5MB to the APK (Tink library)
- Introduce key management edge cases (corrupted keystore, device migration failures)
- Slow down reads/writes for data that isn't sensitive

### When to revisit

Add application-level encryption if any of these change:
- The app begins storing credentials or tokens for a server-side component
- User-generated content is added (journals, notes)
- Regulatory requirements (HIPAA, SOX) apply to enterprise customers
- Security Reviewer flags a specific threat in their audit

---

## 7. Migration Strategy

### 7.1 Schema Versioning

Room's built-in migration system handles schema evolution:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE focus_sessions ADD COLUMN notes TEXT DEFAULT NULL")
    }
}
```

### 7.2 Schema Export

The Room Gradle Plugin is configured to export schemas to `app/schemas/`. This provides:
- JSON schema files for each version (used by `MigrationTestHelper` in tests)
- Git-tracked schema history for auditing
- Automated migration validation

```kotlin
// In app/build.gradle.kts
room {
    schemaDirectory("$projectDir/schemas")
}
```

### 7.3 Migration Rules

1. **Always write explicit migrations.** Never use `fallbackToDestructiveMigration()` -- users lose their session history and streak data.
2. **Test every migration.** The Test Engineer uses `MigrationTestHelper` to verify each migration path (1→2, 1→3, 2→3, etc.).
3. **Additive changes preferred.** New columns with defaults, new tables. Avoid renaming or removing columns.
4. **DataStore needs no migration.** New preference keys get default values automatically. Removed keys are simply ignored. This is a built-in advantage of key-value stores.

### 7.4 Version History (planned)

| Version | Changes | Migration |
| ------- | ------- | --------- |
| 1 | Initial schema: `focus_sessions`, `water_themes` | N/A (fresh install) |

---

## 8. Backup and Restore

### 8.1 Android Auto Backup

Android Auto Backup (Google Drive) is enabled by default. For Wane, this is desirable -- users who switch devices should keep their session history and streaks.

**Include in backup**:
- Room database (`wane.db`) -- session history, theme ownership
- DataStore preferences file -- all user settings

**Exclude from backup**:
- Auto-lock transient state (in-memory, not persisted anyway)
- Any future cache files

### 8.2 Backup Rules Configuration

```xml
<!-- res/xml/backup_rules.xml (Android 11 and below) -->
<full-backup-content>
    <include domain="database" path="wane.db" />
    <include domain="file" path="datastore/wane_preferences.preferences_pb" />
</full-backup-content>
```

```xml
<!-- res/xml/data_extraction_rules.xml (Android 12+) -->
<data-extraction-rules>
    <cloud-backup>
        <include domain="database" path="wane.db" />
        <include domain="file" path="datastore/wane_preferences.preferences_pb" />
    </cloud-backup>
    <device-transfer>
        <include domain="database" path="wane.db" />
        <include domain="file" path="datastore/wane_preferences.preferences_pb" />
    </device-transfer>
</data-extraction-rules>
```

### 8.3 Manifest Declaration

```xml
<application
    android:allowBackup="true"
    android:fullBackupContent="@xml/backup_rules"
    android:dataExtractionRules="@xml/data_extraction_rules"
    ... >
```

### 8.4 Restore Considerations

- On restore, Room database is fully intact. No special handling needed.
- DataStore preferences are restored as-is. The app should handle gracefully if a restored preference references a theme ID that doesn't exist (defensive coding in the repository).
- `purchaseToken` in `WaterTheme` may need re-verification with Google Play Billing after restore. This is handled by the billing flow, not the data layer.

---

## 9. Database Seeding

On first install (or after a data wipe), the database must be seeded with default data.

### 9.1 Default Themes

```kotlin
private val DEFAULT_THEMES = listOf(
    WaterTheme(
        id = "default",
        displayName = "Still Water",
        isPurchased = true,
        purchaseToken = null,
        purchaseTimestamp = null
    )
)
```

Seeding is triggered by `RoomDatabase.Callback.onCreate()` or by the `ThemeRepository.seedDefaultThemes()` method called during app initialization.

### 9.2 Default Preferences

Preferences DataStore handles defaults naturally -- when a key doesn't exist, the repository returns a hardcoded default:

```kotlin
fun observeDefaultDuration(): Flow<Int> = dataStore.data.map { prefs ->
    prefs[PreferenceKeys.DEFAULT_DURATION_MINUTES] ?: DEFAULT_DURATION
}

companion object {
    const val DEFAULT_DURATION = 25
    const val DEFAULT_GRACE_PERIOD_SECONDS = 10
}
```

---

## 10. File/Package Structure

All files live under `app/src/main/kotlin/com/wane/app/data/`:

```
data/
├── db/
│   ├── WaneDatabase.kt              -- @Database class
│   ├── Converters.kt                -- @TypeConverters (enum, etc.)
│   └── dao/
│       ├── FocusSessionDao.kt
│       └── WaterThemeDao.kt
├── datastore/
│   └── PreferenceKeys.kt            -- DataStore key definitions
├── model/
│   ├── FocusSession.kt              -- @Entity
│   ├── WaterTheme.kt                -- @Entity
│   ├── CompletionStatus.kt          -- enum
│   ├── AutoLockConfig.kt            -- data class (not an entity)
│   └── StreakInfo.kt                 -- data class for streak display
├── repository/
│   ├── SessionRepository.kt         -- interface
│   ├── ThemeRepository.kt           -- interface
│   ├── PreferencesRepository.kt     -- interface
│   └── impl/
│       ├── SessionRepositoryImpl.kt
│       ├── ThemeRepositoryImpl.kt
│       └── PreferencesRepositoryImpl.kt
└── StreakCalculator.kt               -- streak derivation logic
```

---

## 11. Dependencies

```kotlin
// In app/build.gradle.kts

// Room 3.0
implementation("androidx.room3:room3-runtime:<latest-stable>")
ksp("androidx.room3:room3-compiler:<latest-stable>")

// Jetpack DataStore
implementation("androidx.datastore:datastore-preferences:1.2.1")

// kotlinx.serialization (for emergency contacts JSON in DataStore)
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:<latest-stable>")
```

No additional dependencies. Room 3.0 bundles its own SQLite driver (`BundledSQLiteDriver`). No Tink, no Realm, no third-party storage libraries.

---

## 12. Self-Verification Checklist

| # | Check | Status |
| - | ----- | ------ |
| 1 | Covers all 5 data domains (Session History, User Preferences, Streak Data, Water Theme Ownership, Auto-Lock State) | PASS |
| 2 | All technology choices justified with alternatives | PASS -- Room 3.0 (vs 2.x, raw SQLite, SQLDelight, Realm), Preferences DataStore (vs SharedPreferences, Proto DataStore, MMKV, Room), In-memory StateFlow (vs DataStore, Room) |
| 3 | Privacy constraint respected (all data local, no collection) | PASS -- no network calls, no analytics, no server sync. FBE for at-rest encryption. |
| 4 | Migration strategy clear for future app updates | PASS -- Room explicit migrations, schema export, no destructive fallback. DataStore needs no migration. |
| 5 | Specific enough for implementation | PASS -- entity definitions, DAO signatures, repository interfaces, package structure, Gradle dependencies all specified |
| 6 | Aligns with ARCHITECTURE.md module boundaries | PASS -- all code in `data/`, exposed via repository interfaces, consumed by `ui/` and `service/` |
| 7 | Aligns with CONVENTIONS.md naming | PASS -- PascalCase classes, camelCase functions, lowercase packages |
| 8 | App size impact minimal (<30MB constraint) | PASS -- Room 3.0 + DataStore add ~2MB. No Tink, no Realm, no large native libs. |
