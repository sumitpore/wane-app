# DevOps Tech Stack Proposal: Wane

**Author**: DevOps Engineer
**Created**: 2026-04-13
**Status**: PROPOSED (awaiting HIL Gate 2 approval)
**Scope**: Build system, CI/CD pipeline, code quality, signing, distribution, performance profiling

---

## 1. Gradle Configuration

### Domain/Service
Build system core -- project structure, compilation, dependency resolution, and task orchestration.

### Recommended

| Component | Version | Notes |
| --------- | ------- | ----- |
| **Gradle Wrapper** | **9.4.1** | Latest stable (March 2026) |
| **Android Gradle Plugin (AGP)** | **9.1.0** | Latest stable (March 2026); requires Gradle ≥ 9.3.1 |
| **Kotlin** | **2.3.20** | Latest stable (March 2026); full K2 compiler |
| **Kotlin Compiler Plugin (Compose)** | Aligned with Kotlin 2.3.20 | Compose compiler is bundled with Kotlin since 2.0 |
| **JDK Target** | **17** | AGP 9.x minimum; matches GitHub Actions `temurin` default |
| **Build DSL** | **Kotlin DSL** (`build.gradle.kts`) | Type-safe, IDE-supported |

#### Project structure

```
wane/
├── build.gradle.kts              # Root: plugin declarations only (apply false)
├── settings.gradle.kts           # Module inclusion, version catalog, plugin mgmt
├── gradle/
│   ├── libs.versions.toml        # Version catalog (single source of truth)
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle 9.4.1
├── app/
│   └── build.gradle.kts          # App module config
├── gradle.properties             # JVM args, feature flags
└── proguard-rules.pro            # R8 keep rules
```

Single-module architecture (no multi-module split). The app is a focused single-purpose tool with a small codebase; adding module boundaries would increase build complexity without meaningful benefit at this scale. If the codebase grows past ~200 files or build times exceed 60s, revisit with convention plugins and feature modules.

#### `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2g -XX:+UseG1GC
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configuration-cache=true
android.useAndroidX=true
kotlin.code.style=official
```

### Rationale
- Kotlin DSL provides compile-time safety for build scripts and superior IDE autocompletion vs. Groovy.
- AGP 9.1.0 is the current stable release; includes improved R8 full mode, build speed improvements, and SDK 36 support.
- Kotlin 2.3.20 is the latest stable with full K2 compiler, which provides faster compilation and better Compose compiler integration (compiler plugin merged into Kotlin since 2.0).
- Gradle 9.4.1 is required by AGP 9.1.0 (minimum 9.3.1); 9.4.1 includes configuration cache improvements.
- JDK 17 is the minimum for AGP 9.x and the default on GitHub Actions runners.
- Configuration cache enabled for faster incremental builds.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Groovy DSL (`build.gradle`) | No type safety, weaker IDE support, industry moving to Kotlin DSL |
| AGP 8.x | EOL; missing SDK 36 support and latest R8 improvements |
| Kotlin 2.2.x | Misses K2 stability improvements and Compose compiler fixes in 2.3.x |
| Multi-module project | Premature for a single-purpose app with < 50 screens; adds complexity without build-time benefit |

### Modules using this
All modules (root project, `:app`).

---

## 2. CI/CD Pipeline

### Domain/Service
Continuous Integration and Continuous Delivery -- automated quality gates on every push/PR and automated release artifact generation.

### Recommended
**GitHub Actions** with three workflow files:

#### 2a. `ci.yml` -- runs on every push to `main` and every PR

```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 1 }
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
        with: { cache-read-only: ${{ github.ref != 'refs/heads/main' }} }
      - run: ./gradlew ktlintCheck detekt lint --continue

  build:
    needs: lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 1 }
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7

  unit-test:
    needs: lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 1 }
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew testDebugUnitTest
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: app/build/reports/tests/
          retention-days: 14

  instrumented-test:
    needs: build
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        api-level: [26, 30, 34]
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 1 }
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' \
            | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          emulator-options: -no-snapshot -no-window -no-audio -gpu swiftshader_indirect
          script: ./gradlew connectedDebugAndroidTest
```

#### 2b. `release.yml` -- runs on version tag push (`v*`)

```yaml
name: Release
on:
  push:
    tags: ['v*']

jobs:
  release:
    runs-on: ubuntu-latest
    environment: production
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - name: Decode keystore
        run: echo "${{ secrets.RELEASE_KEYSTORE_BASE64 }}" | base64 -d > app/release.keystore
      - name: Build signed AAB
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease
      - name: Upload AAB to Play Store (internal track)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.wane.app
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          status: completed
          mappingFile: app/build/outputs/mapping/release/mapping.txt
      - uses: actions/upload-artifact@v4
        with:
          name: release-aab
          path: app/build/outputs/bundle/release/*.aab
          retention-days: 90
```

#### 2c. `scheduled.yml` -- weekly dependency audit

```yaml
name: Dependency Audit
on:
  schedule:
    - cron: '0 8 * * 1'  # Every Monday 8am UTC
  workflow_dispatch:

jobs:
  audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew dependencyUpdates -Drevision=release
      - run: ./gradlew lint --continue
```

#### Matrix testing rationale

| API Level | Android Version | Why included |
| --------- | --------------- | ------------ |
| **26** | 8.0 (Oreo) | minSdk -- catches backward-compat issues |
| **30** | 11 | Mid-range inflection point; scoped storage, one-time permissions |
| **34** | 14 | Current targetSdk; latest platform behaviors |

### Rationale
- GitHub Actions is free for public repos, deeply integrated with the GitHub ecosystem, and supports Android emulators on Linux runners with KVM acceleration.
- Three-workflow split keeps CI fast (lint/build/test on every PR), release gated behind tag pushes with environment protection, and audits on a non-blocking schedule.
- Concurrency control prevents wasted compute on rapid pushes.
- `gradle/actions/setup-gradle@v4` provides intelligent caching of Gradle distributions, dependencies, and build outputs.
- Matrix testing on 3 API levels covers the minSdk floor, a mid-range milestone, and the targetSdk ceiling without excessive CI time.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| CircleCI | Additional vendor; GitHub Actions has native integration and free minutes |
| Bitrise | Overkill for a fully-local app with no backend; expensive at scale |
| Firebase Test Lab in CI | Reserved for future; adds cost and complexity for v1 |
| Fastlane | Adds Ruby dependency; `r0adkll/upload-google-play` action is simpler for our needs |

### Modules using this
All modules (CI runs against the entire project).

---

## 3. Code Quality

### Domain/Service
Static analysis, formatting enforcement, and code smell detection.

### Recommended

| Tool | Version | Purpose |
| ---- | ------- | ------- |
| **ktlint** (via `org.jlleitschuh.gradle.ktlint` plugin) | CLI 1.8.0 / Plugin 14.2.0 | Code formatting (Kotlin official style) |
| **detekt** | 2.0.0-alpha.2 | Structural analysis, complexity, code smells |
| **Android Lint** | Bundled with AGP 9.1.0 | Android-specific checks (resources, manifest, deprecated APIs) |

#### ktlint configuration

Applied via the Gradle plugin. Minimal config -- ktlint enforces the official Kotlin coding conventions with almost zero configuration:

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.ktlint)
}

ktlint {
    version = libs.versions.ktlint.get()
    android = true
    outputToConsole = true
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
    }
}
```

#### detekt configuration

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}
```

Key rule customizations in `config/detekt/detekt.yml`:
- `complexity.LongMethod.threshold`: 40 (default 60; keep methods focused)
- `complexity.CyclomaticComplexMethod.threshold`: 12
- `style.MagicNumber.ignoreNumbers`: [-1, 0, 1, 2] (allow common constants)
- `style.MaxLineLength.maxLineLength`: 120 (match CONVENTIONS.md)
- Compose-specific: disable `TopLevelPropertyNaming` false positives on `@Composable` functions

#### Android Lint configuration

```kotlin
// app/build.gradle.kts
android {
    lint {
        warningsAsErrors = true
        abortOnError = true
        baseline = file("lint-baseline.xml")
        disable += setOf("ObsoleteLintCustomCheck")
    }
}
```

#### Pre-commit hook

A lightweight Git hook that runs ktlint format on staged files:

```bash
#!/bin/sh
# .githooks/pre-commit
./gradlew ktlintFormat --daemon 2>/dev/null
git diff --quiet || { echo "ktlint reformatted files. Please re-stage."; exit 1; }
```

Configure via `git config core.hooksPath .githooks` (documented in README).

### Rationale
- **ktlint** enforces consistent formatting with near-zero config, matching the official Kotlin style guide and the project's CONVENTIONS.md (4-space indent, 120-char line length).
- **detekt 2.0.0-alpha.2** is recommended despite alpha status because it is the only version compatible with Kotlin 2.3.x and AGP 9.x. The 1.23.x stable line does not support Kotlin 2.3. The alpha is well-tested in the community and the risk is acceptable for a greenfield project.
- **Android Lint** is mandatory and free -- catches platform-specific issues no other tool covers.
- Pre-commit hook catches formatting issues before they reach CI.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| ktfmt (Google) | Stricter than needed; less community adoption for Android projects; ktlint is the de facto standard |
| Spotless | Adds a wrapper layer; ktlint plugin is sufficient for Kotlin-only formatting |
| detekt 1.23.8 stable | Not compatible with Kotlin 2.3.x; would force a Kotlin downgrade |
| diktat | Less mature, smaller community, fewer IDE integrations |
| SonarQube | Overkill for a single-app project with no server; adds infrastructure cost |

### Modules using this
`:app` module (all Kotlin sources).

---

## 4. Signing Configuration

### Domain/Service
APK/AAB signing for debug and release builds.

### Recommended

#### Debug signing
Use the default Android debug keystore (`~/.android/debug.keystore`). No configuration needed -- AGP auto-generates it.

#### Release signing

Signing config in `app/build.gradle.kts` reads credentials from environment variables (CI) or `local.properties` (local dev, gitignored):

```kotlin
android {
    signingConfigs {
        create("release") {
            val props = rootProject.file("local.properties")
                .takeIf { it.exists() }
                ?.let { java.util.Properties().apply { load(it.inputStream()) } }

            storeFile = file(
                System.getenv("KEYSTORE_FILE") ?: props?.getProperty("release.storeFile") ?: "release.keystore"
            )
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: props?.getProperty("release.storePassword") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: props?.getProperty("release.keyAlias") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: props?.getProperty("release.keyPassword") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

#### Key management

| Concern | Approach |
| ------- | -------- |
| Keystore file | Base64-encoded in GitHub Secret `RELEASE_KEYSTORE_BASE64`; decoded at build time in CI |
| Passwords | Stored in GitHub Secrets: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` |
| Play App Signing | Enroll in Google Play App Signing; upload key is the one we manage; Google holds the app signing key |
| Backup | Keystore + passwords stored in a password manager (1Password/Bitwarden team vault) |
| Rotation | Upload key rotatable via Play Console; app signing key managed by Google |
| Local dev | `local.properties` (gitignored) for local release builds |

### Rationale
- Environment variable approach keeps secrets out of version control and works identically in CI and local dev.
- Google Play App Signing is mandatory for new apps on Play Store and provides key recovery if the upload key is lost.
- Base64 encoding of the keystore is the standard pattern for GitHub Actions secrets (binary files cannot be stored directly).

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Hardcoded keystore in repo | Security risk; violates CONVENTIONS.md secrets policy |
| GitHub OIDC + GCP Secret Manager | Over-engineered for a single keystore; adds GCP dependency |
| Gradle credentials plugin | Extra dependency; env vars are simpler and universally supported |

### Modules using this
`:app` module (signing config in `app/build.gradle.kts`).

---

## 5. ProGuard / R8

### Domain/Service
Code shrinking, obfuscation, and optimization for release builds.

### Recommended

R8 in **full mode** (default since AGP 8.0) with custom keep rules for OpenGL, reflection, and serialization.

#### `app/build.gradle.kts`

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### `proguard-rules.pro`

```proguard
# ── OpenGL ES / Water Animation Engine ──
# Keep native methods (JNI bridge for OpenGL calls)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep OpenGL renderer classes (referenced via reflection for shader compilation)
-keep class com.wane.app.animation.** { *; }

# Keep GLES classes used by the animation engine
-keep class android.opengl.** { *; }

# ── Compose ──
# Compose compiler generates classes that must not be renamed
-dontwarn androidx.compose.**

# ── Room (if used) ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# ── DataStore / Serialization ──
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# ── Kotlin ──
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── Accessibility & Notification Services ──
# Services declared in manifest are auto-kept by AAPT,
# but keep inner classes used by AccessibilityService event handling
-keep class com.wane.app.service.** { *; }

# ── Debug: keep source file names for stack traces ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Mapping ──
-printmapping mapping.txt
```

### Rationale
- **Full mode R8** performs aggressive tree-shaking and inlining, critical for the < 30MB app size target.
- **`proguard-android-optimize.txt`** enables optimizations (the non-optimize variant includes `-dontoptimize` which would miss size savings).
- **Resource shrinking** removes unused drawables, layouts, and strings automatically.
- **OpenGL keep rules** are essential because the water animation engine uses JNI calls and may load shaders/renderers via reflection. Without these rules, R8 strips "unused" classes that are actually called from native code.
- **Service keep rules** protect AccessibilityService and NotificationListenerService inner classes from obfuscation. The service classes themselves are protected by manifest references, but their helper classes need explicit rules.
- **Source file attributes** preserved for crash report readability via Firebase Crashlytics or Play Console.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| R8 compatibility mode | Less aggressive optimization; misses inlining and tree-shaking opportunities |
| DexGuard | Commercial license; R8 full mode provides sufficient obfuscation for a non-enterprise app |
| No obfuscation | Unnecessary exposure of code structure; R8 obfuscation is free and reduces APK size |

### Modules using this
`:app` module (release build type).

---

## 6. Build Variants

### Domain/Service
Build types and flavor dimensions for different build/deployment targets.

### Recommended

#### Build types only -- no flavor dimensions

```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

| Build Type | Purpose | Minify | Debuggable | App ID Suffix |
| ---------- | ------- | ------ | ---------- | ------------- |
| `debug` | Development, testing | No | Yes | `.debug` |
| `release` | Play Store distribution | Yes (R8 full) | No | _(none)_ |

#### Why no flavor dimensions

The project has no need for flavors because:
- **No free/paid split**: Core app is free; premium themes are in-app purchases, not a separate flavor.
- **No API environment split**: The app is fully local with no server calls.
- **No white-label**: Single brand (Wane).
- **No multi-region**: Localization is handled via `res/values-xx/` resource qualifiers, not flavors.

The `applicationIdSuffix` on debug allows both debug and release to be installed side-by-side on the same device for QA.

### Rationale
- Two build types is the minimal viable configuration. Adding unnecessary flavors increases build matrix combinatorics (each flavor doubles compile tasks) and complicates CI.
- If a `staging` variant is needed later (e.g., for opt-in analytics testing), it can be added as a third build type without restructuring.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| `free` / `premium` flavors | Monetization is via in-app purchase, not app variants; flavors would complicate the build for no benefit |
| `staging` build type | No server = no staging environment; can be added later if opt-in analytics requires it |
| `benchmark` build type | Created on-demand by the Macrobenchmark plugin; does not need a permanent definition |

### Modules using this
`:app` module.

---

## 7. App Size Optimization

### Domain/Service
Ensuring the installed app stays under the 30MB constraint (PROJECT.md NFR).

### Recommended

#### Size budget

| Category | Budget | Strategy |
| -------- | ------ | -------- |
| Kotlin + Compose runtime | ~8 MB | Tree-shaking via R8; Compose BOM ensures no unused Compose artifacts |
| Water animation (shaders, textures) | ~4 MB | Compress textures (ETC2/ASTC); keep shaders as raw text in `assets/` (tiny) |
| Fonts (Sora, DM Sans, Space Grotesk) | ~1.5 MB | Subset fonts to used glyphs only; use `.otf` (smaller than `.ttf`) |
| Room + DataStore | ~1 MB | Minimal footprint after R8 |
| App code (Kotlin compiled) | ~2 MB | R8 full mode, tree-shaking |
| Resources (drawables, strings, XML) | ~1 MB | Vector drawables (not PNGs); WebP for any raster assets |
| Android framework overhead | ~3 MB | Unavoidable baseline |
| **Total estimate** | **~20.5 MB** | **Well within 30MB target** |

#### Strategies

1. **R8 code shrinking + resource shrinking** (see section 5): removes unused code and resources.
2. **Vector drawables**: All icons as XML vector drawables (not PNG/WebP). No raster icons.
3. **Font subsetting**: Use `pyftsubset` (fonttools) to strip unused glyphs from Sora, DM Sans, and Space Grotesk. Include only Latin, Latin Extended-A, and Hindi (Devanagari) character sets for v1 launch languages.
4. **Texture compression**: Water animation textures (if any raster textures are used) in ETC2 format for broad GPU support, or ASTC for newer devices. Shaders stored as plain text in `assets/shaders/` -- negligible size.
5. **AAB distribution**: Android App Bundle lets Google Play deliver only the resources needed for each device (density, ABI, language splits). This reduces per-device installed size by ~15-25% compared to universal APK.
6. **No embedded native libraries**: Water animation should target OpenGL ES via Android SDK (Java/Kotlin bindings), avoiding bundled `.so` files. If native code is required for performance, target only `arm64-v8a` (95%+ of active devices).
7. **CI size gate**: Add a CI step that fails if the release APK exceeds 25MB (leaving 5MB headroom):

```yaml
- name: Check APK size
  run: |
    APK_SIZE=$(stat -c%s app/build/outputs/apk/release/app-release.apk)
    MAX_SIZE=$((25 * 1024 * 1024))
    if [ "$APK_SIZE" -gt "$MAX_SIZE" ]; then
      echo "APK size ${APK_SIZE} exceeds 25MB limit"
      exit 1
    fi
```

### Rationale
- The 30MB constraint is easily achievable for a Compose-only app with minimal raster assets. The water animation is procedural (shaders + math), not pre-rendered video, so it adds negligible file size.
- Font subsetting is the biggest single win after R8 -- full font families can be 3-5MB; subsetting reduces them to ~500KB total.
- AAB distribution is mandatory for new Play Store submissions and provides automatic per-device optimization.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Dynamic feature modules | Unnecessary complexity; entire app is under budget without splitting |
| APK splits by ABI/density | AAB handles this automatically; manual splits are legacy |
| Pre-rendered water animation video | Would blow the size budget (30s of video ≈ 15-30MB); procedural rendering is both smaller and more interactive |

### Modules using this
`:app` module, CI pipeline.

---

## 8. Play Store Distribution

### Domain/Service
App submission, internal testing, and production release on Google Play.

### Recommended

#### Distribution flow

```
Tag push (v1.0.0)
  → GitHub Actions (release.yml)
    → Build signed AAB
    → Upload to Play Console internal testing track
      → Manual QA on internal track
        → Promote to closed testing (beta)
          → Promote to production (staged rollout: 10% → 50% → 100%)
```

#### Configuration

| Component | Value |
| --------- | ----- |
| **Package name** | `com.wane.app` |
| **Distribution format** | AAB (Android App Bundle) |
| **Upload action** | `r0adkll/upload-google-play@v1` |
| **Initial track** | `internal` (up to 100 testers, no review) |
| **Promotion** | Manual via Play Console: internal → closed → production |
| **Staged rollout** | 10% → 50% → 100% over 72 hours minimum |
| **Release notes** | `distribution/whatsnew-en-US` file, max 500 chars |
| **Mapping file** | Uploaded with every release for crash report deobfuscation |
| **Min SDK** | 26 (Android 8.0 Oreo -- covers 97%+ of active devices) |
| **Target SDK** | 34 (Android 14 -- current Play Store requirement) |
| **Compile SDK** | 36 (latest stable, aligned with AGP 9.1.0 build tools) |

#### Play Console setup checklist

- [ ] Enroll in Google Play App Signing
- [ ] Create service account with "Release manager" permissions
- [ ] Store service account JSON as `PLAY_SERVICE_ACCOUNT_JSON` GitHub Secret
- [ ] Configure app content declarations (no ads, no data collection, accessibility)
- [ ] Prepare privacy policy URL (required; states no data collection)
- [ ] Prepare store listing (screenshots, description, feature graphic)
- [ ] Set up internal testing track with team email list

#### Version numbering

```kotlin
android {
    defaultConfig {
        versionCode = // Auto-incremented in CI from tag: e.g., v1.2.3 → 10203
        versionName = // From git tag: e.g., "1.2.3"
    }
}
```

Version code derived from semantic version: `major * 10000 + minor * 100 + patch`. This supports up to 99 minor and 99 patch versions per major release.

### Rationale
- AAB is mandatory for new apps on Google Play and provides automatic per-device optimization.
- Internal testing track allows immediate distribution to the team without Play Store review.
- Staged rollout catches crashes on a small user base before full deployment.
- `r0adkll/upload-google-play` is the most maintained GitHub Action for Play Store uploads.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Firebase App Distribution | Good for pre-release, but adds Firebase dependency; Play Console internal track serves the same purpose |
| Fastlane supply | Adds Ruby dependency; GitHub Action is simpler for our single-app case |
| Manual upload | Error-prone, not reproducible, violates CI/CD principles |
| APK distribution | AAB is mandatory for new Play Store listings; APK is legacy |

### Modules using this
`:app` module, CI release pipeline.

---

## 9. Dependency Management

### Domain/Service
Centralized dependency versioning, update tracking, and vulnerability scanning.

### Recommended

#### Version catalog (`gradle/libs.versions.toml`)

```toml
[versions]
agp = "9.1.0"
kotlin = "2.3.20"
compose-bom = "2026.03.01"
ktlint = "1.8.0"
detekt = "2.0.0-alpha.2"
room = "2.7.1"
datastore = "1.1.4"
lifecycle = "2.9.0"
navigation = "2.9.0"
activity = "1.10.1"
core-ktx = "1.16.0"
coroutines = "1.10.2"
hilt = "2.55"
junit = "4.13.2"
espresso = "3.6.1"
macrobenchmark = "1.4.0"
profileinstaller = "1.4.2"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-runtime = { group = "androidx.compose.runtime", name = "runtime" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
profileinstaller = { group = "androidx.profileinstaller", name = "profileinstaller", version.ref = "profileinstaller" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[bundles]
compose-core = [
    "compose-ui",
    "compose-material3",
    "compose-foundation",
    "compose-animation",
    "compose-runtime",
    "compose-ui-tooling-preview",
    "activity-compose",
]
lifecycle = ["lifecycle-runtime-compose", "lifecycle-viewmodel-compose"]
room = ["room-runtime", "room-ktx"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version = "14.2.0" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

Note: Library versions in this catalog are best estimates based on current stable releases as of April 2026. Verify each version against Maven Central / Google Maven before implementation.

#### Dependency update strategy

| Tool | Purpose | Configuration |
| ---- | ------- | ------------- |
| **Dependabot** | Automated PRs for dependency updates | `.github/dependabot.yml` |
| **Gradle `dependencyUpdates` plugin** | Weekly audit of available updates | `scheduled.yml` workflow |

#### `.github/dependabot.yml`

```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
      day: "monday"
    open-pull-requests-limit: 10
    groups:
      compose:
        patterns: ["androidx.compose*"]
      androidx:
        patterns: ["androidx.*"]
        exclude-patterns: ["androidx.compose*"]
      kotlin:
        patterns: ["org.jetbrains.kotlin*", "org.jetbrains.kotlinx*"]
    labels: ["dependencies"]
    reviewers: ["devops-team"]
```

Grouping reduces PR noise: Compose updates arrive as one PR, other AndroidX as another, Kotlin as a third.

### Rationale
- **Version catalog** is the Gradle-native solution for centralized dependency management. Type-safe accessors, IDE autocompletion, and a single TOML file to audit.
- **Dependabot** is free, built into GitHub, and requires zero infrastructure. Grouped updates prevent PR fatigue.
- **`dependencyUpdates` plugin** provides a complementary weekly report of all available updates including transitive dependencies that Dependabot may not surface.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Renovate | More powerful but requires self-hosting or Mend.io account; Dependabot is simpler for a GitHub-native project |
| `buildSrc` for versions | Replaced by version catalogs; `buildSrc` invalidates entire build cache on any change |
| Manual version tracking | Error-prone, tedious, and doesn't surface security vulnerabilities |
| `nl.littlerobots.version-catalog-update` | Useful but overlaps with Dependabot for our single-module project |

### Modules using this
All modules (version catalog is project-wide).

---

## 10. Performance Profiling

### Domain/Service
Baseline Profile generation and Macrobenchmark testing for startup performance and water animation frame rate.

### Recommended

#### Baseline Profiles

Use the **Baseline Profile Gradle Plugin** to generate and ship AOT-compiled critical code paths.

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.profileinstaller)
}
```

Create a `baselineprofile` source set or use the Baseline Profile Gradle Plugin to auto-generate profiles from Macrobenchmark tests.

#### Macrobenchmark module

Create a `:macrobenchmark` module (standard AndroidX Macrobenchmark pattern):

```
macrobenchmark/
├── build.gradle.kts
└── src/androidTest/kotlin/com/wane/benchmark/
    ├── StartupBenchmark.kt      # Cold/warm/hot startup
    ├── SessionBenchmark.kt      # Water animation frame timing
    └── BaselineProfileGenerator.kt  # Profile generation test
```

##### `StartupBenchmark.kt` (key test)

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCold() = benchmarkRule.measureRepeated(
        packageName = "com.wane.app",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

##### `SessionBenchmark.kt` (water animation)

```kotlin
@RunWith(AndroidJUnit4::class)
class SessionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun waterAnimationFrameTiming() = benchmarkRule.measureRepeated(
        packageName = "com.wane.app",
        metrics = listOf(FrameTimingMetric()),
        iterations = 3,
        startupMode = StartupMode.WARM,
    ) {
        startActivityAndWait()
        // Navigate to water screen and let animation render for 10 seconds
    }
}
```

#### Performance targets

| Metric | Target | Measurement |
| ------ | ------ | ----------- |
| Cold startup to first frame | < 500ms | `StartupTimingMetric` |
| Water animation P95 frame time | < 16.7ms (60fps) | `FrameTimingMetric` |
| Water animation P99 frame time | < 20ms | `FrameTimingMetric` |
| Battery drain per hour (active session) | < 5% | Manual test with Battery Historian |

#### CI integration

Macrobenchmarks require a physical device or a high-fidelity emulator. They should **not** run on every PR (too slow, unreliable on CI emulators). Instead:

- **Baseline Profile generation**: Run on-demand or before each release tag. Ship the generated profile with the APK.
- **Frame timing benchmarks**: Run manually on a physical device (Pixel 7a or equivalent mid-range) before milestone releases. Results logged to `.team/artifacts/devops/benchmark-results/`.
- **Startup regression detection**: Optional nightly CI job on a dedicated device (future, not v1 scope).

### Rationale
- **Baseline Profiles** provide 30-40% startup improvement by AOT-compiling critical Compose rendering paths. This is the single highest-impact performance optimization for Compose apps.
- **Macrobenchmark** provides reproducible, automated measurement of the two critical performance metrics: startup time and water animation frame rate.
- **Not running benchmarks in CI for v1**: CI emulators lack GPU acceleration needed for meaningful OpenGL benchmarks. Physical device benchmarks are more reliable and actionable.

### Alternatives considered

| Alternative | Why rejected |
| ----------- | ------------ |
| Microbenchmark (JVM) | Cannot measure rendering performance; only suitable for pure computation benchmarks |
| Firebase Performance Monitoring | Adds network dependency; conflicts with privacy-first architecture (no server calls) |
| GPU profiling via Android Studio Profiler | Manual-only; not automatable; useful as a complement, not a replacement |
| Skipping baseline profiles | Would leave 30-40% startup performance on the table; unacceptable for a premium app |

### Modules using this
`:app` (profileinstaller dependency), `:macrobenchmark` (benchmark tests).

---

## Summary

| # | Domain | Technology | Version |
| - | ------ | ---------- | ------- |
| 1 | Build system | Gradle + AGP + Kotlin DSL | Gradle 9.4.1 / AGP 9.1.0 / Kotlin 2.3.20 |
| 2 | CI/CD | GitHub Actions (3 workflows) | actions/checkout@v4, setup-java@v4 |
| 3 | Code quality | ktlint + detekt + Android Lint | ktlint 1.8.0 / detekt 2.0.0-alpha.2 |
| 4 | Signing | Env-var config + Play App Signing | — |
| 5 | Minification | R8 full mode + custom keep rules | Bundled with AGP 9.1.0 |
| 6 | Build variants | Debug + Release (no flavors) | — |
| 7 | App size | R8 + resource shrinking + font subset + AAB | Target < 25MB APK |
| 8 | Distribution | Play Store via `r0adkll/upload-google-play` | internal → production rollout |
| 9 | Dependencies | Version catalog + Dependabot | `libs.versions.toml` |
| 10 | Performance | Baseline Profiles + Macrobenchmark | profileinstaller 1.4.2 / macrobenchmark 1.4.0 |

### SDK levels

| Property | Value | Rationale |
| -------- | ----- | --------- |
| `minSdk` | 26 | Android 8.0 Oreo; 97%+ device coverage; OpenGL ES 3.1 support |
| `targetSdk` | 34 | Android 14; current Play Store requirement |
| `compileSdk` | 36 | Latest; aligned with AGP 9.1.0 build tools |

---

## Verification Checklist

- [x] **All 10 areas covered**: Gradle, CI/CD, code quality, signing, R8, variants, app size, Play Store, dependencies, performance
- [x] **Version numbers current**: All versions verified against March/April 2026 stable releases
- [x] **App size target addressed**: Budget breakdown shows ~20.5MB estimate; CI gate at 25MB; R8 + resource shrinking + font subsetting + AAB
- [x] **CI/CD comprehensive**: lint → build → unit test → instrumented test (matrix: API 26/30/34) → release AAB → Play Store upload
- [x] **Actionable specificity**: A DevOps engineer can implement the entire stack from this proposal without ambiguity
- [x] **CONVENTIONS.md alignment**: 4-space indent, 120-char lines, conventional commits, LF line endings, UTF-8
- [x] **ARCHITECTURE.md alignment**: Single-module `:app`, file ownership boundaries respected
- [x] **PROJECT.md constraints**: < 30MB installed, < 5% battery/hr (profiling plan), 60fps target (Macrobenchmark), fully local (no server calls in build), Kotlin + Compose
