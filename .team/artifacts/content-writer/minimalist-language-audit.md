# Minimalist language audit — Wane user-facing copy

**Role:** Content Writer  
**Scope:** Soften the Language (UX Minimalist Roadmap) — neutral, calm, non-evaluative copy; no praise, shame, or banned vocabulary in replacements.  
**Sources audited:** `app/src/main/res/values/strings.xml` (complete), hardcoded user-facing strings in `app/src/main/kotlin/com/wane/app/ui/` and `app/src/main/kotlin/com/wane/app/service/`, plus one related string in `IntentHelpers.kt` (Toast) for completeness.  
**Date:** 2026-04-15

---

## 1. `strings.xml` — full audit (68 resources)

| Resource ID | Current text | Verdict | Replacement (if CHANGE) |
| ------------- | ------------ | ------- | ------------------------- |
| `app_name` | Wane | KEEP | — |
| `accessibility_service_description` | Wane uses this service to gently cover other apps with a calming water animation during focus sessions. It only detects which app is in the foreground — it never reads screen content, passwords, or personal information. The full source is public: https://github.com/sumitpore/wane-app.git | KEEP | — |
| `open_source_url` | https://github.com/sumitpore/wane-app | KEEP | — |
| `welcome_title` | Wane | KEEP | — |
| `welcome_subtitle` | Let your focus flow naturally | KEEP | — |
| `begin` | Begin | KEEP | — |
| `next` | Next | KEEP | — |
| `start` | Start | KEEP | — |
| `duration_title` | Set your focus duration | KEEP | — |
| `minutes_label` | minutes | KEEP | — |
| `decrease_duration` | Decrease duration | KEEP | — |
| `increase_duration` | Increase duration | KEEP | — |
| `accessibility_title` | Stay focused | KEEP | — |
| `accessibility_description` | Wane gently covers other apps during your focus session. Enable accessibility access to activate this. Wane is open source — you can verify the code at github.com/sumitpore/wane-app. | KEEP | — |
| `enable_accessibility` | Open Settings | KEEP | — |
| `notification_title` | Silence distractions | KEEP | — |
| `notification_description` | Wane can temporarily snooze notifications during your focus session, so nothing breaks your flow. Enable notification access to activate this. Wane is open source — you can verify the code at github.com/sumitpore/wane-app. | KEEP | — |
| `enable_notification_access` | Open Settings | KEEP | — |
| `auto_lock_title` | Focus, on its own | KEEP | — |
| `auto_lock_description` | When enabled, a focus session starts quietly a few seconds after you unlock your phone — no need to open Wane. | KEEP | — |
| `auto_lock_toggle_label` | Start focus on unlock | KEEP | — |
| `minutes_format` | %d min | KEEP | — |
| `min_label` | min | KEEP | — |
| `begin_focus_session` | Begin focus session | KEEP | — |
| `wane_logo` | Wane | KEEP | — |
| `day_streak` | day streak | KEEP | — |
| `start_session` | Start Session | KEEP | — |
| `accessibility_prompt_message` | To block distracting apps during focus sessions, Wane needs Accessibility access. It only detects and redirects app launches — no personal data is collected. Wane is open source; verify at github.com/sumitpore/wane-app. | CHANGE | To cover other apps during focus sessions, Wane needs Accessibility access. It only detects and redirects app launches — no personal data is collected. Wane is open source; verify at github.com/sumitpore/wane-app. |
| `notification_prompt_message` | To silence distracting notifications during focus sessions, Wane needs Notification access. It only temporarily snoozes non-essential notifications. Wane is open source; verify at github.com/sumitpore/wane-app. | KEEP | — |
| `no_streak_yet` | Begin your first session | KEEP | — |
| `end_session` | End | KEEP | — |
| `emergency_exit_title` | Are you sure? | KEEP | — |
| `emergency_exit_instruction` | Type the phrase below to end your session early | KEEP | — |
| `cancel` | Cancel | KEEP | — |
| `done` | Done | KEEP | — |
| `session_complete_title` | Well done | CHANGE | Session complete |
| `session_complete_message` | You focused for %d minutes | KEEP | — |
| `session_phone` | Phone | KEEP | — |
| `session_contacts` | Contacts | KEEP | — |
| `session_sms` | Messages | KEEP | — |
| `settings_title` | Settings | KEEP | — |
| `settings_back` | Back | KEEP | — |
| `settings_none` | None | KEEP | — |
| `settings_contacts_count` | %d contacts | KEEP | — |
| `settings_total_sessions` | Total sessions | KEEP | — |
| `settings_total_focus_time` | Total focus time | KEEP | — |
| `settings_hours_format` | %d hours | KEEP | — |
| `settings_clear` | Clear | KEEP | — |
| `settings_version` | Version | KEEP | — |
| `settings_version_value` | 0.1.0 | KEEP | — |
| `focus_section` | Focus | KEEP | — |
| `default_duration` | Default duration | KEEP | — |
| `blocking_section` | Automation | KEEP | — |
| `auto_lock` | Focus on Unlock | KEEP | — |
| `data_section` | Data | KEEP | — |
| `clear_sessions` | Clear all sessions | KEEP | — |
| `clear_sessions_confirm` | This will delete all session history and reset your streak. This cannot be undone. | KEEP | — |
| `about_section` | About | KEEP | — |
| `auto_lock_settings_title` | Focus on Unlock | KEEP | — |
| `auto_lock_enabled` | Start focus on unlock | KEEP | — |
| `auto_lock_duration` | Duration | KEEP | — |
| `grace_period` | Grace period | KEEP | — |
| `skip_window` | Skip window | KEEP | — |
| `skip_while_charging` | Skip while charging | KEEP | — |
| `autolock_seconds_format` | %d sec | KEEP | — |
| `autolock_skip_window_range` | %02d:%02d – %02d:%02d | KEEP | — |
| `error_foreground_service` | Unable to start session. Please allow background activity for Wane in system settings. | KEEP | — |
| `error_accessibility_service` | Accessibility service is required. Please enable it in system settings. | KEEP | — |

**Notes (strings.xml):**

- **`accessibility_prompt_message`:** Violates project **banned word** `block` in user-facing copy (`.team/CONVENTIONS.md`). Replacement mirrors `accessibility_description` / service copy (“cover”) — neutral mechanism, not judgment.
- **`session_complete_title`:** Evaluative praise (“Well done”) — banned pattern per Content Writer conventions. Replacement is **status-only**; duration remains the factual line via `session_complete_message`.
- **`day_streak` / `no_streak_yet`:** Not referenced in current Kotlin sources; still audited. Labels are factual (streak as a count, empty-state invitation). No grade language.
- **`accessibility_title` (`Stay focused`):** Mild imperative onboarding title — not praise/shame. Listed KEEP; optional later softening (e.g. “Focus overlay”) is a design call, not required for this audit’s primary violations.

---

## 2. Hardcoded Kotlin — `ui/` and `service/` (and related Toast)

| Location | Current text | Verdict | Replacement (if CHANGE) |
| -------- | ------------ | ------- | ------------------------- |
| `SessionViewModel.kt` — `EXIT_PHRASES[0]` | I am blessed | CHANGE | End session early |
| `SessionViewModel.kt` — `EXIT_PHRASES[1]` | I feel alive | CHANGE | Stop the timer now |
| `SessionViewModel.kt` — `EXIT_PHRASES[2]` | I am worthy | CHANGE | Leave this session |
| `SessionViewModel.kt` — `EXIT_PHRASES[3]` | I am strong | CHANGE | Confirm session end |
| `SessionViewModel.kt` — `EXIT_PHRASES[4]` | I am loved | CHANGE | End focus now |
| `SessionViewModel.kt` — `EXIT_PHRASES[5]` | I am brave | CHANGE | Stop focus timer |
| `SessionViewModel.kt` — `EXIT_PHRASES[6]` | I am at peace | CHANGE | Close focus overlay |
| `SessionViewModel.kt` — `EXIT_PHRASES[7]` | I am grateful | CHANGE | End this session |
| `SessionViewModel.kt` — `EXIT_PHRASES[8]` | I shine bright | CHANGE | Exit focus early |
| `SessionViewModel.kt` — `EXIT_PHRASES[9]` | I am radiant | CHANGE | Stop and go home |
| `AccessibilityStep.kt` — `contentDescription` | Permission granted | KEEP | — |
| `NotificationStep.kt` — `contentDescription` | Permission granted | KEEP | — |
| `WaneNotificationListener.kt` — channel name | Repeated Caller Alerts | KEEP | — |
| `WaneNotificationListener.kt` — notification title | Repeated caller | KEEP | — |
| `WaneNotificationListener.kt` — notification text | $maskedNumber called 3+ times in 5 minutes | KEEP | — |
| `WaneSessionService.kt` — ongoing notification title | Focus session active | KEEP | — |
| `WaneSessionService.kt` — `CHANNEL_NAME` | Focus Session | KEEP | — |
| `SessionScreen.kt` — emergency exit phrase display | Dynamic: `"{exitPhrase}"` (quotes around phrase from `EXIT_PHRASES`) | (see `EXIT_PHRASES`) | — |
| `WaneButton.kt` — `@Preview` | Begin | EXCLUDE | _(Preview-only; not shipped user copy.)_ |

**Kotlin outside `ui/` / `service/` (reference only):**

| Location | Current text | Verdict | Replacement (if CHANGE) |
| -------- | ------------ | ------- | ------------------------- |
| `IntentHelpers.kt` — Toast | No app found | KEEP | — |

**Notes (Kotlin):**

- **`EXIT_PHRASES`:** User-visible text shown in the emergency exit sheet. Current list is self-affirming / celebratory — aligns with banned evaluative/coachy patterns. Replacements are **procedural, neutral, and factual** (intent to end or stop), not self-praise.
- **Foreground notification / repeated-caller strings:** Factual status or call metadata — no praise or shame.
- **`Permission granted`:** Describes system state for accessibility — not evaluative of the person.

---

## 3. Summary

| Metric | Count |
| ------ | ----- |
| **Strings audited in `strings.xml`** | 68 |
| **Strings in `strings.xml` marked CHANGE** | 2 |
| **Hardcoded user-facing strings audited (`ui/` + `service/` + SessionScreen dynamic note)** | 18 distinct entries (10 exit phrases + 6 notification/service/a11y + 1 preview excluded + 1 SessionScreen note) |
| **Hardcoded entries marked CHANGE** | 10 (exit phrases only) |
| **Total CHANGE rows (authoritative)** | **12** (2 XML + 10 Kotlin exit phrases) |

---

## 4. Self-verification

1. **Every string in `strings.xml` audited?** Yes — 68 `<string name=` entries, each with a row in Section 1.
2. **Hardcoded strings searched in UI and service Kotlin?** Yes — `grep` and `stringResource` / notification / Toast usage reviewed; `SessionViewModel` exit phrases and notification/service strings captured.
3. **Replacements calm, factual, non-judgmental?** Yes — completion title is status-only; prompt uses “cover”; exit phrases state action/stop intent without evaluating the user.
4. **Banned words in replacements?** No — verified: no `addiction`, `limit`, `block`, `detox`, `digital`, `wellbeing` in proposed replacement strings.
5. **Copy length vs Content Writer caps?** `Session complete` ≤ 28 characters (screen title cap). Exit phrases are short CTAs / statements within tooltip-length territory; long-form `accessibility_prompt_message` replacement matches the original’s role (permission explanation) and is not lengthened for flair.
6. **Completion overlay:** Title replacement is **non-praising**; body `You focused for %d minutes` remains **duration fact** only (no “failed,” “abandoned,” or cheerleading). If product later hides the title and shows only the duration, that still satisfies “facts only.”

---

## 5. Implementation handoff (for Frontend Dev; not executed in this artifact)

- Apply `strings.xml` updates for `session_complete_title` and `accessibility_prompt_message`.
- Replace `EXIT_PHRASES` in `SessionViewModel.kt` with the ten strings above (order can stay random at runtime).
