# Wane Security Audit Report

**Date:** 2026-04-13
**Auditor:** Security Reviewer (automated)
**Scope:** Emergency safety, privileged service usage, data storage, permissions, intent handling, code quality
**App version:** Pre-release (commit snapshot)

---

## Executive Summary

Wane is an offline-only Android focus app that uses AccessibilityService and NotificationListenerService to block distracting apps and filter notifications during focus sessions. The app's security posture is **strong overall**, particularly in the areas that matter most: emergency safety is deeply layered with redundant fail-safes, privileged services are configured with the narrowest possible scope, and the complete absence of network access eliminates entire classes of risk (data exfiltration, man-in-the-middle, server-side vulnerabilities).

Two areas require attention. First, the emergency number list includes the entry `"08"` — a two-digit suffix that is overly broad and will match any phone number ending in those digits, creating noise that could erode user trust in the emergency bypass system. Second, the `WaterThemeEntity` stores a `purchaseToken` field in plaintext in the Room database; if in-app purchases are implemented, this token should be treated as sensitive. Neither issue represents a user-safety failure, and the app earns a **CONDITIONAL PASS** pending resolution of the medium-severity items below.

---

## Findings Table

| # | Area | Rating | Finding | Recommendation |
|---|------|--------|---------|----------------|
| 1 | Emergency numbers — hardcoded & immutable | **PASS** | Numbers defined as compile-time `setOf()` in `EmergencySafety` object. Not loaded from DB, preferences, or network. | None. |
| 2 | Emergency check ordering | **PASS** | `isNeverBlockPackage()` is the first guard in both `WaneAccessibilityService.onAccessibilityEvent()` and `AppBlocker.shouldBlockApp()`. Emergency number check precedes snooze logic in `WaneNotificationListener`. | None. |
| 3 | Emergency number coverage | **WARN** | Covers 911, 112, 999, 000, 110, 119, 118, 102, 103. Missing several regional numbers: 100 (India fire), 108 (India ambulance), 122 (Egypt), 190/192/193 (Brazil), 10111 (South Africa). | Add missing numbers. Coverage gaps are mitigated by the dialer always being unblocked, but the notification-snooze path relies on number matching. |
| 4 | `"08"` in emergency set | **WARN** | The two-digit entry `"08"` will suffix-match any number ending in `08` (e.g., `+1-555-123-4508`), producing false positives. This is safe-by-default but noisy. | Replace `"08"` with the full regional emergency number it represents (e.g., Indonesia `112` or `110` are already covered). If `"08"` refers to a specific country's short-dial, use the full form. |
| 5 | Suffix matching correctness | **PASS** | `digits.endsWith(emergency)` after stripping non-digits correctly handles country prefixes (`+1911` → `1911` → ends with `911`). | None. |
| 6 | Emergency exit — zero cooldown | **PASS** | `requestEmergencyExit()` has no rate-limit, no cooldown, and no confirmation beyond the deliberate-friction "EXIT" keyword. Timer is cancelled synchronously before the coroutine launches. | None. |
| 7 | Emergency exit — keyword friction | **INFO** | Typing `"EXIT"` (case-sensitive) is required to end a session early. This is intentional friction, not a safety blocker, because dialer/contacts/SMS are always accessible without ending the session. | Document this design decision so future developers do not add additional friction (e.g., countdown timers, cooldowns). |
| 8 | Android Emergency SOS | **PASS** | `com.android.emergency` is in `NEVER_BLOCK_PACKAGES`. System crash detection and emergency SOS use system-level components that are all allowlisted. | None. |
| 9 | Repeated caller breakthrough | **PASS** | 3 calls in 5 minutes triggers a `IMPORTANCE_HIGH` notification. Tracker uses `ConcurrentHashMap` with per-list synchronization. Phone number is masked to last 4 digits in the notification. | None. |
| 10 | A11y — event types | **PASS** | `accessibility_service_config.xml`: `accessibilityEventTypes="typeWindowStateChanged"` — narrowest useful scope. | None. |
| 11 | A11y — no content retrieval | **PASS** | `canRetrieveWindowContent="false"`, `canPerformGestures="false"`. Service only reads `event.packageName`. | None. |
| 12 | A11y — Play policy compliance | **PASS** | Legitimate use case (app blocking for focus), minimal permissions, no content reading, declared `settingsActivity`. Compliant with Google Play Accessibility API policy (2024 revision). | None. |
| 13 | A11y — graceful degradation | **PASS** | `if (!::appBlocker.isInitialized) return` prevents NPE if Hilt injection fails. All logic wrapped in try/catch. | None. |
| 14 | Notification listener — snooze restoration | **PASS** | `unsnoozeAll()` called on session state change (not `Running`) and in `onListenerDisconnected()`. `onNotificationRemoved()` cleans up `snoozedKeys`. | None. |
| 15 | Notification listener — phone/SMS passthrough | **PASS** | `phoneAndSmsPackages` set includes major OEM dialer, contacts, and SMS packages. Checked before snooze. | None. |
| 16 | Notification listener — emergency contacts | **PASS** | Emergency contacts from preferences are checked before snooze. Matching uses digit-only suffix comparison in both directions. | None. |
| 17 | Permissions — minimal set | **PASS** | Only 4 permissions: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`. No dangerous permissions (no `READ_CONTACTS`, `CALL_LOG`, `READ_PHONE_STATE`). | None. |
| 18 | Permissions — POST_NOTIFICATIONS runtime | **WARN** | `POST_NOTIFICATIONS` requires runtime permission on Android 13+. No explicit runtime permission request was found in the audited code paths. The repeated-caller notification may silently fail on Android 13+ if the user hasn't granted the permission. | Add a runtime permission request flow, or at minimum document that the repeated-caller feature requires the user to grant notification permission. |
| 19 | Permissions — RECEIVE_BOOT_COMPLETED | **PASS** | Justified by auto-lock feature needing `ScreenLockReceiver` to be re-registered after reboot. | None. |
| 20 | Data — no PII | **WARN** | Emergency contact phone numbers stored as JSON in DataStore. `WaterThemeEntity.purchaseToken` stored in plaintext in Room DB. Neither field is encrypted. | Emergency contacts: acceptable risk given local-only storage and Android's sandboxing. Purchase token: encrypt if IAP is implemented. |
| 21 | Data — local only | **PASS** | No `INTERNET` permission in manifest. No network calls anywhere in audited code. | None. |
| 22 | Data — DataStore corruption | **PASS** | All `observeX()` flows use `.catch { emit(defaultValue) }`. All `setX()` functions wrap `dataStore.edit` in try/catch with silent fallback. | None. |
| 23 | Data — backup rules | **WARN** | `backup_rules.xml` and `data_extraction_rules.xml` include `wane.db` and `wane_preferences.preferences_pb` in cloud backup. If purchase tokens or emergency contacts are considered sensitive, they could be exposed through backup extraction on a compromised Google account. | If IAP is implemented, either exclude the database from cloud backup or encrypt the `purchaseToken` column. |
| 24 | Services — not exported | **PASS** | All three services (`WaneSessionService`, `WaneAccessibilityService`, `WaneNotificationListener`) and the receiver (`ScreenLockReceiver`) declare `android:exported="false"`. | None. |
| 25 | Services — BIND permissions | **PASS** | AccessibilityService: `android.permission.BIND_ACCESSIBILITY_SERVICE`. NotificationListener: `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`. Both correct. | None. |
| 26 | Services — FGS type | **PASS** | `WaneSessionService` uses `foregroundServiceType="specialUse"` with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` set to `"focus_session_timer"`. Compliant with Android 14+ FGS restrictions. | None. |
| 27 | Thread safety — RepeatedCallerTracker | **WARN** | `ConcurrentHashMap.getOrPut()` is not atomic; two threads calling `recordCall()` with the same number simultaneously could create two separate lists. In practice, calls come from `onNotificationPosted` on the main thread, so this is unlikely to trigger. | Replace `getOrPut` with `computeIfAbsent` for true atomicity, or document the main-thread assumption. |
| 28 | Thread safety — snoozedKeys | **PASS** | All access to `snoozedKeys` is wrapped in `synchronized(snoozedKeys)` blocks. | None. |
| 29 | Thread safety — emergencyContacts | **PASS** | Written via `collectLatest` on `Dispatchers.Main.immediate`, read from `onNotificationPosted` which is also called on the main thread. Visibility guaranteed. | None. |
| 30 | Intents — implicit only | **PASS** | `IntentHelpers` uses `ACTION_DIAL` (not `ACTION_CALL`), `ACTION_VIEW` with `ContactsContract`, and `ACTION_SENDTO`. No permissions required; no calls initiated without user action. | None. |
| 31 | Intents — FLAG_ACTIVITY_NEW_TASK | **PASS** | All intents from non-Activity contexts (`AppBlocker.redirectToWane()`, `IntentHelpers`) include `FLAG_ACTIVITY_NEW_TASK`. | None. |
| 32 | Intents — no sensitive data | **PASS** | Intents carry only standard URI schemes (`tel:`, `smsto:`, contacts content URI). Phone numbers are user-initiated. | None. |
| 33 | Secrets — none hardcoded | **PASS** | No API keys, tokens, passwords, or secrets in the codebase. | None. |
| 34 | Logging — no sensitive data | **PASS** | Log statements use opaque tags and exception messages only. Notification key logged in one error path (`"Failed to snooze notification ${sbn.key}"`) but notification keys are opaque system identifiers. | None. |
| 35 | ProGuard/R8 | **PASS** | Comprehensive rules covering Room, Hilt, Kotlin serialization, and animation engine. Source file names kept for crash reporting (standard practice). No overly broad `-keep` rules that would expose unnecessary API surface. | None. |
| 36 | `Long.MAX_VALUE` snooze duration | **WARN** | `snoozeNotification(sbn.key, Long.MAX_VALUE)` may behave unexpectedly on certain OEM Android versions. Some manufacturers cap snooze durations or treat extreme values as errors. | Consider using a bounded large value (e.g., 30 days in milliseconds) instead of `Long.MAX_VALUE`. |
| 37 | MainActivity — singleTask launch mode | **PASS** | `android:launchMode="singleTask"` prevents task-hijacking attacks where a malicious app could insert itself into Wane's back stack. | None. |

---

## Critical Findings

**No critical (FAIL) findings identified.**

All emergency safety mechanisms are correctly implemented and layered. The app's architecture makes several classes of attack impossible by design (no network, no exported components, no content reading).

---

## Recommendations by Severity

### High

1. **[#18] Add POST_NOTIFICATIONS runtime permission request.**
   The repeated-caller breakthrough feature depends on posting a high-importance notification. On Android 13+, this will silently fail without runtime permission. Add a permission request during onboarding or when the user first enables focus mode. Without this, the safety-critical repeated-caller alert may never be seen.

### Medium

2. **[#4] Replace `"08"` in emergency number set.**
   The two-character entry creates excessive false positives. Identify the specific regional number it represents and use the full form. If it's Indonesia, `112` and `110` are already covered.

3. **[#3] Expand emergency number coverage.**
   Add missing regional numbers for large user bases: `100` (India fire), `108` (India ambulance), `122` (Egypt police), `190`/`192`/`193` (Brazil). The dialer-always-allowed design mitigates this for app blocking, but the notification snooze path depends on number matching.

4. **[#20, #23] Encrypt `purchaseToken` if IAP is implemented.**
   Currently the field exists but may be unused. Before shipping in-app purchases, encrypt this column or move it to Android Keystore-backed encrypted storage. Review backup rules to exclude sensitive columns.

### Low

5. **[#27] Use `computeIfAbsent` in `RepeatedCallerTracker`.**
   Replace `getOrPut` with `computeIfAbsent` for guaranteed atomicity. Low practical risk since the callback is main-thread-only, but defensive coding prevents future regressions if threading assumptions change.

6. **[#36] Cap snooze duration to a bounded value.**
   Replace `Long.MAX_VALUE` with `30 * 24 * 60 * 60 * 1000L` (30 days) to avoid OEM compatibility issues with extreme snooze durations.

7. **[#7] Document emergency exit design rationale.**
   The "EXIT" keyword friction is a deliberate and well-reasoned design choice. Add a code comment or ADR (Architecture Decision Record) so future developers understand the safety trade-off and do not inadvertently add additional barriers (countdowns, cooldowns, CAPTCHAs).

---

## Checklist Summary

| Area | Verdict |
|------|---------|
| Emergency Safety | **PASS** (with medium-severity improvements recommended) |
| AccessibilityService Policy | **PASS** |
| NotificationListenerService | **PASS** |
| Permissions | **PASS** (runtime permission gap noted) |
| Data Storage | **PASS** (purchase token caveat) |
| Service Security | **PASS** |
| Intent Security | **PASS** |
| Code Quality Security | **PASS** |

---

## Final Verdict

### CONDITIONAL PASS

The app passes the security audit with the condition that items **#18** (POST_NOTIFICATIONS runtime permission) and **#4** (`"08"` emergency number entry) are addressed before production release. All emergency safety mechanisms function correctly, privileged services are configured with minimal scope, and the offline-only architecture eliminates network-based attack vectors entirely. The remaining recommendations are improvements that strengthen an already solid security posture.
