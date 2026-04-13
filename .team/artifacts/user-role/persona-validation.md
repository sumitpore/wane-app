# Wane — User Role Validation Report

**Method:** Code-backed walkthrough of implemented UI strings and navigation (`strings.xml`, Compose screens, `WaneNavHost`). Persona reactions are inferential from that implementation.

**Date:** 2026-04-13

---

## 1. Meera (22–30, Freelancer, Doom-scroller)

### Flow 1: First Launch → Onboarding — **PASS**

She sees a glowing orb, **Wane**, “Let your focus flow naturally,” then a large numeric duration picker (5–120 min, default 25), then auto-lock with “Stay focused, even when tempted” and copy about unlock behavior. Buttons: **Begin → Next → Start**. Feels aesthetic and Instagram-adjacent; three steps are quick.

**Notable:** Auto-lock body copy includes the phrase **“screen time”** (`auto_lock_description`), which can read like system surveillance language rather than a calm companion.

### Flow 2: Start Focus from Home — **PASS**

Home shows the app title, streak or “Begin your first session,” a prominent **Start Session** FAB, and **“X min”** under it. Matches the brief. No settings affordance on screen (see Flow 6).

### Flow 3: During Session — **WARN**

Full-screen water + bottom row: Phone, Contacts, Messages, and a small **End** control. **Contradiction vs validation spec:** the session screen shows a **large MM:SS countdown** (`TimerDisplay` / `time_remaining`), not water-only with no numbers.

**Notable:** Ending the session uses **long-press** on “End” to open the sheet—easy to miss; spec described typing EXIT in a field, which only appears after that gesture.

### Flow 4: Emergency Exit — **PASS**

Sheet: “Are you sure?”, “Type EXIT to end your session early,” field, Cancel, **End** (enabled only when input is exactly `EXIT`). No cooldown in code path described for validation.

### Flow 5: Session Complete — **WARN**

Overlay: **“Well done”**, “You focused for X minutes,” **Done**. Tone is mild praise; brand rules disallow cheerleading like “Great job!”—**“Well done”** is adjacent and may feel slightly performative for this persona.

### Flow 6: Settings — **FAIL**

`WaneNavHost` passes `onNavigateToSettings`, but **`HomeScreen` never calls it**—there is no visible entry to Settings from Home. She cannot complete the Settings / Auto-Lock flows from the UI as implemented.

**Brand:** Section title **“Blocking”** (`blocking_section`) uses the forbidden word **block** in user-facing copy.

### Persona summary (Meera)

| Criterion            | Assessment |
|----------------------|------------|
| First impression     | Strong visual hook; onboarding copy mostly calm. |
| Core experience      | Water + numbers: numbers dominate; may prefer water-only per brand story. |
| Friction             | Hidden settings; long-press to exit; “screen time” phrasing. |
| Emotional register   | Mostly understated; “Well done” / streak can feel slightly reward-y. |
| Missing pieces       | Settings entry; theme picking if promised elsewhere. |
| Brand compliance     | **FAIL** — “Blocking”; “screen time” in auto-lock copy. |
| Accessibility        | Long-press discoverability; no path to settings. |

**Flow ratings:** F1 PASS · F2 PASS · F3 WARN · F4 PASS · F5 WARN · F6 FAIL

---

## 2. Arjun (30–42, Engineering Manager)

### Flow 1 — **PASS**

Minimal steps, clear defaults (25 min), auto-lock optional off by default in state—fits “efficiency, minimal setup.”

### Flow 2 — **PASS**

Single primary action; duration visible. Good for a fast “start focus after work” habit—if he can change defaults (requires Settings—see Flow 6).

### Flow 3 — **WARN**

Precise MM:SS fits an engineering mindset but **conflicts** with the “no timer numbers” product story. Toolbar for dialer/contacts/SMS matches “calls still work.”

### Flow 4 — **PASS**

Deliberate friction (EXIT) without extra timers or shaming.

### Flow 5 — **WARN**

Completion gives minutes focused—factual, not a dashboard; still overlaps with “avoid stats” positioning if taken strictly.

### Flow 6 — **FAIL**

Cannot open Settings from Home UI. Data section in code shows **Total sessions** and **Total focus time** (hours)—useful for him but **conflicts** with brand line “no screen time stats” if interpreted broadly.

**Brand:** “Blocking” section label fails word list.

### Persona summary (Arjun)

| Criterion            | Assessment |
|----------------------|------------|
| First impression     | Quick; auto-lock explained in one screen. |
| Core experience      | Timer visibility is high; water is secondary visually. |
| Friction             | No settings from Home; long-press exit undocumented in UI. |
| Emotional register   | Neutral-to-positive; aggregate stats may feel “managerial.” |
| Missing pieces       | Obvious Settings gear; optional Slack/package nuance not in UI. |
| Brand compliance     | **FAIL** — “Blocking”; stats rows vs “no stats” stance. |
| Accessibility        | Same as above. |

**Flow ratings:** F1 PASS · F2 PASS · F3 WARN · F4 PASS · F5 WARN · F6 FAIL

---

## 3. Priya (28–40, Parent, UX Researcher)

### Flow 1 — **WARN**

“Stay focused, even when tempted” may imply **moral weakness**—slightly judgment-adjacent for a parent already sensitive to guilt. “Mindful of your screen time” doubles down on monitoring framing.

### Flow 2 — **PASS**

“Begin your first session” is gentle; streak later is a **double-edged**: motivating vs. guilt if she breaks it.

### Flow 3 — **WARN**

Constant countdown can increase **time anxiety** during kid time; water-only (per spec) would better match “presence without clock-watching.”

### Flow 4 — **PASS**

Respects autonomy; no penalty copy in strings.

### Flow 5 — **WARN**

“Well done” risks sounding like **praise for good behavior**—exactly what she wants to avoid when the child is the moral compass.

### Flow 6 — **FAIL**

Cannot reach Settings from Home. Emergency contacts row exists but is display-only in this screen—no edit flow visible in `SettingsScreen` snippet (values only).

**Brand:** “Blocking” fails; streak + aggregate totals may feel like **tracking**.

### Persona summary (Priya)

| Criterion            | Assessment |
|----------------------|------------|
| First impression     | Calm visuals; some copy leans “self-control” framing. |
| Core experience      | Numeric timer stresses time over presence. |
| Friction             | No settings; emergency contacts UX unclear. |
| Emotional register   | Mixed—UI is quiet, but streak/praise/stats pull toward metrics. |
| Missing pieces       | Non-judgmental auto-lock copy; family-friendly settings path. |
| Brand compliance     | **FAIL** — “Blocking”; tension with “no guilt / no stats.” |
| Accessibility        | Long-press; readers get countdown announced if TalkBack reads MM:SS. |

**Flow ratings:** F1 WARN · F2 PASS · F3 WARN · F4 PASS · F5 WARN · F6 FAIL

---

## 4. Kabir (16–24, Student)

### Flow 1 — **PASS**

Short onboarding; aesthetic welcome step helps social “show friends” angle.

### Flow 2 — **PASS**

Bold center button; streak number could be shareable or cringe—depends on peer group.

### Flow 3 — **WARN**

Session view is **cool water + obvious clock**—friends see a timer, not just art. Product story said water-only.

### Flow 4 — **PASS**

EXIT gate is strict; fine for users who know the trick; **discoverability** of long-press is weak for first-time users.

### Flow 5 — **WARN**

“Well done” is acceptable socially but edges toward **school report** tone.

### Flow 6 — **FAIL**

Cannot open Settings from Home; students who want sounds/haptics must have another path (none in Home).

### Persona summary (Kabir)

| Criterion            | Assessment |
|----------------------|------------|
| First impression     | Visual polish aligns with aesthetic expectations. |
| Core experience      | Timer-forward UI vs “immersive water” marketing. |
| Friction             | Exit gesture discovery; no settings. |
| Emotional register   | Mostly fine; “Blocking” in settings sounds punitive if he finds it. |
| Missing pieces       | Settings entry; optional shareable completion without stats. |
| Brand compliance     | **FAIL** — “Blocking.” |
| Accessibility        | Same navigation gaps. |

**Flow ratings:** F1 PASS · F2 PASS · F3 WARN · F4 PASS · F5 WARN · F6 FAIL

---

## Cross-Persona Analysis

### Common strengths

- Onboarding strings largely match `strings.xml` (Welcome, Duration, Auto-Lock, Begin / Next / Start).
- Emergency exit copy matches spec; EXIT confirmation is explicit.
- Session tooling (Phone, Contacts, Messages) matches “calls/SMS still available.”
- Visual direction (gradient, water, typography) supports a calm first impression.

### Common gaps

- **Settings unreachable from Home** — `onNavigateToSettings` is never invoked in `HomeScreen`.
- **Session UI shows MM:SS** — does not match “no timer numbers” validation spec.
- **Emergency exit** requires **long-press** on “End” before the EXIT field—spec implied a direct affordance.
- **Emergency contacts** appear as read-only summary in Settings; full edit flow not validated here.
- **Streak + total sessions + total focus time** — aggregate metrics may conflict with “no screen time stats” and non-surveillance positioning.

### Brand compliance check

| Rule | Status | Evidence |
|------|--------|----------|
| Avoid: addiction, limit, detox, digital, wellbeing | **PASS** | No matches in `strings.xml` for these. |
| Avoid: **block** | **FAIL** | `blocking_section` → **“Blocking”** in Settings. |
| No surveillance-style tracking / no screen time stats | **WARN** | “screen time” in auto-lock description; totals in Settings; streak. |
| No “Great job!” | **WARN** | **“Well done”** on completion (not identical, but praise-adjacent). |
| Companion, not warden | **WARN** | “Tempted,” “screen time,” “Blocking” lean disciplinary. |

---

## Recommendations

### Must-Fix

1. **Add a visible Settings entry on Home** (and wire `onNavigateToSettings`) so Flow 6 is completable.
2. **Rename the “Blocking” section** to a neutral label (e.g. “During focus” / “Auto-lock”) — remove the word **block** from user-facing UI.
3. **Align Session UI with product intent:** either remove or greatly de-emphasize MM:SS if the promise is water-only pacing, **or** update the spec/copy so “no numbers” is not promised.

### Should-Fix

4. **Rewrite auto-lock description** to drop **“screen time”** and soften “tempted” if targeting non-judgmental tone.
5. **Revisit completion title** (“Well done”) vs neutral closure (e.g. session ended + duration only).
6. **Clarify emergency exit** in-session (hint text or icon) so long-press is not the only implicit cue.
7. **Reconcile Settings aggregates** (total sessions / focus hours) with brand “no stats” stance — hide, opt-in, or reframe.

### Nice-to-Have

8. **Default duration editing** from Home without deep settings (for Arjun).
9. **Theming / water variants** surfaced in UI for Meera/Kabir if product roadmap includes it.
10. **TalkBack labels** audit for long-press “End” and toolbar.

---

## Overall Verdict: **CONDITIONAL PASS**

Onboarding and core session/exit flows are largely implemented and string-aligned, but **Settings is not reachable from Home**, the **session screen shows a prominent numeric timer** (conflict with the stated water-only experience), and **brand rules are violated** by the **“Blocking”** section label, with additional **WARN** items around praise (“Well done”), **“screen time”** phrasing, and aggregate metrics.

---

**Artifact path:** `.team/artifacts/user-role/persona-validation.md`
