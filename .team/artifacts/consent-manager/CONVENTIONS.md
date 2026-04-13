# Consent Manager Conventions: Wane

**Created**: 2026-04-13  
**Last Updated**: 2026-04-13  
**Role**: Consent Manager (privacy, permissions, Play compliance, policy text)

> These conventions extend `.team/CONVENTIONS.md` (General Conventions). All sub-agents working on permissions, disclosures, data flows, or Play listing must follow this file **and** the project-wide file. Where **Emergency safety** (§8) conflicts with any other rule, **§8 wins**.

---

## 0. Alignment with project sources

| Source | Use |
|--------|-----|
| `.team/CONVENTIONS.md` | Language, tone, banned words in marketing/brand copy, accessibility baselines |
| `.team/PROJECT.md` | Privacy requirements, emergency rules, functional scope |
| `.team/DESIGN.md` | Visual and interaction patterns for consent and settings UI |
| `.team/artifacts/consent-manager/compliance-report.md` | Detailed permission audit, Play policy citations, legal risk flags |

**Compliance report is not optional background:** engineering choices (especially SMS/Call Log minimization, Accessibility disclosure, and SDK audit) must stay consistent with that document unless formally superseded after legal review.

---

## 1. Privacy-by-default

These rules ensure **no user data leaves the device** unless the user explicitly enables an exception listed below.

### 1.1 Default posture

- **No network transmission** of user content, telemetry, or identifiers in v1 except: (a) **Google Play Billing** for purchases (handled by Google; disclose in policy and Data safety), and (b) **optional analytics** only if/when implemented with **separate opt-in** (default **off**; SDKs must not initialize before consent).
- **No ads**, no ad SDKs, no undisclosed third-party data collection.
- **No cloud backup of app-private data** as a product feature unless explicitly designed, disclosed, and consented; prefer **local-only** storage (Room, DataStore, app storage).
- **AccessibilityService** and **NotificationListenerService** must not log captured events or notification content to files for analytics, crash reporting, or remote upload. Debug builds: **no** shipping accessibility payloads to external services.

### 1.2 Implementation checks (developer-facing)

- Manifest and dependency review: every `INTERNET` use and every SDK must map to a **documented** purpose and Data safety row.
- **Pre-consent**: do not initialize analytics, crash SDKs that phone home with PII, or any network library that sends user data until the user has opted in (if analytics exist).
- **ProGuard/R8 + release builds**: verify no accidental reflection-based logging of sensitive APIs.

### 1.3 User-facing honesty

- Disclosures must state what is **observed on device** (e.g., foreground app for redirection, notification content for filtering) and that **it is not uploaded** when that is true — **do not** imply server-side processing if none exists.

---

## 2. Permission requests: how and when

Follow **progressive disclosure**: explain **immediately before** each request; **never** treat Back or dismiss as consent; **never** bundle unrelated consents on one screen.

### 2.1 Sequencing principles

1. Deliver **value first** (onboarding, home) before routing users to system settings.
2. Request **only when** the feature that needs the permission is **about to be used** or **clearly imminent** (e.g., Contacts when opening contacts or emergency setup — not on app launch for all permissions at once).
3. **Standalone prominent disclosures** for **AccessibilityService** and **NotificationListenerService** — **separate** screens, separate from each other and from generic onboarding.
4. Deep-link to the **correct** system settings screen; after return, show **clear** “enabled / still needed” states.

### 2.2 Permission matrix (Wane-relevant)

| Capability | Permission / capability | When to ask | Notes |
|------------|-------------------------|-------------|--------|
| **Accessibility (app redirection)** | `BIND_ACCESSIBILITY_SERVICE` (AccessibilityService enabled in system UI) | Before first session that needs redirection; after standalone in-app disclosure + affirmative consent | **Do not** set `isAccessibilityTool="true"` unless primary purpose is disability access per Google. Narrowest event types and packages. See Play policy §5. |
| **Notification filtering** | `BIND_NOTIFICATION_LISTENER_SERVICE` (Notification access in system UI) | When user enables focus features that filter notifications; separate disclosure from Accessibility | No logging full notification text to disk or analytics. |
| **Foreground session / status** | `POST_NOTIFICATIONS` (Android 13+) | Before showing non-exempt notifications; explain FGS / persistent notification if applicable | Pair with honest copy about ongoing notification during focus. |
| **Contacts (search, emergency)** | `READ_CONTACTS` (runtime) | When user opens Contacts during session or configures emergency contacts | Minimal columns via `ContactsContract`; document “not uploaded.” |
| **Place calls from app** | `CALL_PHONE` | Only when user initiates a call action; alternative: `ACTION_DIAL` without permission (weaker UX, lower burden) | Never auto-dial. |
| **Phone state / caller awareness** | `READ_PHONE_STATE` / `READ_PHONE_NUMBERS` as needed | Only if required for incoming UI or breakthrough; justify minimally | Align with Data safety and policy. |
| **SMS** | `SEND_SMS`, `READ_SMS`, group as declared | When user opens SMS UI during session | **High Play risk** if not default SMS handler — **legal/product strategy required** per compliance report; prefer minimal permission patterns and document exceptions. |
| **Repeated caller (3× / 5 min)** | Prefer **no** `READ_CALL_LOG` if engineering can use **NotificationListener** + telephony callbacks | Only if unavoidable after review | **Permissions Declaration Form** may apply; see compliance report. |
| **DND / interruption** | `ACCESS_NOTIFICATION_POLICY` or equivalent | When user enables a feature that changes interruption rules | Restore prior state on session end where applicable. |
| **Boot** | `RECEIVE_BOOT_COMPLETED` | No runtime prompt; disclose in privacy policy | Reschedule local work only; no tracking. |
| **Foreground service** | FGS types + manifest | Disclose persistent notification in onboarding/settings | Use accurate FGS type; `specialUse` requires Play declaration. |
| **Billing** | `com.android.vending.BILLING` | Implicit via Play | Disclose Google as processor in policy. |
| **Network (optional)** | `INTERNET` | Only if Billing/analytics/opt-in features exist | Gate analytics behind opt-in. |

### 2.3 If denied or “Don’t ask again”

- **Degrade gracefully** with clear copy on what no longer works (without banned brand words — e.g., “Without this access, other apps won’t return you to the water during a session.”).
- Provide **retry** from Settings (**Permission hub** — see §3).
- **Never** fake system dialogs or imply Google endorsement.

### 2.4 Google Play Console

- Complete **Data safety** to match **actual** behavior and privacy policy.
- File **Accessibility declaration** and provide **review video** for the disclosure path when required for non–accessibility-tool use.
- Submit **Permissions Declaration Form** for any **restricted** permission groups (SMS/Call Log) per current Play rules — **before** assuming approval.

---

## 3. Consent UI patterns (DESIGN.md)

Consent screens are **part of the product**, not legal boilerplate: they must **look and feel like Wane**.

### 3.1 Visual system

- **Backgrounds**: Use `Background Settings` / gradient stack from `DESIGN.md` (`#0F1624`, deep gradients) — consistent with settings and sheets.
- **Typography**: Headlines **Sora**; body **DM Sans**; labels **Space Grotesk** uppercase for section labels and metadata.
- **Primary CTA**: Accent Primary `#38A3DC`, full width, 16dp vertical padding, 12dp corner radius; press: scale 0.97 + translateY(-1dp).
- **Secondary**: Ghost / pill per `DESIGN.md` for “Not now” or “Learn more” where needed.
- **Touch targets**: Minimum **44dp**; **56dp** for critical actions where appropriate.
- **No emojis**; **no** banned brand words in consent copy (use accurate plain language — e.g., “which app is open” vs vague “help you focus” only).

### 3.2 Structure and interaction

- **Accessibility disclosure**: Full-screen or large modal; **scrollable** text; **checkbox** (unchecked by default) + **Continue** to affirmative consent; then **Open system settings** with deep link.
- **Notification listener disclosure**: **Separate** screen; explain metadata and content visibility for **classification/dismissal during focus**; link to system notification access settings.
- **Motion**: Spring physics (stiffness ~100, damping ~20) for sheet entry; 0.6–0.8s fades between steps.
- **Permission hub** (Settings): One row per privilege — **name**, **status** (on/off), **Fix** / **Open settings** — matches `DESIGN.md` settings row pattern (dividers, Accent Light section labels).

### 3.3 Copy tone

- Calm, non-judgmental, **accurate** (required for Play). Brand voice from `.team/CONVENTIONS.md` applies; **compliance accuracy** overrides vague marketing tone when they conflict.

---

## 4. Data retention and local storage

### 4.1 Categories (on-device)

| Category | Examples | Retention default |
|----------|----------|-------------------|
| Session / history | Duration bands, timestamps, streak | Keep until user clears app data or uninstalls; **no** surveillance-style detail. |
| Settings | Durations, auto-lock, themes, sounds | Same as above. |
| Emergency config | Designated contacts, toggles | Same; user-editable deletion in Settings. |
| Notification / accessibility handling | **Transient** processing | **Do not** retain full notification bodies or accessibility trees for analytics. |
| Repeated-caller logic | Minimal rolling window | **Short window only**; no full call history archive for analytics. |

### 4.2 Deletion

- **Uninstall**: Standard Android removal of app-private data; state in privacy policy.
- **In-app**: Provide **clear app data** / reset where appropriate for session history and settings (product decision — if offered, document in policy).
- **Exports**: Share feature (animation loop) — user-initiated only; no hidden uploads.

### 4.3 Third-party processors

- **Google Play Billing**: Purchase records per Google; disclose in privacy policy.
- **Future analytics (opt-in)**: Retention must match SDK reality and be disclosed.

---

## 5. Policy compliance: Google Play (AccessibilityService & NotificationListenerService)

### 5.1 AccessibilityService

- **Honesty**: Do **not** claim verified accessibility tool status unless the app’s **primary** purpose is disability access as Google defines it. Wane is a **focus / phone-mode** product — use **non–accessibility-tool** automation path with **full disclosure and consent**.
- **Automation**: **Deterministic, user-intended** rules (e.g., session on → redirect when foreground package not allowlisted). **No** autonomous planning/execution at scale; **no** credential harvesting.
- **Scope**: Narrowest `accessibilityEventTypes` and package filters; avoid `canRetrieveWindowContent` unless strictly necessary; **no** scraping view text beyond detection needs.
- **Console**: Accessibility declaration + **video** of disclosure flow when required.

### 5.2 NotificationListenerService

- **Purpose**: Filter/dismiss during focus; allow Phone/SMS/Contacts per product rules; emergency breakthrough per **§8**.
- **No exfiltration**: Notification payloads **must not** go to analytics, ads, or servers.
- **Android 15+**: Be aware of **sensitive notification** restrictions; do not rely on redacted channels for safety-critical logic without engineering validation.

### 5.3 Declared vs actual

- Any mismatch between manifest, runtime behavior, **Data safety**, and privacy policy is a **rejection risk**. CI should track **manifest ↔ code** and **SDK inventory**.

### 5.4 References

- Use official Help Center articles (Accessibility API, User Data, SMS/Call Log, Permissions Declaration Form, Data safety) — **refresh links** before submission; policies evolve.

---

## 6. Privacy policy: required content

The privacy policy must be a **public non-PDF URL** in Play Console and linked in-app where Google requires.

### 6.1 Must include (minimum)

- **Developer identity** and contact for privacy inquiries.
- **Data accessed, collected, used, shared** — including **on-device** processing (accessibility, notifications, contacts) and **SDKs** (Billing, future opt-in analytics).
- **Security** at a high level (local storage, no server copy for v1 core features).
- **Retention and deletion** (including uninstall; in-app clear if offered).
- **No account** / **no server-side** profile for v1 core — if true, say so clearly.
- **Permissions rationale** in plain language aligned with in-app disclosures.
- **Children**: If not child-directed, state positioning; avoid knowing collection from under-13 without parental consent. (Legal review for COPPA/Play Families if strategy changes.)

### 6.2 Regional transparency

- **GDPR-style**: Transparency and lawful basis language — **legal review** recommended for EU/EEA/UK users; DPIA may be warranted for monitoring-adjacent features.
- **CCPA/CPRA**: If no sale and no cross-context ads, state clearly; **contact** for requests if thresholds apply — **legal review**.

### 6.3 Consistency

- **Data safety** answers must **not contradict** the privacy policy or the app’s actual behavior.

---

## 7. Third-party code and libraries

### 7.1 Before adding a dependency

- **Purpose**: Required for product, or removable?
- **Data flow**: Does it send **network** data? If yes, map to **opt-in** or **explicit** disclosure (Billing is expected).
- **Manifest merge**: New permissions from transitive deps must be **noticed** and justified.
- **Play policy**: Ads, aggressive analytics, or undeclared sensitive collection are **out of scope** for v1 values unless product and policy are updated.

### 7.2 Ongoing

- **SBOM / dependency audit** at release gates (align with compliance report action items).
- **No** “debugging” to Firebase or similar with accessibility or notification payloads **ever** in production.

---

## 8. Emergency safety (non-negotiable)

These rules **override** all other conventions in this document and `.team/CONVENTIONS.md` when there is a conflict. Product and engineering **must not** ship a feature that violates them.

1. **Emergency numbers** (e.g., 911, 112, 999 as applicable in region): **NEVER** blocked, **NEVER** redirected away from the user’s ability to complete an emergency call when the OS allows it.
2. **Android Emergency SOS**: **NEVER** interfered with; **NEVER** disabled or hidden by Wane.
3. **Emergency contacts**: **Always** ring through (notifications/calls per `PROJECT.md`) — **no** “opt-out” that would silence them during focus.
4. **Repeated caller breakthrough**: Same number **3× in 5 minutes** **rings through** — implementation must preserve this **safety** behavior; permission minimization (§2) must **not** remove this outcome without a **documented** equivalent.
5. **Emergency exit**: Long-press + typed confirmation word — must remain **reachable**; do not remove or obscure as a dark pattern.
6. **Medical ID / lock screen info**: **NEVER** blocked.
7. **Crash / fall detection**: **NEVER** blocked.
8. **Copy and compliance**: Emergency-related UI copy must **match engineering reality** — **no** overclaiming; **no** implying carrier or Google endorsement.

If a Play policy interpretation or UX pattern would violate §8, **§8 wins** — escalate to product/legal for an alternative approach.

---

## 9. Self-verification (this document)

| Check | Question |
|-------|----------|
| **Specificity** | Can a developer implement features and disclosures from this file alone? **Supplement with** `compliance-report.md` for edge cases and legal escalation. |
| **Permissions** | Does this cover AccessibilityService, NotificationListenerService, Contacts, Phone, SMS group, Call Log (if used), POST_NOTIFICATIONS, DND policy, boot, FGS, Billing, and optional INTERNET? **Yes** — see §2.2; **update** when manifest exists. |
| **Emergency** | Are §8 rules clear and non-negotiable? **Yes** — they explicitly override other conventions. |

---

*End of Consent Manager conventions.*
