package com.team271.lib.vendor.ctre;

import com.team271.lib.api.sensor.Encoder;
import com.team271.lib.hardware.sensors.encoders.EncoderFX;
import com.team271.lib.nt.NTTable;

/**
 * CTRE TalonFX internal encoder exposed through the vendor-neutral {@link Encoder} interface.
 *
 * <p>Wraps {@link EncoderFX}. For absolute encoder support, use {@link CTREAbsoluteEncoder} with a
 * CANCoder.
 */
public class CTREEncoder implements Encoder {

    /*
     * Encoder
     */
    private final EncoderFX mEncoder;

    /*
     * Constructor
     */
    public CTREEncoder(final EncoderFX argEncoder) {
        mEncoder = argEncoder;
    }

    /*
     * Passthrough
     */

    /** Returns the underlying EncoderFX for passthrough access. */
    public EncoderFX getEncoderFX() {
        return mEncoder;
    }

    /*
     *
     * Encoder Interface
     *
     */

    @Override
    public double getPositionRotations() {
        return mEncoder.getPosRotations();
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
        return true; // FX internal encoder is always connected when motor is connected
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
