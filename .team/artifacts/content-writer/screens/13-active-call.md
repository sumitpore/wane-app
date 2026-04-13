# Screen 13 — Active Call

**Flow:** Basic phone mode (during active session)
**Purpose:** Standard call controls during a call.

---

## Content

### Caller Information

| Element | Format |
|---------|--------|
| **Caller name** | [Name] or [Phone Number] |
| **Call duration** | [M:SS] — standard call timer |
| **Caller photo** | _(Contact photo if available, otherwise neutral avatar.)_ |

### Controls

| Element | Label |
|---------|-------|
| **Mute** | Mute |
| **Speaker** | Speaker |
| **Keypad** | Keypad |
| **End call** | _(Icon only — red phone handset.)_ |

### Control States

| State | Label Change |
|-------|-------------|
| **Mute active** | Muted |
| **Speaker active** | Speaker on |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **Call dropped** | Call ended. |
| **Call on hold** | On hold |
| **Bluetooth connected** | _(Show Bluetooth icon in audio routing. No text label.)_ |
| **Call ended normally** | _(Return to water screen. No "Call ended" toast.)_ |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Call with [Name]. Duration: [time]." |
| **Mute button** | "Mute. Currently [on/off]. Double-tap to toggle." |
| **Speaker button** | "Speaker. Currently [on/off]. Double-tap to toggle." |
| **Keypad button** | "Open keypad." |
| **End call button** | "End call." |
| **Hold state** | "Call on hold." |

---

## Notes
- The active call screen is the one place where a numeric timer is acceptable — call duration is a phone standard.
- After the call ends, the user returns to the water screen, not the home screen. The session is still running.
- Control labels are single words. "Mute" not "Mute Microphone." "Speaker" not "Speakerphone."
- No Wane branding on this screen. It's a phone call. Be invisible.
