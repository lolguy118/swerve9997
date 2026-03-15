package com.team271.libtest.subsystems;

import com.team271.libtest.subsystems.Input.InputDriver;
import com.team271.libtest.subsystems.Input.InputOp;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.subsystem.Subsystem;

public class Superstructure extends Subsystem {
    /*
     * Singleton
     */
    private static Superstructure mInstance;

    public static Superstructure getInstance(final TObj argParent) {
        if (mInstance == null) {
            mInstance = new Superstructure(argParent);
        }

        return mInstance;
    }

    public static Superstructure getInstance() {
        return mInstance;
    }

    /*
     * Other Singletons
     */
    protected final InputDriver mInputDriver = InputDriver.getInstance();
    protected final InputOp mInputOp = InputOp.getInstance();

    /*
     * Constants
     */
    protected static final class SuperstructureConstants {
    }

    public enum ROBOT_STATE {
        MANUAL,
    }

    /*
     * Variables
     */
    protected ROBOT_STATE state = ROBOT_STATE.MANUAL;
    protected ROBOT_STATE statePrev = ROBOT_STATE.MANUAL;
    protected ROBOT_STATE stateGoal = ROBOT_STATE.MANUAL;


    /*
     *
     * Telemetry (NT)
     * 
     */
    final NTEntry tState = new NTEntry(table, "State", "None");
    final NTEntry tStatePrev = new NTEntry(table, "State Prev", "None");
    final NTEntry tStateGoal = new NTEntry(table, "State Goal", "None");

    /*
     * 
     * Constructors
     * 
     */
    public Superstructure(final TObj argParent) {
        super(argParent, "Superstructure");
    }

    /*
     * 
     * Robot States
     * 
     */
    @Override
    public void robotInit(final double timestamp) {
    }

    /*
     * 
     * Superstructure
     * 
     */
    public ROBOT_STATE getState()
    {
        return state;
    }

    /*
     *
     * Robot States
     * 
     */
    @Override
    public void robotPeriodicBefore(final double timestamp) {
        statePrev = state;
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
    }

    @Override
    public void disabledInit(final double argTimestamp) {
    }

    @Override
    public void teleopInit(final double argTimestamp) {
    }

    @Override
    public void teleopPeriodic(final double argTimestamp) {
        if (mInputDriver.getDisableSensors()) {
            sensorsDisable();
            state = ROBOT_STATE.MANUAL;
        } else if (mInputDriver.getEnableSensors()) {
            sensorsEnableManual();
        }
    }

    @Override
    public void autonomousInit(final double argTimestamp) {
    }

    @Override
    public void autonomousPeriodic(final double argTimestamp) {
    }

    @Override
    public void simulationInit(final double argTimestamp) {
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
    }

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();


        tState.publish(stateToString(state));
        tStatePrev.publish(stateToString(statePrev));
        tStateGoal.publish(stateToString(stateGoal));
    }

    protected String stateToString(ROBOT_STATE argState)
    { 
        switch (argState) {
            case MANUAL:
                return "MANUAL";

            default:
                return "INVALID";
        }
    }
}
