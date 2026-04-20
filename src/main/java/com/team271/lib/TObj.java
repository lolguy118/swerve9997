package com.team271.lib;

import com.team271.lib.nt.NTTable;

public abstract class TObj implements Lifecycle, Named {
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
    @Override
    public NTTable getTable() {
        return table;
    }

    @Override
    public String getName() {
        return name;
    }

    /** Build an AdvantageKit log key from this object's NT path. */
    @Override
    public String logKey(final String suffix) {
        return table.getPath() + "/" + suffix;
    }

    /*
     *
     * Robot
     *
     */
    @Override
    public void robotInit(final double argTimestamp) {}

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {}

    @Override
    public void robotPeriodicAfter(final double argTimestamp) {}

    /*
     *
     * Disabled
     *
     */
    @Override
    public void disabledInit(final double argTimestamp) {}

    @Override
    public void disabledPeriodic(final double argTimestamp) {}

    @Override
    public void disabledExit(final double argTimestamp) {}

    /*
     *
     * Auto
     *
     */
    @Override
    public void autonomousInit(final double argTimestamp) {}

    @Override
    public void autonomousPeriodic(final double argTimestamp) {}

    @Override
    public void autonomousExit(final double argTimestamp) {}

    /*
     *
     * Teleop
     *
     */
    @Override
    public void teleopInit(final double argTimestamp) {}

    @Override
    public void teleopPeriodic(final double argTimestamp) {}

    @Override
    public void teleopExit(final double argTimestamp) {}

    /*
     *
     * Sim
     *
     */
    @Override
    public void simulationInit(final double argTimestamp) {}

    @Override
    public void simulationPeriodic(final double argTimestamp) {}

    /*
     *
     * Test
     *
     */
    @Override
    public void testInit(final double argTimestamp) {}

    @Override
    public void testPeriodic(final double argTimestamp) {}

    @Override
    public void testExit(final double argTimestamp) {}

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {}
}
