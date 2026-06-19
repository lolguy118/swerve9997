/**
 * CTRE Phoenix 6 implementations of the vendor-neutral {@code api} interfaces ({@code CTREMotor},
 * {@code CTREEncoder}, {@code CTREGyro}, {@code CTRELimitSwitch}, {@code CTRERangeSensor}).
 *
 * <p>{@link org.jspecify.annotations.NullMarked @NullMarked} per ADR-018: non-null by default;
 * {@code null}-able usages are tagged {@link org.jspecify.annotations.Nullable @Nullable}. Enforced
 * by NullAway at build time (warning during rollout).
 */
@NullMarked
package com.team271.lib.vendor.ctre;

import org.jspecify.annotations.NullMarked;
