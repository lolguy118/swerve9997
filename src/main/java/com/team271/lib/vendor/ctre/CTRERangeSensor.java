package com.team271.lib.vendor.ctre;

import com.team271.lib.api.sensor.RangeSensor;
import com.team271.lib.hardware.sensors.range.RangeCANrange;
import com.team271.lib.nt.NTTable;

/**
 * CTRE CANrange time-of-flight sensor exposed through the vendor-neutral {@link RangeSensor}
 * interface.
 *
 * <p>Wraps {@link RangeCANrange}.
 */
public class CTRERangeSensor implements RangeSensor {

    /*
     * Sensor
     */
    private final RangeCANrange mSensor;

    /*
     * Constructor
     */
    public CTRERangeSensor(final RangeCANrange argSensor) {
        mSensor = argSensor;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying RangeCANrange for passthrough access. */
    public RangeCANrange getRangeCANrange() {
        return mSensor;
    }

    /*
     *
     * RangeSensor Interface
     *
     */

    @Override
    public double getDistanceMeters() {
        return mSensor.getDist();
    }

    @Override
    public boolean isConnected() {
        /* CANrange connection inferred from device availability */
        return mSensor.getCANrange() != null;
    }

    /*
     *
     * Lifecycle
     *
     */

    @Override
    public void robotInit(final double argTimestamp) {
        mSensor.robotInit(argTimestamp);
    }

    @Override
    public void robotPeriodicBefore(final double argTimestamp) {
        mSensor.robotPeriodicBefore(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        mSensor.outputTelemetry();
    }

    /*
     *
     * Named
     *
     */

    @Override
    public String getName() {
        return mSensor.getName();
    }

    @Override
    public NTTable getTable() {
        return mSensor.getTable();
    }

    @Override
    public String logKey(final String argSuffix) {
        return mSensor.logKey(argSuffix);
    }
}
