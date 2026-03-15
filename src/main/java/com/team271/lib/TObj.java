package com.team271.lib;

import com.team271.lib.nt.NTTable;

public abstract class TObj {
    protected final String name;

    protected final NTTable table;

    /*
     *
     * Constructors
     *
     */
    protected TObj(final TObj argParent, final String argName) {
        name = argName;

        if (argParent != null) {
            table = new NTTable(argParent.getTable(), argName);
        } else {
            table = new NTTable(argName);
        }
    }

    protected TObj(final String argName) {
        this(null, argName);
    }

    /*
     *
     * Getters
     */
    public NTTable getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    /*
     *
     * Robot
     *
     */
    public void robotInit(final double argTimestamp) {}

    public void robotPeriodicBefore(final double argTimestamp) {}

    public void robotPeriodicAfter(final double argTimestamp) {}

    /*
     *
     * Disabled
     *
     */
    public void disabledInit(final double argTimestamp) {}

    public void disabledPeriodic(final double argTimestamp) {}

    public void disabledExit(final double argTimestamp) {}

    /*
     *
     * Auto
     *
     */
    public void autonomousInit(final double argTimestamp) {}

    public void autonomousPeriodic(final double argTimestamp) {}

    public void autonomousExit(final double argTimestamp) {}

    /*
     *
     * Teleop
     *
     */
    public void teleopInit(final double argTimestamp) {}

    public void teleopPeriodic(final double argTimestamp) {}

    public void teleopExit(final double argTimestamp) {}

    /*
     *
     * Sim
     *
     */
    public void simulationInit(final double argTimestamp) {}

    public void simulationPeriodic(final double argTimestamp) {}

    /*
     *
     * Test
     *
     */
    public void testInit(final double argTimestamp) {}

    public void testPeriodic(final double argTimestamp) {}

    public void testExit(final double argTimestamp) {}

    /*
     *
     * Telemetry
     *
     */
    public void outputTelemetry() {}
}
