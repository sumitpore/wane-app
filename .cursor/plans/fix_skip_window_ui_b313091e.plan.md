---
name: Fix Skip Window UI
overview: Add user-editable time pickers to the Skip Window setting under "Focus on Unlock" so users can define their own skip window start/end times instead of the current hardcoded 22:00-07:00 default.
todos:
  - id: triage
    content: "Triage: Identify root cause of hardcoded skip window (DONE -- hardcoded values in AutoLockSettingsScreen.kt line 109)"
    status: completed
  - id: frontend-fix
    content: Spawn Frontend Developer specialist to add time pickers to SkipWindowRow composable in AutoLockSettingsScreen.kt, allowing users to define custom skip window start/end times
    status: completed
  - id: verify-build
    content: "Run automated verification: build, lint, and existing tests to confirm no regressions"
    status: completed
  - id: lead-verify
    content: "Lead verification: review the fix for completeness, UI consistency, and correct persistence flow"
    status: completed
isProject: false
---

# Fix Skip Window UI -- User-Definable Times

## Problem Statement

The Skip Window feature under "Focus on Unlock" settings currently only has a toggle (on/off). When enabled, it hardcodes the window to 22:00-07:00 with no way for the user to customize it.

The backend already supports arbitrary times -- the issue is **purely in the UI** in [`AutoLockSettingsScreen.kt`](app/src/main/kotlin/com/wane/app/ui/settings/AutoLockSettingsScreen.kt).

Specifically, line 109 hardcodes `SetSkipWindow(22, 0, 7, 0)`:

```106:116:app/src/main/kotlin/com/wane/app/ui/settings/AutoLockSettingsScreen.kt
                    onToggle = { enabled ->
                        if (enabled) {
                            viewModel.onEvent(
                                AutoLockUiEvent.SetSkipWindow(22, 0, 7, 0),
                            )
                        } else {
                            viewModel.onEvent(
                                AutoLockUiEvent.SetSkipWindow(null, null, null, null),
                            )
                        }
                    },
```

The `SkipWindowRow` composable (lines 264-320) displays the time range as read-only text with no way to edit it.

## Approach (Team-of-Agents Debug Mode)

Since this is a **fix** for an existing project, we use **Debug Mode** (Section 4 of the skill). The `.team/` directory is not required for debugging. The Delegation Mandate applies -- all specialist work is delegated to sub-agents.

### Debug Phase 1: Triage (already complete)

The problem is fully triaged above. The symptom, root cause, and affected files are identified.

### Debug Phase 2: Diagnosis

**Specialist: Frontend Developer**

Task: Confirm the diagnosis and propose the best Material3 Compose approach for time picking. The specialist should evaluate:
- Whether to use `TimePickerDialog` (Material3) or a simpler tap-to-open approach
- How to fit the pickers into the existing `SkipWindowRow` layout (e.g., tappable time labels that open a picker)
- How it integrates with the existing `AutoLockUiEvent.SetSkipWindow` event

### Debug Phase 3: Fix

**Specialist: Frontend Developer**

Task: Implement the fix. The key files involved:
- [`AutoLockSettingsScreen.kt`](app/src/main/kotlin/com/wane/app/ui/settings/AutoLockSettingsScreen.kt) -- modify `SkipWindowRow` to add tappable start/end time pickers; update the `onToggle` callback to use default times only on initial enable
- [`AutoLockViewModel.kt`](app/src/main/kotlin/com/wane/app/ui/settings/AutoLockViewModel.kt) -- may need a new event `SetSkipWindowStart`/`SetSkipWindowEnd` or keep using `SetSkipWindow` for atomic updates
- [`strings.xml`](app/src/main/res/values/strings.xml) -- add any new string resources if needed (e.g., "Start time", "End time" labels)

No changes needed in:
- [`AutoLockConfig.kt`](app/src/main/kotlin/com/wane/app/shared/AutoLockConfig.kt) -- already supports arbitrary hours/minutes
- [`PreferencesRepositoryImpl.kt`](app/src/main/kotlin/com/wane/app/data/repository/impl/PreferencesRepositoryImpl.kt) -- persistence already works
- [`AutoLockScheduler.kt`](app/src/main/kotlin/com/wane/app/service/AutoLockScheduler.kt) -- `isInSkipWindow()` already handles arbitrary times including overnight wrapping

### Debug Phase 4: Verify

**Specialist: Frontend Developer** (self-verification) followed by **Lead Verification**:
- Does the fix allow the user to set custom start and end times?
- Is the default 22:00-07:00 only applied on initial toggle-on?
- Are the selected times correctly persisted via `AutoLockUiEvent.SetSkipWindow`?
- Does the `isInSkipWindow` logic in `AutoLockScheduler` correctly respect the user's custom window? (it already does -- just verify no regressions)
- Do existing tests pass?
- Is the UI consistent with the app's design system (theme colors, typography from [`WaneTypography`], card style)?

**Specialist: Test Engineer** (optional, if time permits):
- Verify existing `AutoLockSchedulerTest` and `PreferencesRepositoryImplTest` still pass
- Consider adding a test for the ViewModel event handling of custom skip window times

## Execution Order

1. Spawn **Frontend Developer** to diagnose, implement the fix, and self-verify
2. Run automated verification (build + lint + existing tests)
3. Spawn **Test Engineer** if the Frontend Developer's self-verification surfaces any concerns
