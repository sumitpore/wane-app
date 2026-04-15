package com.wane.app.animation

/**
 * GLSL sources for the water effect. ES 3.0 uses GLSL 300 es; ES 2.0 uses GLSL 100 with
 * matching uniforms (see [FRAGMENT_SHADER_ES2]).
 *
 * Uniform naming: `u_camelCase` (mediump floats in fragment unless noted).
 */
object WaterShaders {
    /**
     * Fullscreen clip-space quad. [a_position] is in NDC [-1, 1].
     *
     * Uniforms: none.
     * Attributes:
     * - `a_position` vec2 — corner position.
     * Varyings:
     * - `v_uv` — normalized UV (0–1), origin bottom-left.
     */
    const val VERTEX_SHADER = """#version 300 es
precision mediump float;
layout(location = 0) in vec2 a_position;
out vec2 v_uv;
void main() {
  v_uv = vec2(a_position.x * 0.5 + 0.5, a_position.y * 0.5 + 0.5);
  gl_Position = vec4(a_position, 0.0, 1.0);
}
"""

    /**
     * Procedural water, sky, waves, caustics, tilt, and touch ripple.
     *
     * Uniforms (all documented):
     * - `u_resolution` vec2 — framebuffer size in pixels.
     * - `u_time` float — monotonic seconds for animation.
     * - `u_waterLevel` float — fill height in UV space [0, 1] before wave displacement.
     * - `u_tiltX` float — horizontal tilt [-1, 1]; offsets wave phase and caustic anchors.
     * - `u_tiltY` float — vertical tilt [-1, 1]; offsets wave phase and caustic anchors.
     * - `u_touchX` float — last touch x in UV [0, 1].
     * - `u_touchY` float — last touch y in UV [0, 1].
     * - `u_touchTime` float — `u_time` at touch, or negative if no active ripple.
     * - `u_backgroundStart` vec4 — sky gradient color at top (RGBA, 0–1).
     * - `u_backgroundEnd` vec4 — sky gradient color near horizon (RGBA).
     * - `u_gradientTop` vec4 — water surface tint (RGBA).
     * - `u_gradientUpper` vec4 — upper water body (RGBA).
     * - `u_gradientLower` vec4 — lower water body (RGBA).
     * - `u_gradientBottom` vec4 — deepest water (RGBA).
     * - `u_wave1Color` vec4 — first wave layer color (premultiplied-friendly RGBA).
     * - `u_wave1Amplitude` float — wave height in UV units.
     * - `u_wave1Frequency` float — spatial frequency along x.
     * - `u_wave1Speed` float — temporal phase multiplier.
     * - `u_wave2Color` … `u_wave3Speed` — same pattern for layers 2 and 3.
     * - `u_causticCenterColor` vec4 — caustic highlight tint (RGBA).
     * - `u_causticCount` int — active caustic blobs (0 disables; capped in shader).
     * - `u_causticBaseRadius` float — base UV radius of caustic discs.
     * - `u_causticRadiusOscillation` float — radius modulation amplitude (UV).
     */
    const val FRAGMENT_SHADER = """#version 300 es
precision highp float;
precision mediump int;

const float TAU = 6.2831853;
const int MAX_CAUSTICS = 4;

in vec2 v_uv;
out vec4 fragColor;

uniform vec2 u_resolution;
uniform float u_time;
uniform float u_waterLevel;
uniform float u_tiltX;
uniform float u_tiltY;
uniform float u_touchX;
uniform float u_touchY;
uniform float u_touchTime;

uniform vec4 u_backgroundStart;
uniform vec4 u_backgroundEnd;
uniform vec4 u_gradientTop;
uniform vec4 u_gradientUpper;
uniform vec4 u_gradientLower;
uniform vec4 u_gradientBottom;

uniform vec4 u_wave1Color;
uniform float u_wave1Amplitude;
uniform float u_wave1Frequency;
uniform float u_wave1Speed;

uniform vec4 u_wave2Color;
uniform float u_wave2Amplitude;
uniform float u_wave2Frequency;
uniform float u_wave2Speed;

uniform vec4 u_wave3Color;
uniform float u_wave3Amplitude;
uniform float u_wave3Frequency;
uniform float u_wave3Speed;

uniform vec4 u_causticCenterColor;
uniform int u_causticCount;
uniform float u_causticBaseRadius;
uniform float u_causticRadiusOscillation;

float waveOffset(vec2 uv) {
  float x = uv.x;
  float t = u_time;
  float tx = u_tiltX;
  float ty = u_tiltY;
  float w1 = u_wave1Amplitude * sin(TAU * u_wave1Frequency * x + u_wave1Speed * t + tx * 2.2 + ty * 0.7);
  float w2 = u_wave2Amplitude * sin(TAU * u_wave2Frequency * x + u_wave2Speed * t - ty * 2.0 + tx * 0.9);
  float w3 = u_wave3Amplitude * sin(TAU * u_wave3Frequency * x + u_wave3Speed * t + (tx + ty) * 1.3);
  return w1 + w2 + w3;
}

vec4 mixFour(vec4 c0, vec4 c1, vec4 c2, vec4 c3, float p) {
  float s = clamp(p, 0.0, 1.0);
  if (s < 0.333333) {
    float k = s / 0.333333;
    return mix(c0, c1, k);
  } else if (s < 0.666666) {
    float k = (s - 0.333333) / 0.333333;
    return mix(c1, c2, k);
  }
  float k = (s - 0.666666) / 0.333334;
  return mix(c2, c3, k);
}

void main() {
  vec2 uv = v_uv;
  float surfaceY = clamp(u_waterLevel, 0.001, 0.999);
  surfaceY += waveOffset(uv);

  if (u_touchTime >= 0.0) {
    float dt = u_time - u_touchTime;
    if (dt >= 0.0 && dt < 4.0) {
      vec2 tc = vec2(u_touchX, u_touchY);
      float d = length(uv - tc);
      float ripple = 0.045 * sin(d * 55.0 - dt * 14.0) * exp(-d * 5.0) * exp(-dt * 1.2);
      surfaceY += ripple;
    }
  }

  bool belowSurface = uv.y <= surfaceY;

  if (!belowSurface) {
    float skyT = smoothstep(0.0, 1.0, uv.y);
    fragColor = mix(u_backgroundEnd, u_backgroundStart, skyT);
    return;
  }

  float depthT = 0.0;
  if (surfaceY > 0.0001) {
    depthT = clamp(uv.y / surfaceY, 0.0, 1.0);
  }
  vec4 waterCol = mixFour(u_gradientBottom, u_gradientLower, u_gradientUpper, u_gradientTop, depthT);

  float x = uv.x;
  float t = u_time;
  float edge1 = abs(uv.y - surfaceY);
  float foam = smoothstep(0.04, 0.001, edge1);
  float edgeLine = smoothstep(0.006, 0.0, edge1);
  waterCol += u_wave1Color * foam * 0.4;
  waterCol.rgb += vec3(1.0) * edgeLine * 0.25;

  float s1 = sin(TAU * u_wave1Frequency * x + u_wave1Speed * t + u_tiltX * 2.0) * 0.5 + 0.5;
  float s2 = sin(TAU * u_wave2Frequency * x + u_wave2Speed * t + u_tiltY * 2.0) * 0.5 + 0.5;
  float s3 = sin(TAU * u_wave3Frequency * x + u_wave3Speed * t) * 0.5 + 0.5;
  waterCol += u_wave1Color * (0.04 * s1 * (1.0 - depthT * 0.5));
  waterCol += u_wave2Color * (0.03 * s2 * (1.0 - depthT * 0.4));
  waterCol += u_wave3Color * (0.02 * s3 * (1.0 - depthT * 0.3));

  float causticAccum = 0.0;
  for (int i = 0; i < MAX_CAUSTICS; i++) {
    if (i >= u_causticCount) break;
    float fi = float(i);
    vec2 seed = vec2(
      fract(fi * 0.7531 + 0.143),
      fract(fi * 0.3713 + 0.527)
    );
    vec2 center = vec2(0.12, 0.18) + seed * vec2(0.76, 0.62);
    center += vec2(u_tiltX, u_tiltY) * 0.12;
    center += 0.04 * vec2(sin(t * 0.37 + fi * 1.7), cos(t * 0.29 + fi * 2.1));
    float r = u_causticBaseRadius + u_causticRadiusOscillation * sin(t * 2.1 + fi * 0.8);
    vec2 diff = uv - center;
    float dist2 = dot(diff, diff);
    float blob = exp(-dist2 / (r * r * 3.5));
    float d = sqrt(dist2);
    float ring = sin(d * 38.0 - t * 3.5 + fi) * 0.5 + 0.5;
    causticAccum += blob * ring;
  }
  waterCol.rgb += u_causticCenterColor.rgb * causticAccum * u_causticCenterColor.a;

  fragColor = clamp(waterCol, 0.0, 1.0);
}
"""

    /**
     * ES 2.0 vertex shader — matches [VERTEX_SHADER] IO contract with `attribute`/`varying`.
     */
    const val VERTEX_SHADER_ES2 = """attribute vec2 a_position;
varying vec2 v_uv;
void main() {
  v_uv = vec2(a_position.x * 0.5 + 0.5, a_position.y * 0.5 + 0.5);
  gl_Position = vec4(a_position, 0.0, 1.0);
}
"""

    /**
     * ES 2.0 fragment shader — same uniforms and behavior as [FRAGMENT_SHADER] (mediump).
     */
    const val FRAGMENT_SHADER_ES2 = """#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
precision mediump int;

const float TAU = 6.2831853;
const int MAX_CAUSTICS = 4;

varying vec2 v_uv;

uniform vec2 u_resolution;
uniform float u_time;
uniform float u_waterLevel;
uniform float u_tiltX;
uniform float u_tiltY;
uniform float u_touchX;
uniform float u_touchY;
uniform float u_touchTime;

uniform vec4 u_backgroundStart;
uniform vec4 u_backgroundEnd;
uniform vec4 u_gradientTop;
uniform vec4 u_gradientUpper;
uniform vec4 u_gradientLower;
uniform vec4 u_gradientBottom;

uniform vec4 u_wave1Color;
uniform float u_wave1Amplitude;
uniform float u_wave1Frequency;
uniform float u_wave1Speed;

uniform vec4 u_wave2Color;
uniform float u_wave2Amplitude;
uniform float u_wave2Frequency;
uniform float u_wave2Speed;

uniform vec4 u_wave3Color;
uniform float u_wave3Amplitude;
uniform float u_wave3Frequency;
uniform float u_wave3Speed;

uniform vec4 u_causticCenterColor;
uniform int u_causticCount;
uniform float u_causticBaseRadius;
uniform float u_causticRadiusOscillation;

float waveOffset(vec2 uv) {
  float x = uv.x;
  float t = u_time;
  float tx = u_tiltX;
  float ty = u_tiltY;
  float w1 = u_wave1Amplitude * sin(TAU * u_wave1Frequency * x + u_wave1Speed * t + tx * 2.2 + ty * 0.7);
  float w2 = u_wave2Amplitude * sin(TAU * u_wave2Frequency * x + u_wave2Speed * t - ty * 2.0 + tx * 0.9);
  float w3 = u_wave3Amplitude * sin(TAU * u_wave3Frequency * x + u_wave3Speed * t + (tx + ty) * 1.3);
  return w1 + w2 + w3;
}

vec4 mixFour(vec4 c0, vec4 c1, vec4 c2, vec4 c3, float p) {
  float s = clamp(p, 0.0, 1.0);
  if (s < 0.333333) {
    float k = s / 0.333333;
    return mix(c0, c1, k);
  } else if (s < 0.666666) {
    float k = (s - 0.333333) / 0.333333;
    return mix(c1, c2, k);
  }
  float k = (s - 0.666666) / 0.333334;
  return mix(c2, c3, k);
}

void main() {
  vec2 uv = v_uv;
  float surfaceY = clamp(u_waterLevel, 0.001, 0.999);
  surfaceY += waveOffset(uv);

  if (u_touchTime >= 0.0) {
    float dt = u_time - u_touchTime;
    if (dt >= 0.0 && dt < 4.0) {
      vec2 tc = vec2(u_touchX, u_touchY);
      float d = length(uv - tc);
      float ripple = 0.045 * sin(d * 55.0 - dt * 14.0) * exp(-d * 5.0) * exp(-dt * 1.2);
      surfaceY += ripple;
    }
  }

  bool belowSurface = uv.y <= surfaceY;

  if (!belowSurface) {
    float skyT = smoothstep(0.0, 1.0, uv.y);
    gl_FragColor = mix(u_backgroundEnd, u_backgroundStart, skyT);
    return;
  }

  float depthT = 0.0;
  if (surfaceY > 0.0001) {
    depthT = clamp(uv.y / surfaceY, 0.0, 1.0);
  }
  vec4 waterCol = mixFour(u_gradientBottom, u_gradientLower, u_gradientUpper, u_gradientTop, depthT);

  float x = uv.x;
  float t = u_time;
  float edge1 = abs(uv.y - surfaceY);
  float foam = smoothstep(0.04, 0.001, edge1);
  float edgeLine = smoothstep(0.006, 0.0, edge1);
  waterCol += u_wave1Color * foam * 0.4;
  waterCol.rgb += vec3(1.0) * edgeLine * 0.25;

  float s1 = sin(TAU * u_wave1Frequency * x + u_wave1Speed * t + u_tiltX * 2.0) * 0.5 + 0.5;
  float s2 = sin(TAU * u_wave2Frequency * x + u_wave2Speed * t + u_tiltY * 2.0) * 0.5 + 0.5;
  float s3 = sin(TAU * u_wave3Frequency * x + u_wave3Speed * t) * 0.5 + 0.5;
  waterCol += u_wave1Color * (0.04 * s1 * (1.0 - depthT * 0.5));
  waterCol += u_wave2Color * (0.03 * s2 * (1.0 - depthT * 0.4));
  waterCol += u_wave3Color * (0.02 * s3 * (1.0 - depthT * 0.3));

  float causticAccum = 0.0;
  for (int i = 0; i < MAX_CAUSTICS; i++) {
    if (i >= u_causticCount) break;
    float fi = float(i);
    vec2 seed = vec2(
      fract(fi * 0.7531 + 0.143),
      fract(fi * 0.3713 + 0.527)
    );
    vec2 center = vec2(0.12, 0.18) + seed * vec2(0.76, 0.62);
    center += vec2(u_tiltX, u_tiltY) * 0.12;
    center += 0.04 * vec2(sin(t * 0.37 + fi * 1.7), cos(t * 0.29 + fi * 2.1));
    float r = u_causticBaseRadius + u_causticRadiusOscillation * sin(t * 2.1 + fi * 0.8);
    vec2 diff = uv - center;
    float dist2 = dot(diff, diff);
    float blob = exp(-dist2 / (r * r * 3.5));
    float d = sqrt(dist2);
    float ring = sin(d * 38.0 - t * 3.5 + fi) * 0.5 + 0.5;
    causticAccum += blob * ring;
  }
  waterCol.rgb += u_causticCenterColor.rgb * causticAccum * u_causticCenterColor.a;

  gl_FragColor = clamp(waterCol, 0.0, 1.0);
}
"""
}
