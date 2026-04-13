# Conventions: Wane

**Created**: 2026-04-10
**Last Updated**: 2026-04-12

> Conventions keep the project simple and consistent. This file contains project-wide conventions that ALL sub-agents must follow. Each sub-agent also defines their own domain-specific conventions in their artifacts directory.

## General Conventions

> Project-wide rules. Every sub-agent MUST follow these.

- **Language**: American English for all user-facing text. Code comments in English.
- **App name**: "Wane" -- always capitalized, never all-caps (except in uppercase-transformed UI labels).
- **File encoding**: UTF-8
- **Line endings**: LF (Unix-style)
- **Indentation**: 4 spaces for Kotlin; 2 spaces for XML/JSON/YAML
- **Max line length**: 120 characters
- **Naming style**:
  - Kotlin files: PascalCase (e.g., `WaterCanvas.kt`, `FocusSession.kt`)
  - Kotlin classes/objects: PascalCase
  - Kotlin functions/properties: camelCase
  - Kotlin constants: UPPER_SNAKE_CASE
  - XML resources: snake_case (e.g., `activity_main.xml`, `ic_phone.xml`)
  - Compose composables: PascalCase (e.g., `@Composable fun WaterScreen()`)
  - Package names: lowercase, no underscores (e.g., `com.wane.app.ui.session`)
- **Commit messages**: Conventional commits -- `feat:`, `fix:`, `chore:`, `docs:`, `style:`, `refactor:`, `test:`
- **Environment variables**: Not applicable (all data local, no server)
- **Secrets**: No API keys, no server credentials. All data stays on device. If future analytics opt-in added, use Android BuildConfig or local.properties (gitignored).
- **Banned words in user-facing copy**: "addiction", "limit", "block", "detox", "digital", "wellbeing"
- **Tone**: Calm, non-judgmental, understated, never preachy. The app is a companion, not a warden.
- **No emojis** in any user-facing text or UI elements.
- **Accessibility**: Minimum 44dp touch targets (56dp for toolbar). Content descriptions on all interactive elements. High contrast support.
- **Privacy**: No network calls (except optional opt-in analytics in future). No user data collection. No ads. All data stored locally.

## Role-Specific Conventions

Each sub-agent writes their own conventions file at `.team/artifacts/{role-name}/CONVENTIONS.md`. Non-technical roles (Content Writer, UI Designer, Consent Manager, Business/Marketing) define conventions during Phase 2 (Foundation). Technical roles (Frontend Dev, Backend Dev, DB Engineer, DevOps, etc.) define conventions during Phase 3 (Architecture), after the tech stack is approved.

Before writing code or content, every sub-agent reads:
1. **This file** (General conventions)
2. **Their own** `.team/artifacts/{role-name}/CONVENTIONS.md`
3. **Any upstream role's conventions** they depend on (e.g., Frontend Dev reads UI Designer's conventions for handoff format)

| Role | Conventions File |
| ---- | ---------------- |
| Backend Developer | `.team/artifacts/backend-dev/CONVENTIONS.md` |
| Frontend Developer | `.team/artifacts/frontend-dev/CONVENTIONS.md` |
| DB Engineer | `.team/artifacts/db-engineer/CONVENTIONS.md` |
| Test Engineer | `.team/artifacts/test-engineer/CONVENTIONS.md` |
| DevOps | `.team/artifacts/devops/CONVENTIONS.md` |
| Content Writer | `.team/artifacts/content-writer/CONVENTIONS.md` |
| UI Designer | `.team/artifacts/ui-designer/CONVENTIONS.md` |
| Data Engineer | `.team/artifacts/data-engineer/CONVENTIONS.md` |
| Security Reviewer | `.team/artifacts/security-reviewer/CONVENTIONS.md` |
| Consent Manager | `.team/artifacts/consent-manager/CONVENTIONS.md` |
