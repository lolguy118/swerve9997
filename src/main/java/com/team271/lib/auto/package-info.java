/**
 * Autonomous-move composition — the {@code AutoMove} primitive and its composition blocks ({@code
 * AutoMoveSequence}, {@code AutoMoveParallel}, {@code AutoMoveConditional}, {@code AutoMoveTimed},
 * {@code WaitMove}), plus {@code AutoMode}.
 *
 * <p>{@link org.jspecify.annotations.NullMarked @NullMarked} per ADR-018: non-null by default;
 * {@code null}-able usages are tagged {@link org.jspecify.annotations.Nullable @Nullable}. Enforced
 * by NullAway at build time (warning during rollout).
 */
@NullMarked
package com.team271.lib.auto;

import org.jspecify.annotations.NullMarked;
