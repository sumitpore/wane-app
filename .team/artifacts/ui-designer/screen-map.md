# Screen Map -- Wane

> **Updated**: 2026-04-13. Scope: 9 Wane-owned screens. Call/SMS/Contacts use native Android apps. Emergency Contacts, Water Theme Picker, and History removed from v1.

## Flow 1: Onboarding (first launch only)

| # | Screen | Purpose | Key Elements | Status |
|---|--------|---------|--------------|--------|
| 1 | Welcome | First impression IS the product | Full-screen water animation, "Tap to begin" at bottom | APPROVED |
| 2 | Duration Setup | One decision, no overwhelm | "How long do you want the water?" + slider (15m/30m/1h/2h/custom) + "You can still make calls and send texts." | APPROVED |
| 3 | Auto-Lock Intro | Introduce killer feature (opt-in) | "Start water when you lock your phone?" toggle + one-line explanation | APPROVED |

## Flow 2: Home (idle state)

| # | Screen | Purpose | Key Elements | Status |
|---|--------|---------|--------------|--------|
| 4 | Home Screen | Command center, start sessions | Duration picker (chevron up/down), "Start" CTA (80dp circle), subtle stats ribbon (streak), settings gear | APPROVED |

## Flow 3: Active Focus Session

| # | Screen | Purpose | Key Elements | Status |
|---|--------|---------|--------------|--------|
| 5 | Water Screen (Hero) | THE product experience | Full-screen water animation, NO timer, translucent bottom toolbar (Phone / Contacts / SMS -- 3 icons, launch native apps), "End" ghost text, "Wane" watermark | APPROVED |
| 6 | Emergency Exit Sheet | Deliberate friction for early exit | Bottom sheet: "Type EXIT to confirm", text input, End Session CTA, Cancel ghost button | Design using prototype system |
| 7 | Session Complete | Gentle re-entry | Blurred overlay, droplets icon, "Session Complete", "{N} minutes of focus", "Done" pill button | Design using prototype system |

## Flow 4: Basic Phone (during session) -- NATIVE ANDROID

> These screens are NOT implemented by Wane. The bottom toolbar launches native Android intents.
> Notifications from Phone, SMS, and Contacts apps pass through during focus sessions. All other notifications are blocked.

| # | Screen | Implementation | Notes |
|---|--------|---------------|-------|
| 8 | Dialer | Native Phone app | `Intent.ACTION_DIAL` |
| 9 | Contacts | Native Contacts app | `Intent.ACTION_PICK` with ContactsContract |
| 10 | SMS Inbox | Native Messages app | `Intent.ACTION_MAIN` with SMS category |
| 11 | SMS Thread | Native Messages app | `Intent.ACTION_SENDTO` with sms: URI |
| 12 | Incoming Call | Native Phone app | System-managed |
| 13 | Active Call | Native Phone app | System-managed |

## Flow 5: Settings

| # | Screen | Purpose | Key Elements | Status |
|---|--------|---------|--------------|--------|
| 14 | Settings Main | All configuration | Spring slide-up panel, grouped sections (Session, Auto-Lock, Safety, About) | APPROVED |
| 15 | Auto-Lock Settings | Configure auto-lock | Toggle, duration, grace period, skip between (time window), skip while charging. Helper text below every setting. | Design using prototype system |

## Removed from v1

| # | Screen | Reason |
|---|--------|--------|
| 16 | Emergency Contacts | Cut from v1 scope |
| 17 | Water Theme Picker | Cut from v1 scope |
| 18 | History | Cut from v1 scope |

## Summary

- **Wane-owned screens**: 9 (6 approved from prototype, 3 designed using prototype system)
- **Native Android screens**: 6 (launched via intents)
- **Removed from v1**: 3
