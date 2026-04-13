# Task Board: Wane

**Last Updated**: 2026-04-12

> All teammates read and update this file. Use task IDs for dependency references.

## Legend

- `PENDING` -- Not started, may be blocked by dependencies
- `IN_PROGRESS` -- Actively being worked on by assigned role
- `COMPLETED` -- Done and verified
- `BLOCKED` -- Waiting on dependency
- `REVISION` -- Failed verification, needs rework

---

## Phase 0: Discovery

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |
| P0-1 | Define project requirements and vision | Business/Marketing | COMPLETED | — | PROJECT.md created |
| P0-2 | User personas and value proposition | Business/Marketing | COMPLETED | — | discovery.md created |

## Phase 1: Content + Design

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |
| P1-1 | Create screen map | UI Designer | COMPLETED | P0-1 | 18 screens mapped, reduced to 10 owned + 6 native + 2 removed |
| P1-2 | Write content for all screens | Content Writer | COMPLETED | P1-1 | 18 screen content artifacts created |
| P1-3 | Create initial DESIGN.md | UI Designer | COMPLETED | P0-2 | Design system defined |
| P1-4 | Compliance review | Consent Manager | COMPLETED | P1-2 | compliance-report.md created |
| P1-5 | Generate Stitch screens | UI Designer | COMPLETED | P1-2, P1-3 | All 18 screens generated in Stitch project 453137823679633111 |
| P1-6 | Export design prototype | UI Designer | COMPLETED | P1-5 | Exported to design/Focus Mode App/ (React/Vite) |
| P1-7 | Design audit | UI Designer | COMPLETED | P1-6 | design-audit.md: 35+ colors, 16 typography styles, 10 screen specs, Compose-ready code |
| P1-8 | Revise DESIGN.md with prototype values | Lead | COMPLETED | P1-7 | Prototype colors as source of truth per user decision |
| P1-9 | Update screen map (scope changes) | Lead | COMPLETED | P1-7 | Removed screens 17, 18; marked 8-13 as native |
| P1-10 | HIL Gate 1: Design approval | Lead | COMPLETED | P1-7 | All 10 screens approved. 3 toolbar buttons (Phone + Contacts + SMS). |

## Phase 2: Foundation

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |
| P2-1 | Define project-wide conventions | Lead | COMPLETED | P1-10 | CONVENTIONS.md General section: language, encoding, indentation, naming, banned words, accessibility, privacy |
| P2-2 | Define role-specific conventions | Content Writer, UI Designer, Consent Manager | COMPLETED | P2-1 | All three convention files created with comprehensive domain-specific rules |
| P2-3 | Scaffold project structure | Lead | COMPLETED | P2-1 | Android project directories: ui/{onboarding,home,session,settings}, theme, components, service, data, util, res/, test/ |

## Phase 3: Architecture

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |
| P3-1 | Define module boundaries | Lead | COMPLETED | P2-3 | 7 modules, clear file ownership per role |
| P3-2 | Tech stack proposals | Backend/Frontend/DB/DevOps | COMPLETED | P3-1 | 4 proposals completed |
| P3-3 | HIL Gate 2: Tech stack approval | Lead | COMPLETED | P3-2 | All approved. compileSdk=36/targetSdk=36, minSdk=28, Nav3, Room 3.0, Hilt 2.57.1 |
| P3-4 | Define interfaces | Specialists | COMPLETED | P3-3 | INTERFACES.md: 12 API contracts, 9 shared types, 5 event contracts |
| P3-5 | Tech-specific conventions | All tech roles | COMPLETED | P3-3 | All 6 technical role convention files updated |
| P3-6 | Full scaffold with stubs | Frontend/Backend Dev | COMPLETED | P3-4, P3-5 | 42 files: build config, manifest, shared types, interface stubs, theme foundation, entry points |

## Phase 4+: Implementation

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |

## Integration

| ID | Task | Role | Status | Depends On | Notes |
| -- | ---- | ---- | ------ | ---------- | ----- |
