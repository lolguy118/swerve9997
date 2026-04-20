package com.team271.lib.vendor.ctre;

import com.ctre.phoenix6.hardware.CANcoder;
import com.team271.lib.api.sensor.AbsoluteEncoder;
import com.team271.lib.hardware.sensors.encoders.EncoderCANCoder;
import com.team271.lib.nt.NTTable;

/**
 * CTRE CANCoder exposed through the vendor-neutral {@link AbsoluteEncoder} interface.
 *
 * <p>Wraps {@link EncoderCANCoder} and provides both relative and absolute position.
 */
public class CTREAbsoluteEncoder implements AbsoluteEncoder {

    /*
     * Encoder
     */
    private final EncoderCANCoder mEncoder;

    /*
     * Constructor
     */
    public CTREAbsoluteEncoder(final EncoderCANCoder argEncoder) {
        mEncoder = argEncoder;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying EncoderCANCoder for passthrough access. */
    public EncoderCANCoder getEncoderCANCoder() {
        return mEncoder;
    }

    /** Returns the raw CTRE CANcoder device. */
    public CANcoder getCANcoder() {
        return mEncoder.getCANcoder();
    }

    /*
     *
     * AbsoluteEncoder Interface
     *
     */

    @Override
    public double getPositionRotations() {
        return mEncoder.getPosRotations();
    }

    @Override
    public double getAbsolutePositionRotations() {
        return mEncoder.getPosAbsRotations();
    }

    @Override
    public double getVelocityRPS() {
        return mEncoder.getVelRPS();
    }

    @Override
    public void setPositionRotations(final double argRotations) {
        mEncoder.setPosRotations(argRotations);
    }

    @Override
    public void reset() {
        mEncoder.reset();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    /*
     *
     * Simulation
     *
     */

    @Override
    public void setSimPosition(final double argRotations) {
        mEncoder.setSimPosRotations(argRotations);
    }

    @Override
    public void setSimVelocity(final double argRPS) {
        mEncoder.setSimVelRotations(argRPS);
    }

    /*
     *
     * Lifecycle
     *
     */

    @Override
    public void robotInit(final double argTimestamp) {
        mEncoder.robotInit(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        mEncoder.outputTelemetry();
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        mEncoder.simulationInit(argTimestamp);
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        mEncoder.simulationPeriodic(argTimestamp);
    }

    /*
     *
     * Named
     *
     */

    @Override
    public String getName() {
        return mEncoder.getName();
    }

    @Override
    public NTTable getTable() {
        return mEncoder.getTable();
    }

    @Override
    public String logKey(final String argSuffix) {
        return mEncoder.logKey(argSuffix);
    }
}
