# Screen 05 — Water Screen (Hero)

**Flow:** Active session
**Purpose:** THE product experience. Almost no text. The water is everything.

---

## Content

### Visual
Full-screen water animation. No status bar. No clock. No notification badges. The water level indicates elapsed time without numbers.

### Text Elements

| Element | Copy |
|---------|------|
| **On screen** | _(None. Zero text during normal viewing.)_ |

### Bottom Toolbar (translucent, minimal)

Three icons only. No text labels visible by default.

| Icon | Action |
|------|--------|
| Phone icon | Opens dialer |
| Contacts icon | Opens contacts |
| Messages icon | Opens SMS inbox |

### Emergency Icon

| Element | Copy |
|---------|------|
| **Location** | Bottom-left corner, small, low opacity |
| **Visible label** | _(None. Icon only — a small circle.)_ |
| **Long-press tooltip** | End session early |

### First Session Only — Contextual Hint

| Element | Copy |
|---------|------|
| **Hint text (appears for 5s, then fades)** | Swipe up for calls and texts. |
| **Dismissal** | Fades automatically. Tap to dismiss immediately. |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **User taps an app notification (if one leaks through)** | _(No message. Redirect to water screen silently.)_ |
| **User tries to open another app** | _(Water ripples gently. No text. No popup. Just water.)_ |
| **Screen turned on mid-session** | _(Water at current level. No "You have X minutes left." No text at all.)_ |
| **Low battery during session (<15%)** | _(System low-battery warning passes through. No custom message from Wane.)_ |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Water animation** | "Water session in progress. Water is at approximately [high/mid/low] level." |
| **Phone icon** | "Phone. Open dialer." |
| **Contacts icon** | "Contacts. Search contacts." |
| **Messages icon** | "Messages. Open text messages." |
| **Emergency icon** | "End session early. Long-press to exit." |
| **First-session hint** | "Swipe up for calls and texts. Other apps are resting." |

---

## Notes
- This is the most important screen in the app. It has almost no text by design.
- The water IS the content. Adding words to this screen undermines the entire product philosophy.
- No timer. No percentage. No "halfway there." The water level is a felt sense, not a measurement.
- When the user tries to open another app, the response is the gentlest possible: a ripple. No lecture, no popup, no countdown.
- The three toolbar icons use universal symbols (handset, person, speech bubble). No labels needed.
- The emergency exit icon is deliberately understated — available but not advertised.
