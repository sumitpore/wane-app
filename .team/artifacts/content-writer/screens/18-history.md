# Screen 18 — History

**Flow:** History (via bottom nav)
**Purpose:** Non-judgmental reflection. Facts, not performance reviews.

---

## Content

### Header

| Element | Copy |
|---------|------|
| **Screen title** | History |

---

### Streak Counter

| Scenario | Copy |
|----------|------|
| **No sessions yet** | _(Don't show streak section.)_ |
| **1 day** | One day of water. |
| **2 days** | Two days of water. |
| **3 days** | Three days of water. |
| **7 days** | Seven days of water. |
| **14 days** | Fourteen days of water. |
| **30 days** | Thirty days of water. |
| **Pattern** | [Written-out number] days of water. |
| **100+ days** | [Numeral] days of water. _(e.g., "142 days of water." — written-out numbers get unwieldy past 100.)_ |

---

### Weekly Visualization

| Element | Copy |
|---------|------|
| **Type** | Abstract bar chart — one bar per day, 7 days visible |
| **Bar height represents** | Session duration band (short / medium / long) — not exact minutes |
| **Day labels** | Mon · Tue · Wed · Thu · Fri · Sat · Sun |
| **No data for a day** | _(Empty space. No bar. No "0" label.)_ |
| **Axis labels** | _(None. No Y-axis numbers. The bars are relative, not measured.)_ |

---

### Session Log

| Element | Format |
|---------|--------|
| **Date** | Today / Yesterday / [Day, Month Date] |
| **Duration band** | Short · Medium · Long |
| **Time of day** | Morning / Afternoon / Evening / Night |
| **Auto-lock indicator** | _(Small icon if session was auto-started. No text label.)_ |

#### Duration Band Definitions (internal — not shown to user)

| Band | Range |
|------|-------|
| Short | 5–20 min |
| Medium | 21–60 min |
| Long | 61+ min |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **No sessions ever** | Your water will appear here. |
| **Streak broken (gap in days)** | _(Reset streak counter silently. No "You broke your streak!" message. Just show the new count.)_ |
| **Streak restarts after break** | One day of water. _(Start fresh. No reference to the previous streak.)_ |
| **Scrolling past available history** | _(History keeps what's on device. No "Load more" — scroll naturally ends.)_ |
| **Only one session today** | _(Show it. No "Just 1 session today" commentary.)_ |
| **Multiple sessions in one day** | _(Show each as a separate log entry.)_ |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "History. Your recent water sessions." |
| **Streak** | "[N] days of water." |
| **Weekly chart** | "This week's water. [Day]: [short/medium/long session] or no session." |
| **Session log entry** | "[Date]. [Duration band] session. [Time of day]." |
| **Auto-lock indicator** | "Started automatically." |
| **Empty state** | "Your water will appear here." |
| **Day with no bar** | "[Day]: No session." |

---

## Notes
- The streak is stated in words, not numbers. "Seven days of water." not "7-day streak!" Words slow the reader down — they feel the count instead of scanning it.
- Duration bands (Short / Medium / Long) instead of exact minutes. "You had a long session this afternoon." carries a different feeling than "You had a 94-minute session at 3:47 PM."
- Time of day (Morning / Afternoon / Evening / Night) instead of exact timestamps. Precision implies surveillance.
- The weekly visualization has no Y-axis, no numbers, no gridlines. It is an impression, not a report.
- Empty days are simply empty. No zero bars, no "missed day" indicators, no broken-streak markers.
- "Your water will appear here." for first-time empty state. Warm, forward-looking, not prescriptive.
- The streak resets silently. No punishment. No attention drawn to the gap. Just a fresh count.
