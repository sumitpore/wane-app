# Project: Wane

**Created**: 2026-04-10
**Status**: Active
**App Name**: Wane (primary) | Cove (secondary)

## 1. Vision

Wane is a premium North American Style Designed Android app that transforms the smartphone into a basic phone during focus sessions. When activated, the entire screen fills with a beautiful water animation that slowly recedes over the user's chosen duration. There is NO numeric timer -- the water level is the only progress indicator. During a session, users can make calls, receive calls, search contacts, send SMS, and receive SMS. All other apps and notifications are blocked. The app is free, with revenue from premium water themes and family/enterprise plans. The design quality is award-nominated agency level -- "Jo dikhta he wo bikta he" (what looks good, sells).

## 2. Requirements

### Functional Requirements

1. **Focus Session**: User starts a session by choosing a duration. Water fills the screen and slowly recedes over that duration. No timer numbers displayed anywhere.
2. **Basic Phone Mode (during session)**: Make/receive calls, search contacts, send/receive SMS. All other apps blocked.
3. **App Blocking**: AccessibilityService monitors foreground app and redirects to water screen. No app launches permitted except dialer, contacts, SMS.
4. **Notification Filtering**: NotificationListenerService allows notifications from Phone, SMS, and Contacts apps through during focus sessions. All other app notifications are blocked. Emergency contacts always ring through regardless.
5. **Auto-Lock Trigger**: When enabled, locking the phone automatically starts a focus session for a pre-set duration. Configurable: duration, grace period (5s/10s/15s after unlock before re-engaging), skip-between window (any time range where auto-lock doesn't activate), skip while charging.
6. **Emergency Safety (non-negotiable)**:
   - Emergency numbers (911, 112, 999) NEVER blocked
   - Android Emergency SOS NEVER interfered with
   - Emergency contacts always ring through
   - Repeated caller breakthrough: same number 3x in 5 min = rings through
   - Emergency exit via deliberate long-press + type confirmation word
   - Medical ID / lock screen info never blocked
   - Crash/fall detection never blocked
7. **Session Complete**: Water fully drains, gentle haptic, "Welcome back." fades in for 2 seconds.
8. **Water Animation**: GPU-accelerated, 60fps, realistic fluid dynamics with subtle light refraction. Responds to device tilt (gyroscope). Touch causes ripples. Daily light-angle variations.
9. **Water Themes**: Default theme free forever. Premium themes purchasable (Monsoon, Glacier, Koi, Bioluminescence, seasonal themes).
10. **Session History**: Simple, non-judgmental. No surveillance-style tracking. Streak counter ("Seven days of water."), basic session log.
11. **Settings**: Duration picker, auto-lock toggle + configuration, emergency contacts, water theme, ambient sounds, haptic feedback.
12. **Onboarding**: 3 screens maximum. First screen: water animation + "Tap to begin." No account creation, no email, no tutorial.
13. **Share Feature**: 5-second animation loop export for social sharing. No branding watermark.

### Non-Functional Requirements

- **Performance**: Water animation at 60fps on mid-range Android devices (Snapdragon 600-series and above). Battery impact < 5% per hour of active session.
- **Security**: All data stored locally on device. No network calls except optional analytics (opt-in). No user data collection. No ads.
- **Accessibility**: Minimum 44px touch targets. High contrast mode support. Screen reader announcements for session start/end.
- **Compliance**: Google Play AccessibilityService policy compliance. Play Store privacy policy required. GDPR-ready (even though no data is collected -- privacy policy must state this explicitly).
- **App Size**: < 30MB installed. Water animation assets optimized.

## 3. Scope

### In Scope

- Android app (Kotlin + Jetpack Compose)
- Water animation engine (OpenGL ES or Compose Canvas)
- Basic phone mode (dialer, contacts, SMS)
- App/notification blocking (AccessibilityService + NotificationListenerService)
- Auto-lock trigger feature
- Emergency safety system
- Session history with streaks
- Premium water themes (in-app purchase)
- Settings (duration, auto-lock, emergency, appearance)
- 3-screen onboarding
- Home screen widget (one-tap start)
- Share feature (animation loop export)

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

- **Technology**: Android only (v1). Kotlin + Jetpack Compose. AccessibilityService for app blocking (the only viable Android approach).
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
- **Localization**: English first. Hindi, Spanish, Portuguese, Japanese planned for v1.1.

## 8. Business Context

### Value Proposition
"Your phone becomes a quiet lake. Calls work. Everything else waits."

The app is a **Transformer** -- not a tracker, not a blocker, not a guilt-tripper. It replaces the entire dopamine-seeking interface with something that rewards stillness.

### Competitive Position
Every competitor falls into trackers (Digital Wellbeing, Opal), blockers (Freedom, AppBlock), or guilt-trippers (Forest, Flora). Wane is none of these. The phone becomes something else entirely during a session. No data, no walls, no guilt. Just water.

### Monetization
Free forever core. Revenue from premium water themes ($1.99-$5.99), seasonal drops, artist collaborations, ambient sound packs, family plans ($4.99/month), enterprise wellness licenses (per-seat). Never monetize: ads, user data, core functionality, guilt-based upsells.

Full discovery analysis: `.team/artifacts/business/discovery.md`
