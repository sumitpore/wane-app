---
name: Wane Design Brief
overview: Complete Pre-Flight Design Brief for Wane -- the premium Android focus app that replaces the smartphone screen with a calming water animation. This brief locks all visual, motion, narrative, and technical decisions before implementation begins.
todos:
  - id: approve-brief
    content: User approval of Pre-Flight Design Brief (this document) -- all subsequent phases implement these locked decisions
    status: pending
  - id: persist-brief
    content: After approval, write the complete brief to User-Approved-Design-Choices.md in project root
    status: pending
  - id: phase1-shaders
    content: "Phase 1-B: Generate OpenGL ES water simulation shaders (vertex displacement + fragment shading) in /app/src/main/assets/shaders/"
    status: pending
  - id: phase1-postprocess
    content: "Phase 1-C: Generate film grain and bloom post-processing shader passes"
    status: pending
  - id: phase1-geometry
    content: "Phase 1-D: Generate water plane geometry factory (subdivided plane with custom attributes for shader)"
    status: pending
  - id: phase1-assets
    content: "Phase 1-A: Generate visual assets via Google Flow (normal map, caustic texture, onboarding illustrations) and place in /app/src/main/res/"
    status: pending
  - id: phase2-scene
    content: "Phase 2: Build OpenGL ES water simulation renderer (GLSurfaceView, shader integration, gyroscope input, touch raycasting)"
    status: pending
  - id: phase2-ui
    content: "Phase 2: Build Jetpack Compose UI layer (onboarding, duration picker, bottom toolbar, settings, session history)"
    status: pending
  - id: phase3-animation
    content: "Phase 3: Wire animation layer (spring physics for UI, water level recession over time, touch ripple system, session state machine)"
    status: pending
  - id: phase4-quality
    content: "Phase 4: Quality gate -- run 16-test visual rubric, aesthetic self-critique loop, cheapness/liveliness/specificity audits"
    status: pending
isProject: false
---

# Wane -- Pre-Flight Design Brief

> Design tool selection: **Option C (Direct)** -- skip Phase 0, proceed to asset generation and implementation.
>
> This brief adapts the beautiful-designs-skill pipeline for a native Android mobile app (Kotlin + Jetpack Compose + OpenGL ES) rather than a web experience.

---

## CONCEPT

**1. Emotional register and concept statement**
Wane transforms the smartphone into a quiet body of water. The user should feel the same small release as setting down something heavy -- not accomplishment, not guilt, just a shift from restlessness to stillness.

**Aesthetic intent word: Refined** -- near-monochromatic palette, low saturation, generous negative space, one accent, minimal separation mechanisms. See Emotional Intent Dictionary implications: whitespace 35-45% of viewport empty, typography medium weight (400-500), tight letter-spacing on headings (-0.02 to -0.03em), near-monochromatic with one accent, low to medium content density, 0 or small border-radius, shadows barely visible or absent, borders subtle or absent.

**2. Technique selection**
**Turbulent Surface** -- calibrated to serene rather than agitated. Water IS a fluid surface; the technique provides the underlying shader architecture (noise-displaced mesh, Perlin/simplex-driven vertex deformation, Fresnel-based surface shading). Turbulence parameters are set low: long wavelengths, slow frequency, gentle amplitude -- producing a hypnotic, meditative surface that holds attention without demanding it.

**3. Primary 3D form**
**Displaced water plane** -- a subdivided plane mesh rendered via OpenGL ES with vertex displacement driven by layered simplex noise. The plane represents a body of water viewed from a slight downward angle. The water level (visible fill height) is the sole progress indicator. This form serves the concept because the water IS the product -- it replaces the entire phone interface with a single, living surface.

---

## VISUAL LANGUAGE

**4. Palette and spatial rhythm**
Deep oceanic dark palette anchored by near-black with cold blue undertone. Single teal accent at ~58% saturation for CTAs and active states. No second accent. No purple. No neon. Typographic register: geometric display (Sora) at moderate scale, clean body (DM Sans) with relaxed leading. Spatial rhythm: aggressive negative space -- each screen anchored by one dominant element. The water screen is 100% bleed with zero competing chrome. Default to **dark background** (justified: the water animation reads as physical and immersive against dark; light mode would undermine the meditation metaphor).

**Fonts:** Display: **Sora** (approved list) -- geometric, modern, distinctive letter forms with a technical-organic quality matching the water concept. Body: **DM Sans** (approved list) -- warm readability at all sizes, pairs well with Sora's geometric precision. Labels/Mono: **Space Grotesk** (approved list) -- mono-adjacent feel for settings metadata.

**4a. Aesthetic foundation**

- **Aesthetic intent word:** Refined -- near-monochromatic with one accent, low saturation, generous whitespace, subtle or absent shadows, borders absent (whitespace separates), font weight and color drive hierarchy.

- **Spacing scale:** 8px base unit. All spacing values use multiples: 8, 16, 24, 32, 48, 64, 96, 128, 192, 256. No exceptions.

- **Type scale:** `12 · 14 · 16 · 18 · 20 · 24 · 30 · 36 · 48 · 60` -- all font-size values from this list only. 16dp base for body. Display headings use 36-48dp. Settings labels use 12-14dp.

- **HSL color palette:**

  Primary (deep blue, hue 220):
  `blue-50: hsl(220, 40%, 97%) · blue-100: hsl(220, 38%, 90%) · blue-200: hsl(220, 36%, 78%) · blue-300: hsl(220, 34%, 62%) · blue-400: hsl(220, 32%, 45%) · blue-500: hsl(220, 36%, 30%) · blue-600: hsl(220, 38%, 20%) · blue-700: hsl(220, 40%, 14%) · blue-800: hsl(220, 42%, 10%) · blue-900: hsl(220, 44%, 6%)`

  Accent (teal, hue 195):
  `teal-50: hsl(195, 60%, 96%) · teal-100: hsl(195, 58%, 88%) · teal-200: hsl(195, 56%, 76%) · teal-300: hsl(195, 55%, 64%) · teal-400: hsl(195, 58%, 52%) · teal-500: hsl(195, 58%, 42%) · teal-600: hsl(195, 60%, 34%) · teal-700: hsl(195, 62%, 26%) · teal-800: hsl(195, 64%, 18%) · teal-900: hsl(195, 66%, 12%)`

  Cool grey (hue 215, 6% saturation -- never pure 0% grey):
  `grey-50: hsl(215, 6%, 97%) · grey-100: hsl(215, 8%, 93%) · grey-200: hsl(215, 6%, 82%) · grey-300: hsl(215, 6%, 68%) · grey-400: hsl(215, 6%, 56%) · grey-500: hsl(215, 8%, 50%) · grey-600: hsl(215, 6%, 40%) · grey-700: hsl(215, 6%, 30%) · grey-800: hsl(215, 6%, 20%) · grey-900: hsl(215, 6%, 10%)`

- **Border-radius personality:** 12dp -- playful/approachable, consistent with the water metaphor's organic softness. Applied consistently to cards, sheets, and primary containers. Bottom sheet corners use 12dp top-left/top-right. Buttons use 12dp.

- **Dimensional references:** Spacing from Fix Studio (single-column with generous vertical rhythm, aggressive negative space between service blocks). Typography from Fear of God (minimal text, heavy display weight contrast against dark backgrounds, sparse copy). Color from The Sea We Breathe (deep oceanic palette limited to 2-3 hues, teal/cyan accent, immersive dark environment).

**5. Material surfaces**
**Clearcoat** (wet, organic) -- literally water. The water simulation surface has subtle Fresnel reflectance at grazing angles, creating a wet-glass quality at the water's edge. Light caustic patterns dance on the surface. This material vocabulary matches the concept's emotional register: calm, physical, alive.

**6. Atmospheric depth**
**FogExp2 equivalent** (exponential distance fog in the OpenGL ES shader) -- a subtle depth haze below the water surface creates the illusion of volume. The water is not flat; it has perceived depth that increases the lower the water level gets, revealing darker, deeper tones as the session progresses. Fog communicates mystery and depth -- appropriate for a meditative experience.

---

## NARRATIVE ARCHITECTURE

**7. Narrative beats** (mapped to app user journey)

1. **First Light** (Onboarding) -- curiosity, gentle invitation -- warm-cool neutral lighting -- first 3 screens on first launch only
2. **The Asking** (Duration selection) -- deliberate choice, agency -- cool ambient with teal accent -- single screen before each session
3. **Still Water** (Active focus session) -- calm immersion, presence -- slow lighting cycle (cool blue shifting subtly over session duration, daily light-angle variations) -- the entire session duration (the water IS this beat)
4. **Return** (Session complete) -- gentle re-entry, quiet satisfaction -- warm shift as water fully drains -- "Welcome back." fades in for 2 seconds, gentle haptic

**8. Section color modes**
All dark. The entire app lives in a single dark color mode. Surface elevation differences (blue-900 for base, blue-700 for elevated cards) create hierarchy within the dark palette. No light/dark toggle -- dark IS the brand.

---

## CINEMATOGRAPHY

**9. Camera strategy**
**Static with subtle tilt response** -- the virtual camera in the water simulation looks down at the water surface at approximately 15 degrees from vertical, creating a sense of looking into a pool. The camera subtly responds to device gyroscope (parallax tilt on both axes, max 3 degrees deviation). This camera behavior serves the concept because the user should feel like they are gazing into actual water on their device, and the tilt response reinforces the physical metaphor.

**10. Lighting direction**
**Naturalistic with slow temporal shift** -- a single soft directional light illuminates the water surface, creating caustic highlights that drift slowly. The light angle shifts based on time of day (derived from device clock): cooler/bluer in morning, warmer/golden at sunset hours. During a session, the light subtly dims as the water recedes -- the bottom of the pool is darker, quieter. At session completion, a brief warm pulse accompanies the "Welcome back" text.

---

## MOTION AND INTERACTION

**11. Motion register**
**Fluid/organic** -- all animation follows the physics of water: spring-damped (stiffness ~100, damping ~20), no linear easing, no snappy mechanical movement. Everything feels like it has mass and inertia.

- **Scroll-velocity behavior:** N/A (no scroll in the traditional sense). However, rapid repeated touches on the water surface during a session create progressively larger ripple overlaps that naturally decay -- speed of interaction produces a visible, proportional response.
- **Text entrance style:** Fade with subtle vertical rise (16dp over 400ms, ease-out). Character-by-character stagger only for the session-complete message "Welcome back." (30ms per character).
- **UI microinteractions:**
  - Pressable elements: `scale(0.97)` + `-1dp translateY` on press, spring-back on release (150ms). Bottom toolbar icons: teal-500 fill on active, grey-400 outline on inactive.
  - Hover behavior: N/A (touch-only mobile device).
  - Navigation: Bottom toolbar appears with 200ms fade-in + 8dp slide-up on session start. Disappears with 150ms fade-out on session end.
  - Modals/drawers: Bottom sheet slides up from below viewport (300ms, spring-damped), dismisses with 200ms slide-down.
  - Tooltips: N/A (minimal UI, no tooltips needed).
  - List/grid stagger: Settings items enter with 50ms stagger delay between rows.

**12. Interaction model**
Touch is protagonist. Named touch states: **still** (water at rest), **ripple** (single touch creates concentric wave from touch point), **disturb** (sustained touch or multi-touch creates local surface agitation that decays over 2 seconds after release). The water simulation responds to touch position via raycasting from screen coordinates to the water plane. Device tilt via gyroscope creates gentle sloshing (0.5-second lag, spring-damped). This interaction model serves the concept because the water must feel physically real -- touch it and it responds; tilt the device and it shifts.

**13. Scroll behavior strategy**
No scroll-driven narrative. The app uses discrete screen transitions (onboarding carousel, duration picker, focus screen, settings). The focus screen is a single, non-scrolling, full-bleed water surface with a translucent bottom toolbar. Settings and history screens use standard vertical scroll. Justification: the focus session is about presence, not progression through content.

---

## DIRECTOR'S VISION

You open Wane for the first time and the screen is dark -- the deep blue-black of a lake at dusk. Water rises from the bottom of your phone, filling the screen silently. It catches light as it moves, a single soft caustic pattern drifting across the surface. A line of text appears at the center, unhurried: "Tap to begin." You tap. The water ripples outward from your fingertip, concentric circles expanding and fading. A second screen asks how long you want the water. You drag a simple arc -- fifteen minutes, thirty, an hour. You tap again. The water is now full, edge to edge, alive with the gentlest turbulence. You can see nothing else -- no clock, no notifications, no apps. Just water. A translucent dock sits at the very bottom with three icons: phone, contacts, messages. You tilt your device slightly and the water shifts, light refracting across the surface. You set the phone down. Over the next thirty minutes, the water slowly, imperceptibly recedes -- revealing the dark depth beneath. You pick the phone up and the water sloshes gently in response. You touch it and ripples spread from your finger. When the water finally drains to nothing, the screen holds a moment of pure stillness. Then, letter by letter: "Welcome back." A gentle vibration in your palm. The phone is yours again.

---

## TECHNICAL DIRECTION

**14. UI rendering architecture**
**Option A equivalent -- Compose UI over OpenGL ES canvas.** The water animation renders in a `GLSurfaceView` (or `SurfaceView` with OpenGL ES 3.0 context) positioned as a full-screen background layer. Jetpack Compose UI elements (bottom toolbar, text overlays, settings screens) sit above the GL surface with `pointer-events` passing through to the water for touch interactions when appropriate. Reason: Compose provides native Android accessibility, text rendering, and system integration; the water simulation gets dedicated GPU rendering without UI framework overhead.

**15. Post-processing intent**
Adapted for OpenGL ES fragment shader passes:
- **Film grain:** Active -- subtle noise overlay makes the water surface feel physical and cinematic, not digital. Intensity: 2-3% opacity.
- **Chromatic aberration:** Inactive -- unnecessary for a calm, centered water surface. Would introduce visual tension counter to the concept.
- **Bloom:** Active -- controlled glow on caustic highlights and water surface reflections. Threshold set high so only the brightest specular points bloom. Creates the perception of real light on real water.
- **Depth of field:** Inactive -- the water fills the entire screen; there is no subject/background separation to exploit.

Maximum 2 active passes (film grain + bloom). Both are disabled on `quality === 'low'` tier (Snapdragon 600-series, targeting 60fps at reduced pixel ratio).

**16. Loading experience**
**Minimal water fill animation** -- on cold start, the screen is pure blue-900. Water rises from the bottom over 1.5 seconds, accompanied by a single haptic pulse at full. No percentage counter. No progress bar. The water rising IS the loading indicator. This sets the tone: everything in this app is water.

**17. Frame-Sequence flag**
No -- the water simulation is real-time procedural via OpenGL ES shaders, not pre-rendered frames.

---

## ASSET INVENTORY

- **Water simulation shaders:** Vertex shader (simplex noise displacement, 3 octaves) + Fragment shader (Fresnel reflectance, caustic texture sampling, foam at water-edge, color grading). Placed in `app/src/main/assets/shaders/`.
- **Water normal map:** Tileable water normal texture (1024x1024 PNG) for micro-surface detail layered on top of macro vertex displacement.
- **Caustic texture:** Tileable caustic light pattern (512x512 PNG, animated UV offset in shader) for dappled light on the water surface.
- **Noise texture:** 256x256 blue noise texture for film grain post-processing pass.
- **Ambient sound pack:** Default water ambience (lapping, distant waves). WAV or OGG, loopable, <2MB.
- **App icon:** Water-themed icon, following Material Design 3 adaptive icon spec.
- **Onboarding assets:** 3 minimal illustrations or animations for first-launch screens (water rising, duration arc, touch ripple).
- **Premium theme assets (v1.1):** Each premium theme requires: water color palette override, unique caustic pattern, unique ambient sound, unique surface shader variant.
- **Section background videos:** None -- all water animation is real-time procedural.
- **GLTF/GLB models:** None -- the water is a procedural plane, not an imported model.

---

## VISUAL ASSET GENERATION -- GOOGLE FLOW PROMPTS

**IMAGE ASSETS (Imagen):**

**Asset: Water normal map**
Imagen prompt: seamless tileable water surface normal map texture, deep blue-teal ocean water, soft undulating ripples, physically accurate normal direction encoding, RGB normal map format, flat even lighting, no visible seam edges, 1024x1024, photorealistic water surface detail, meditative calm

**Asset: Caustic light pattern**
Imagen prompt: seamless tileable underwater caustic light pattern, bright white-teal light rays refracted through water surface, abstract organic shapes, dark background, high contrast, dappled sunlight through water, flat even lighting, no visible seam, 512x512, ethereal calm

**Asset: Onboarding illustration 1 -- Water Rising**
Imagen prompt: minimal abstract illustration of water rising in a dark container, deep blue-black background (#0A0E17), teal water (#2E86AB) filling from bottom, subtle light reflections on water surface, no text, no people, serene, premium, single focal element, clean vector style

**Asset: Onboarding illustration 2 -- Duration Arc**
Imagen prompt: minimal abstract illustration of a curved arc shape suggesting time, deep blue-black background (#0A0E17), thin teal arc line (#2E86AB), subtle water droplets along the arc, no text, no people, clean, refined, premium mobile app aesthetic

**Asset: Onboarding illustration 3 -- Touch Ripple**
Imagen prompt: minimal abstract illustration of concentric ripple circles expanding on a dark water surface, deep blue-black background (#0A0E17), teal-white ripple rings radiating outward from center, no text, no people, meditative, premium, clean vector style

**VIDEO ASSETS (Veo):** None required -- all animation is real-time procedural.

---

## REFERENCE SITES BY COLLECTION (visited via WebFetch)

**Font selection (approved-fonts.md):**
- **Sora** -- geometric sans-serif with distinctive letter forms; the round 'o' and open counters carry a technical-organic quality that bridges the app's engineering precision with its water-nature concept. Available on Google Fonts.
- **DM Sans** -- warm, readable body text with relaxed letter spacing at body sizes. Pairs with Sora's geometric display without competing. Available on Google Fonts.
- **Space Grotesk** -- mono-adjacent proportions give settings metadata a technical register without the coldness of a true monospace. Available on Google Fonts.

**Microcopy (microcopy-ux-writing.md):**
- **Slowness** (visited) -- uses calm, philosophical brand voice: "An intention, a wish, a recourse" is their tagline. The journal section titles ("The Long Wave," "Building a Modern Temple") demonstrate how to name things poetically without being precious. Adopting this understated, non-prescriptive voice for Wane's session prompts: "How long do you want the water?" rather than "Set your focus timer."

**Spatial rhythm and layout (clean-layout.md / minimal-layout.md):**
- **Fix Studio** from clean-layout.md (visited) -- single-column layout with extreme content restraint. "Function is the substance of aesthetic experience" philosophy with generous vertical rhythm between service blocks, every element in its own clear zone. Adopting this content restraint and single-column mobile rhythm for Wane's settings and history screens.
- **Neverland Agency** from clean-layout.md (visited) -- numbered service sections (01, 02, 03) with generous whitespace between modules, single-column scroll. Adopting this numbered section rhythm pattern for Wane's stepped onboarding flow.

**Color and visual tone (color-exploration.md):**
- **The Sea We Breathe** (visited) -- underwater WebGL experience using deep oceanic blues with "Depth: 0m, Temp: 20c" metadata overlay on immersive content. Dark background with minimal UI chrome. The "Inhale Exhale" breathing prompt demonstrates how to overlay minimal text on fluid animation without visual competition. Adopting this deep oceanic palette and chrome-free immersive approach for Wane's focus screen.
- **Pienso** (visited) -- enterprise AI product with muted, restrained dark palette and single accent. Demonstrates how single-accent restraint communicates premium quality in a technical product. Adopting this single-accent strategy for Wane's UI elements.

**Technique and motion (transitions.md):**
- **Akaru** from transitions.md (visited) -- Lyon agency with 35 Awwwards and 19 FWA awards. Smooth project transitions with fluid ease-out timing. "We love precision" philosophy aligns with Wane's need for micro-animation polish. Adopting their spring-physics transition timing (smooth, weighty, never snappy) for Wane's screen transitions and UI chrome animations.
- **Venus Story** (visited) -- chapter-based narrative ("Intro, Myths, Epochs, Modernity") with typography-driven transitions and scroll-triggered text reveals. Each chapter has a distinct emotional register. Adopting this chapter-beat structure for Wane's user journey (First Light, The Asking, Still Water, Return).

**Navigation and interaction (best-of-navigation.md):**
- **Modden** (visited) -- LA design studio with playful touch interactions and minimal navigation. The entire site communicates through a single statement and direct engagement. "Hey, this feels soooooooo nice!" shows how minimal UI can feel alive through careful interaction design. Adopting this principle of minimal navigation with maximum tactile feedback for Wane's 3-icon bottom toolbar.

**Narrative and storytelling (storytelling.md):**
- **The Sea We Breathe** (visited) -- immersive underwater breathing experience by Blue Marine Foundation. "Take a minute to relax and breathe in time with the rolling waves." Uses audio, depth cues, and temperature data to create a meditative underwater journey. Directly informing Wane's focus session as a meditative, breathing-paced interaction with ambient sound and environmental cues.

**SOTD Elements (best-of-sotd-elements.md):**
- **Garden Eight** from best-of-sotd-elements.md (referenced) -- infinite project archive canvas with smooth scroll navigation. The principle of a single, infinite canvas for content display informs Wane's session history as a calm, scrollable log without pagination or complexity.

**Content presentation:**
- **Slowness** from editorial sites (visited) -- journal-style content blocks with generous caption spacing, asymmetric image placement, and philosophical tone ("Cultivating arts, crops and inner gardens"). The "Encyclopedia of the Farm" section demonstrates how to present structured content with editorial calm. Adopting this editorial calm for Wane's session history: "Seven days of water." as a streak counter, simple session log without charts or data visualization.

**Responsive (responsive-design.md):**
- N/A -- Wane is mobile-only (Android). No responsive web breakpoints needed.

**Mobile (mobile-ui.md):**
- **Fear of God** (visited) -- premium fashion brand with extreme minimalism on dark mobile layout. "ESSENTIALS SPRING 2026" as the only visible content on the homepage -- a single statement against darkness. Adopting this radical content restraint and premium dark aesthetic density for Wane's visual approach.
- **Grayscale Studio** (visited) -- pure typographic focus with zero visual noise, structured around the numbers "0 0 0 0 0 0" and black/white/grey palette. Demonstrates how restraint creates premium perception. Adopting the grayscale-first hierarchy principle: Wane's UI must read clearly without the teal accent.
- **53W53** (visited) -- luxury Manhattan condominiums ("A Modern Heirloom"). Single hero image per viewport, "A world away from ordinary" positioning. Demonstrates cinematic section pacing with one dominant element per view. Adopting this one-element-per-viewport pacing for Wane's onboarding and settings screens.

**Three.js and WebGL (threejs-references.md):**
- **The Sea We Breathe** (visited) -- underwater WebGL experience with scroll-driven depth changes, particle effects, environmental audio, and breathing prompts. Uses the ocean as both medium and metaphor. Directly informing Wane's water simulation approach (adapted from Three.js/WebGL to OpenGL ES): fluid surface displacement, caustic lighting, and audio-enhanced immersion.

