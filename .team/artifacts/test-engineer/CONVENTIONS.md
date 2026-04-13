# Test Engineer Conventions — Wane

**Created**: 2026-04-13
**Tech Stack**: JUnit 4, Kotlin Coroutines Test, Turbine, Compose Testing APIs, Room MigrationTestHelper, Hilt Testing
**Scope**: `app/src/test/`, `app/src/androidTest/`

> Prerequisites: Read `CONVENTIONS.md` (general), `ARCHITECTURE.md` (module map, file ownership), and every role's `CONVENTIONS.md` to understand what each layer expects.

---

## 1. Test Directory Structure

```
app/src/
├── test/                                    -- JVM unit tests (no Android framework)
│   └── kotlin/com/wane/app/
│       ├── ui/screens/
│       │   ├── HomeViewModelTest.kt
│       │   ├── SessionViewModelTest.kt
│       │   ├── SettingsViewModelTest.kt
│       │   └── OnboardingViewModelTest.kt
│       ├── service/
│       │   ├── SessionManagerTest.kt
│       │   ├── AppBlockerTest.kt
│       │   ├── RepeatedCallerTrackerTest.kt
│       │   └── AutoLockSchedulerTest.kt
│       ├── data/
│       │   ├── repository/
│       │   │   ├── SessionRepositoryImplTest.kt
│       │   │   ├── ThemeRepositoryImplTest.kt
│       │   │   └── PreferencesRepositoryImplTest.kt
│       │   └── StreakCalculatorTest.kt
│       └── shared/
│           ├── FakeSessionRepository.kt
│           ├── FakePreferencesRepository.kt
│           ├── FakeThemeRepository.kt
│           └── TestFixtures.kt
│
├── androidTest/                             -- Instrumented tests (require device/emulator)
│   └── kotlin/com/wane/app/
│       ├── ui/
│       │   ├── HomeScreenTest.kt
│       │   ├── SessionScreenTest.kt
│       │   ├── OnboardingFlowTest.kt
│       │   └── SettingsScreenTest.kt
│       ├── data/
│       │   └── db/
│       │       ├── MigrationTest.kt
│       │       └── FocusSessionDaoTest.kt
│       └── NavigationTest.kt
```

---

## 2. Test Class & Method Naming

| Convention | Pattern | Example |
| ---------- | ------- | ------- |
| Test class | `{ClassUnderTest}Test` | `HomeViewModelTest`, `SessionManagerTest` |
| Test method | `methodName_condition_expectedResult` | `onEvent_increaseDuration_incrementsByFive` |
| Parameterized test | Append scenario to method name | `onEvent_decreaseDuration_clampsAtMinimum` |

**Rules:**
- Test class names mirror the production class exactly, with `Test` suffix.
- Test method names use underscores to separate three parts: the method/behavior being tested, the condition/input, and the expected result.
- Never use `should` prefix. The three-part convention is sufficient.
- Use `@DisplayName` annotation only if the test framework supports it (JUnit 5). For JUnit 4, the method name IS the description.

```kotlin
class HomeViewModelTest {
    @Test
    fun onEvent_startSession_emitsNavigateEffect() { /* ... */ }

    @Test
    fun onEvent_increaseDuration_incrementsByFive() { /* ... */ }

    @Test
    fun onEvent_decreaseDuration_clampsAtFiveMinutes() { /* ... */ }

    @Test
    fun uiState_initialLoad_showsDefaultDuration() { /* ... */ }
}
```

---

## 3. Unit Tests (JVM-Based)

Location: `app/src/test/`

**Rules:**
- Unit tests run on the JVM with no Android framework dependency. They test ViewModels, repositories, services logic, and utility classes.
- Use `kotlinx-coroutines-test` for all coroutine testing:

```kotlin
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeSessionRepository = FakeSessionRepository()
    private val fakePreferencesRepository = FakePreferencesRepository()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel(fakePreferencesRepository, fakeSessionRepository)
    }
}
```

- Use `TestCoroutineScheduler` (via `StandardTestDispatcher` or `UnconfinedTestDispatcher`) to control coroutine timing.
- Create a shared `MainDispatcherRule` (JUnit `TestRule`) that replaces `Dispatchers.Main` with a test dispatcher:

```kotlin
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### Flow Testing with Turbine

Use [Turbine](https://github.com/cashapp/turbine) for testing `Flow` emissions:

```kotlin
@Test
fun observeCurrentStreak_newSessionAdded_updatesStreak() = runTest {
    fakeSessionRepository.observeCurrentStreak().test {
        assertEquals(0, awaitItem())
        fakeSessionRepository.addSession(completedSession())
        assertEquals(1, awaitItem())
        cancelAndConsumeRemainingEvents()
    }
}
```

**Rules:**
- Always use `turbine.test { }` blocks for Flow assertions. Never use `first()` or `toList()` for reactive flows — they don't verify emission timing.
- Call `cancelAndConsumeRemainingEvents()` at the end of Turbine blocks to prevent dangling coroutines.

---

## 4. Instrumented Tests (Device/Emulator)

Location: `app/src/androidTest/`

**Rules:**
- Instrumented tests require a device or emulator. They test Compose UI rendering, Room database operations, and navigation flows.
- Use Compose testing APIs for UI tests:

```kotlin
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysDurationPicker() {
        composeTestRule.setContent {
            WaneTheme {
                HomeScreen(viewModel = fakeHomeViewModel())
            }
        }
        composeTestRule.onNodeWithText("25").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Increase duration").assertIsDisplayed()
    }
}
```

- Use `createComposeRule()` (not `createAndroidComposeRule<Activity>()`) when the test doesn't need an actual Activity.
- Use `onNodeWithContentDescription(...)` for finding interactive elements — this also validates that accessibility descriptions exist.
- Use `onNodeWithText(...)` for finding display elements.
- Use `performClick()`, `performTextInput()`, `performScrollTo()` for interactions.

---

## 5. Room Migration Testing

Use `MigrationTestHelper` to verify every migration path:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WaneDatabase::class.java,
    )

    @Test
    fun migrate1To2_addsNotesColumn() {
        // Create database at version 1
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO focus_sessions (startTime, endTime, plannedDurationMs, actualDurationMs, completionStatus, themeId) VALUES (1000, 2000, 1000, 1000, 'COMPLETED', 'default')")
            close()
        }

        // Run migration and validate
        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)
        val cursor = db.query("SELECT notes FROM focus_sessions")
        assertTrue(cursor.moveToFirst())
        assertNull(cursor.getString(0)) // default is NULL
        cursor.close()
        db.close()
    }

    companion object {
        private const val DB_NAME = "wane-migration-test"
    }
}
```

**Rules:**
- Test every migration path, including skip migrations (1→3 must work if a user skips version 2).
- The schema JSON files in `app/schemas/` are used by `MigrationTestHelper` — keep them in version control.

---

## 6. Mocking Strategy

**Prefer fakes over mocking libraries.**

| Strategy | When to use | Example |
| -------- | ----------- | ------- |
| **Fakes** (manual implementations) | Repositories, DataStore, DAOs | `FakeSessionRepository implements SessionRepository` |
| **Mocking library** (Mockito-Kotlin) | Android framework classes, system services | `mock<SensorManager>()` |
| **Test doubles** | ViewModels in UI tests | Factory function returning ViewModel with fake deps |

**Rules:**
- Fakes live in `app/src/test/kotlin/com/wane/app/shared/`. They are shared across all test classes.
- Fakes implement the same interface as the production code: `class FakeSessionRepository : SessionRepository`.
- Fakes store data in-memory (e.g., `MutableList`, `MutableStateFlow`) and support all interface methods.
- Never mock the class under test. Only mock its dependencies.
- Use Mockito-Kotlin only when faking is impractical (Android framework APIs, sensor callbacks).

```kotlin
class FakeSessionRepository : SessionRepository {
    private val sessions = MutableStateFlow<List<FocusSession>>(emptyList())

    override fun observeAllSessions(): Flow<List<FocusSession>> = sessions

    override suspend fun recordSession(session: FocusSession): Long {
        sessions.update { it + session }
        return session.id
    }

    override fun observeCurrentStreak(): Flow<Int> = sessions.map { /* calculate */ }

    override suspend fun clearAllSessions() {
        sessions.value = emptyList()
    }
}
```

---

## 7. Test Fixtures

Shared test data lives in `app/src/test/kotlin/com/wane/app/shared/TestFixtures.kt`:

```kotlin
object TestFixtures {
    fun completedSession(
        id: Long = 0,
        durationMs: Long = 25 * 60 * 1000L,
        startTime: Long = System.currentTimeMillis(),
    ) = FocusSession(
        id = id,
        startTime = startTime,
        endTime = startTime + durationMs,
        plannedDurationMs = durationMs,
        actualDurationMs = durationMs,
        completionStatus = CompletionStatus.COMPLETED,
        themeId = "default",
    )

    fun earlyExitSession(/* ... */) = FocusSession(/* ... */)

    val defaultAutoLockConfig = AutoLockConfig(
        enabled = true,
        durationMinutes = 30,
        gracePeriodSeconds = 10,
    )
}
```

**Rules:**
- Use factory functions (not raw constructors) for test data. This lets each test override only the fields it cares about.
- Factory functions have sensible defaults for all parameters.
- Name factory functions after the scenario: `completedSession()`, `earlyExitSession()`, `premiumTheme()`.

---

## 8. Test Coverage Targets

| Layer | Target | Rationale |
| ----- | ------ | --------- |
| Data layer (`data/`) | **80%** | Core business logic: repositories, streak calculator, DAOs. Bugs here corrupt user data. |
| Services layer (`service/`) | **70%** | Session state machine, app blocking logic, emergency safety. Safety-critical code must be well-tested. |
| UI layer (`ui/`) | **60%** | ViewModels are well-tested; Compose UI rendering tests cover critical flows but not every visual variant. |

**Rules:**
- Coverage is measured with JaCoCo, configured by DevOps.
- Coverage targets are aspirational for v1 and enforced as CI gates starting v1.1.
- Emergency safety paths (`EmergencySafety`, `RepeatedCallerTracker`, emergency exit flow) must have **100% branch coverage** regardless of layer targets.

---

## 9. Test Organization Patterns

### Arrange-Act-Assert

Every test follows the AAA pattern with clear visual separation:

```kotlin
@Test
fun onEvent_startSession_transitionsToRunningState() = runTest {
    // Arrange
    val viewModel = createViewModel()

    // Act
    viewModel.onEvent(HomeEvent.StartSession)

    // Assert
    viewModel.effect.test {
        assertEquals(HomeEffect.NavigateToSession(25 * 60 * 1000L), awaitItem())
        cancelAndConsumeRemainingEvents()
    }
}
```

### Test Isolation

- Each test creates its own ViewModel/repository instance. Never share mutable state between tests.
- Use `@Before` for common setup (creating fakes, injecting dependencies).
- Use `@After` only if cleanup is needed (closing databases, resetting dispatchers).

---

## 10. Hilt Testing

For instrumented tests that need DI:

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FocusSessionDaoTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var database: WaneDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }
}
```

**Rules:**
- Use `@HiltAndroidTest` for instrumented tests that need injected dependencies.
- Use `@TestInstallIn` modules to replace production bindings with test fakes (e.g., in-memory Room database).
- The `HiltAndroidRule` must have `order = 0` to ensure Hilt initializes before other rules.

---

## 11. What NOT to Test

- Compose layout rendering pixel-by-pixel (use screenshot tests only for critical flows, not for every component).
- Android framework internals (e.g., don't test that `Intent.ACTION_DIAL` opens the dialer — that's Android's responsibility).
- Third-party library behavior (e.g., don't test that Room correctly executes SQL — test your queries and business logic).
- OpenGL rendering output (shader correctness is verified visually during development, not via automated tests).
