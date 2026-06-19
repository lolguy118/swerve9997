/**
 * PID controller implementations behind a common {@code PIDBase} — software, WPILib-backed, and
 * TalonFX-hardware variants ({@code PIDSimple}, {@code PIDWPI}, {@code PIDWPI_Trap}, {@code PIDFX},
 * {@code PIDTrap}).
 *
 * <p>{@link org.jspecify.annotations.NullMarked @NullMarked} per ADR-018: non-null by default;
 * {@code null}-able usages are tagged {@link org.jspecify.annotations.Nullable @Nullable}. Enforced
 * by NullAway at build time (warning during rollout).
 */
@NullMarked
package com.team271.lib.control.pid;

import org.jspecify.annotations.NullMarked;
