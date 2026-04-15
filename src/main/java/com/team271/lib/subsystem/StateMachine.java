package com.team271.lib.subsystem;

import com.team271.lib.nt.NTEntry;
import com.team271.lib.nt.NTTable;

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

    private final NTEntry ntCurrent;
    private final NTEntry ntDesired;

    public StateMachine(final NTTable argTable, final S argInitialState) {
        currentState = argInitialState;
        desiredState = argInitialState;

        ntCurrent = new NTEntry(argTable, "Current State", argInitialState.name());
        ntDesired = new NTEntry(argTable, "Desired State", argInitialState.name());
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

    /** Transitions the current state. Call this after the subsystem has applied the state. */
    public void transition(final S argNewState) {
        currentState = argNewState;
    }

    /** Publishes current and desired state to NetworkTables. */
    public void outputTelemetry() {
        ntCurrent.publish(currentState.name());
        ntDesired.publish(desiredState.name());
    }
}
