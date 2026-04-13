# DevOps Conventions — Wane

**Created**: 2026-04-13
**Tech Stack**: Gradle 9.4.1, AGP 9.1.0, Kotlin DSL, GitHub Actions, ktlint 1.8.0, detekt 2.0.0-alpha.2, R8 full mode
**Scope**: `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, `gradle/`, `.github/`, `proguard-rules.pro`, `gradle.properties`

> Prerequisites: Read `CONVENTIONS.md` (general) and `ARCHITECTURE.md` (build/infrastructure section) before modifying build files or CI.

---

## 1. Gradle File Organization

| File | Purpose | Rules |
| ---- | ------- | ----- |
| `build.gradle.kts` (root) | Plugin declarations only | `apply false` on all plugins. No dependency declarations. No `allprojects {}` blocks. |
| `settings.gradle.kts` | Module inclusion, plugin management, version catalog | Declare `pluginManagement {}`, `dependencyResolutionManagement {}`, `include(":app")`. |
| `app/build.gradle.kts` | App module configuration | All `android {}`, `dependencies {}`, plugin applications, signing configs. |
| `gradle.properties` | JVM args, Gradle feature flags | Org.gradle flags, android.useAndroidX, caching/parallel settings. |
| `gradle/libs.versions.toml` | Version catalog | All dependency versions. See section 2. |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle wrapper version | Pin to `9.4.1`. Never modify without verifying AGP compatibility. |
| `proguard-rules.pro` | R8 keep rules | See section 7. |

**Rules:**
- Single-module architecture for v1. No `:core`, `:feature`, or `:shared` modules.
- Root `build.gradle.kts` is minimal — only `plugins { }` block with `apply false`.
- Never use `buildscript { }` blocks (legacy; version catalog replaces this).
- Never use `allprojects { }` or `subprojects { }` (use convention plugins if multi-module is added later).

---

## 2. Version Catalog (`gradle/libs.versions.toml`)

**All dependency versions are declared here. Never inline version strings in `build.gradle.kts`.**

```toml
[versions]
agp = "9.1.0"
kotlin = "2.3.20"
ksp = "2.3.20-1.0.30"
compose-bom = "2026.03.01"
hilt = "2.57.1"
hilt-navigation-compose = "1.3.0"
navigation3 = "1.0.0"
room = "3.0.0"        # Room 3.0 (androidx.room3)
datastore = "1.2.1"
ktlint = "1.8.0"
detekt = "2.0.0-alpha.2"
# ... etc.

[libraries]
# ... library declarations referencing version.ref

[bundles]
compose-core = ["compose-ui", "compose-material3", "compose-foundation", ...]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
# ... etc.
```

**Rules:**
- Every third-party dependency version is in `[versions]`. No exceptions.
- Use `version.ref` in library declarations — never inline a version string.
- Group related libraries into `[bundles]` for cleaner dependency blocks.
- When adding a new dependency, add it to the version catalog first, then reference it in `build.gradle.kts` via `libs.{name}`.
- Keep the file sorted alphabetically within each section (`[versions]`, `[libraries]`, `[bundles]`, `[plugins]`).

---

## 3. CI/CD Workflow Naming & Structure

| Workflow File | Trigger | Purpose |
| ------------- | ------- | ------- |
| `.github/workflows/ci.yml` | Push to `main`, all PRs | Lint → Build → Unit Test → Instrumented Test (matrix) |
| `.github/workflows/release.yml` | Tag push (`v*`) | Build signed AAB → Upload to Play Store internal track |
| `.github/workflows/scheduled.yml` | Cron (Monday 8am UTC) + manual | Dependency audit, lint report |

**Rules:**
- Workflow files use lowercase with hyphens: `ci.yml`, not `CI.yml` or `ci_pipeline.yml`.
- All workflows use `actions/checkout@v4`, `actions/setup-java@v4` (temurin, JDK 17), and `gradle/actions/setup-gradle@v4`.
- CI workflows set `concurrency.cancel-in-progress: true` to kill redundant runs on rapid pushes.
- Cache is read-only on PR branches (`cache-read-only: ${{ github.ref != 'refs/heads/main' }}`), read-write on `main`.
- Instrumented tests run on a matrix of API levels: `[28, 30, 36]` — covering minSdk (28), mid-range (30), and targetSdk (36).

### CI Job Order

```
lint (ktlintCheck + detekt + Android lint)
  ├── build (assembleDebug)        -- depends on lint
  ├── unit-test (testDebugUnitTest) -- depends on lint
  └── instrumented-test (connectedDebugAndroidTest) -- depends on build
```

---

## 4. Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <short description>

[optional body]

[optional footer]
```

| Type | When to use |
| ---- | ----------- |
| `feat:` | New feature or capability |
| `fix:` | Bug fix |
| `chore:` | Build, CI, dependency updates, tooling |
| `docs:` | Documentation changes only |
| `style:` | Code formatting (no logic change) |
| `refactor:` | Code restructuring (no feature or fix) |
| `test:` | Adding or updating tests |
| `perf:` | Performance improvement |

**Rules:**
- Type is lowercase, followed by colon and space.
- Short description is imperative mood ("add session timer", not "added session timer").
- Max 72 characters for the first line.
- Reference issue numbers in the footer: `Closes #42`.
- Breaking changes: add `!` after type: `feat!: replace Nav2 with Nav3`.

---

## 5. Tag & Version Format

| Convention | Format | Example |
| ---------- | ------ | ------- |
| Git tag | `v{major}.{minor}.{patch}` | `v1.0.0`, `v1.2.3` |
| `versionName` | `{major}.{minor}.{patch}` (from tag, no `v` prefix) | `"1.2.3"` |
| `versionCode` | `major * 10000 + minor * 100 + patch` | `10203` for v1.2.3 |

**Rules:**
- Tags are annotated: `git tag -a v1.0.0 -m "Release 1.0.0"`.
- Only tag on `main` branch after all CI checks pass.
- The `release.yml` workflow triggers on tag push matching `v*`.
- Never reuse a tag. If a release needs a fix, increment patch: `v1.0.1`.

---

## 6. Branch Naming

| Branch | Purpose | Example |
| ------ | ------- | ------- |
| `main` | Production-ready code, protected | — |
| `feature/{name}` | New feature development | `feature/water-animation`, `feature/auto-lock` |
| `fix/{name}` | Bug fix | `fix/timer-drift`, `fix/accessibility-crash` |
| `release/{version}` | Release preparation (if needed) | `release/1.0.0` |
| `chore/{name}` | Build/tooling changes | `chore/update-compose-bom` |

**Rules:**
- Branch names are lowercase with hyphens. No underscores, no camelCase.
- `main` is the default branch and is protected (requires PR, passing CI).
- Feature branches are short-lived (< 1 week). Long-running branches require team discussion.
- Delete branches after merge.

---

## 7. ProGuard / R8 Rules

All keep rules in `proguard-rules.pro` must include a comment explaining why the rule exists.

```proguard
# OpenGL native method bridge — called via JNI, invisible to R8 tree-shaking
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Water animation renderer — referenced via GLSurfaceView reflection for lifecycle callbacks
-keep class com.wane.app.animation.** { *; }

# Android services — inner helper classes used by AccessibilityService event dispatch
-keep class com.wane.app.service.** { *; }

# Room entities — annotation-driven code generation; R8 may strip "unused" constructors
-keep @androidx.room.Entity class *
```

**Rules:**
- Every `-keep` rule has a comment on the line above explaining the reason.
- Rules without comments will be flagged in code review and must be documented before merge.
- Prefer narrow rules (`-keep class com.wane.app.animation.**`) over broad rules (`-keep class **`).
- After adding or modifying keep rules, verify the release APK works correctly on a physical device.
- Upload `mapping.txt` with every release for crash report deobfuscation.

---

## 8. Secret Management

| Secret | Storage | Usage |
| ------ | ------- | ----- |
| Release keystore | GitHub Secret (`RELEASE_KEYSTORE_BASE64`) | Base64-decoded at build time in CI |
| Keystore password | GitHub Secret (`KEYSTORE_PASSWORD`) | Env var in release workflow |
| Key alias | GitHub Secret (`KEY_ALIAS`) | Env var in release workflow |
| Key password | GitHub Secret (`KEY_PASSWORD`) | Env var in release workflow |
| Play Console service account | GitHub Secret (`PLAY_SERVICE_ACCOUNT_JSON`) | Used by upload action |
| Local dev signing | `local.properties` (gitignored) | `release.storeFile`, `release.storePassword`, etc. |

**Rules:**
- **Never commit secrets to version control.** No API keys, no keystore files, no passwords in any tracked file.
- `local.properties` is in `.gitignore`. Verify this before every new clone.
- GitHub Secrets are managed via the repository settings UI. Document required secrets in the README.
- Keystore file + passwords are backed up in a team password manager (1Password, Bitwarden, or equivalent).
- If a secret is accidentally committed, rotate it immediately and force-push to remove from history (with team notification).

---

## 9. Build Variants

| Build Type | Minify | Debuggable | App ID Suffix | Use |
| ---------- | ------ | ---------- | ------------- | --- |
| `debug` | No | Yes | `.debug` | Development, testing |
| `release` | Yes (R8 full mode) | No | _(none)_ | Play Store distribution |

**Rules:**
- No product flavors for v1. The app is single-brand, fully local, with no free/paid split.
- `applicationIdSuffix = ".debug"` on debug allows side-by-side installation with release on the same device.
- If a `staging` or `benchmark` variant is needed later, add it as a build type, not a flavor.

---

## 10. App Size Gate

CI enforces a **25MB APK size limit** (5MB headroom below the 30MB PROJECT.md constraint):

```yaml
- name: Check APK size
  run: |
    APK_SIZE=$(stat -c%s app/build/outputs/apk/release/app-release.apk)
    MAX_SIZE=$((25 * 1024 * 1024))
    if [ "$APK_SIZE" -gt "$MAX_SIZE" ]; then
      echo "::error::APK size ${APK_SIZE} bytes exceeds 25MB limit"
      exit 1
    fi
```

**Rules:**
- The size check runs in the CI build job after `assembleRelease`.
- If the limit is exceeded, investigate: check font sizes (subset?), drawable format (vector vs raster?), unused resources (R8 shrinking?), new dependencies.
- Log the APK size in CI output for trend tracking.

---

## 11. Code Quality Tool Configuration

### ktlint

- Plugin: `org.jlleitschuh.gradle.ktlint` v14.2.0
- CLI version: 1.8.0
- Config: `android = true`, default Kotlin official style
- Pre-commit hook runs `ktlintFormat` on staged files

### detekt

- Version: 2.0.0-alpha.2 (required for Kotlin 2.3.x compatibility)
- Config file: `config/detekt/detekt.yml`
- Key thresholds: `LongMethod = 40`, `CyclomaticComplexMethod = 12`, `MaxLineLength = 120`
- Build on default config with overrides

### Android Lint

- `warningsAsErrors = true`, `abortOnError = true`
- Baseline file: `lint-baseline.xml` (committed, updated deliberately)
- Disabled: `ObsoleteLintCustomCheck` (false positives with AGP 9.x)

**Rules:**
- All three tools run in the `lint` CI job: `./gradlew ktlintCheck detekt lint --continue`.
- `--continue` ensures all linters report errors, not just the first failure.
- Developers run `./gradlew ktlintFormat` locally before committing (enforced by pre-commit hook).

---

## 12. Gradle Properties

```properties
org.gradle.jvmargs=-Xmx2g -XX:+UseG1GC
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configuration-cache=true
android.useAndroidX=true
kotlin.code.style=official
```

**Rules:**
- `configuration-cache=true` is enabled for faster incremental builds. If a plugin is incompatible, file an issue and add `org.gradle.configuration-cache.problems=warn` temporarily.
- JVM heap is set to 2GB. Increase only if out-of-memory errors occur during CI builds.
- Never add `org.gradle.unsafe.*` flags without documenting the risk.

---

## 13. Dependabot Configuration

```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
      day: "monday"
    groups:
      compose:
        patterns: ["androidx.compose*"]
      androidx:
        patterns: ["androidx.*"]
        exclude-patterns: ["androidx.compose*"]
      kotlin:
        patterns: ["org.jetbrains.kotlin*", "org.jetbrains.kotlinx*"]
    labels: ["dependencies"]
```

**Rules:**
- Dependency updates are grouped (Compose, AndroidX, Kotlin) to reduce PR noise.
- Review grouped PRs weekly. Ensure CI passes before merging.
- Major version bumps (e.g., Room 3.x → 4.x) require manual review and migration plan — do not auto-merge.
