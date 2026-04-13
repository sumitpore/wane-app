# Screen 15 — Auto-Lock Settings

**Flow:** Settings → Auto-Lock
**Purpose:** Configure the stickiest feature. Clear options, no overwhelm.

---

## Content

### Header

| Element | Copy |
|---------|------|
| **Screen title** | Auto-lock |
| **Back button** | _(Icon only — back arrow.)_ |

---

### Master Toggle

| Element | Copy |
|---------|------|
| **Label** | Auto-lock |
| **Helper** | Water starts when you lock your phone. |
| **Default** | Off (unless enabled during onboarding) |

---

### Settings (visible when toggle is on)

Every setting has a short helper text directly below it.

| Setting | Label | Helper text | Control |
|---------|-------|-------------|---------|
| **Duration** | Duration | How long each auto-started session lasts. | Picker: 15 min / 30 min / 1 hr / 2 hr / Custom |
| **Grace period** | Grace period | Quick unlock-and-lock won't start new water. | Picker: 5 sec / 10 sec / 15 sec |
| **Skip between** | Skip between | Auto-lock won't run during this window. | Time range picker (start / end) |
| **Skip while charging** | Skip while charging | No sessions start while your phone is plugged in. | Toggle |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **Toggle turned on for the first time** | Your phone will find its calm on its own now. _(Toast, shown once, 3 seconds.)_ |
| **Toggle turned off** | _(No confirmation message. The toggle state is enough.)_ |
| **Skip between — times set** | No auto-lock between [start] and [end]. |
| **Skip between — overlap warning (start = end)** | Start and end times are the same. Auto-lock will always be active. |
| **All settings hidden when toggle is off** | _(Settings section smoothly collapses. No "Enable auto-lock to see these options" message.)_ |

---

## Permission Requests (if not yet granted when enabling)

### Accessibility Service

| Element | Copy |
|---------|------|
| **Explanation** | Auto-lock needs permission to detect when your phone is locked and start the water. |
| **If denied** | Auto-lock won't work without this permission. You can grant it in your phone's Settings. |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Auto-lock settings." |
| **Master toggle** | "Auto-lock. Water starts when you lock your phone. Currently [on/off]." |
| **Duration** | "Auto-lock duration. Currently [X]. Tap to change. How long each auto-started session lasts." |
| **Grace period** | "Grace period. Currently [X] seconds. Quick unlock and lock won't start new water." |
| **Skip between** | "Skip between. Auto-lock won't run between [start] and [end]." |
| **Skip while charging** | "Skip while charging. Currently [on/off]. No sessions start while your phone is plugged in." |

---

## Notes
- Settings only appear when the master toggle is on. No grayed-out disabled states — just clean collapse.
- "Your phone will find its calm on its own now." — the one moment of warmth, shown once.
- Every setting has a helper line below the label. Short, factual, one sentence max. Explains what the setting does, not why.
- "Skip between" replaces "Quiet hours" — the old name implied a basic-phone-mode period. "Skip between" is time-neutral (could be night, work hours, mornings — any window the user chooses).
- "Skip while charging" — consistent "Skip" language with "Skip between."
- Grace period helper is plain language: "Quick unlock-and-lock won't start new water."
