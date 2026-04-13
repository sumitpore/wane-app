# Security Reviewer Conventions — Wane

**Created**: 2026-04-13
**Scope**: Security audit reports in `.team/artifacts/security-reviewer/`. **No production code modification.**
**Baseline**: OWASP Mobile Top 10 (2024) + Android-specific compliance requirements

> Prerequisites: Read `CONVENTIONS.md` (general), `ARCHITECTURE.md` (full system overview), and the backend-dev and db-engineer `CONVENTIONS.md` files to understand what you are auditing.

---

## 1. Role Boundaries

The Security Reviewer **reports only**. This role:

- **Does**: Write audit reports, identify vulnerabilities, recommend fixes, verify compliance, flag policy risks.
- **Does NOT**: Modify production code, create PRs with code changes, edit build files, or write tests.
- **Deliverables**: Markdown reports in `.team/artifacts/security-reviewer/`. Nothing else.

If a finding requires a code change, the report specifies the exact file, line range, and recommended fix. The owning role (Frontend Dev, Backend Dev, DB Engineer, DevOps) implements the fix.

---

## 2. Audit Report Format

Every audit report follows this structure:

```markdown
# Security Audit: {Scope Description}

**Auditor**: Security Reviewer
**Date**: YYYY-MM-DD
**Scope**: {Files, services, or features audited}
**Status**: DRAFT | REVIEW | FINAL

## Executive Summary
{1-3 sentence summary of overall security posture and critical findings count}

## Findings

### Finding {N}: {Short Title}

| Field | Value |
| ----- | ----- |
| **Severity** | CRITICAL / HIGH / MEDIUM / LOW / INFO |
| **Category** | {OWASP category or Wane-specific category} |
| **Location** | `{file path}:{line range}` |
| **Status** | OPEN / FIXED / ACCEPTED_RISK / FALSE_POSITIVE |

**Description**: {What the vulnerability is}

**Evidence**: {Code snippet, configuration excerpt, or behavioral observation}

**Impact**: {What could happen if exploited}

**Recommendation**: {Specific fix with code example if applicable}

**Owner**: {Role responsible for fixing: Frontend Dev / Backend Dev / DB Engineer / DevOps}

---

## Compliance Checklist
{Role-specific checklist — see section 5}

## Appendix
{Supporting evidence, tool output, reference links}
```

---

## 3. Severity Definitions

| Severity | Definition | SLA |
| -------- | ---------- | --- |
| **CRITICAL** | Immediate safety risk. Emergency calls could be blocked, user data could be destroyed, or the app could be removed from Play Store. | Must fix before any release. Blocks all development. |
| **HIGH** | Significant security or compliance risk. Could cause Play Store rejection or user harm under specific conditions. | Must fix before the next release. |
| **MEDIUM** | Moderate risk. Exploitable under unlikely conditions or violates best practices with limited impact. | Should fix within 2 sprints. |
| **LOW** | Minor issue. Defense-in-depth improvement or code hygiene. | Fix when convenient. |
| **INFO** | Informational observation. Not a vulnerability but worth noting for awareness. | No action required. |

---

## 4. Audit Scope for Wane

Every full audit covers these five focus areas. Partial audits specify which areas are in scope.

### 4a. AccessibilityService Policy Compliance

This is the **highest-risk area** for Play Store approval.

| Check | What to verify |
| ----- | -------------- |
| Event scope | Only `TYPE_WINDOW_STATE_CHANGED` in service config XML |
| Content access | `canRetrieveWindowContent="false"` in service config XML |
| Gesture access | `canPerformGestures="false"` in service config XML |
| Data minimization | `event.packageName` read only; never logged, stored, or transmitted |
| In-app disclosure | Full-screen disclosure shown before requesting accessibility enable |
| Privacy policy | Explicit statement about accessibility service scope |
| Play Console declaration | Non-accessibility-tool declaration with justification and video walkthrough |
| Android 17 APM handling | Graceful fallback when APM revokes the service |
| User can disable | No Device Admin, no uninstall prevention, no guilt-tripping |

### 4b. Data Storage Security

| Check | What to verify |
| ----- | -------------- |
| PII inventory | No sensitive PII stored (no passwords, no behavioral logs, no app usage history) |
| Encryption at rest | Android FBE is sufficient (no application-level encryption needed for non-PII) |
| Purchase tokens | Google Play `purchaseToken` is opaque; verify it's not logged or exposed |
| Emergency contacts | Phone numbers stored in DataStore as JSON; verify not logged |
| Database file | Room database file permissions are default (app-private) |
| Backup rules | `backup_rules.xml` and `data_extraction_rules.xml` include only db and preferences |
| No external storage | No data written to shared/external storage |

### 4c. Permissions Audit

| Check | What to verify |
| ----- | -------------- |
| Declared permissions | Only: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS` |
| No dangerous permissions | No `READ_CONTACTS`, `CALL_PHONE`, `READ_PHONE_STATE`, `CAMERA`, `LOCATION`, etc. |
| Runtime permissions | `POST_NOTIFICATIONS` (API 33+) requested at appropriate time with explanation |
| Permission rationale | In-app explanation before each permission request |
| Graceful degradation | App functions (with reduced capability) if permissions are denied |

### 4d. Foreground Service Compliance

| Check | What to verify |
| ----- | -------------- |
| Service type | `foregroundServiceType="specialUse"` with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` |
| Play Console declaration | `specialUse` type declaration submitted with justification |
| Notification | Persistent notification displayed while service is running |
| Service exported | `android:exported="false"` on all services |
| Lifecycle | Service stops when session ends; no orphaned foreground services |

### 4e. Emergency Safety Verification

This is the **most safety-critical audit area**. Every release must pass these checks.

| Check | What to verify |
| ----- | -------------- |
| Emergency numbers immutable | `EmergencySafety.EMERGENCY_NUMBERS` is `val Set<String>`, not loaded from any mutable source |
| Never-block packages immutable | `EmergencySafety.NEVER_BLOCK_PACKAGES` is `val Set<String>`, not filtered or reduced |
| Check ordering (AccessibilityService) | `NEVER_BLOCK_PACKAGES` check comes BEFORE session-active check in `onAccessibilityEvent()` |
| Check ordering (NotificationListener) | Emergency number check comes BEFORE session-active check in `onNotificationPosted()` |
| Emergency exit no cooldown | Exit confirmation has no rate limit, no cooldown, no maximum attempts |
| Repeated caller always on | Breakthrough feature is not a setting; cannot be disabled by user or code path |
| System emergency unblocked | SOS (power x5), fall detection, Medical ID, emergency calling from lock screen all unreachable by our services |
| Battery critical mode | Low battery degrades visuals but never disables timer, emergency exit, or repeated caller breakthrough |

---

## 5. OWASP Mobile Top 10 Checklist

Apply the [OWASP Mobile Top 10 (2024)](https://owasp.org/www-project-mobile-top-10/) as a baseline for every full audit:

| # | OWASP Category | Wane Relevance | Key Checks |
| - | -------------- | -------------- | ---------- |
| M1 | Improper Credential Usage | LOW — no server credentials | Verify no hardcoded keys, no API tokens |
| M2 | Inadequate Supply Chain Security | MEDIUM | Dependency audit (Dependabot), verify no malicious transitive deps |
| M3 | Insecure Authentication/Authorization | LOW — no user accounts | Verify no auth bypass for premium theme purchases |
| M4 | Insufficient Input Validation | MEDIUM | Emergency exit "EXIT" input, duration picker bounds, settings inputs |
| M5 | Insecure Communication | LOW — no network calls | Verify no accidental network calls, no analytics SDKs |
| M6 | Inadequate Privacy Controls | HIGH | AccessibilityService scope, notification content access, data minimization |
| M7 | Insufficient Binary Protections | MEDIUM | R8 obfuscation enabled, ProGuard rules documented, no debug info in release |
| M8 | Security Misconfiguration | MEDIUM | Manifest permissions, service exports, backup rules, debuggable flag |
| M9 | Insecure Data Storage | MEDIUM | Room database app-private, DataStore app-private, no external storage |
| M10 | Insufficient Cryptography | LOW | No custom crypto needed; Android FBE handles at-rest encryption |

---

## 6. Play Store Declaration Requirements

The Security Reviewer verifies that all required Play Store declarations are prepared:

| Declaration | Status | Notes |
| ----------- | ------ | ----- |
| AccessibilityService non-accessibility-tool declaration | Must verify | Justification text, video walkthrough of single-purpose use |
| Foreground service `specialUse` declaration | Must verify | Justification for focus session timer use case |
| Data safety form | Must verify | No data collected, no data shared, encryption at rest (Android FBE) |
| Privacy policy URL | Must verify | Explicit statement about accessibility service and notification listener scope |
| App content declarations | Must verify | No ads, no data collection, target audience (16+) |
| Notification listener declaration | Must verify | Justification for notification management during focus sessions |

---

## 7. Audit Cadence

| Trigger | Audit Type | Scope |
| ------- | ---------- | ----- |
| Before each release | Full audit | All 5 focus areas + OWASP checklist |
| New service added | Targeted audit | New service + manifest + permissions |
| Dependency update (major version) | Supply chain audit | OWASP M2 + dependency tree review |
| Play Store policy update | Compliance re-check | AccessibilityService + FGS + declarations |
| Emergency safety code change | Mandatory audit | Section 4e (emergency safety) — blocks merge until PASS |

---

## 8. Report File Naming

Audit reports are stored in `.team/artifacts/security-reviewer/`:

```
security-reviewer/
├── CONVENTIONS.md                          -- This file
├── audit-v1.0.0-pre-release.md            -- Full pre-release audit
├── audit-accessibility-service.md         -- Targeted AccessibilityService audit
├── audit-emergency-safety.md              -- Targeted emergency safety audit
└── audit-dependency-2026-04.md            -- Monthly dependency audit
```

**Naming pattern**: `audit-{scope}-{date or version}.md`

---

## 9. Verification of Fixes

When a finding is marked as `FIXED`:

1. The Security Reviewer verifies the fix by reading the code change (diff or file read).
2. The finding status is updated to `FIXED` with a verification note: `Verified in commit {hash} on {date}`.
3. If the fix is incomplete, the finding remains `OPEN` with an updated recommendation.
4. The Security Reviewer never modifies the production code directly — only updates the audit report.

---

## 10. Escalation Protocol

| Condition | Action |
| --------- | ------ |
| CRITICAL finding discovered | Immediately flag in PROGRESS.md and notify all active roles. Blocks all development. |
| Emergency safety code modified | Mandatory security audit before merge. No exceptions. |
| Play Store rejection related to security | Full re-audit of rejected area. Update compliance checklist. |
| Dependency CVE (critical/high) | Notify DevOps for immediate update. Verify no exploitation path in Wane's usage. |
