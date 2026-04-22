# Input Shaping Guide

> **Scope:** This guide explains when to use each input shaping mode.
> For the Input class hierarchy and controller implementations, see
> [Hardware Abstraction — Input System](../planning/sdd/SDD-hardware.md#34-input-system).

---

## What Input Shaping Does

Input shaping maps raw joystick axis values (linear -1.0 to 1.0)
through a mathematical curve. The goal is to adjust the relationship
between stick position and motor output:

- **Reduced center sensitivity** — fine control near center without
  sacrificing full-range authority at extremes
- **Increased center sensitivity** — faster response for experienced
  drivers who want maximum responsiveness

The sign of the input is preserved; shaping is applied to the absolute
value.

---

## Mode Comparison

| Mode | Formula (applied to `|v|`) | Center Feel | Edge Feel | Curve Shape |
| ------ | --------------------------- | ------------- | ----------- | ------------- |
| `NONE` / `LINEAR` | `v` | Normal | Normal | Straight line |
| `SOFT` | `v^1.48` | Slightly reduced | Slightly increased | Gentle power curve |
| `SQUARED` | `v^2` | Moderately reduced | Moderately increased | Parabolic |
| `CUBED` | `v^3` | Strongly reduced | Strongly increased | Cubic S-shape |
| `AGGRESSIVE` | `sqrt(v)` | Increased (more sensitive) | Reduced | Square root |
| `MORE_AGGRESSIVE` | `sqrt(1 - (v-1)^2)` | Strongly increased | Strongly reduced | Quarter circle |
| `DYNAMIC` | `(cos(pi*(v+1))/2) + 0.5` | Variable (S-curve) | Variable | Cosine S-curve |

### Response Curve Characteristics

**Reduced center sensitivity** (SOFT, SQUARED, CUBED):
The output is smaller than the input near center, giving fine control.
At the extremes, the curve catches up to full output. Think of it as
"slow start, fast finish."

**Increased center sensitivity** (AGGRESSIVE, MORE_AGGRESSIVE):
The output is larger than the input near center, giving fast initial
response. At the extremes, the curve flattens. Think of it as
"fast start, slow finish."

**DYNAMIC** (cosine S-curve):
Combines both — reduced sensitivity at center AND at extremes, with
a steep transition in the middle. Creates an S-shaped response.

---

## Selection Decision Tree

### 1. Is the mechanism safety-critical?

Mechanisms near hard stops (elevators, arms, wrists) benefit from
reduced center sensitivity to prevent overshoot.

**Recommendation:** `SQUARED` or `CUBED`

### 2. Is this a precision mechanism?

Turrets, fine-positioning systems, and mechanisms where small
adjustments matter most.

**Recommendation:** `CUBED` (strongest center deadening)

### 3. Is the driver experienced and wants maximum responsiveness?

Experienced drivers who find `SQUARED` too sluggish.

**Recommendation:** `AGGRESSIVE` (instant response at center)

### 4. Is this the first time setting up a new robot?

When you don't know what the robot needs yet.

**Recommendation:** Start with `SQUARED` — it's the most forgiving
default. Tune from there based on driver feedback.

### 5. Is this a drivetrain?

| Driver Preference | Mode |
| ------------------- | ------ |
| Smooth, easy to control | `SQUARED` |
| Fast robot, hard to control | `CUBED` |
| Experienced, wants speed | `SOFT` or `LINEAR` |

### Quick Reference

| Mechanism | Recommended Mode | Why |
| ----------- | ----------------- | ----- |
| Drivetrain (new driver) | `SQUARED` | Forgiving center, full range available |
| Drivetrain (fast robot) | `CUBED` | Strong center damping prevents jerky driving |
| Drivetrain (experienced) | `SOFT` or `LINEAR` | Minimal interference with driver intent |
| Elevator/Arm | `SQUARED` or `CUBED` | Prevents slamming into hard stops |
| Turret/Fine aim | `CUBED` | Maximum precision near center |
| Intake wheels | `LINEAR` | Direct control, no precision needed |

---

## Code Example

```java
// In your Input subclass or teleopPeriodic():
double rawY = driver.getLeftY();
double shapedY = driver.inputShaping(InputShaping.SQUARED, rawY);

// Apply deadzone BEFORE shaping for best feel:
double deadzoned = Util.handleDeadzone(rawY, 0.05);
double shaped = driver.inputShaping(InputShaping.SQUARED, deadzoned);
```

### Combining with Deadzone

Apply deadzone first, then shaping. This ensures the deadzone is a
clean cutoff (no tiny outputs from shaping noise near zero), and the
shaping curve operates on the full remaining range.

---

## Tuning Tips

1. **Start with SQUARED** — it works well for most mechanisms
2. **Test in teleop** — watch the robot's response to small and large
   stick movements
3. **Ask the driver** — "Is center control too twitchy or too sluggish?"
   - Too twitchy → move toward CUBED
   - Too sluggish → move toward SOFT or LINEAR
4. **Different mechanisms can use different modes** — drivetrain on
   SQUARED, turret on CUBED, intake on LINEAR
5. **DYNAMIC mode** is experimental — test thoroughly before competition
