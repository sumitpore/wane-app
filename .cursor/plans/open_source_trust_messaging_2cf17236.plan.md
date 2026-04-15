---
name: Open source trust messaging
overview: Add open-source trust/transparency messaging wherever the app requests Accessibility or Notification permissions, and create an MIT LICENSE file. Uses team-of-agents delegation to Content Writer and Frontend Developer specialists.
todos:
  - id: content-writer
    content: Spawn Content Writer to draft trust/transparency copy for all 5 permission-related string resources
    status: completed
  - id: license-file
    content: Create MIT LICENSE file at repo root (license text only, no copyright line)
    status: completed
  - id: frontend-dev
    content: Spawn Frontend Developer to implement updated strings, clickable GitHub links in onboarding steps and home banners
    status: completed
  - id: verify
    content: "Lead verification: build compiles, strings correct, links functional, LICENSE present, no lint errors"
    status: completed
isProject: false
---

# Open Source Trust Messaging + MIT License

## Context

The Wane app asks users for two sensitive permissions -- **Accessibility Service** and **Notification Listener** access. Users may feel uneasy granting these. Since the code is open-sourced at `https://github.com/sumitpore/wane-app.git`, we should proactively communicate this to build trust.

## Locations That Need Trust Messaging

There are **4 UI surfaces** where permission access is requested, plus 1 system-level description:

| Location | File | Current copy (string resource) |
|---|---|---|
| Onboarding: Notification step (page 3) | [NotificationStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/NotificationStep.kt) | `notification_description` |
| Onboarding: Accessibility step (page 4) | [AccessibilityStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/AccessibilityStep.kt) | `accessibility_description` |
| Home: Accessibility banner | [HomeScreen.kt](app/src/main/kotlin/com/wane/app/ui/home/HomeScreen.kt) | `accessibility_prompt_message` |
| Home: Notification banner | [HomeScreen.kt](app/src/main/kotlin/com/wane/app/ui/home/HomeScreen.kt) | `notification_prompt_message` |
| System: Accessibility service config | [strings.xml](app/src/main/res/values/strings.xml) | `accessibility_service_description` |

All string resources are centralized in [strings.xml](app/src/main/res/values/strings.xml).

## Specialist Delegation Plan

### Round 1: Content Writer (parallel with LICENSE creation)

**Goal**: Draft trust/transparency copy for all 5 string resources listed above.

**Deliverables**:
- Updated text for `accessibility_description`, `notification_description`, `accessibility_prompt_message`, `notification_prompt_message`, and `accessibility_service_description`
- Each should reassure the user that the app is open source, their privacy is respected, and they can verify the code themselves at the GitHub repo URL
- Copy should feel natural and calming (consistent with existing Wane brand voice -- minimal, reassuring, not corporate/legalese)
- The GitHub URL `https://github.com/sumitpore/wane-app.git` must be included or referenced

**Constraint**: The system-level `accessibility_service_description` is shown by Android in the Accessibility Settings UI, which does **not** render clickable links -- it is plain text only. The in-app surfaces (onboarding steps and home banners) can support clickable links via Compose.

### Round 1 (parallel): LICENSE File

**Goal**: Create an MIT LICENSE file at the repository root.

**Deliverable**: MIT LICENSE file with license text only -- no copyright line per user request.

**File**: [LICENSE](LICENSE) (new file at repo root)

### Round 2: Frontend Developer

**Goal**: Implement the trust messaging in the app UI.

**Deliverables**:
- Update [strings.xml](app/src/main/res/values/strings.xml) with the Content Writer's revised copy
- Add a new shared string resource for the open-source link text (e.g., `open_source_link_text` and `open_source_url`)
- Modify [AccessibilityStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/AccessibilityStep.kt) and [NotificationStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/NotificationStep.kt) to display the trust message with a tappable link to the GitHub repo
- Modify the `AccessibilityPromptBanner` and `NotificationPromptBanner` composables in [HomeScreen.kt](app/src/main/kotlin/com/wane/app/ui/home/HomeScreen.kt) to include the trust message with a tappable link
- The link should open the GitHub URL in the device browser

**Self-verification**: Build compiles, no lint errors, link is tappable and opens the correct URL, copy matches Content Writer output exactly.

## Additional Change

- Optionally update [README.md](README.md) to mention the MIT license (e.g., add a "License" section at the bottom).
