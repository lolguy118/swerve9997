/**
 * Subsystem infrastructure — the {@code Subsystem} base class, {@code SubsystemManager} (lifecycle
 * orchestration with per-subsystem exception isolation), and {@code StateMachine}.
 *
 * <p>{@link org.jspecify.annotations.NullMarked @NullMarked} per ADR-018: non-null by default;
 * {@code null}-able usages are tagged {@link org.jspecify.annotations.Nullable @Nullable}. Enforced
 * by NullAway at build time (warning during rollout).
 */
@NullMarked
package com.team271.lib.subsystem;

import org.jspecify.annotations.NullMarked;
