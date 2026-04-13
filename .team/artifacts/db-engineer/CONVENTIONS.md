# DB Engineer Conventions — Wane

**Created**: 2026-04-13
**Tech Stack**: Room 3.0 (KSP, BundledSQLiteDriver), Preferences DataStore 1.2.x, kotlinx.serialization
**Scope**: `app/src/main/kotlin/com/wane/app/data/`

> Prerequisites: Read `CONVENTIONS.md` (general) and `ARCHITECTURE.md` (data layer ownership) before writing data layer code.

---

## 1. Package Structure

```
data/
├── db/
│   ├── WaneDatabase.kt              -- @Database class, version constant
│   ├── Converters.kt                -- @TypeConverters (CompletionStatus enum)
│   └── dao/
│       ├── FocusSessionDao.kt
│       └── WaterThemeDao.kt
├── datastore/
│   └── PreferenceKeys.kt            -- DataStore key definitions
├── model/
│   ├── FocusSession.kt              -- @Entity
│   ├── WaterTheme.kt                -- @Entity
│   ├── CompletionStatus.kt          -- enum (COMPLETED, EARLY_EXIT)
│   ├── AutoLockConfig.kt            -- data class (not a Room entity)
│   └── StreakInfo.kt                 -- data class for streak display
├── repository/
│   ├── SessionRepository.kt         -- interface
│   ├── ThemeRepository.kt           -- interface
│   ├── PreferencesRepository.kt     -- interface
│   └── impl/
│       ├── SessionRepositoryImpl.kt
│       ├── ThemeRepositoryImpl.kt
│       └── PreferencesRepositoryImpl.kt
└── StreakCalculator.kt               -- streak derivation from session history
```

**Rules:**
- Entities live in `data/model/`, not in `data/db/`. They are data classes that may be used throughout the app.
- DAO interfaces live in `data/db/dao/`.
- Repository interfaces live in `data/repository/`. Implementations live in `data/repository/impl/`.
- Non-entity data classes (configs, display models) also live in `data/model/`.

---

## 2. Entity Naming

| Convention | Rule | Example |
| ---------- | ---- | ------- |
| Entity class | PascalCase, singular noun | `FocusSession`, `WaterTheme` |
| Table name | `snake_case`, plural | `@Entity(tableName = "focus_sessions")` |
| Column names | `camelCase` in Kotlin, auto-mapped to `snake_case` by Room (or use `@ColumnInfo` if explicit mapping needed) | `startTime`, `plannedDurationMs` |
| Primary key | `id` column, type `Long` (auto-generated) or `String` (natural key) | `@PrimaryKey(autoGenerate = true) val id: Long = 0` |
| Foreign key references | `{entity}Id` camelCase | `themeId: String` |
| Enum type converters | Store as string (enum name), not ordinal | `@TypeConverter fun fromStatus(s: String) = CompletionStatus.valueOf(s)` |
| Timestamps | `Long` (epoch milliseconds). Never use `Date`, `Instant`, or formatted strings. | `val startTime: Long` |

---

## 3. DAO Naming & Patterns

| Convention | Rule | Example |
| ---------- | ---- | ------- |
| DAO interface | `{Entity}Dao` | `FocusSessionDao`, `WaterThemeDao` |
| Read queries | Return `Flow<T>` | `fun getAllSessions(): Flow<List<FocusSession>>` |
| Write operations | `suspend` functions | `suspend fun insert(session: FocusSession): Long` |
| Upsert operations | Use Room's `@Upsert` | `@Upsert suspend fun upsert(theme: WaterTheme)` |
| Delete all | Explicit naming | `@Query("DELETE FROM focus_sessions") suspend fun deleteAll()` |

**Rules:**
- All read queries return `Flow<T>`. This makes the data layer reactive — Compose UIs automatically recompose when data changes.
- All write operations are `suspend` functions. Callers use `viewModelScope.launch { }` or `lifecycleScope.launch { }`.
- **No LiveData anywhere.** The project is Compose-first; `Flow` integrates natively via `collectAsStateWithLifecycle()`.
- Complex queries (streaks, aggregates) use raw SQL via `@Query` with window functions. Document the SQL logic with a comment above the query.

---

## 4. Repository Naming & Patterns

| Convention | Rule | Example |
| ---------- | ---- | ------- |
| Interface | `{Domain}Repository` | `SessionRepository`, `ThemeRepository`, `PreferencesRepository` |
| Implementation | `{Domain}RepositoryImpl` | `SessionRepositoryImpl`, `ThemeRepositoryImpl` |
| Read methods | `observe{What}(): Flow<T>` | `fun observeAllSessions(): Flow<List<FocusSession>>` |
| Write methods | `suspend {verb}{What}()` | `suspend fun recordSession(session: FocusSession): Long` |
| Single-value reads | `suspend get{What}(): T?` | `suspend fun getThemeById(id: String): WaterTheme?` |

**Rules:**
- Repository interfaces contain no implementation details. They never reference Room, DataStore, or SQLite.
- Implementations are annotated with `@Inject constructor(...)` for Hilt.
- The UI and Services layers never touch DAOs or DataStore directly — only through repository interfaces.
- Repository methods never throw exceptions to callers. Wrap database operations in `try/catch` and return `null`, empty lists, or default values on failure.

---

## 5. DataStore Preference Key Naming

```kotlin
object PreferenceKeys {
    val DEFAULT_DURATION_MINUTES = intPreferencesKey("default_duration_minutes")
    val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
    val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val EMERGENCY_CONTACTS_JSON = stringPreferencesKey("emergency_contacts_json")
    // ... etc.
}
```

| Convention | Rule |
| ---------- | ---- |
| Key object | `PreferenceKeys` singleton object in `data/datastore/PreferenceKeys.kt` |
| Key constant names | `UPPER_SNAKE_CASE` | 
| Key string values | `lower_snake_case` matching the constant name |
| Repository property wrappers | `camelCase` method names: `observeDefaultDuration()`, `setDefaultDuration()` |

**Rules:**
- Every preference key is declared in `PreferenceKeys`. Never create ad-hoc keys elsewhere.
- New keys added in future versions automatically get default values — no migration needed.
- Complex objects (emergency contacts list) are serialized to JSON via `kotlinx.serialization` and stored under a single `stringPreferencesKey`.

---

## 6. Default Values

All default values are hardcoded constants in the repository implementation's `companion object`. Never load defaults from a file, network, or dynamic source.

```kotlin
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    fun observeDefaultDuration(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEFAULT_DURATION_MINUTES] ?: DEFAULT_DURATION
    }

    companion object {
        const val DEFAULT_DURATION = 25
        const val DEFAULT_GRACE_PERIOD_SECONDS = 10
        const val DEFAULT_AUTO_LOCK_DURATION_MINUTES = 30
        const val DEFAULT_THEME_ID = "default"
    }
}
```

---

## 7. Migration Rules

**Rules:**
1. **Always write explicit migrations.** Never use `fallbackToDestructiveMigration()`. Users lose session history and streak data if the database is destroyed.
2. **Prefer additive changes.** New columns with `DEFAULT` values, new tables. Avoid renaming or removing columns.
3. **Test every migration.** The Test Engineer uses `MigrationTestHelper` to verify each migration path (1→2, 1→3, 2→3, etc.).
4. **Name migrations clearly:** `val MIGRATION_1_2 = object : Migration(1, 2) { ... }`.
5. **Migration SQL uses Room 3.0 `SQLiteConnection` API:**

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE focus_sessions ADD COLUMN notes TEXT DEFAULT NULL"
        )
    }
}
```

6. **DataStore needs no migration.** New keys get default values automatically. Removed keys are ignored.

---

## 8. Schema Export

Schema export is enabled via the Room Gradle Plugin. JSON schema files are committed to version control for auditing and test tooling.

```kotlin
// In app/build.gradle.kts
room {
    schemaDirectory("$projectDir/schemas")
}
```

**Rules:**
- Schema files (`schemas/com.wane.app.data.db.WaneDatabase/{version}.json`) are committed to git.
- Every database version bump produces a new schema file.
- The `MigrationTestHelper` in test code reads these schema files to validate migrations.

---

## 9. Database Construction

```kotlin
@Database(
    entities = [FocusSession::class, WaterTheme::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(WaneDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class WaneDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun waterThemeDao(): WaterThemeDao
}
```

**Rules:**
- The database class is named `WaneDatabase` and lives in `data/db/WaneDatabase.kt`.
- Database version is a single `Int` constant. Increment by 1 for each schema change.
- Use `@ConstructedBy` (Room 3.0) instead of `Room.databaseBuilder()` reflection. The constructor is generated by KSP.
- Use `BundledSQLiteDriver` for consistent SQLite behavior across Android versions.
- Type converters are centralized in `data/db/Converters.kt`.

---

## 10. Streak Derivation

Streaks are computed from `FocusSession` data via SQL window functions. There is no separate `streaks` table.

**Rules:**
- The `StreakCalculator` class lives in `data/StreakCalculator.kt`, injected with `FocusSessionDao`.
- `observeCurrentStreak(): Flow<Int>` re-emits whenever the session table changes (backed by Room's invalidation tracker).
- If today has no session yet, the streak reflects the state through yesterday — do not penalize the user before the day is over.
- Never persist a "streak counter" independently. It must always be derived from the session history to prevent sync bugs.

---

## 11. Hilt DI Module

The data layer provides a Hilt module for database and DataStore bindings:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WaneDatabase { /* ... */ }

    @Provides
    fun provideFocusSessionDao(db: WaneDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideWaterThemeDao(db: WaneDatabase): WaterThemeDao = db.waterThemeDao()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> { /* ... */ }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
    @Binds abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
    @Binds abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
```

**Rules:**
- `WaneDatabase` and `DataStore<Preferences>` are `@Singleton` scoped — one instance for the entire app.
- DAOs are provided without a scope (they are lightweight interfaces backed by the singleton database).
- Repository bindings use `@Binds` (not `@Provides`) to map interface to implementation.

---

## 12. Backup Compatibility

**Rules:**
- The Room database file is named `wane.db`. This name is stable and must not change across versions (backup/restore depends on it).
- The DataStore preferences file is `wane_preferences.preferences_pb`. The name is set when creating the DataStore instance and must not change.
- After a restore, the repository must handle gracefully if a preference references a non-existent theme ID (return the default theme).
- `purchaseToken` in `WaterTheme` may need re-verification after restore — this is handled by the billing flow, not the data layer.
