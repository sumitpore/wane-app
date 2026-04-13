# Wane — Privacy & Compliance Report (Consent Manager)

**Document version**: 1.0  
**Date**: 2026-04-10  
**Scope**: Requirements and screen-flow review per `.team/PROJECT.md` and `.team/artifacts/ui-designer/screen-map.md`  
**Codebase note**: This repository currently contains planning artifacts only (no `AndroidManifest.xml` or Kotlin sources). Permission and data-flow conclusions below are **requirements-derived**. Before release, re-audit against the shipping manifest, ProGuard/R8 mappings, and all integrated SDKs (billing, analytics, crash reporting).

---

## Executive summary

Wane is a **high-privilege** Android app by design: it uses **AccessibilityService** for app interception, **NotificationListenerService** for notification filtering, telephony and SMS permissions for “basic phone mode,” and likely a **foreground service** plus **boot completion** to keep policies consistent across restarts. These choices are **justifiable relative to the product vision** but create **material Google Play review risk** unless disclosures, Console declarations, and **permission minimization** are executed precisely.

**Highest-risk compliance items for v1**

1. **AccessibilityService** — Wane is **not** a “verified accessibility tool” in Google’s sense (primary purpose: disability access). It therefore needs **in-app prominent disclosure + affirmative consent**, Play Console **accessibility declaration**, and a **review video** that shows the full disclosure flow. Mis-declaring `isAccessibilityTool="true"` would be **policy fraud** and can trigger account termination.  
2. **SMS and Call Log permission groups** — Play policy **heavily restricts** these unless the app is the **registered default SMS, Phone, or Assistant handler**, or the app qualifies for a **documented exception** and passes **Permissions Declaration Form** review. An in-app dialer/SMS experience during focus sessions may **not** automatically qualify; architectural alternatives or a formal exception narrative may be required.  
3. **Declared vs. actual use** — Any mismatch between manifest permissions, runtime behavior, Data safety answers, and privacy policy text is a common rejection cause.

Primary policy references used in this report:

- [Use of the AccessibilityService API](https://support.google.com/googleplay/android-developer/answer/10964491)  
- [User Data](https://support.google.com/googleplay/android-developer/answer/10144311) (prominent disclosure, privacy policy, Data safety alignment)  
- [Declare permissions for your app / Permissions Declaration Form](https://support.google.com/googleplay/android-developer/answer/9214102)  
- [Use of SMS or Call Log permission groups](https://support.google.com/googleplay/android-developer/answer/9047303)  
- [Provide information for Google Play’s Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469)

---

## 1. Android permissions audit

Legend: **Core** = aligned with a documented functional requirement in `PROJECT.md`. **Scope** = minimum reasonable surface. **Play** = Google Play–specific expectation. **Alt** = mitigations if any exist.

### 1.1 `BIND_ACCESSIBILITY_SERVICE` (AccessibilityService)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes.** Req. #3: monitor foreground app and redirect to the water UI; this is the standard viable approach on Android for cross-app interception. |
| **Minimum scope** | Service configured with the **narrowest** `accessibilityEventTypes`, `packageNames` (if feasible), and **no** broad `canRetrieveWindowContent` unless strictly required for detection/redirection. Avoid reading view text beyond what is necessary. Do not perform unrelated automation. |
| **Google Play policy** | Apps using the API for **automation** must ensure actions are for a **narrow, clearly understood purpose**. **Autonomous** initiation/planning/execution of actions is **prohibited** except for verified accessibility tools. **Deterministic, rule-based** automation (e.g., “if foreground package ∉ allowlist → show overlay”) is explicitly described as **not prohibited** by that rule. Apps **not** eligible for `isAccessibilityTool` must meet **prominent disclosure and consent** in line with the [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311), **separate** from other disclosures. Targeting API 31+ with an AccessibilityService requires the Play Console **accessibility declaration** and, for non-tools, a **video** showing the disclosure path. Source: [AccessibilityService API policy](https://support.google.com/googleplay/android-developer/answer/10964491). |
| **Disclosure** | In-app, **before** enabling the service: what is observed (e.g., **which app is in foreground / window changes**), **why**, **that it is not uploaded** (if true), and that the user can disable it in system settings. Must not rely on privacy policy alone. |
| **Alternatives** | **None** for equivalent global blocking without OEM APIs. Usage Stats (`PACKAGE_USAGE_STATS`) do not replace interception; overlays still need accessibility or other privileged patterns. Parental/device-owner solutions are out of scope for a consumer app. |

**Wane-specific caution:** Do **not** set `isAccessibilityTool="true"` unless the app’s **primary purpose** is disability support as defined by Google (screen readers, switch access, etc.). Wane is a **focus / phone-mode transformer**; it should be declared as **non–accessibility-tool** automation with full disclosure.

---

### 1.2 `BIND_NOTIFICATION_LISTENER_SERVICE` (NotificationListenerService)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes.** Req. #4: filter/dismiss non-essential notifications during sessions; allow calls/SMS per emergency rules. |
| **Minimum scope** | Post fewer events to your logic; **do not log** full notification text to disk or analytics. On Android 15+, be aware of **sensitive notification** restrictions for untrusted listeners; design breakthrough rules not to depend on OTP/PII in notifications where the OS redacts content. |
| **Google Play policy** | Treated as **high sensitivity** (can expose message content, auth codes, personal data). Must align with [User Data](https://support.google.com/googleplay/android-developer/answer/10144311): **limited use**, no undisclosed collection/sharing, prominent disclosure when access may exceed user expectations. Third-party SDKs in the listener path must be audited. |
| **Disclosure** | Explain that the app **sees notification metadata and content** while enabled, **only to classify/dismiss** during focus, and whether anything is **stored or sent off-device** (should be **neither** for Wane v1). Link user to **Notification listener** system settings. |
| **Alternatives** | **DND** alone cannot selectively allow only certain senders with full fidelity across OEMs. **Bubble/metadata-only** approaches are weaker. Listener is the usual pattern for “filter except X.” |

---

### 1.3 Do Not Disturb / notification policy access (`ACCESS_NOTIFICATION_POLICY` or equivalent)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Supporting.** Quieter device during focus; may pair with notification filtering. |
| **Minimum scope** | Request **only** the minimal policy changes you apply (e.g., **alarms only** vs **total silence**); restore prior state on session end. |
| **Google Play policy** | Generally allowed when **transparent** and tied to **user-facing** feature; still subject to [User Data](https://support.google.com/googleplay/android-developer/answer/10144311) and **deceptive behavior** rules (no silent hijacking of ringer state). |
| **Disclosure** | Explain that focus mode may **change interruption settings** and that the user can revoke in settings. |
| **Alternatives** | Rely on **NotificationListener** only — may suffice for many cases but not identical to DND semantics. |

---

### 1.4 `RECEIVE_BOOT_COMPLETED`

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Supporting.** Req. #5 / architecture: restore auto-lock or scheduled behavior after reboot. |
| **Minimum scope** | Receiver should **only** reschedule alarms/workers or restore **local** flags; avoid starting heavy work or tracking. |
| **Google Play policy** | Routine; subject to **background execution** limits and **misleading** / **spyware** policies if abused. |
| **Disclosure** | Privacy policy: “runs at boot to restore your chosen settings.” |
| **Alternatives** | `WorkManager` with constraints; still often paired with boot for reliability on some OEMs. |

---

### 1.5 `FOREGROUND_SERVICE` (+ Android 14+ **foreground service types**)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes** for reliable session integrity while user switches apps or under memory pressure — aligned with “focus session” and blocking stack. |
| **Minimum scope** | Use the **most accurate** FGS type Google allows (often **`specialUse`** for novel cases, which requires **Play declaration**). Provide a **clear, honest ongoing notification** (user-visible). Avoid abusing `mediaPlayback` etc. |
| **Google Play policy** | Misuse of FGS types or **undeclared** `specialUse` is enforced. Review [Permissions and APIs that access sensitive information](https://support.google.com/googleplay/android-developer/answer/9888170) and Android 14+ FGS documentation when implementing. |
| **Disclosure** | Onboarding/settings: user understands a **persistent notification** may appear during focus. |
| **Alternatives** | No perfect substitute for **reliable** long-running gating; `WorkManager` is not a replacement for active session enforcement. |

---

### 1.6 `READ_CONTACTS`

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes.** Screen map: Contacts during session; emergency contacts feature. |
| **Minimum scope** | Use **ContactsContract** projections with **minimal columns**; no bulk export; no sync to servers (v1: none). |
| **Google Play policy** | [User Data](https://support.google.com/googleplay/android-developer/answer/10144311): contacts are **personal and sensitive**; require **runtime permission**, disclosure immediately **before** request, and policy text. |
| **Disclosure** | “To show people you can call or message during a session and to let you pick emergency contacts, Wane reads **names and phone numbers** on your device. **Contacts are not uploaded.**” |
| **Alternatives** | **System contact picker** (`ACTION_PICK` / `PickContact`) for **one-off** selection reduces ongoing read scope but **does not** replace searchable in-app list UX; consider hybrid (picker for emergency, limited read only when session starts) if minimizing risk. |

---

### 1.7 `READ_CALL_LOG` (repeated caller breakthrough)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes** per Req. #6 (same number 3× in 5 minutes). |
| **Minimum scope** | Read **only** what is needed to count recent inbound events **locally** in a short time window; **no** full log retention or analytics. |
| **Google Play policy** | **Critical.** [SMS and Call Log permission groups](https://support.google.com/googleplay/android-developer/answer/9047303): default **Phone/SMS/Assistant** handler path **or** a **listed exception** with **Permissions Declaration Form** approval. “Repeated caller safety” is **not** named verbatim; **caller ID / spam**, **device automation**, or other buckets may be argued **only with legal/product review** — approval is **not guaranteed**. |
| **Disclosure** | Prominent: why call log is read, **time window**, **local-only** processing, **stopped when permission revoked**. |
| **Alternatives** | **Strongly preferred for Play risk:** infer repeats via **NotificationListener** (incoming call notifications) or **telephony callbacks** where available, **without** `READ_CALL_LOG`, if technically sufficient on target API/OEMs. Or guide user to set **default Phone app** only if product accepts that UX. Use **`ACTION_DIAL`** flows where possible to avoid `CALL_PHONE` when acceptable. |

---

### 1.8 `CALL_PHONE`

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes** for one-tap call from in-app dialer (screen map). |
| **Minimum scope** | Only place calls **on explicit user tap**; never auto-dial. |
| **Google Play policy** | Sensitive; must match **disclosed** behavior. Not in the same restricted **declaration bucket** as SMS/Call Log **group**, but still **sensitive**. |
| **Disclosure** | “Tap to call” is expected; still mention in privacy policy. |
| **Alternatives** | `ACTION_DIAL` **without** `CALL_PHONE` — opens dialer for user to press call; worse UX but **lower** permission burden. |

---

### 1.9 `SEND_SMS` / `READ_SMS` (and related SMS group members if declared)

| Question | Assessment |
|----------|------------|
| **Core feature?** | **Yes** for SMS inbox/thread during session. |
| **Minimum scope** | If the product can use **SMS DB** only while default SMS app, scope to session UI; **no** mining. |
| **Google Play policy** | **Critical.** Same [SMS / Call Log policy](https://support.google.com/googleplay/android-developer/answer/9047303): default **SMS handler** is the **primary** permitted path for broad SMS access. Exceptions exist (e.g., **SMS Retriever** for OTP is an **alternative** to `READ_SMS` for verification — not applicable here). In-app SMS client **without** being default SMS is **high rejection risk**. |
| **Disclosure** | If default handler: explain switching default; data stays on device. If exception path: **exact** scope per approved use case. |
| **Alternatives** | **Compose via `ACTION_SENDTO` / `SmsManager` with user-sent intent** patterns may reduce permissions depending on API level, but **full inbox** generally implies default SMS or strong justification. Consider **deferring inbox** to system Messages via deep link when session allows “break glass” (may conflict with blocking vision — product tradeoff). |

---

### 1.10 Additional permissions likely needed (verify at implementation)

| Permission / capability | Why Wane may need it | Notes |
|-------------------------|---------------------|--------|
| `POST_NOTIFICATIONS` (Android 13+) | FGS notification, session alerts | Runtime permission; disclosure before ask. |
| `READ_PHONE_STATE` / `READ_PHONE_NUMBERS` | Incoming call UI, possibly breakthrough without full call log | Still sensitive; minimize; follow User Data policy. |
| `USE_FULL_SCREEN_INTENT` | Incoming call UI when app is call UI | Restricted on newer APIs; declare and justify. |
| `VIBRATE` | Haptics for session complete | Normal permission. |
| `INTERNET` | Billing, **optional** analytics | If present, Data safety + SDK audit required. |
| `com.android.vending.BILLING` | Premium themes | No user data by itself; still third-party (Google Play Billing). |

---

## 2. Google Play policy compliance

### 2.1 AccessibilityService

Google **permits** many uses of AccessibilityService but distinguishes:

- **Verified accessibility tools** (`isAccessibilityTool`): exempt from the **prominent disclosure** requirement for that API; must truly serve **disability access** as **primary purpose**. Examples given explicitly **exclude** “antivirus, automation tools, assistants, monitoring apps, cleaners, password managers, and launchers.” See [policy text](https://support.google.com/googleplay/android-developer/answer/10964491).

- **Automation (non-tool):** Must not enable **autonomous** planning/execution of actions at scale; **deterministic rules** tied to user intent (focus session toggled by user) are the compliant framing. Must implement **standalone prominent disclosure** and **affirmative consent** per [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311).

**What reviewers look for**

- Video shows **exact** path to disclosure (not buried only in `accessibility_service_description`).  
- No **undeclared** collection of content via accessibility.  
- No behavior that resembles **spyware** or **credential harvesting** ([Spyware policy](https://support.google.com/googleplay/android-developer/answer/9888380) / related).  
- **Honesty** in Console declarations about **whether** personal/sensitive data is collected via accessibility capabilities.

### 2.2 NotificationListenerService

- User must enable in **system** notification access UI; explain **why** and **retention**.  
- Ensure **no** exfiltration of notification payloads to analytics or servers.  
- Align **Data safety** with reality (often “not collected” if nothing leaves device — but **access** may still need explanation in privacy policy).

### 2.3 Play Store listing

Per [User Data](https://support.google.com/googleplay/android-developer/answer/10144311):

- **Privacy policy** URL in Console **and** in-app (even if no server-side collection).  
- **Data safety** completed accurately; must be **consistent** with policy text.  
- For Accessibility + restricted permissions: expect **extended review** ([Permissions Declaration Form](https://support.google.com/googleplay/android-developer/answer/9214102) notes multi-week possibility for sensitive permissions).

### 2.4 Declared vs. actual usage

Common rejection patterns:

- Manifest includes permissions **unused** in build.  
- SDK adds **unexpected** collectors (crash, ads, analytics).  
- Accessibility events logged to **Firebase** “for debugging.”

**Action:** CI check: **manifest ↔ API usage matrix**; dependency **SBOM** review; disable third-party collection unless Data safety updated.

---

## 3. Data handling

### 3.1 What data does Wane collect or process?

Per `PROJECT.md` and screen map (intended design):

| Data category | Examples | Typical legal character |
|---------------|----------|-------------------------|
| **Session / history** | Duration bands, streak counts, timestamps | Likely **personal data** (behavioral) on device |
| **Settings** | Durations, auto-lock, quiet hours, theme, sound | Preferences |
| **Emergency configuration** | Designated contacts, repeated-caller toggle | Sensitive (contact relationships) |
| **Contacts (processing)** | Read for UI | Sensitive |
| **Call / SMS metadata** | Breakthrough logic, caller ID display | Sensitive |
| **Notifications** | Transient access for filtering | Potentially highly sensitive content |
| **Accessibility-derived data** | Foreground package, UI events as implemented | Potentially sensitive |

### 3.2 Where is it stored?

**Target architecture (per requirements):** **local only** — e.g. **Room** + **DataStore** as stated in `PROJECT.md`. No backend in v1 scope.

### 3.3 Is any data transmitted off-device?

**Designed:** **No**, except:

- **Google Play Billing** (purchases handled by Google; still a **third party** — disclose in policy and Data safety as appropriate).  
- **Optional analytics (opt-in)** per `PROJECT.md` — if implemented, every SDK must be **declared**, **opt-in gated**, and documented.

### 3.4 Analytics opt-in

If added:

- **Pre-consent** must not initialize analytics.  
- [User Data](https://support.google.com/googleplay/android-developer/answer/10144311): match **prominent disclosure** to what the SDK actually collects.  
- **Children**: if any child-directed positioning, stricter rules apply ([Designed for Families](https://support.google.com/googleplay/android-developer/answer/9893335) — see §5).

### 3.5 Uninstall / retention

- **Local data** generally removed with app uninstall (normal Android behavior); **external** storage exports (e.g. shared video loop) are user-visible.  
- Privacy policy should state **no account** and **no server copy** for v1.  
- If analytics ever used: describe **retention** and **deletion** mechanics.

---

## 4. Privacy policy requirements

Must satisfy Google Play [User Data](https://support.google.com/googleplay/android-developer/answer/10144311) **and** applicable law.

### 4.1 Play-required elements (minimum)

- Developer identity and **contact** for privacy inquiries.  
- Types of data **accessed, collected, used, shared** (including via **SDKs**).  
- **Security** practices at a high level.  
- **Retention and deletion** (even if “deleted on uninstall”).  
- Clear **labeling** as privacy policy; **public non-PDF URL** in Console.

### 4.2 GDPR (EU/EEA/UK users)

Even **without a server**, you may be **processing personal data on device** (session history, contacts processing). Typical obligations:

- **Transparency** (Art. 13/14-style information in policy + in-app disclosures).  
- **Lawful basis**: often **contract** (provide the app) + **consent** for **optional** analytics and **sensitive** processing where required; **legitimate interests** may apply to strictly necessary security — **legal review** recommended.  
- **Data subject rights**: for purely local processing with no remote copy, rights are **simpler**, but policy should explain how users **withdraw consent** (toggle analytics) and **erase** data (clear app data / uninstall).  
- **DPIA** may be warranted given **systematic monitoring** of apps/notifications — legal advice.

### 4.3 CCPA/CPRA (California)

If you **do not sell/share** personal information and have **no cross-context behavioral advertising**, disclosures can state that **clearly**. Describe **categories** collected (aligned with Data safety). Provide **contact** for requests if scale/triggering thresholds apply — **legal review**.

### 4.4 Children (COPPA / Play Families)

Persona “Kabir (16–24)” does not make the app child-directed, but if marketing or UI could be construed as **primarily child-directed**, rules tighten. If **not** child-directed:

- Avoid **knowingly** collecting from under-13 without parental consent.  
- If **Designed for Families** is **not** used, still avoid **interest-based ads** and risky SDKs.

---

## 5. Permission request flow design (UX / compliance)

Align with Android patterns and [User Data](https://support.google.com/googleplay/android-developer/answer/10144311): **disclosure immediately precedes** runtime requests; **no** interpreting “back” as consent.

### 5.1 Progressive sequencing (recommended)

1. **First session value** — After onboarding (screen map Flow 1), user reaches Home. **Do not** dump all system settings on first tap.  
2. **Pre-permission education screens** — One capability at a time: **Accessibility** (blocking), **Notification listener** (filtering), **Contacts** (when user opens Contacts or configures emergency), **Phone/SMS** (when user opens dialer/messages), **DND** (when user enables “quiet” feature), **Notifications** (Android 13+ for FGS).  
3. **Deep links** — Each screen ends with **“Open settings”** to the **correct** system page.  
4. **Verification** — Return to app with clear **“You’re set” / “Still needed”** states.

Suggested **order** (tunable with product):

1. **POST_NOTIFICATIONS** (if needed for lawful FGS UX) + explain persistent notification.  
2. **Accessibility** (blocking) — **largest** trust moment; full **standalone** disclosure + consent.  
3. **Notification listener** — separate rationale.  
4. **Contacts** — when user first needs contact search or emergency setup.  
5. **CALL_PHONE** / **SMS group** — only when user initiates those surfaces, **after** legal confirmation of handler/exception strategy.  
6. **Call log** — **only** if engineering confirms no alternative; else **omit**.  
7. **DND / notification policy** — when enabling audio/interruption controls.  
8. **Boot/FGS** — largely normal permissions; explain in policy and first-run “reliability” copy if needed.

### 5.2 If denied

- **Degrade gracefully:** explain which features fail (e.g., “Without accessibility, other apps can’t return you to the water.”).  
- Offer **retry** entry points from **Settings** screens (screen map #14–16).  
- **Never** fake system dialogs.

### 5.3 “Don’t ask again”

- Detect **permanently denied** runtime permissions; show **instructions** to system app settings.  
- For **special access** (accessibility, notification listener), always use **settings intents**; users may revoke anytime.

### 5.4 Android 13+ notifications

- Request **`POST_NOTIFICATIONS`** before showing non-FGS notifications if any; FGS still requires **appropriate typing** and user-visible notification.

---

## 6. Compliance recommendations for UI Designer & product

### 6.1 Required / strongly advised screens (beyond current screen map)

| Screen / pattern | Purpose |
|------------------|--------|
| **Accessibility disclosure** | Standalone full-screen or modal **before** system accessibility enable; scrollable text; **checkbox + Continue** (affirmative). |
| **Notification access disclosure** | Separate from accessibility; clarify **content visibility**. |
| **Permission hub in Settings** | Single place listing **each** privilege, **status**, and **fix** button. |
| **“Basic phone mode” legal UX** | If default handler is required, explain **role change**; if not, explain **limitations** without log/SMS permissions. |
| **Analytics opt-in** | Single toggle with plain explanation; default **off** if “optional.” |

### 6.2 Copy constraints (brand + compliance)

- Brand avoids certain words (`PROJECT.md`); **compliance copy** must still be **plain and accurate** (“We see which app is on screen…” not vague “we help you focus”).  
- **No** implying Google endorsement.  
- **Emergency** promises (911, SOS) must match **engineering reality** — avoid overclaiming.

### 6.3 Data safety section (illustrative — verify before submit)

If **no data leaves the device** and **no analytics**:

- **Data collection:** Often **“No”** for types transmitted off-device — **confirm** Billing/analytics.  
- **Data shared:** **No** (or **Google Play Billing** only, per form definitions).  
- **Encryption in transit:** **N/A** or **yes** only if something is sent.  
- **Deletion:** Describe **in-app clear data** / uninstall.

If **accessibility/listener** “collection” is interpreted as **on-device only**, align answers with [Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469); **do not** contradict the privacy policy.

---

## 7. Self-verification (task checklist)

| # | Question | Status |
|---|----------|--------|
| 1 | Have all sensitive permissions been identified and justified? | **Yes** for listed items; **implementation audit pending** (no manifest in repo). **SMS/Call Log** flagged for **policy re-validation** with counsel. |
| 2 | Are Google Play policies correctly cited? | **Yes** — primary links are official Help Center articles; enforcement details may evolve; recheck before submission. |
| 3 | Does the flow follow Android / Play best practices? | **Recommended sequence** documented; **must** separate **prominent disclosures** per [User Data](https://support.google.com/googleplay/android-developer/answer/10144311) and [Accessibility API policy](https://support.google.com/googleplay/android-developer/answer/10964491). |
| 4 | Is local-only data handling accurate? | **Accurate per `PROJECT.md`**; **re-verify** for every SDK at ship time. |
| 5 | Are requirements specific enough to act on? | **Yes** — UI additions, Console forms, and **architecture** decisions (especially **SMS/Call Log** minimization) are explicit. |

---

## 8. Action items (engineering / legal / design)

1. **Legal review** of **SMS/Call Log** strategy against [9047303](https://support.google.com/googleplay/android-developer/answer/9047303); prioritize **permission-free** breakthrough detection if feasible.  
2. **Architect** AccessibilityService for **minimum event surface**; document in **Console declaration** video.  
3. **Implement** **permission hub** and **standalone** disclosure screens; **never** bundle with unrelated consents.  
4. **Publish** privacy policy URL early; keep **Data safety** synchronized.  
5. **Ship gate:** manifest diff review + **SDK data flow** audit + **staged** Play **internal testing** track.

---

*End of report.*
