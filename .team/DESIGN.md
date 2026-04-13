# Design System: Wane

> **Source of truth**: All values extracted from the approved design prototype (`design/Focus Mode App/`).
> Full audit with screen-by-screen specs, component catalog, and Compose-ready code in `.team/artifacts/ui-designer/design-audit.md`.

## 1. Visual Theme & Atmosphere

Wane is a premium Android focus app: the phone becomes a basic phone behind a full-screen water animation. The water **is** the product -- award-nominated agency polish. Atmosphere is **Art Gallery Airy**: the animation owns the canvas; chrome, copy, and controls recede into generous darkspace so nothing competes with the fluid surface. **Motion** is **Cinematic Choreography**: the drain/fill reads as the brand; every interactive element uses spring physics (stiffness ~100, damping ~20). Target: ages 16-42, broad; tone is calm and dignified, never preachy.

## 2. Color Palette & Roles

### Backgrounds (dark gradient system)

- **Background Deep** `#0A1628` -- Primary gradient start (home, session, onboarding)
- **Background Deep Mid** `#0D1F3C` -- Gradient midpoint (home screen via)
- **Background Deep End** `#060E1A` -- Gradient end; session complete overlay base
- **Background Abyss** `#050C16` -- Session/onboarding gradient terminus
- **Background Settings** `hsl(220, 42%, 10%)` / `#0F1624` -- Settings and bottom sheets (solid)
- **Background Overlay** `#060E1A` @ 80% -- Session complete scrim

### Accent & Brand

- **Accent Primary** `#38A3DC` -- Single accent for primary CTAs, active states, ambient glow, focus rings
- **Accent Light** `#64B8E8` -- Active progress dots, section labels, icon highlights

### Text (white-alpha scale)

- `white/90` -- Primary text (logo, duration number)
- `white/80` -- Secondary (icons, session complete title)
- `white/70` -- Tertiary (done button text)
- `white/60` -- Status bar elements
- `white/30` -- Muted labels ("min", chevrons, subtitles)
- `white/20` -- Subtle ("Begin focus session", watermarks)
- `white/15` -- Ghost (session "End" button idle)
- `white/40` -- Ghost active (pressed states)

### Named Colors

- **Crystalline** `hsl(220, 40%, 97%)` / `#F4F6FA` -- Headlines, settings labels, CTA text
- **Muted Tide** `hsl(215, 6%, 56%)` / `#888E96` -- Secondary text, icons, settings values
- **Body Text** `hsl(215, 6%, 68%)` / `#A8ADB2` -- Onboarding body copy
- **Dot Inactive** `hsl(215, 6%, 30%)` / `#484C51` -- Inactive progress dots
- **Divider** `hsla(220, 40%, 14%, 0.8)` / `#151F32` @ 80% -- Settings row borders

### Surfaces (glass effects)

- **Surface Glass** `white/10` -- Toolbar buttons, avatars
- **Surface Dim** `white/5` -- Start button, done button backgrounds
- **Border Subtle** `white/20` -- Button borders, toolbar borders

### Water Animation Palette

- Wave layers: `rgba(56,149,211,0.3)`, `rgba(40,120,190,0.4)`, `rgba(30,100,170,0.3)`
- Gradient: `rgba(56,163,220,0.85)` -> `rgba(40,130,195,0.9)` -> `rgba(25,95,165,0.92)` -> `rgba(15,60,120,0.95)`
- Caustics: `rgba(120,200,255,0.12)` center, transparent edge

**Color constraints:** Dark mode only. Maximum one accent color; no neon, no purple, no second accent competing with Accent Primary.

## 3. Typography Rules

- **Headlines/Display:** **Sora** -- Geometric, modern, distinctive. Weights 200-600. Track-tight for headlines, wide for logos/watermarks.
- **Body:** **DM Sans** -- Clean readability; relaxed line-height (1.6); comfortable line length on mobile.
- **Labels/Technical:** **Space Grotesk** -- Metadata, counters, section labels, status bar. Always uppercase for labels.

Key sizes (see full audit for exhaustive table):
- Duration display: Sora 64sp / weight 200 (or 300 fallback)
- Onboarding headline: Sora 36sp / -0.03em
- Settings/sheet title: Sora 24sp / -0.02em
- Body/labels: Sora 16sp / DM Sans 16sp (1.6 line-height)
- Metadata/counters: Space Grotesk 12-14sp / uppercase / wide tracking
- Micro labels: Space Grotesk 10sp / uppercase / tracking-widest

**Banned:** Inter, generic serifs, system default UI fonts, Times/Georgia/Garamond.

## 4. Component Stylings

* **Buttons (Primary CTA):** Accent Primary fill, full width, 16dp vertical padding, 12dp corner radius. Press: scale(0.97) + translateY(-1dp).
* **Buttons (Start):** 80dp circle, Surface Dim bg, Border Subtle 1px, Play icon. Press: scale(0.95).
* **Buttons (Ghost/Done):** Pill shape, Surface Dim bg, Border Subtle 1px. Press: scale(0.95).
* **Buttons (Text/End):** No bg/border, Space Grotesk 10sp uppercase, white/15 idle -> white/40 pressed.
* **Duration Picker:** Vertical stack: chevron up -> baseline-aligned number (Sora 64sp) + "min" label (Space Grotesk 12sp) -> chevron down. Gap 16dp.
* **Settings Panel:** Spring slide-up from bottom (stiffness 100, damping 20). Solid Background Settings, 12dp top corners. Section labels in Accent Light, uppercase. Rows: icon + label left, value right, 1px divider.
* **Bottom Toolbar (session):** Absolute bottom-32dp, centered. 3 circular buttons (56dp), Surface Glass + backdrop-blur + Border Subtle. Phone, Contacts, and SMS icons, 20dp, white. Gap 24dp between buttons.
* **Progress Dots:** Horizontal row, gap 8dp. Active: 24x4dp pill, Accent Light. Inactive: 8x4dp circle, Dot Inactive. 300ms transition.
* **Toggle Switch:** Track 52x28dp, rounded-full. On: Accent Primary. Off: white/10. Thumb: 24dp white circle, 2dp inset.
* **Water animation:** Full-bleed, edge-to-edge. 3 wave layers + gradient body + 5 caustic lights. 60fps, 2x resolution canvas. No chrome overlays.
* **Inputs:** 56dp height, white/10 bg, white/20 border, 12dp corner. Focus: 2dp Accent Primary ring. Space Grotesk 18sp, uppercase, centered.

**No timer/progress UI on the water screen.** The water level is the only temporal expression.

## 5. Layout Principles

**Platform:** Android mobile only; **single column** everywhere. Minimum **24dp** internal padding. **Touch targets:** minimum 44dp general, 56dp for toolbar icons, wrap smaller elements (gear, end button) in larger hit areas. Generous vertical rhythm between sections. Corner radius: 12dp for sheets/CTAs, rounded-full for circular elements.

## 6. Motion & Interaction

- **Spring feel:** stiffness 100, damping 20 -- weighty, premium, never snappy-linear
- **Screen transitions:** 0.6-0.8s fade with AnimatePresence
- **Stagger pattern:** Home elements at 0.2s intervals (y+20 + fade); Settings rows at 0.05s intervals (y+8 + fade)
- **Interactive feedback:** scale(0.95) for circular buttons, scale(0.97) + translateY(-1dp) for CTAs
- **Water animation:** Continuous 60fps rendering; composite sine waves for organic motion; caustic lights with slow drift

## 7. Anti-Patterns (Banned)

**Global:** No emojis in UI. No Inter font. No pure black `#000000` for surfaces (device frame exception). No neon/outer-glow. No oversaturated accents. No overlapping elements. No three equal cards in a row. No AI copy cliches. No fake statistics.

**Wane-specific:** No timer numbers on the water screen. No usage statistics. No gamification. No guilt-inducing copy. No social comparison.

**Words banned from UI copy:** "addiction", "limit", "block", "detox", "digital", "wellbeing".

**Visual bans:** No chrome covering water; no purple or neon-blue; no second accent color.
