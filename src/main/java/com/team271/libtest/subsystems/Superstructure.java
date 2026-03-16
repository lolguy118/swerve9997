package com.team271.libtest.subsystems;

import com.team271.lib.TObj;
import com.team271.lib.auto.AutoMode;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.subsystem.Subsystem;
import com.team271.libtest.auto.auto_modes.Auto0;

/**
 * High-level coordination subsystem. Manages the overall robot state and coordinates between
 * Infrastructure, Input, and autonomous modes.
 *
 * <p>Uses the desired-state/actual-state pattern: desired state is set in teleopPeriodic() or
 * autonomousPeriodic(), and applied in robotPeriodicAfter().
 */
public class Superstructure extends Subsystem {
    public enum RobotState {
        IDLE,
        TESTING,
        AUTO_RUNNING
    }

    /*
     * Variables
     */
    private RobotState desiredState = RobotState.IDLE;
    private RobotState actualState = RobotState.IDLE;

    private AutoMode autoMode;

    /*
     * Telemetry
     */
    private final NTEntry ntDesiredState = new NTEntry(table, "Desired State", 0);
    private final NTEntry ntActualState = new NTEntry(table, "Actual State", 0);

    /*
     * Constructor
     */
    public Superstructure(final TObj argParent) {
        super(argParent, "Superstructure");
    }

    /*
     * Getters
     */
    public RobotState getActualState() {
        return actualState;
    }

    public RobotState getDesiredState() {
        return desiredState;
    }

    public void setDesiredState(final RobotState argState) {
        desiredState = argState;
    }

    /*
     * Lifecycle
     */
    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        if (autoMode != null) {
            autoMode.robotPeriodicBefore(argTimestamp);
        }
    }

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {
        /* State transition logic */
        if (desiredState != actualState) {
            actualState = desiredState;
        }

        if (autoMode != null) {
            autoMode.robotPeriodicAfter(argTimestamp);
        }
    }

    @Override
    public void autonomousInit(final double argTimestamp) {
        autoMode = new Auto0();
        desiredState = RobotState.AUTO_RUNNING;

        autoMode.autonomousInit(argTimestamp);
    }

    @Override
    public void autonomousPeriodic(final double argTimestamp) {
        if (autoMode != null) {
            autoMode.autonomousPeriodic(argTimestamp);
        }
    }

    @Override
    public void autonomousExit(final double argTimestamp) {
        if (autoMode != null) {
            autoMode.autonomousExit(argTimestamp);
        }
        desiredState = RobotState.IDLE;
    }

    @Override
    public void teleopInit(final double argTimestamp) {
        desiredState = RobotState.IDLE;
    }

    @Override
    public void disabledInit(final double argTimestamp) {
        desiredState = RobotState.IDLE;
    }

    /*
     * Telemetry
     */
    @Override
    public void outputTelemetry() {
        super.outputTelemetry();

        ntDesiredState.publish(desiredState.ordinal());
        ntActualState.publish(actualState.ordinal());
    }
}
