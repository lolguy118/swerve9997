package com.team271.lib.subsystem;

import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;

/*
 * The Subsystem abstract class, which serves as a basic framework for all robot
 * subsystems. Each subsystem outputs
 * commands to SmartDashboard, has a stop routine (for after each match), and a
 * routine to zero all sensors, which helps
 * with calibration.
 * <p>
 * All Subsystems only have one instance (after all, one robot does not have two
 * drivetrains), and functions get the
 * instance of the drivetrain and act accordingly. Subsystems are also a state
 * machine with a desired state and actual
 * state; the robot code will try to match the two states with actions. Each
 * Subsystem also is responsible for
 * instantiating all member components at the start of the match.
 */
public abstract class Subsystem extends TObj {
    public enum SensorMode {
        SENSORED_AUTO,
        SENSORED_MANUAL,
        SENSORLESS,
        SYSID
    }

    /*
     *
     * Variables
     *
     */
    protected SensorMode mode = SensorMode.SENSORED_AUTO;
    protected boolean isZeroed = false;

    final NTEntry ntMode = new NTEntry(table, "Mode", 0);
    final NTEntry ntIsZeroed = new NTEntry(table, "IsZeroed", isZeroed);

    /*
     *
     * Constructors
     *
     */
    protected Subsystem(final TObj argParent, final String argName) {
        super(argParent, argName);
    }

    /*
     *
     * Getters
     *
     */
    public SensorMode getSensorMode() {
        return mode;
    }

    public boolean isZeroed() {
        return isZeroed;
    }

    /*
     *
     * Sensors
     *
     */
    public void sensorsDisable() {
        mode = SensorMode.SENSORLESS;

        isZeroed = false;
    }

    public void sensorsEnableAuto() {
        if (mode != SensorMode.SENSORED_AUTO) {
            mode = SensorMode.SENSORED_AUTO;
            isZeroed = false;
        }
    }

    public void sensorsEnableManual() {
        if (mode != SensorMode.SENSORED_MANUAL) {
            mode = SensorMode.SENSORED_MANUAL;
            isZeroed = false;
        }
    }

    public void sensorsZero() {
        isZeroed = false;
    }

    /*
     *
     * Telemetry
     *
     */
    public void outputTelemetry() {
        if (mode == SensorMode.SENSORLESS) {
            ntMode.publish(0);
        } else if (mode == SensorMode.SENSORED_AUTO) {
            ntMode.publish(1);
        } else if (mode == SensorMode.SENSORED_MANUAL) {
            ntMode.publish(2);
        } else if (mode == SensorMode.SYSID) {
            ntMode.publish(3);
        } else {
            ntMode.publish(-1);
        }

        ntIsZeroed.publish(isZeroed());
    }
}
