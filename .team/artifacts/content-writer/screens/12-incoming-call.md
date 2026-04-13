# Screen 12 — Incoming Call

**Flow:** Basic phone mode (during active session)
**Purpose:** Receive calls without leaving the session.

---

## Content

### Caller Information

| Element | Format |
|---------|--------|
| **Caller name** | [Name] (from contacts) |
| **Caller number** | [Phone Number] (if not in contacts) |
| **Caller photo** | _(Contact photo if available, otherwise a neutral avatar.)_ |
| **Call type** | _(No label like "Incoming Call" — the ringing and UI make it obvious.)_ |

### Action Buttons

| Element | Copy |
|---------|------|
| **Accept button** | _(Icon only — green phone handset. Swipe or tap.)_ |
| **Decline button** | _(Icon only — red phone handset. Swipe or tap.)_ |

### Quick Reply (on decline)

| Option | Copy |
|--------|------|
| **Quick reply 1** | Can't talk right now. |
| **Quick reply 2** | I'll call you back. |
| **Quick reply 3** | _(No third option. Two is enough.)_ |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **Unknown caller** | Unknown |
| **Spam/suspected spam (if system flags it)** | Suspected spam |
| **Emergency contact calling** | _(No special badge or label. The call comes through regardless — the user's emergency contacts are configured, not surfaced during the call.)_ |
| **Repeated caller breakthrough (3x in 5 min)** | _(Call comes through. No "This person has called 3 times" message. Just ring.)_ |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Incoming call from [Name or Number]." |
| **Caller photo** | "[Name]'s photo." or "Unknown caller." |
| **Accept button** | "Answer call." |
| **Decline button** | "Decline call." |
| **Quick reply option** | "Reply with message: [text]." |

---

## Notes
- The incoming call screen is full-screen, overlaying the water.
- No "You're in a session" reminder. The user knows. Just show the call.
- Quick replies are short and neutral. No "I'm using Wane right now!" — the app never draws attention to itself.
- Emergency contact calls and repeated caller breakthroughs ring through silently — no special notification or explanation at the moment of ringing.
