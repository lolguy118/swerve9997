package com.team271.lib.subsystem;

import com.team271.lib.nt.NTEntry;
import com.team271.lib.nt.NTTable;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

/**
 * Generic desired-to-actual state machine helper.
 *
 * <p>Subsystems use this as a composition field, not a base class:
 *
 * <pre>{@code
 * public class Arm extends Subsystem {
 *     enum ArmState { STOWED, SCORING, CLIMBING }
 *     private final StateMachine<ArmState> sm;
 *
 *     public Arm(TObj parent) {
 *         super(parent, "Arm");
 *         sm = new StateMachine<>(table, ArmState.STOWED);
 *     }
 * }
 * }</pre>
 *
 * @param <S> the enum type representing valid states
 */
public class StateMachine<S extends Enum<S>> {
    private S currentState;
    private S desiredState;

    @Nullable private BiConsumer<S, S> onEnter;
    @Nullable private BiConsumer<S, S> onExit;
    private boolean transitioning = false;

    private final NTEntry ntCurrent;
    private final NTEntry ntDesired;

    public StateMachine(final NTTable argTable, final S argInitialState) {
        currentState = argInitialState;
        desiredState = argInitialState;

        ntCurrent = new NTEntry(argTable, "Current State", argInitialState.name());
        ntDesired = new NTEntry(argTable, "Desired State", argInitialState.name());
    }

    /**
     * Sets a callback invoked when entering a new state (after currentState is updated).
     *
     * @param callback receives (fromState, toState)
     * @return this, for chaining
     */
    public StateMachine<S> withOnEnter(final BiConsumer<S, S> callback) {
        this.onEnter = callback;
        return this;
    }

    /**
     * Sets a callback invoked when exiting the current state (before currentState is updated).
     *
     * @param callback receives (fromState, toState)
     * @return this, for chaining
     */
    public StateMachine<S> withOnExit(final BiConsumer<S, S> callback) {
        this.onExit = callback;
        return this;
    }

    /** Sets the desired state. Applied by the subsystem in robotPeriodicAfter(). */
    public void setDesiredState(final S argState) {
        desiredState = argState;
    }

    /** Returns the current (actual) state. */
    public S getCurrentState() {
        return currentState;
    }

    /** Returns the desired (target) state. */
    public S getDesiredState() {
        return desiredState;
    }

    /** Returns true if the current state differs from the desired state. */
    public boolean isTransitioning() {
        return currentState != desiredState;
    }

    /**
     * Transitions the current state. Call this after the subsystem has applied the state.
     *
     * <p>If the state actually changes, {@code onExit} is called before the update and {@code
     * onEnter} is called after. Both callbacks receive (fromState, toState). If the new state
     * equals the current state, no callbacks fire.
     */
    public void transition(final S argNewState) {
        if (currentState == argNewState) {
            return;
        }
        if (transitioning) {
            // Prevent re-entrant calls from onExit/onEnter callbacks
            edu.wpi.first.wpilibj.DriverStation.reportWarning(
                    "StateMachine: ignoring re-entrant transition() call during callback", false);
            return;
        }
        transitioning = true;
        try {
            S from = currentState;
            if (onExit != null) {
                onExit.accept(from, argNewState);
            }
            currentState = argNewState;
            if (onEnter != null) {
                onEnter.accept(from, argNewState);
            }
        } finally {
            transitioning = false;
        }
    }

    /** Publishes current and desired state to NetworkTables. */
    public void outputTelemetry() {
        ntCurrent.publish(currentState.name());
        ntDesired.publish(desiredState.name());
    }
}
