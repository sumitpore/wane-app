# Content Writer Conventions: Wane

**Created**: 2026-04-13  
**Last Updated**: 2026-04-13

> Domain-specific rules for all Wane copy and screen content artifacts. Read `.team/CONVENTIONS.md` (project-wide) first, then this file.

---

## 1. Content structure (screen files)

### Location and one file per screen

- **Path**: `.team/artifacts/content-writer/screens/`
- **One markdown file per screen**, named `NN-kebab-slug.md` where `NN` is zero-padded order in the user journey (see **Naming**).

### Required top matter

Every screen file opens with:

1. **Title**: `# Screen NN — Human-Readable Name`
2. **Flow**: `**Flow:**` — short flow label (e.g., `Onboarding`, `Home`, `Active session`)
3. **Purpose**: `**Purpose:**` — one sentence: what this screen must achieve emotionally or functionally

Then a horizontal rule (`---`).

### Standard sections (in this order)

Use these **H2** section headings unless a screen truly has no content in that category (state `_(None.)_` or omit only when genuinely N/A):

| Section | Purpose |
| -------- | -------- |
| `## Content` | Visible UI copy: grouped by subsection (`### Header`, `### Section: Name`, etc.). Use **tables** for element-to-copy mapping. |
| `## Microcopy` | Conditional, rare, or overflow copy: tooltips, first-run hints, share strings, permission nudges, empty states, edge cases. **Table**: Scenario → Copy. |
| `## Accessibility` | Non-visible strings for screen readers and assistive tech. **Table**: Element → Screen reader text. |
| `## Notes` | Rationale, restraint rules, and anti-patterns for this screen. Bullet list. |

### Tables inside `## Content`

- Prefer **Markdown tables** with columns such as: `Element | Copy`, `Setting | Label | Description | Control`, or `Icon | Action`.
- Use **bold** for element names in the first column (e.g., `**Screen title**`).
- When copy is intentionally absent, use: `_(None.)_`, `_(No label.)_`, or `_(Icon only.)_` — never leave cells blank without explanation.

### Subsections

- Use `###` for logical groupings (e.g., `### Section: Session`, `### Duration Picker`, `### Bottom Navigation`).
- Keep hierarchy shallow: `#` → `##` → `###` only.

---

## 2. Tone rules (brand-aligned)

Copy must match **PROJECT.md** and `.team/CONVENTIONS.md`:

- **Calm**: Short sentences. No hype. No urgency marketing.
- **Non-judgmental**: No praise (“Great job!”), no shame, no streak punishment copy, no “failure” framing for early exit or gaps in history.
- **Understated**: Facts over cheerleading. Streaks and milestones are stated like weather, not trophies.
- **Companion, not warden**: The app explains what *is* and what *works*; it does not police or motivate through guilt.
- **Show, don’t lecture**: Prefer silence, water, and UI affordances over explanatory paragraphs.

### Emotional register (journey)

Match copy to the arc in PROJECT.md: curious on first open → small release when starting → calm during session → gentle re-entry when ending → subtle relationship shift over time. **Never** snap to celebration or surveillance.

### Positive framing for restrictions

- Say what **still works** (calls, SMS, contacts) where relevant.
- Avoid describing what is “blocked” or “limited” in user-facing strings; use neutral or positive framing consistent with banned words (see below). Technical docs for engineers may use precise terms; **UI copy may not**.

### What the app never sounds like

- Cheerleader, coach, therapist, or competitor to other users.
- Analytics dashboard or performance review.
- Gamification or achievement language.

---

## 3. Banned words and phrases

### Mandatory banned words (UI copy)

Never use these in **any** user-facing English string (including buttons, settings, marketing lines inside the app, share sheets, and empty states):

`addiction`, `limit`, `block`, `detox`, `digital`, `wellbeing`

*(Source: `.team/CONVENTIONS.md` / PROJECT.md — same list.)*

### Additional banned patterns (content-writer extensions)

Avoid in user-facing copy unless a lawyer-approved external document requires an exact term:

| Pattern | Why |
| -------- | ----- |
| `Great job!`, `Well done!`, `You did it!`, `Congrats`, `Awesome` | Celebratory / evaluative — conflicts with understated voice |
| `Stay strong`, `Keep going`, `Don’t give up` | Coachy / pressure |
| `We missed you`, `Welcome back!` (with exclamation), `Hurry` | Emotional manipulation or false enthusiasm |
| Emoji or emoticons | Project-wide ban |
| Fake stats, placeholder metrics in copy | Anti-pattern per DESIGN.md |
| `streak!`, `record!`, leaderboard-style hype | Gamification |
| Surveillance tone: exact minutes in history lines, “You used…”, “Screen time” | PROJECT anti-metrics / persona |

### Allowed technical vocabulary

- **“Session”** is acceptable in accessibility strings, developer-facing notes, and internal band labels **when** the visible UI uses water-first language; some onboarding screens intentionally avoid the word “session” on-screen — follow each screen’s **Notes**.
- **“Water”** is the preferred metaphor for duration and progress in user-visible copy.

---

## 4. Punctuation and capitalization

### Periods vs exclamation marks

- **Default**: End standalone UI sentences and short prompts with a **period** (e.g., `Tap to begin.`, `Welcome back.`).
- **Exclamation marks**: Do **not** use in normal product copy. Reserve for genuine legal/safety warnings only if ever required — default is none.
- **Questions**: Allowed when truly asking (e.g., `How long do you want the water?`). No rhetorical question stacks.

### Capitalization

- **Sentence case** for headlines, body, button labels, and sheet titles unless a design spec calls for **all-caps** for a **Space Grotesk** label role (see DESIGN.md — labels are uppercase in UI; in **content artifacts**, write label copy as it should read in sentence case in tables, and note uppercase in **Notes** when implementation uses small caps / all-caps).
- **Product name**: `Wane` — always capitalized, never `WANE` in prose.
- **CTAs**: `Start`, `Done`, `Next`, `Continue` — capitalize first word only; no ALL CAPS except the deliberate confirmation token **`EXIT`** in emergency exit flows.

### Contractions

- Prefer **light** use: `It's`, `You'll`, `Don't` where natural — never at the cost of clarity for ESL readers (v1 is English; v1.1 adds more locales).

### Hyphens and separators

- Use **` · `** between parallel nav or tab labels in documentation tables when listing items on one line (match existing screens).
- **En dash** — rarely; prefer periods or commas for readability.

---

## 5. Copy length (targets and hard caps)

Lengths assume **English**; other languages may expand — stay under caps in English to leave room for localization.

| Type | Target | Hard max |
| ------ | ------ | -------- |
| **Screen title** (settings, sheets) | 1 word – 3 words | 28 characters |
| **Headline** (onboarding/marketing hero) | 6 – 10 words | 72 characters |
| **Body line** (single line under headline) | 8 – 14 words | 96 characters |
| **Setting label** | 1 – 3 words | 24 characters |
| **Setting description** | 1 short sentence | 72 characters |
| **Primary CTA** | 1 word preferred | 20 characters |
| **Secondary CTA** | 1 – 3 words | 28 characters |
| **Toolbar / tab label** | 1 word | 12 characters |
| **Tooltip / short hint** | 1 sentence | 72 characters |
| **Empty state** | 1 sentence | 96 characters |
| **Error or recovery** (if shown) | 1 sentence | 120 characters |
| **Share / external blurb** | 2 sentences max | 240 characters |
| **Screen reader string** | Complete, clear sentence(s) | 180 characters (split announcements in **Notes** if needed) |

**Water screen**: Default visible copy is **zero lines**; hints ≤ one short sentence, time-limited.

---

## 6. Naming

### Screen files

- Pattern: **`NN-kebab-slug.md`**
  - `NN`: Two-digit order (`01`, `02`, …).
  - `kebab-slug`: lowercase, hyphens, no filler words (`welcome`, `session-complete`, `settings-main`).

### Section headings inside a file

- Use **Title Case** for the H1 screen name after the em dash.
- Use **sentence case** for `##` / `###` headings (`## Microcopy`, `### Section: Auto-Lock`).

### Stable IDs for engineering handoff (optional)

- When useful, refer to elements consistently: `screen-{slug}-{element}` in **Notes** only — not required unless Frontend Dev asks.

---

## 7. Accessibility text

### Format

Use a dedicated `## Accessibility` section with a table:

| Column | Content |
| -------- | -------- |
| **Element** | Short name matching UI (e.g., `**Start button**`) |
| **Screen reader text** | Full announcement. Include state: “Currently [on/off].” |

### Writing rules for a11y strings

- **Complete sentences** with punctuation.
- Include **role and outcome**: what happens when activated.
- Replicate **calm tone**; no extra enthusiasm.
- For **live regions**, note timing in **Notes** (e.g., “announce once on completion”).
- **Never** rely on color-only information in the spoken string; say the meaning (“on,” “off,” “selected”).

### Example row

`| **Done button** | "Done. Return to home screen." |`

---

## 8. Localization readiness

PROJECT.md: English first; **Hindi, Spanish, Portuguese, Japanese** planned for v1.1.

### Write for translation

- **No idioms** that do not translate (e.g., avoid obscure sports metaphors).
- **One idea per string** — do not concatenate sentence fragments across variables in ways that break grammar in other languages.
- **Avoid embedded HTML** in copy artifacts; use plain text. Mark emphasis in **Notes**, not in translatable strings.
- **Consistent terminology**: pick one term for core concepts (`water`, `session` in meta/a11y) and reuse — maintain a glossary in this file when new terms ship.

### Placeholders

- Use named placeholders in descriptions: `[N]`, `[X]`, `[theme name]`, `[Day, Month Date]` — never rely on word order that only works in English.

### Expansion budget

- English UI at **max** caps should leave **~30%** shorter than the hard max for longer languages (German, etc.) where applicable.

### Locale-agnostic behavior

- Dates and numbers: specify **patterns** in screen docs (e.g., “localized date format”) rather than hardcoding `MM/DD` in user-visible examples.

---

## 9. Content types — specific rules

### Headlines

- Sora voice in UI (see DESIGN.md): short, geometric breathing room — avoid stacked clauses.
- Ask a real question or state one calm fact — not two ideas fighting.

### Body

- DM Sans voice: readable, **one thought per line** where possible.
- No bullet lists in primary onboarding body on-screen (artifacts may use bullets in **Notes**).

### CTAs

- Verbs or single acknowledgments: `Start`, `Done`, `Next`, `End`, `Continue`.
- No question CTAs unless the screen is a choice (`Cancel` / `End` pairing).

### Microcopy (tooltips, hints)

- Optional, **short**, dismissible. Never blame.

### Errors

- Prefer **silent prevention** (e.g., button disabled) over error copy when possible — see Emergency Exit screen.
- If a message is required: neutral, **what happened + what to do**, no personality.

### Empty states

- Warm, forward-looking, not corrective (e.g., `Your water will appear here.`).
- No illustration of “failure.”

### Success / completion

- **No confetti copy**. Session complete uses minimal fade text — see Screen 07 pattern.

### History and “stats”

- **Bands and words**, not precise minutes, in user-visible history copy — see Screen 18.
- No “personal record” or competitive language.

---

## 10. Typography alignment (handoff)

From **DESIGN.md** (for coordination with UI — copy length and hierarchy should match):

| Role | Font | Content writer focus |
| ----- | ----- | --------------------- |
| Headlines / display | **Sora** | Fewer, shorter words; breathing room |
| Body | **DM Sans** | Plain, readable, no jargon |
| Labels / metadata | **Space Grotesk** | Uppercase in UI; keep label strings short |

---

## 11. Self-check before shipping content

1. **Specific enough?** Another writer can produce a new screen file using **Section 1** structure and tables without guessing.
2. **Aligned with PROJECT.md?** Calm, non-judgmental, companion positioning; no banned words; no anti-metrics tone.
3. **All content types covered?** Headlines, body, CTAs, microcopy, errors, empty states, success — each has rules in **Sections 5 and 9**.
4. **A11y + localization?** Tables follow **Section 7**; strings follow **Section 8**.

---

## 12. Revision history

| Date | Change |
| ------ | ------ |
| 2026-04-13 | Initial Content Writer conventions for Wane |
