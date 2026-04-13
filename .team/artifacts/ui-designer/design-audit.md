# Wane — Design Audit & Implementation Spec

> **Source of truth:** All values extracted directly from prototype code (`design/Focus Mode App/src/`).
> Where the current `.team/DESIGN.md` conflicts, prototype code wins.
> Differences are called out inline (⚠️) and collected in §8.

---

## 1. Color Tokens

### 1.1 Backgrounds

| Token Name | Value | Hex Approx | Usage | Compose Name |
|---|---|---|---|---|
| `backgroundShell` | `#0f0f0f` | `#0F0F0F` | Outer app shell behind device frame (App.tsx) | `WaneColors.backgroundShell` |
| `backgroundDevice` | `#000000` | `#000000` | Device container background (App.tsx `bg-black`) ⚠️ | `WaneColors.backgroundDevice` |
| `backgroundDeep` | `#0a1628` | `#0A1628` | Home screen gradient start; session gradient start; onboarding gradient start | `WaneColors.backgroundDeep` |
| `backgroundDeepMid` | `#0d1f3c` | `#0D1F3C` | Home screen gradient via (midpoint) | `WaneColors.backgroundDeepMid` |
| `backgroundDeepEnd` | `#060e1a` | `#060E1A` | Home screen gradient end; session complete overlay base | `WaneColors.backgroundDeepEnd` |
| `backgroundAbyss` | `#050c16` | `#050C16` | Session gradient end; onboarding gradient end | `WaneColors.backgroundAbyss` |
| `backgroundSettings` | `hsl(220, 42%, 10%)` | `#0F1624` | Settings panel background | `WaneColors.backgroundSettings` |
| `backgroundOverlay` | `rgba(6, 14, 26, 0.80)` | `#060E1A` @ 80% | Session complete overlay | `WaneColors.backgroundOverlay` |

### 1.2 Accent & Brand

| Token Name | Value | Hex Approx | Usage | Compose Name |
|---|---|---|---|---|
| `accentPrimary` | `#38a3dc` | `#38A3DC` | CTA button fill (Onboarding), ambient glow base, SMS send button ⚠️ | `WaneColors.accentPrimary` |
| `accentLight` | `#64b8e8` | `#64B8E8` | Active progress dot, section labels (Settings), droplets icon (home @ 80%, session @ 60%), SMS icon | `WaneColors.accentLight` |
| `accentGlow` | `rgba(56, 163, 220, 0.08)` | `#38A3DC` @ 8% | Ambient glow on home screen (300×300 blurred circle) | `WaneColors.accentGlow` |

### 1.3 Text Colors (white-alpha scale)

| Token Name | Value | RGBA | Usage | Compose Name |
|---|---|---|---|---|
| `textPrimary` | `white/90` | `rgba(255,255,255,0.90)` | Logo text, duration number | `WaneColors.textPrimary` |
| `textSecondary` | `white/80` | `rgba(255,255,255,0.80)` | Play icon, session complete title, toolbar icons (full) | `WaneColors.textSecondary` |
| `textTertiary` | `white/70` | `rgba(255,255,255,0.70)` | Done button text | `WaneColors.textTertiary` |
| `textStatusBar` | `white/60` | `rgba(255,255,255,0.60)` | Status bar time, battery fill | `WaneColors.textStatusBar` |
| `textMuted` | `white/30` | `rgba(255,255,255,0.30)` | Chevron buttons, "min" label, session duration subtitle | `WaneColors.textMuted` |
| `textSubtle` | `white/20` | `rgba(255,255,255,0.20)` | "Begin focus session", session "Wane" watermark, start button border | `WaneColors.textSubtle` |
| `textGhost` | `white/15` | `rgba(255,255,255,0.15)` | "End" button on session screen | `WaneColors.textGhost` |
| `textGhostActive` | `white/40` | `rgba(255,255,255,0.40)` | "End" button pressed state, battery border | `WaneColors.textGhostActive` |
| `textWhite` | `white` | `rgba(255,255,255,1.00)` | Toolbar phone/SMS icons | `WaneColors.textWhite` |

### 1.4 HSL Named Colors

| Token Name | Value | Hex Approx | Usage | Compose Name |
|---|---|---|---|---|
| `hslCrystalline` | `hsl(220, 40%, 97%)` | `#F4F6FA` | Onboarding title, CTA text, settings title, setting labels ⚠️ | `WaneColors.crystalline` |
| `hslMutedTide` | `hsl(215, 6%, 56%)` | `#888E96` | Step counter, settings gear icon, settings close icon, setting value text, settings row icons ⚠️ | `WaneColors.mutedTide` |
| `hslBodyText` | `hsl(215, 6%, 68%)` | `#A8ADB2` | Onboarding body text | `WaneColors.bodyText` |
| `hslDotInactive` | `hsl(215, 6%, 30%)` | `#484C51` | Inactive progress dots | `WaneColors.dotInactive` |
| `hslDivider` | `hsla(220, 40%, 14%, 0.8)` | `#151F32` @ 80% | Settings row divider border | `WaneColors.divider` |

### 1.5 Surface Colors (white-alpha for glass effects)

| Token Name | Value | RGBA | Usage | Compose Name |
|---|---|---|---|---|
| `surfaceGlass` | `white/10` | `rgba(255,255,255,0.10)` | Toolbar button background, contact avatars | `WaneColors.surfaceGlass` |
| `surfaceDim` | `white/5` | `rgba(255,255,255,0.05)` | Start button background, Done button background | `WaneColors.surfaceDim` |
| `borderSubtle` | `white/20` | `rgba(255,255,255,0.20)` | Start button border, Done button border, toolbar button border | `WaneColors.borderSubtle` |

### 1.6 Water Animation Colors

| Token Name | Value | Usage | Compose Name |
|---|---|---|---|
| `waveLayer1` | `rgba(56, 149, 211, 0.3)` | Front wave layer | `WaneColors.waveLayer1` |
| `waveLayer2` | `rgba(40, 120, 190, 0.4)` | Mid wave layer | `WaneColors.waveLayer2` |
| `waveLayer3` | `rgba(30, 100, 170, 0.3)` | Back wave layer | `WaneColors.waveLayer3` |
| `waterGradientTop` | `rgba(56, 163, 220, 0.85)` | Water body gradient stop 0.0 | `WaneColors.waterGradientTop` |
| `waterGradientUpper` | `rgba(40, 130, 195, 0.9)` | Water body gradient stop 0.3 | `WaneColors.waterGradientUpper` |
| `waterGradientLower` | `rgba(25, 95, 165, 0.92)` | Water body gradient stop 0.7 | `WaneColors.waterGradientLower` |
| `waterGradientBottom` | `rgba(15, 60, 120, 0.95)` | Water body gradient stop 1.0 | `WaneColors.waterGradientBottom` |
| `causticCenter` | `rgba(120, 200, 255, 0.12)` | Caustic light center | `WaneColors.causticCenter` |
| `causticEdge` | `rgba(120, 200, 255, 0.0)` | Caustic light edge (transparent) | `WaneColors.causticEdge` |

---

## 2. Typography Scale

### 2.1 Font Families

| Family | Weights Imported (fonts.css) | Role |
|---|---|---|
| **Sora** | 300, 400, 500, 600 | Headlines, logo, durations, CTAs |
| **DM Sans** | 300, 400, 500 | Body copy, secondary text |
| **Space Grotesk** | 400, 500 | Labels, metadata, counters, technical |

### 2.2 Full Typography Table

| Style Name | Font | Size (px) | Size (sp) | Weight | Letter Spacing | Line Height | Used In | Compose TextStyle |
|---|---|---|---|---|---|---|---|---|
| Duration Display | Sora | 64 (4rem) | 64sp | 200* | default (0) | default | Home → duration number | `displayLarge` |
| Onboarding Headline | Sora | 36 | 36sp | 400 | -0.03em (-1.08px) | default | Onboarding → step title | `headlineLarge` |
| Settings Title | Sora | 24 | 24sp | 400 | -0.02em (-0.48px) | default | Settings → "Settings" title | `headlineMedium` |
| Logo | Sora | 24 (1.5rem) | 24sp | 300 | 0.3em (7.2px) | default | Home → "Wane" logo text | `headlineMedium` (variant) |
| Session Complete Title | Sora | 17.6 (1.1rem) | 18sp | 300 | 0.2em (3.52px) | default | Session → "Session Complete" | `titleLarge` |
| Body / Setting Label | Sora | 16 | 16sp | 400 | default | default | Settings → row labels; Onboarding → CTA button text | `titleMedium` |
| CTA Button | Sora | 16 | 16sp | 400 | -0.01em (-0.16px) | default | Onboarding → Continue/Begin button | `labelLarge` |
| Onboarding Body | DM Sans | 16 | 16sp | 400 | default | 1.6 (25.6px) | Onboarding → step body text | `bodyLarge` |
| Step Counter | Space Grotesk | 14 | 14sp | 400 | 0.05em (0.7px) | default | Onboarding → "01 / 03" | `bodyMedium` |
| Setting Value | DM Sans | 14 | 14sp | 400 | default | default | Settings → row values | `bodyMedium` (variant) |
| Done Button | Sora | 13.6 (0.85rem) | 14sp | 400 | default | default | Session complete → "Done" | `bodySmall` |
| Duration Subtitle | DM Sans | 12.8 (0.8rem) | 13sp | 400 | default | default | Session complete → "X minutes of focus" | `bodySmall` (variant) |
| Status Bar / Min Label | Space Grotesk | 12 (0.75rem) | 12sp | 400 | 0.1em** (widest) | default | Status bar time; Home → "min" label | `labelSmall` |
| Section Label | Space Grotesk | 12 | 12sp | 400 | 0.08em (0.96px) | default | Settings → section headings | `labelSmall` (variant) |
| Session Watermark | Space Grotesk | 11.2 (0.7rem) | 11sp | 400 | 0.3em (3.36px) | default | Session → "Wane" text | `labelSmall` (custom) |
| Helper Text | Space Grotesk | 10.4 (0.65rem) | 10sp | 400 | 0.1em (widest) | default | Home → "Begin focus session" | `microLabel` (custom) |
| End Button | Space Grotesk | 9.6 (0.6rem) | 10sp | 400 | 0.1em (widest) | default | Session → "End" button | `microLabel` (custom) |

> *Weight 200 is used in code but NOT in the Google Fonts import (which starts at 300). The browser falls back to 300. For Compose, use `FontWeight.ExtraLight` (200) and include the weight in the Sora font resource, OR use `FontWeight.Light` (300) to match browser rendering.

> **Tailwind `tracking-widest` = `letter-spacing: 0.1em`.

### 2.3 Text Transform & Case

All labels, watermarks, and the "min" label use `text-transform: uppercase`. All headline tracking uses tight negative values. The logo and watermark "Wane" are uppercase with very wide tracking (0.3em).

---

## 3. Spacing Scale

### 3.1 Spacing Tokens

| Token | CSS / Tailwind | Value (px) | Value (dp) | Usage |
|---|---|---|---|---|
| `spacing-1` | `gap-1`, `ml-1` | 4 | 4 | Battery icons gap; play icon left offset |
| `spacing-2` | `gap-2`, `mb-2`, `p-2` | 8 | 8 | Progress dots gap; session complete title-to-subtitle; gear/close button padding |
| `spacing-3` | `gap-3`, `pt-3`, `mb-3` | 12 | 12 | Logo icon-to-text gap; status bar top padding; section label margin-bottom; settings row icon gap |
| `spacing-4` | `gap-4`, `mb-4`, `py-4` | 16 | 16 | Duration picker internal gap; onboarding title-to-body; setting row vertical padding; CTA button vertical padding |
| `spacing-6` | `gap-6`, `mt-6`, `right-6`, `px-6` | 24 | 24 | Bottom toolbar button gap; helper text margin-top; progress dots margin-top; settings horizontal padding; gear/end button right offset |
| `spacing-8` | `px-8`, `p-8`, `top-8`, `pt-8` | 32 | 32 | Status bar horizontal padding; onboarding content padding; gear icon top offset; settings header top padding; done button horizontal padding |
| `spacing-10` | `mb-10` | 40 | 40 | Session complete subtitle-to-button |
| `spacing-12` | `mb-12`, `top-12` | 48 | 48 | Onboarding step text bottom margin; session watermark/end button top offset |
| `spacing-16` | `mb-16`, `pb-16` | 64 | 64 | Logo-to-picker gap; picker-to-button gap; onboarding bottom padding; settings content bottom padding |

### 3.2 Component Dimensions

| Element | CSS | Size (px) | Size (dp) | Notes |
|---|---|---|---|---|
| Device frame width | `max-w-[420px]` | 420 | 420 | Max container width |
| Device frame height | `max-h-[900px]` | 900 | 900 | Max container height |
| Start button | `w-20 h-20` | 80×80 | 80×80 | Rounded full, circular |
| Toolbar button | `w-14 h-14` | 56×56 | 56×56 | Rounded full, circular |
| Chevron icon | `w-6 h-6` | 24×24 | 24×24 | Duration picker up/down |
| Play icon | `w-7 h-7` | 28×28 | 28×28 | Inside start button |
| Toolbar icon | `w-5 h-5` | 20×20 | 20×20 | Phone and SMS icons |
| Settings gear icon | SVG `width="20" height="20"` | 20×20 | 20×20 | Custom SVG |
| Settings row icon | `size={18}` | 18×18 | 18×18 | Lucide icons |
| Logo droplets icon | `w-8 h-8` | 32×32 | 32×32 | Home screen |
| Session complete icon | `w-10 h-10` | 40×40 | 40×40 | Droplets icon |
| Battery icon | `w-4 h-2` | 16×8 | 16×8 | Status bar |
| Active progress dot | explicit `width: 24` | 24×4 | 24×4 | Pill shape |
| Inactive progress dot | explicit `width: 8` | 8×4 | 8×4 | Circle shape |
| Ambient glow | `w-[300px] h-[300px]` | 300×300 | 300×300 | Blurred circle |
| Onboarding body max-width | `maxWidth: 320` | 320 | 320 | Text line length cap |

---

## 4. Corner Radius

| Token | Value (px) | Value (dp) | Usage |
|---|---|---|---|
| `radiusDevice` | 40 (`rounded-[2.5rem]`) | 40 | Device container frame (sm+ breakpoint only) |
| `radiusXL` | 12 (`rounded-xl`) | 12 | CTA button, settings panel top corners |
| `radiusFull` | 9999 (`rounded-full`) | 9999 | Start button, toolbar buttons, done button, progress dots, toolbar button, contact avatars |
| `radiusSm` | 2 (`rounded-sm`) | 2 | Battery icon border |
| `radiusCaustic` | variable (30±15) | — | Caustic circles (render only) |
| `radius1px` | 1 (`rounded-[1px]`) | 1 | Battery fill inner |

> DESIGN.md specifies `ROUND_TWELVE` (12px) as the default. The prototype agrees for settings and CTA buttons, but uses `rounded-full` for all interactive circular elements and 40px for the device frame.

---

## 5. Motion Specs

### 5.1 Spring Parameters

| Parameter | Value | Usage |
|---|---|---|
| `type` | `"spring"` | Settings panel slide-up |
| `stiffness` | `100` | Settings panel |
| `damping` | `20` | Settings panel |

> These match DESIGN.md's recommendation (stiffness ~100, damping ~20).

### 5.2 Transition Durations & Easing

| Transition | Duration | Easing | Usage |
|---|---|---|---|
| Screen fade (onboarding ↔ home) | 0.6s | default (ease) | `AnimatePresence mode="wait"` |
| Screen fade (→ session) | 0.8s | default | Session screen entry |
| Onboarding step content | 0.4s | `easeOut` | Step text slide + fade |
| Settings item stagger | 0.3s per item | default | Settings row entrance |
| Progress dot width | 0.3s (CSS `duration-300`) | default (ease) | Active/inactive dot transition |
| Button press | instant (CSS `transition-transform`) | default | Scale on `:active` |
| Opacity transitions | instant (CSS `transition-opacity`) | default | Chevron disabled state |
| Color transitions | instant (CSS `transition-colors`) | default | End button press color |

### 5.3 Stagger & Entrance Patterns

| Pattern | Details | Usage |
|---|---|---|
| Home screen elements | Delays: 0.2s (logo), 0.4s (picker), 0.6s (start btn), 0.8s (helper text). Each slides up 20px + fades in. | Home screen initial load |
| Settings items | Delay: `i * 0.05s` per item. Each slides up 8px + fades in over 0.3s. | Settings list items |
| Onboarding step swap | Current step: slides up 16px + fades out. New step: slides up 16px + fades in. 0.4s, easeOut. | Step content transitions |

### 5.4 Interactive Feedback

| Interaction | Transform | Usage |
|---|---|---|
| Button press (primary) | `scale(0.95)` (active:scale-95) | Start button, toolbar buttons |
| Button press (CTA) | `scale(0.97), translateY(-1px)` | Onboarding CTA, gear, settings close |
| End button press | color change only (`white/15` → `white/40`) | Session "End" text |

### 5.5 Water Animation Specification

#### Canvas Setup
- Resolution: 2× device pixels (`canvas.width = offsetWidth * 2`)
- Frame timing: `requestAnimationFrame` (60fps target)
- Time increment per frame: `+0.015` (unitless accumulator)

#### Wave Layers (background-to-foreground)

| Layer | Amplitude | Frequency | Speed | Color |
|---|---|---|---|---|
| Layer 3 (back) | 6px | 0.018 | 1.5 | `rgba(30, 100, 170, 0.3)` |
| Layer 2 (mid) | 8px | 0.012 | 0.8 | `rgba(40, 120, 190, 0.4)` |
| Layer 1 (front) | 12px | 0.008 | 1.2 | `rgba(56, 149, 211, 0.3)` |

Each wave layer uses a composite sine formula:
```
y = baseY
  + sin(x * freq + t * speed) * amp
  + sin(x * freq * 0.5 + t * speed * 0.7) * amp * 0.5
```

#### Main Water Fill
Sine composite over the gradient body:
```
y = baseY
  + sin(x * 0.01 + t) * 10
  + sin(x * 0.006 + t * 0.6) * 6
```

#### Main Water Gradient

| Stop Position | Color |
|---|---|
| 0.0 (surface) | `rgba(56, 163, 220, 0.85)` |
| 0.3 | `rgba(40, 130, 195, 0.9)` |
| 0.7 | `rgba(25, 95, 165, 0.92)` |
| 1.0 (bottom) | `rgba(15, 60, 120, 0.95)` |

Direction: vertical, from `baseY` (surface) to canvas bottom.

#### Caustic Lights

| Parameter | Value |
|---|---|
| Count | 5 |
| Horizontal position | `(sin(t * 0.3 + i * 1.8) * 0.5 + 0.5) * canvasWidth` |
| Vertical position | `baseY + (canvasHeight - baseY) * (0.2 + i * 0.15)` |
| Base radius | 30 |
| Radius oscillation | `±15` (`30 + sin(t + i) * 15`) |
| Center color | `rgba(120, 200, 255, 0.12)` |
| Edge color | `rgba(120, 200, 255, 0.0)` |
| Gradient type | Radial |

#### Water Level Mapping
- `waterLevel` is a float from 1.0 (full) to 0.0 (empty)
- `baseY = canvasHeight * (1 - waterLevel)` — surface rises from bottom at level=1 to top at level=0 (draining moves surface down)
- Update interval during session: every 50ms via `setInterval`

---

## 6. Component Catalog

### 6.1 Start Button (Home Screen)

```
Shape:       Circle, 80dp diameter
Background:  white/5 (rgba 255,255,255,0.05)
Border:      1px solid white/20
Icon:        Play (Lucide), 28×28dp, white/80, 4dp left offset (visual centering)
Press state: scale(0.95) via CSS transition-transform
Corner:      rounded-full
```

### 6.2 CTA Button (Onboarding)

```
Shape:       Rectangle, full width, 16dp vertical padding
Background:  #38a3dc (accentPrimary)
Text:        Sora 16sp, -0.01em, hsl(220,40%,97%) (crystalline)
Corner:      12dp (rounded-xl)
Press state: scale(0.97), translateY(-1dp)
Label:       "Continue" (steps 1-2), "Begin" (step 3)
```

### 6.3 Done Button (Session Complete)

```
Shape:       Pill, px-32dp py-12dp
Background:  white/5
Border:      1px solid white/20
Text:        Sora 13.6sp, white/70
Corner:      rounded-full
Press state: scale(0.95)
```

### 6.4 End Button (Session)

```
Type:        Ghost text button (no background, no border)
Position:    absolute, top 48dp, right 24dp
Text:        Space Grotesk 9.6sp, uppercase, tracking 0.1em
Color idle:  white/15
Color press: white/40 (transition-colors)
```

### 6.5 Settings Gear Button (Home)

```
Position:    absolute, top 32dp, right 24dp
Padding:     8dp touch area extension
Icon:        Custom SVG 20×20dp
             - Circle r=3 at center
             - 8 tick marks radiating from center
             - Stroke: hsl(215,6%,56%), width 1.5, linecap round
Press state: scale(0.97)
```

### 6.6 Duration Picker (Home)

```
Layout:      Vertical stack, center-aligned, gap 16dp
Chevron Up:  24×24dp icon, white/30, disabled:opacity-10
Number:      Sora 64sp weight-200, white/90
             Baseline-aligned with "min" label (gap 8dp)
Min Label:   Space Grotesk 12sp, uppercase, tracking-widest, white/30
Chevron Down: 24×24dp icon, white/30, disabled:opacity-10
Values:      [5, 10, 15, 20, 25, 30, 45, 60, 90, 120]
```

### 6.7 Progress Dots (Onboarding)

```
Layout:      Horizontal row, center-aligned, gap 8dp, margin-top 24dp
Dot height:  4dp
Active:      width 24dp, #64b8e8 (accentLight), rounded-full
Inactive:    width 8dp, hsl(215,6%,30%) (~#484C51), rounded-full
Transition:  width + color, 300ms ease
```

### 6.8 Bottom Toolbar (Session)

```
Position:    absolute, bottom 32dp, left 0, right 0 (centered content)
Layout:      Horizontal flex, center-justified, gap 24dp
Button size: 56×56dp (matches DESIGN.md 56dp minimum)
Background:  white/10 + backdrop-blur-md (12dp blur)
Border:      1px solid white/20
Corner:      rounded-full
Icon size:   20×20dp, color white
Press state: scale(0.95)
Buttons:     Phone (Phone icon), Contacts (Users icon), SMS (MessageSquare icon) — 3 buttons per user approval
```

### 6.9 Settings Panel

```
Entry:       Spring animation from y=100% to y=0; stiffness 100, damping 20
Background:  hsl(220,42%,10%) (~#0F1624), solid
Corner:      12dp top-left and top-right only
Z-index:     50 (above all content)
Scrollable:  overflow-y auto

Header:
  Padding:   px-24dp pt-32dp pb-16dp
  Title:     Sora 24sp, -0.02em tracking, crystalline color
  Close:     X icon 20×20, hsl(215,6%,56%), p-8dp touch area, scale(0.97) on press

Section Label:
  Font:      Space Grotesk 12sp, uppercase, tracking 0.08em
  Color:     #64b8e8 (accentLight)
  Margin:    mt-32dp mb-12dp

Setting Row:
  Padding:   py-16dp
  Divider:   1px solid hsla(220,40%,14%,0.8)
  Left:      Icon (18×18, stroke 1.5, hsl(215,6%,56%)) + gap 12dp + Label (Sora 16sp, crystalline)
  Right:     Value text (DM Sans 14sp, hsl(215,6%,56%))

Stagger:     Each row delays entry by i * 50ms; slides up 8dp + fades in over 300ms
Footer pad:  pb-64dp
```

### 6.10 Session Complete Overlay

```
Coverage:    Full screen (absolute inset-0)
Background:  rgba(6,14,26,0.80) (backgroundDeepEnd @ 80%)
Blur:        backdrop-blur-sm (4dp blur)
Z-index:     20
Layout:      Flex column, center-center

Content (top to bottom):
  Icon:      Droplets 40×40dp, accentLight @ 60%, mb-16dp
  Title:     Sora 18sp weight-300, 0.2em tracking, uppercase, white/80, mb-8dp
  Subtitle:  DM Sans 13sp, white/30, mb-40dp
  Button:    Done button (see §6.3)

Entry:       opacity 0→1 (no exit defined in code)
```

### 6.11 Status Bar (Mock)

```
Position:    absolute top-0, left-0, right-0, z-40
Padding:     px-32dp pt-12dp pb-4dp
Layout:      Flex row, space-between, center-aligned

Left:        Time string (HH:MM), Space Grotesk 12sp, white/60
Right:       Battery icon — 16×8dp rect, 2dp corner, 1px border white/40
             Inner fill: inset 1px all sides, white/60, 1px corner
Visibility:  Hidden during onboarding; shown on home + session
```

### 6.12 Onboarding Step Content

```
Position:    Absolute bottom-aligned content area
Padding:     p-32dp pb-64dp
Layout:      Flex column, justify-end

Step text block (mb-48dp):
  Counter:   Space Grotesk 14sp, 0.05em tracking, hsl(215,6%,56%), mb-12dp
  Title:     Sora 36sp, -0.03em tracking, crystalline, mb-16dp
  Body:      DM Sans 16sp, line-height 1.6, max-width 320dp, hsl(215,6%,68%)

CTA Button:  (see §6.2) below step text
Dots:        (see §6.7) below CTA
```

### 6.13 Ambient Glow (Home)

```
Position:    absolute, top 33% vertically, center horizontally
Size:        300×300dp
Shape:       Circle (rounded-full)
Color:       accentPrimary (#38a3dc) @ 8% opacity
Filter:      blur-3xl (64dp blur radius)
```

---

## 7. Screen Specs

### Screen 1 — Welcome (Onboarding Step 1)

```
┌─────────────────────────────────────────┐
│                                         │
│          [WaterCanvas level=0.33]       │
│          (water fills bottom ~33%)       │
│                                         │
│                                         │
│                                         │
│                                         │
│                                         │
│   01 / 03          ← counter            │
│   Water rises.     ← headline           │
│   A living surface replaces...  ← body  │
│                                         │
│   ┌─────────────────────────────┐       │
│   │         Continue            │       │
│   └─────────────────────────────┘       │
│          ━━━━━━  ──  ──   ← dots        │
└─────────────────────────────────────────┘

Background: gradient #0a1628 → #050c16
Content anchored to bottom: p-32dp, pb-64dp
Step text area: mb-48dp
Counter: Space Grotesk 14sp, mutedTide color
Title: Sora 36sp, -0.03em, crystalline
Body: DM Sans 16sp, 1.6 line-height, bodyText color
CTA: full-width, #38a3dc, rounded-12dp, py-16dp
Dots: 3 dots, first active (24×4dp #64b8e8), others inactive (8×4dp)
```

### Screen 2 — Duration Setup (Onboarding Step 2)

Same layout as Screen 1. Differences:
- `waterLevel` = 0.67 (water fills ~67%)
- Counter: "02 / 03"
- Title: "You choose the duration."
- Body: "Fifteen minutes, thirty, an hour. The water drains slowly over that time."
- CTA: "Continue"
- Dots: second dot active

### Screen 3 — Auto-Lock Intro (Onboarding Step 3)

Same layout as Screen 1. Differences:
- `waterLevel` = 1.0 (water fills 100%)
- Counter: "03 / 03"
- Title: "Touch it. Tilt it."
- Body: "The water responds to you. When it's gone, you're back."
- CTA: "Begin" (not "Continue")
- Dots: third dot active

### Screen 4 — Home

```
┌─────────────────────────────────────────┐
│  12:34                      ⚙ [gear]   │
│                                         │
│                                         │
│        ○ ambient glow (300dp, blur)     │
│                                         │
│          💧 Wane         ← logo         │
│                                         │
│             ∧             ← chevron up  │
│            25              ← duration   │
│           min              ← label      │
│             ∨             ← chevron dn  │
│                                         │
│           (▶)             ← start btn   │
│        Begin focus session              │
│                                         │
└─────────────────────────────────────────┘

Background: linear gradient top-to-bottom
  from: #0a1628
  via:  #0d1f3c
  to:   #060e1a

Status bar: top, z-40
  Time: left, Space Grotesk 12sp, white/60
  Battery: right, 16×8dp outline

Gear: top-32dp right-24dp, custom SVG 20×20, hsl(215,6%,56%)

Logo: center, droplets icon 32×32 (#64b8e8 @ 80%) + "WANE" Sora 24sp wt-300 tracking-0.3em white/90
  Gap between icon and text: 12dp
  Margin below: 64dp

Duration picker: centered column, gap-16dp, mb-64dp
  Chevrons: 24×24dp, white/30, disabled: opacity 10%
  Number + "min": baseline-aligned row, gap-8dp

Start button: 80×80dp circle, white/5 bg, white/20 border
  Play icon: 28×28dp, white/80, ml-4dp
  Below: "BEGIN FOCUS SESSION" Space Grotesk 10sp, white/20, mt-24dp

Entrance animation: staggered 0.2s intervals, y+20→0 + fade
```

### Screen 5 — Water Screen (Active Session)

```
┌─────────────────────────────────────────┐
│  12:34                                  │
│              WANE              End       │
│                                         │
│                                         │
│  ~~~~~~~~~~~~~~~~~~~~~~~~~ ← waves      │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░ ← water     │
│  ░░░░░ caustic lights ░░░░░             │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░             │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░             │
│                                         │
│         (📞)    (💬)       ← toolbar    │
│                                         │
└─────────────────────────────────────────┘

Background: gradient #0a1628 → #050c16
WaterCanvas: absolute, full bleed, pointer-events none
  (see §5.5 for full water animation spec)

Watermark: "WANE" Space Grotesk 11sp, tracking 0.3em, uppercase, white/20
  Position: top-48dp, center-aligned

End button: top-48dp, right-24dp
  Space Grotesk 10sp, uppercase, widest tracking, white/15
  Press: white/40

Bottom toolbar: bottom-32dp, centered
  3× circular buttons 56×56dp, gap 24dp
  white/10 bg, backdrop-blur-md, white/20 border
  Phone icon + Users (Contacts) icon + MessageSquare icon, 20×20dp, white

Status bar: same as home (z-40 above water)
```

### Screen 6 — Emergency Exit Sheet (DESIGNED — not in prototype)

```
┌─────────────────────────────────────────┐
│  [Water Screen visible behind]          │
│                                         │
│  ░░░ dimmed backdrop ░░░░░░░░░░░░░░░░░  │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │         Emergency Exit              ││
│  │                                     ││
│  │  Type EXIT to end your session.     ││
│  │                                     ││
│  │  ┌───────────────────────────────┐  ││
│  │  │          E X I T              │  ││
│  │  └───────────────────────────────┘  ││
│  │                                     ││
│  │  ┌───────────────────────────────┐  ││
│  │  │         End Session           │  ││
│  │  └───────────────────────────────┘  ││
│  │                                     ││
│  │           Cancel                    ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘

Scrim:
  Color:     rgba(6, 14, 26, 0.60)
  Blur:      backdrop-blur-sm (4dp)

Bottom Sheet:
  Background: hsl(220, 42%, 10%) (~#0F1624) — same as settings
  Corner:     12dp top-left, top-right
  Entry:      Spring y=100%→0, stiffness 100, damping 20
  Padding:    px-24dp, pt-32dp, pb-32dp

Title:
  Font:      Sora 24sp, -0.02em tracking
  Color:     crystalline (hsl(220,40%,97%))
  Margin:    mb-12dp

Body:
  Font:      DM Sans 16sp, line-height 1.6
  Color:     bodyText (hsl(215,6%,68%))
  Margin:    mb-24dp

Input Field:
  Height:    56dp
  Background: white/10
  Border:    1px solid white/20
  Corner:    12dp
  Font:      Space Grotesk 18sp, uppercase, tracking 0.2em, center-aligned
  Color:     crystalline when filled, white/30 placeholder
  Focus ring: 2dp solid accentPrimary (#38a3dc)

End Session Button (enabled when input = "EXIT"):
  Style:     Same as CTA (§6.2): full-width, #38a3dc, 12dp corner, py-16dp
  Disabled:  opacity 0.3, no press feedback
  Margin:    mt-24dp

Cancel Button:
  Style:     Ghost text, center-aligned
  Font:      Sora 16sp, mutedTide color
  Touch:     Full width, py-16dp
  Margin:    mt-8dp
```

### Screen 7 — Session Complete

```
┌─────────────────────────────────────────┐
│                                         │
│  ░░░ blurred water behind ░░░░░░░░░░░  │
│                                         │
│                                         │
│              💧                         │
│        Session Complete                 │
│        25 minutes of focus              │
│                                         │
│            [ Done ]                     │
│                                         │
│                                         │
└─────────────────────────────────────────┘

Layer: absolute inset-0, z-20, on top of water canvas
Background: rgba(6,14,26,0.80) + backdrop-blur-sm (4dp)
Layout: flex column, center-center

Icon:      Droplets 40×40dp, #64b8e8 @ 60%, mb-16dp
Title:     "SESSION COMPLETE" — Sora 18sp wt-300, tracking 0.2em, uppercase, white/80, mb-8dp
Subtitle:  "{N} minutes of focus" — DM Sans 13sp, white/30, mb-40dp
Button:    "Done" — pill shape, px-32dp py-12dp, white/5 bg, white/20 border, Sora 14sp, white/70

Entry: opacity 0→1
Exit:  (not animated in prototype — recommend opacity 1→0, 0.4s)
```

### Screen 14 — Settings

```
┌─────────────────────────────────────────┐
│                                         │
│  Settings                          ✕    │
│                                         │
│  EXPERIENCE                ← section    │
│  ─────────────────────────────────────  │
│  🔊  Ambient Sound      Lapping waves  │
│  ─────────────────────────────────────  │
│  🕐  Default Duration   15 minutes     │
│  ─────────────────────────────────────  │
│                                         │
│  ABOUT                     ← section    │
│  ─────────────────────────────────────  │
│  ℹ️  About Wane            v1.0         │
│  ─────────────────────────────────────  │
│                                         │
│                                         │
│                                         │
└─────────────────────────────────────────┘

Background: hsl(220,42%,10%) solid, 12dp top corners
Entry: spring slide from bottom (stiffness 100, damping 20)
Z-index: 50

Header: px-24dp, pt-32dp, pb-16dp
  Title: "Settings" Sora 24sp, -0.02em, crystalline
  Close: X 20×20, mutedTide, p-8dp, scale(0.97) press

Content: px-24dp, pb-64dp
  Section label: mt-32dp mb-12dp, Space Grotesk 12sp uppercase, 0.08em, accentLight
  Row: py-16dp, 1px bottom border hsla(220,40%,14%,0.8)
    Left: icon 18×18 mutedTide + gap-12dp + Sora 16sp crystalline
    Right: DM Sans 14sp mutedTide

Stagger animation: i*50ms delay, y+8→0 + fade, 300ms
```

### Screen 15 — Auto-Lock Settings (DESIGNED — not in prototype)

```
┌─────────────────────────────────────────┐
│  ←  Auto-Lock                           │
│                                         │
│  ─────────────────────────────────────  │
│       Auto-lock                [toggle]  │
│       Water starts when you lock         │
│       your phone.                        │
│  ─────────────────────────────────────  │
│                                         │
│  (visible when toggle is on)             │
│                                         │
│  ─────────────────────────────────────  │
│       Duration                    30m >  │
│       How long each auto-started         │
│       session lasts.                     │
│  ─────────────────────────────────────  │
│       Grace period                 5s >  │
│       Quick unlock-and-lock won't        │
│       start new water.                   │
│  ─────────────────────────────────────  │
│       Skip between              [toggle] │
│       Auto-lock won't run during         │
│       this window.                       │
│  ─────────────────────────────────────  │
│       Start                    10:00 PM  │
│  ─────────────────────────────────────  │
│       End                       7:00 AM  │
│  ─────────────────────────────────────  │
│       Skip while charging      [toggle]  │
│       No sessions start while your       │
│       phone is plugged in.               │
│  ─────────────────────────────────────  │
└─────────────────────────────────────────┘

Background: hsl(220,42%,10%) — same as settings
Full screen, not a sheet (no top corner radius)

Header:
  Layout:    flex row, center-aligned, px-24dp, pt-32dp, pb-16dp
  Back:      ArrowLeft 20×20dp, mutedTide, p-8dp touch area
  Title:     "Auto-Lock" — Sora 24sp, -0.02em, crystalline

Setting Row (with helper text):
  Layout:    py-16dp, 1px divider bottom
  Left col:
    Label:   Sora 16sp, crystalline
    Helper:  DM Sans 13sp, line-height 1.5, mutedTide, pt-4dp
  Right:     Value text (DM Sans 14sp, mutedTide) or Toggle switch or chevron
  Tappable:  entire row (for value rows)

Toggle Switch:
    Track:   52×28dp, rounded-full (14dp)
    Track on:  accentPrimary (#38a3dc)
    Track off: white/10
    Thumb:   24×24dp, white, rounded-full, 2dp inset from track edge
    Transition: 200ms ease

Helper Text Pattern:
  Every setting has a one-line helper directly below its label.
  Font:      DM Sans 13sp, line-height 1.5
  Color:     mutedTide
  Spacing:   pt-4dp below label

Collapse behavior:
  All settings below master toggle collapse smoothly (spring, 200ms)
  when toggle is off. No "Enable to see options" message.

Entry animation: same stagger pattern as Settings (i*50ms, 300ms duration)
```

### Screen 16 — Emergency Contacts (REMOVED from v1)

> This screen was removed from v1 scope. The repeated caller breakthrough feature remains as a toggle in Settings Main (Screen 14, Safety section). Emergency services (911/112/999) always work regardless.

---

## 8. Prototype vs DESIGN.md Delta

| # | Property | DESIGN.md Value | Prototype Value | Winner | Notes |
|---|---|---|---|---|---|
| 1 | Primary background | `#0A0E17` (Deep Abyss) | `#0a1628` / `#060e1a` / `#050c16` (gradient) | Prototype | DESIGN.md uses a single flat color; prototype uses a 2-3 stop gradient with different hex values |
| 2 | Container background | Should avoid pure black | `#000000` (bg-black) | Prototype ⚠️ | DESIGN.md explicitly bans `#000000`; prototype uses it for the device frame. Consider `#0A0E17` or `#060e1a` in production |
| 3 | Accent color | `#2E86AB` (Still Water, ~65% sat) | `#38a3dc` (brighter, higher sat) | Prototype | Different hue and saturation. `#38A3DC` ≈ hsl(201, 66%, 54%); `#2E86AB` ≈ hsl(196, 57%, 42%). Prototype is lighter and bluer |
| 4 | Secondary accent | Not listed | `#64b8e8` (accentLight) | Prototype | Used for active dots, section labels, icons. DESIGN.md has no equivalent token |
| 5 | Primary text color | `#E8EDF5` (Crystalline) | `hsl(220,40%,97%)` ≈ `#F4F6FA` | Prototype | Prototype version is brighter (~97% lightness vs ~94%). ~4% luminance difference |
| 6 | Secondary text color | `#6B7A94` (Muted Tide) | `hsl(215,6%,56%)` ≈ `#888E96` | Prototype | Prototype is more neutral gray (6% saturation vs ~18% in DESIGN.md). Visually distinct |
| 7 | Elevated surface | `#121A2E` (Ocean Surface) | `hsl(220,42%,10%)` ≈ `#0F1624` | Prototype | Slightly darker and different hue angle than DESIGN.md |
| 8 | Divider color | `rgba(46,134,171,0.12)` (Whisper Line) | `hsla(220,40%,14%,0.8)` ≈ `#151F32` @ 80% | Prototype | DESIGN.md uses accent-tinted dividers; prototype uses dark desaturated blue. Different approach |
| 9 | Body text color | Not explicitly listed as separate token | `hsl(215,6%,68%)` ≈ `#A8ADB2` | Prototype | New token between Crystalline and Muted Tide |
| 10 | Dot inactive color | Not listed | `hsl(215,6%,30%)` ≈ `#484C51` | Prototype | New token |
| 11 | Bottom toolbar buttons | 3 (Phone, Contacts, Messages) | 2 (Phone, Messages) | DESIGN.md (3 buttons) | User approved 3 buttons: Phone + Contacts + SMS |
| 12 | Sora weight 200 | Not mentioned (imports 300-600) | Used for duration number (64px) | Prototype | Weight 200 is coded but not in Google Fonts import; browser falls back to 300. Decision needed: import wt-200 or use wt-300 |
| 13 | Corner radius default | 12px everywhere | 12px for sheets/CTAs, rounded-full for circular buttons | Prototype | DESIGN.md implies 12px universal; prototype correctly uses full-round for circles |
| 14 | Outer background | Not specified | `#0f0f0f` | Prototype | Only relevant for web preview frame; irrelevant on Android |
| 15 | Touch target minimum | 44dp general, 56dp toolbar | 56dp toolbar (matches), 80dp start button (exceeds), no explicit 44dp minimum enforced | Both | Compatible, but some elements (gear 20+8+8=36dp, end button ~24dp height) may need touch area expansion in Compose |
| 16 | Ambient glow | Not specified | 300×300dp circle, accentPrimary @ 8%, blur-3xl | Prototype | New visual element not in DESIGN.md |
| 17 | Water animation colors | Not specified beyond "Still Water" | Full palette of 10+ water-specific colors (see §1.6) | Prototype | DESIGN.md gives no water color spec; prototype is authoritative |

### Summary of Critical Deltas Requiring Decision

1. **Pure black usage** (#2): Prototype uses `#000000` for the device frame. Decide whether to keep or replace with `#060e1a` per DESIGN.md spirit.
2. **Accent color mismatch** (#3): `#38a3dc` vs `#2E86AB` — these are perceptibly different blues. The prototype's `#38a3dc` is the implementation-ready value.
3. **Three toolbar buttons** (#11): RESOLVED — user approved 3 buttons (Phone + Contacts + SMS). Contacts gets its own button.
4. **Sora weight 200** (#12): Either add weight 200 to the font bundle or accept weight 300 fallback for the duration display.
5. **Touch target compliance** (#15): Gear icon (36dp) and End button (~24dp touch height) are below the 44dp DESIGN.md minimum. Wrap in larger touch targets in Compose.

---

## Appendix A: Tailwind-to-Compose Quick Reference

| Tailwind | Compose (Modifier) | Value |
|---|---|---|
| `p-8` | `.padding(32.dp)` | 32dp |
| `px-6` | `.padding(horizontal = 24.dp)` | 24dp |
| `py-4` | `.padding(vertical = 16.dp)` | 16dp |
| `gap-6` | `Arrangement.spacedBy(24.dp)` | 24dp |
| `mb-16` | `Spacer(Modifier.height(64.dp))` | 64dp |
| `rounded-xl` | `RoundedCornerShape(12.dp)` | 12dp |
| `rounded-full` | `CircleShape` | — |
| `backdrop-blur-md` | Custom `RenderEffect.createBlurEffect(12f, 12f, ...)` | 12dp |
| `backdrop-blur-sm` | Custom `RenderEffect.createBlurEffect(4f, 4f, ...)` | 4dp |
| `blur-3xl` | `Modifier.blur(64.dp)` | 64dp |
| `tracking-widest` | `letterSpacing = 0.1.em` | 0.1em |
| `active:scale-95` | `Modifier.clickable { }.graphicsLayer { scaleX = 0.95f; scaleY = 0.95f }` | — |
| `transition-transform` | `animateFloatAsState(...)` | — |

## Appendix B: Compose Color Declarations (Ready to Copy)

```kotlin
object WaneColors {
    // Backgrounds
    val backgroundShell = Color(0xFF0F0F0F)
    val backgroundDevice = Color(0xFF000000)
    val backgroundDeep = Color(0xFF0A1628)
    val backgroundDeepMid = Color(0xFF0D1F3C)
    val backgroundDeepEnd = Color(0xFF060E1A)
    val backgroundAbyss = Color(0xFF050C16)
    val backgroundSettings = Color(0xFF0F1624)
    val backgroundOverlay = Color(0xFF060E1A).copy(alpha = 0.80f)

    // Accent
    val accentPrimary = Color(0xFF38A3DC)
    val accentLight = Color(0xFF64B8E8)
    val accentGlow = Color(0xFF38A3DC).copy(alpha = 0.08f)

    // Text (white alpha scale)
    val textPrimary = Color.White.copy(alpha = 0.90f)
    val textSecondary = Color.White.copy(alpha = 0.80f)
    val textTertiary = Color.White.copy(alpha = 0.70f)
    val textStatusBar = Color.White.copy(alpha = 0.60f)
    val textMuted = Color.White.copy(alpha = 0.30f)
    val textSubtle = Color.White.copy(alpha = 0.20f)
    val textGhost = Color.White.copy(alpha = 0.15f)
    val textGhostActive = Color.White.copy(alpha = 0.40f)
    val textWhite = Color.White

    // Named HSL colors
    val crystalline = Color(0xFFF4F6FA)
    val mutedTide = Color(0xFF888E96)
    val bodyText = Color(0xFFA8ADB2)
    val dotInactive = Color(0xFF484C51)
    val divider = Color(0xFF151F32).copy(alpha = 0.80f)

    // Surfaces
    val surfaceGlass = Color.White.copy(alpha = 0.10f)
    val surfaceDim = Color.White.copy(alpha = 0.05f)
    val borderSubtle = Color.White.copy(alpha = 0.20f)

    // Water animation
    val waveLayer1 = Color(0xFF3895D3).copy(alpha = 0.3f)
    val waveLayer2 = Color(0xFF2878BE).copy(alpha = 0.4f)
    val waveLayer3 = Color(0xFF1E64AA).copy(alpha = 0.3f)
    val waterGradientTop = Color(0xFF38A3DC).copy(alpha = 0.85f)
    val waterGradientUpper = Color(0xFF2882C3).copy(alpha = 0.9f)
    val waterGradientLower = Color(0xFF195FA5).copy(alpha = 0.92f)
    val waterGradientBottom = Color(0xFF0F3C78).copy(alpha = 0.95f)
    val causticCenter = Color(0xFF78C8FF).copy(alpha = 0.12f)
    val causticEdge = Color(0xFF78C8FF).copy(alpha = 0.0f)
}
```

## Appendix C: Compose Typography Declarations (Ready to Copy)

```kotlin
val WaneTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.ExtraLight, // 200 (or Light/300 if 200 unavailable)
        fontSize = 64.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        letterSpacing = (-0.03).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = (-0.02).em,
    ),
    titleLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Light,
        fontSize = 18.sp,
        letterSpacing = 0.2.em,
    ),
    titleMedium = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp, // 1.6 ratio
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.05.em,
    ),
    bodySmall = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // 13.6 rounded
    ),
    labelLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = (-0.01).em,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.08.em,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.1.em,
    ),
)
```
