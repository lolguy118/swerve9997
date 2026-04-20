package com.team271.lib.hardware.sensors.range;

import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;

public abstract class RangeBase extends TObj {
    public enum RangeType {
        CANRANGE
    }

    /*
     * Range
     */
    protected final RangeType type;

    protected double scale = 1.0;

    protected double dist = 0.0;

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntScale = new NTEntry(table, "Scale", 0.0);

    final NTEntry ntPosRaw = new NTEntry(table, "DistRaw", 0.0);
    final NTEntry ntPos = new NTEntry(table, "Dist", 0.0);

    /*
     *
     * Constructors
     *
     */
    protected RangeBase(final TObj argParent, final String argName, final RangeType argRangeType) {
        super(argParent, "(Range)" + argName);

        /*
         * Store Type
         */
        type = argRangeType;
    }

    /*
     *
     * Encoder
     *
     */
    protected abstract void create();

    public void reset() {}

    /*
     * Position Raw - Rotations
     */
    public double getDistRaw() {
        return dist;
    }

    /*
     * Unit Scaling
     */
    public void setScale(final double argScale) {
        scale = argScale;
    }

    public double getScale() {
        return scale;
    }

    /*
     * Position - Units
     */
    public double getDist() {
        return getDistRaw() * scale;
    }

    /*
     *
     * Simulation
     *
     */
    @Override
    public void simulationInit(final double argTimestamp) {}

    @Override
    public void simulationPeriodic(final double argTimestamp) {}

    /*
     *
     * Telemetry
     *
     */
    @Override
    public void outputTelemetry() {
        ntScale.publish(getScale());

        ntPosRaw.publish(getDistRaw());
        ntPos.publish(getDist());
    }
}
