# UI Designer Conventions: Wane

**Created**: 2026-04-13  
**Last Updated**: 2026-04-13

> These conventions apply to the **UI Designer** role. They extend [`.team/CONVENTIONS.md`](../../CONVENTIONS.md) (project-wide rules) and must stay aligned with [`.team/DESIGN.md`](../../DESIGN.md) (design system summary) and [`.team/artifacts/ui-designer/design-audit.md`](design-audit.md) (exhaustive tokens, components, and screen specs).

---

## 1. Design token naming

### 1.1 Two layers: semantic name → implementation name

| Layer | Format | Example | Where defined |
| --- | --- | --- | --- |
| **Semantic token** (documentation, specs) | `lowerCamelCase`, descriptive of role | `accentPrimary`, `backgroundDeep`, `textMuted` | Tables in `design-audit.md` §1–3 |
| **Compose / Kotlin** (handoff to Frontend Dev) | `WaneColors.<semantic>` or `WaneTypography.<slot>` | `WaneColors.accentPrimary`, `WaneTypography.headlineLarge` | `design-audit.md` Appendices B–C |

**Rule:** In screen specs, component specs, and review notes, always cite **semantic names** first, then the Compose name in parentheses on first mention per document, e.g. `accentPrimary` (`WaneColors.accentPrimary`).

### 1.2 Color tokens

- Use **exact names** from `design-audit.md` §1 (`backgroundDeep`, `accentLight`, `textGhost`, `crystalline`, `mutedTide`, `waveLayer1`, etc.).
- **White-alpha text** is documented as `textPrimary` … `textGhostActive` (not raw `white/90` alone in handoff docs—pair with the token).
- **HSL-named colors** in the audit map to `WaneColors.crystalline`, `WaneColors.mutedTide`, `WaneColors.bodyText`, `WaneColors.dotInactive`, `WaneColors.divider`.
- **Water-only** colors use `waveLayer*`, `waterGradient*`, `causticCenter`, `causticEdge`—never repurpose these for non-water UI.

### 1.3 Spacing, radius, and motion

- **Spacing:** `spacing-1` … `spacing-16` as in `design-audit.md` §3.1 (maps to 4dp … 64dp). In specs, prefer **dp values** plus token when helpful: `24dp` (`spacing-6`).
- **Radius:** `radiusXL` (12dp), `radiusFull`, `radiusDevice` (40dp frame), etc.—see §4 of the audit.
- **Typography:** Reference **style names** from `design-audit.md` §2.2 (e.g. Duration Display, Section Label) and the suggested **Compose TextStyle slot** (`displayLarge`, `labelSmall`, …).

### 1.4 Source of truth order

1. **`design-audit.md`** — exhaustive values, prototype alignment, ⚠️ deltas.  
2. **`DESIGN.md`** — principles and constraints when the audit is silent.  
3. If `DESIGN.md` and the audit disagree, **follow the audit** and note it (see `design-audit.md` §8).

---

## 2. Screen spec format

Every Wane-owned screen documented by UI Design MUST use this structure (see `design-audit.md` §7 as reference models).

### 2.1 Required sections (in order)

1. **Title** — `### Screen <id> — <Name>` (ids align with [`.team/artifacts/ui-designer/screen-map.md`](../screen-map.md) where possible).
2. **ASCII wireframe** — Fixed-width box (~41–43 chars wide), top-to-bottom layout. Use `┌`, `├`, `└`, `│`, `─` for structure. Label regions with short arrows (`←`) and callouts.
3. **Annotation block** — Plain-language layout and token list:
   - Background (gradient stops → `backgroundDeep`, etc. or solid `backgroundSettings`).
   - Z-order and key layers (water, status bar, overlays).
   - Positions in **dp** from edges (e.g. `top-32dp`, `right-24dp`).
   - Components by **§6 catalog name** or new component spec id.
4. **Motion** (if non-default) — Entrance/exit, stagger, springs (see §7 Motion conventions).
5. **Prototype / implementation note** — e.g. `DESIGNED — not in prototype` or `matches prototype`.

### 2.2 ASCII rules

- **No emoji** in wireframes or user-visible copy in specs (per project rules). Use text labels like `[water]`, `(play)`, `[gear]` instead of emoji placeholders (replace legacy emoji examples in old drafts when updating).
- **Single column** — no multi-column layouts in wireframes unless a future breakpoint is explicitly in scope (Android v1: single column).

### 2.3 File location

- **Primary living specs:** `design-audit.md` §7 for screens already cataloged.  
- **New or exploratory screens** may be drafted in `.team/artifacts/ui-designer/screens/<screen-name>.md` until merged into the audit.

---

## 3. Component documentation

### 3.1 Where components live

- **Canonical catalog:** `design-audit.md` §6 (component blocks).
- **New components:** Add a subsection `### 6.x <Component Name>` with the same structure as existing entries.

### 3.2 Required fields per component

Each component spec MUST include:

| Field | Content |
| --- | --- |
| **Identity** | Name (matches Figma / prototype if any) and primary screen(s). |
| **Layout** | Structure, alignment, gaps using **dp** and `spacing-*` tokens where applicable. |
| **Visual tokens** | Colors (`WaneColors.*`), surfaces, borders, corner radius. |
| **Typography** | Font, size (sp), weight, letter-spacing, casing, color token. |
| **States** | Default, pressed, disabled, focused (as relevant). |
| **Motion** | Scale, translation, duration, spring (if any). |
| **Accessibility** | Min touch target, role/label expectations for Frontend (see §8). |

### 3.3 Variants

- Document **variants** as bullet lists or sub-subsections (e.g. CTA vs. ghost) without splitting into multiple top-level components unless behavior/layout diverges significantly.

### 3.4 Cross-references

- Reference **reused** pieces as “see §6.x” instead of duplicating full specs.

---

## 4. Handoff format (Frontend Developer)

### 4.1 What Frontend Dev receives

| Deliverable | Purpose |
| --- | --- |
| **`.team/DESIGN.md`** | Design intent, principles, banned patterns. |
| **`.team/artifacts/ui-designer/design-audit.md`** | Tokens, typography table, spacing, motion, §6 components, §7 screens, Appendices B–C (Compose snippets). |
| **`.team/artifacts/ui-designer/screen-map.md`** | Screen list, flow grouping, scope. |
| **Optional:** `screens/*.md` | Draft specs prior to audit merge. |

### 4.2 Spec file structure (checklist for designers)

For each screen or feature handoff, ensure:

- [ ] **Screen id + name** consistent with `screen-map.md`.  
- [ ] **All measurements in dp/sp** (not px-only) for Android.  
- [ ] **Color tokens** named (`WaneColors.*`); no ad-hoc hex unless flagged as new token candidate.  
- [ ] **Typography** references style name + `WaneTypography` slot when applicable.  
- [ ] **Motion:** spring `stiffness` / `damping` OR duration + easing; stagger pattern (delay list, offset px, fade).  
- [ ] **Interactive feedback:** scale, translateY, color transitions per §5.4 of audit.  
- [ ] **Water / session rules:** No numeric timer on water screen; water level is the only progress indicator.  
- [ ] **Deltas:** ⚠️ prototype vs. production called out (see audit §8).  
- [ ] **Accessibility:** Touch targets, focus ring, semantics (§8 below).

### 4.3 Motion handoff

- Copy **numeric values** from `design-audit.md` §5 (durations, spring, stagger, interactive transforms).  
- For springs used in Compose, specify **stiffness** and **damping** explicitly (see §7).

### 4.4 Assets

- Raster/vector exports from Figma/design tool: **naming** `ic_<purpose>_<size>.xml` or WebP as agreed with Frontend; **default staging area** `design/Focus Mode App/` for prototype assets, production pipeline TBD by Frontend Dev.

---

## 5. Design file organization

| Location | Contents |
| --- | --- |
| **`design/Focus Mode App/`** | Approved **HTML/React prototype** — source of truth for measurements unless overridden in audit. |
| **`.team/DESIGN.md`** | Curated design system (colors, type, components, motion summary). |
| **`.team/artifacts/ui-designer/design-audit.md`** | Full token audit, component catalog, screen specs, Compose appendices. |
| **`.team/artifacts/ui-designer/screen-map.md`** | Screen inventory and flows. |
| **`.team/artifacts/ui-designer/screens/`** | Optional drafts for new screens before merging into audit. |
| **Figma / external** (if used) | Named files linked from project docs; not a substitute for the audit. |

**Rule:** Do not scatter authoritative measurements in chat only—**update the audit or a `screens/` draft** so Frontend Dev has a single linkable reference.

---

## 6. Design review checklist (before Frontend handoff)

### 6.1 Brand & copy

- [ ] **American English**; **Wane** capitalized correctly.  
- [ ] **Banned words** never appear in UI copy: `addiction`, `limit`, `block`, `detox`, `digital`, `wellbeing` (see project `CONVENTIONS.md`).  
- [ ] **No emojis** in UI.  
- [ ] Tone: calm, non-judgmental, understated (see `PROJECT.md` §7).

### 6.2 Visual system

- [ ] **Dark-only** palette; **one accent** (`accentPrimary`); no second competing accent.  
- [ ] **No pure black** `#000000` for app surfaces (device frame / legacy prototype exception noted in audit §8).  
- [ ] **Fonts:** Sora, DM Sans, Space Grotesk only—**no** Inter, system UI, or banned serif stack.  
- [ ] **Water screen:** No timer numerals; no chrome covering the water hero.

### 6.3 Layout & density

- [ ] **Minimum 24dp** internal padding where applicable (`DESIGN.md` §5).  
- [ ] **Touch targets** meet §8.  
- [ ] **Corner radius:** 12dp for sheets/CTAs; circular controls use full round, not 12dp circles.

### 6.4 Motion

- [ ] Springs and durations match `design-audit.md` §5 (or deviation documented with rationale).  
- [ ] No gratuitous linear snapping on primary transitions—prefer spring or specified easing.

### 6.5 Accessibility

- [ ] §8 checklist satisfied.  
- [ ] Focus states defined for inputs and interactive rows.  
- [ ] **Color is not the only** indicator for state (toggle, selection).

### 6.6 Documentation

- [ ] Screen/component updates reflected in **design-audit.md** (or a linked `screens/` draft with merge plan).  
- [ ] **screen-map.md** updated if screens added/removed/renamed.

---

## 7. Motion conventions

### 7.1 Default spring (bottom sheets, panels)

| Parameter | Value | Usage |
| --- | --- | --- |
| Type | Spring | Settings panel, emergency exit sheet, sheet-like surfaces |
| **Stiffness** | **100** | Matches `DESIGN.md` §6 and `design-audit.md` §5.1 |
| **Damping** | **20** | Same |

### 7.2 Durations & easing (from audit §5.2)

| Use case | Duration | Notes |
| --- | --- | --- |
| Screen fade (onboarding ↔ home) | 0.6s | `AnimatePresence`-style wait |
| Screen fade → session | 0.8s | Session entry |
| Onboarding step content | 0.4s | easeOut |
| Settings row stagger | 300ms per item | With `i * 50ms` delay |
| Progress dot width/color | 300ms | Active/inactive dot |
| Toggle thumb/track | 200ms | ease (auto-lock toggle) |

### 7.3 Stagger patterns (audit §5.3)

- **Home:** 0.2s between elements (logo → picker → start → helper); each **y +20dp → 0** + fade.  
- **Settings rows:** `i * 0.05s` delay; **y +8dp → 0** + fade over 0.3s.  
- **Onboarding step swap:** ±16dp vertical + fade, 0.4s easeOut.

### 7.4 Interactive feedback (audit §5.4)

- **Primary circular:** `scale(0.95)` (start, toolbar).  
- **Primary CTA / gear / close:** `scale(0.97)` + `translateY(-1dp)`.  
- **End ghost:** `textGhost` → `textGhostActive` (no scale).

### 7.5 Water animation

- **60fps** target; **2×** resolution canvas; timing and layers per `design-audit.md` §5.5—do not hand-wave; reference section numbers in handoff.

---

## 8. Accessibility

### 8.1 Touch targets

| Context | Minimum size | Notes |
| --- | --- | --- |
| General interactive | **44dp** | Project `CONVENTIONS.md` + `DESIGN.md` §5 |
| Bottom toolbar (session) | **56dp** | Circular buttons; `DESIGN.md` §4 |
| **Visual** smaller controls (gear, End text) | **Expand hit area** to ≥44dp | Document **padding** in component spec; see audit §8 touch target delta |

### 8.2 Contrast & color

- **Primary text** on dark backgrounds: use `textPrimary`–`textMuted` scale from audit; avoid placing `textSubtle` on `surfaceDim` for critical actions.  
- **High contrast mode:** Prefer tested pairs (crystalline / mutedTide on `backgroundSettings`); flag new pairs for Frontend verification.  
- **Non-color cues:** Toggles, errors, and selection use shape/position/text, not color alone.

### 8.3 Focus indicators

- **Inputs:** `2dp` ring in **`accentPrimary`** (`#38A3DC`) on focused field (see Emergency Exit sheet §6 and Screen 6).  
- **Keyboard / D-pad:** Every interactive component in a spec must have a **visible focus** state or defer to Material 3 with Wane-colored focus ring.

### 8.4 Semantics (for handoff)

- Document **content description intent** (e.g. “Duration picker, increases minutes”) for custom controls.  
- **Session start/end:** Screen reader announcements required per `PROJECT.md` NFRs—coordinate copy with Content Writer; UI Designer notes **when** announcements fire.

---

## 9. Anti-patterns (banned in Wane)

### 9.1 Global (from `DESIGN.md` §7 + project rules)

- Emojis in UI.  
- **Inter** or generic system UI fonts as brand.  
- **Pure black** `#000000` for primary surfaces (audit exception for legacy device frame only).  
- Neon, outer-glow, oversaturated purple, **second accent** competing with `accentPrimary`.  
- Overlapping cluttered chrome; **three equal cards in a row** (not applicable to single-column v1, but avoid introducing the pattern).  
- Fake statistics, gamification, guilt-inducing copy, social comparison.

### 9.2 Wane-specific product

- **Numeric timer** or countdown on the **water** screen.  
- Usage statistics in the hero experience.  
- **Gamification** framing (badges, streaks as UI chrome—see product for streak exception in history, if/when shipped).  
- **Chrome covering** the water canvas (water is full-bleed hero).

### 9.3 Copy (banned words)

`addiction`, `limit`, `block`, `detox`, `digital`, `wellbeing` — **no** user-facing use.

### 9.4 Visual

- **Purple** or neon-blue as accent.  
- **Timer/progress UI** on the water screen (water level is the only temporal expression).  
- **AI-style** stock clichés in UI copy (coordinate with Content Writer).

---

## Self-verification (this document)

| Criterion | Status |
| --- | --- |
| **Specific enough** for another UI Designer to follow naming, screen format, component docs, handoff, file locations, review, motion, a11y, and bans | Yes — sections 1–9 |
| **References correct tokens** from `DESIGN.md` and `design-audit.md` (`WaneColors.*`, spacing, typography slots, §5–§7) | Yes |
| **Handoff to Frontend Dev** clearly defined (§4: deliverables, checklist, motion, assets) | Yes |

---

*When in doubt, update **`design-audit.md`** first, then **`DESIGN.md`** if principles change.*
