# BeOS Icon Style Guide

## Overview

BeOS icons follow a consistent style. They should be immediately recognizable, readable at small sizes, and detailed without depending on scaling or vector rendering.

They look like small 3D objects from the same desktop world. They are not abstract symbols.

## 1. Core Characteristics

### Perspective

- Draw icons in a fixed 3/4 isometric perspective
- The virtual camera is slightly above and to the left
- Objects are never front-on or arbitrarily rotated
- All icons share the same implied viewpoint

### Lighting

- Use one main light source from the upper left
- Top faces are light
- Right faces are mid-tone
- Left and bottom faces are darkest
- Shadows are painted into the icon itself, not dropped by the UI

### Shape Language

- Objects are chunky, thick, and physically plausible
- Edges are usually beveled or rounded
- Thin lines and flat shapes are avoided

## 2. Rendering Style

### Pixel-First (Not Vector)

- Design primarily for 32x32 and 16x16
- Every pixel is manually controlled
- No procedural shading
- No automatic scaling logic

### Shading

- Use hand-painted gradients
- Use 3-6 distinct tones per surface
- No noise or texture unless the material requires it (for example, wood, metal)

### Outlines

- Avoid black outlines
- Define edges with color contrast and shading transitions
- Occasionally a single dark pixel is used to separate from the background

## 3. Color Usage

### Palette

- High saturation
- Clean, bright colors
- Minimal gray unless the object is metallic or neutral

### Contrast

- Strong value separation between faces:
  - Top = light
  - Side = mid
  - Bottom = dark

### Background Treatment

- Icons usually float freely or sit on a small painted base or shadow
- Rarely boxed or framed

## 4. Metaphor and Symbolism

- Icons represent literal physical objects
- Use real-world objects such as a mailbox, disk, folder, or chip
- They should not be abstract glyphs or logos

## 5. Comparison With Other Systems

### Versus Windows 95/98

- BeOS icons use stricter perspective, stronger lighting, fewer black outlines, and a more consistent physical world.

### Versus Classic Mac OS

- BeOS icons are more isometric, more volumetric, less sketch-like, and more object-centric.

## 6. Practical Construction Rules

1. Choose a simple, real object
2. Rotate it to the standard 3/4 view
3. Apply one light source from the upper left
4. Use at least three tone bands per major surface
5. Avoid black outlines
6. Make the object look thick and solid
7. Ensure the object is readable at 32x32

## 7. One-Sentence Summary

BeOS icons are hand-painted, isometric, strongly lit, saturated mini-objects with consistent perspective and little or no outlining.
