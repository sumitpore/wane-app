# Project: Wane

**Created**: 2026-04-10
**Last Updated**: 2026-04-20
**Status**: Active
**Version**: 0.1.0 (Internal Testing)
**App Name**: Wane (primary) | Cove (secondary)

## 1. Vision

Wane is a premium North American Style Designed Android app that transforms the smartphone into a basic phone during focus sessions. When activated, the entire screen fills with a beautiful water animation that slowly recedes over the user's chosen duration. There is NO numeric timer -- the water level is the only progress indicator. During a session, users can make calls, receive calls, search contacts, send SMS, and receive SMS. All other apps and notifications are blocked. The app is free, with revenue from premium water themes and family/enterprise plans. The design quality is award-nominated agency level -- "Jo dikhta he wo bikta he" (what looks good, sells).

## 2. Requirements

### Functional Requirements

1. **Focus Session**: User starts a session by choosing a duration. Water fills the screen and slowly recedes over that duration. No timer numbers displayed on the session screen -- the water level is the only visual progress indicator. The foreground service notification shows a MM:SS countdown for system-level awareness.
2. **Basic Phone Mode (during session)**: Make/receive calls, search contacts, send/receive SMS. All other apps blocked.
3. **App Blocking**: AccessibilityService monitors foreground app and redirects to water screen. No app launches permitted except dialer, contacts, SMS.
4. **Notification Filtering**: NotificationListenerService allows notifications from Phone, SMS, and Contacts apps through during focus sessions. All other app notifications are snoozed until the session ends.
5. **Auto-Lock Trigger**: When enabled, unlocking the phone opens the app after a configurable grace period, prompting the user to start a session. Configurable: grace period (slider), skip-between window (any time range where auto-lock doesn't activate), skip while charging. Auto-lock duration is stored in preferences but not yet used to auto-start sessions -- the user must manually start after the app opens.
6. **Session Exit**: User can leave a session early via graduated exit flow triggered by back gesture or toolbar button (option to extend 5 minutes, or type confirmation phrase to exit).
7. **Session Complete**: Water fully drains, "Session complete" fades in with a done action.
8. **Water Animation**: GPU-accelerated OpenGL ES 2.0/3.0 water shader, targeting 60fps. Procedural sine-wave surface with foam highlight line and stylized caustic blobs. Responds to device tilt (rotation vector sensor / accelerometer with low-pass smoothing). Touch causes shader-based ripples. Battery-aware LOD: caustics disabled below 15% battery. Compose Canvas gradient fallback if shader compilation fails.
9. **Water Themes**: Default water theme included
10. **Session History**: Data layer complete: Room database records all sessions with start/end times, duration, and completion status. StreakCalculator computes current streak, longest streak, total sessions, and total minutes. Settings screen displays total session count and total focus time.
11. **Settings**: Default duration display, auto-lock toggle + navigation to auto-lock configuration, session data stats (total sessions, total focus time), clear all sessions, and app version.
12. **Onboarding**: 5-step onboarding flow: (1) Welcome with logo and brand copy, (2) Default duration picker, (3) Auto-lock introduction toggle, (4) Notification listener permission prompt, (5) Accessibility service permission prompt. No account creation, no email, no tutorial.

### Non-Functional Requirements

- **Performance**: Water animation at 60fps on mid-range Android devices (Snapdragon 600-series and above). Battery impact < 5% per hour of active session. OpenGL ES 3.0 optional (declared with `required=false`); falls back to ES 2.0.
- **Battery Awareness**: Rendering LOD automatically reduces when battery is below 15% (caustics disabled). Foreground service uses specialUse type with low-importance notification channel.
- **Security**: All data stored locally on device. No network calls except optional analytics (opt-in). No user data collection. No ads.
- **Accessibility**: Minimum 44px touch targets. High contrast mode support. Screen reader announcements for session start/end.
- **Compliance**: Google Play AccessibilityService policy compliance. Play Store privacy policy required. GDPR-ready (even though no data is collected -- privacy policy must state this explicitly).
- **App Size**: < 30MB installed. Water animation assets optimized.

## 3. Scope

### Implemented (v0.1.0)

- Android app (Kotlin + Jetpack Compose, minSdk 28, targetSdk 36)
- Water animation engine (OpenGL ES 2.0/3.0 with Compose Canvas fallback)
- Basic phone mode during session (dialer, contacts, SMS via allowlisting + intents)
- App blocking (AccessibilityService redirect to water screen)
- Notification filtering (NotificationListenerService snooze/unsnooze)
- Auto-lock trigger (grace period, skip window, skip while charging)
- 5-step onboarding (welcome, duration, auto-lock, notification permission, accessibility permission)
- Session data persistence (Room + DataStore) with streak calculation
- Settings (duration, auto-lock config, session stats, clear data)
- Graduated session exit (extend 5 min or type phrase to leave)
- CI/CD (GitHub Actions: lint, build, test, release to Play Store internal track)
- Code quality (ktlint + Detekt with baselines)
- Release infrastructure (ProGuard/R8, signed builds, Play Store upload workflow)

### Out of Scope

- iOS version (future)
- Server/backend infrastructure (app is fully local)
- Social features (no multiplayer, no leaderboards)
- Usage analytics dashboard for the user (we do not track)
- Family plan features (v2)
- Enterprise features (v2)
- Smartwatch companion app (future)

## 4. Target Users

Broad audience -- anyone who feels they check their phone too much. Four primary personas (full details in `.team/artifacts/business/discovery.md`):

1. **Meera (22-30)**: Doom-scroller, freelancer, 120+ daily unlocks. Wants something beautiful to replace the scroll.
2. **Arjun (30-42)**: After-hours professional, engineering manager, checks Slack after dinner. Wants a clean break between work and home.
3. **Priya (28-40)**: Present parent, part-time UX researcher, picks up phone during playtime. Wants to be present with her kids.
4. **Kabir (16-24)**: Distracted student, 7-hour daily screen time. Wants something aesthetic enough to show friends without embarrassment.

## 5. Constraints

- **Technology**: Android only (v1). Kotlin + Jetpack Compose (Material 3). Navigation3 for routing. Hilt for dependency injection. Room + SQLite Bundled for persistence, DataStore Preferences for user settings. OpenGL ES 2.0/3.0 for water animation. AccessibilityService for app blocking. minSdk 28 (Android 9), compileSdk/targetSdk 36. App version: 0.1.0.
- **Monetization**: Free core. Revenue from premium themes only (v1). No ads, no user data sales, no feature gates on core functionality.
- **Design Quality**: Award-nominated agency level. The water animation is the product's billboard. Every pixel matters.
- **Brand**: No words: "addiction", "limit", "block", "detox", "digital", "wellbeing" in any user-facing copy.
- **Privacy**: All data local. No server. No analytics without explicit opt-in.

## 6. Success Criteria

1. Install-to-first-session rate: 55% (Month 1), 60% (Month 6)
2. Average session duration: 25 min (Month 1), 35 min (Month 6)
3. Session completion rate: 70% (Month 1), 80% (Month 6)
4. D7 retention: 30% (Month 1), 40% (Month 6)
5. D30 retention: 15% (Month 1), 25% (Month 6)
6. Auto-lock adoption rate: 35% by Month 3
7. Organic share rate: 8% by Month 3
8. NPS: 50+

Anti-metrics (NOT tracked as KPIs): total screen time reduced, number of apps blocked, "failures" (early exits framed as failures).

## 7. Content Strategy

- **Brand voice**: Calm, non-judgmental, understated, never preachy. The app is a companion, not a warden.
- **What the app says**: "Let's start with some water." / "How long do you want the water?" / "The water's gone. Welcome back." / "Three days of water."
- **What the app NEVER says**: "Great job!", screen time stats, anything with the word "addiction", "Stay strong!", competitive comparisons to other users.
- **Emotional register**: Curious on first open -> small release when starting -> calm during session -> gentle re-entry when ending -> subtle shift in phone relationship over weeks.

## 8. Business Context

### Value Proposition
"Your phone becomes a quiet lake. Calls work. Everything else waits."

The app is a **Transformer** -- not a tracker, not a blocker, not a guilt-tripper. It replaces the entire dopamine-seeking interface with something that rewards stillness.

### Competitive Position
Every competitor falls into trackers (Digital Wellbeing, Opal), blockers (Freedom, AppBlock), or guilt-trippers (Forest, Flora). Wane is none of these. The phone becomes something else entirely during a session. No data, no walls, no guilt. Just water.

### Monetization
Free forever core. Revenue from premium water themes ($1.99-$5.99), seasonal drops, artist collaborations, ambient sound packs, family plans ($4.99/month), enterprise wellness licenses (per-seat). Never monetize: ads, user data, core functionality, guilt-based upsells.

Full discovery analysis: `.team/artifacts/business/discovery.md`
