package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.hardware.transmissions.GearRatio;

/**
 * Adapter wrapping a TalonFX internal encoder ({@link EncoderFX}) with gear ratio conversions. Uses
 * the rotor-to-mechanism conversion chain.
 */
public class FXEncoderAdapter implements EncoderAdapter {
    private final EncoderFX encoder;
    private GearRatio gearRatio;

    public FXEncoderAdapter(EncoderFX encoder, GearRatio gearRatio) {
        this.encoder = encoder;
        this.gearRatio = gearRatio;
    }

    @Override
    public void refresh() {
        encoder.refresh();
    }

    @Override
    public double getPosition() {
        return gearRatio.rotorToOutput(encoder.getPosRotations());
    }

    @Override
    public double getAbsolutePosition() {
        return 0.0; // FX internal encoder has no absolute position
    }

    @Override
    public double getVelocity() {
        return gearRatio.rotorToOutput(encoder.getVelRPS());
    }

    @Override
    public double getPositionRotations() {
        return encoder.getPosRotations();
    }

    @Override
    public double getVelocityRPS() {
        return encoder.getVelRPS();
    }

    @Override
    public void setPositionRotations(double rotations) {
        encoder.setPosRotations(rotations);
    }

    @Override
    public void reset() {
        encoder.reset();
    }

    @Override
    public double mechanismToNative(double mechanismUnits) {
        return gearRatio.outputToRotor(mechanismUnits);
    }

    @Override
    public double mechanismVelocityToNative(double mechanismUnitsPerSec) {
        return gearRatio.outputToRotor(mechanismUnitsPerSec);
    }

    @Override
    public GearRatio getGearRatio() {
        return gearRatio;
    }

    @Override
    public void updateGearRatio(GearRatio newRatio) {
        this.gearRatio = newRatio;
    }

    @Override
    public void robotInit(double timestamp) {
        encoder.robotInit(timestamp);
    }

    @Override
    public void outputTelemetry() {
        encoder.outputTelemetry();
    }

    @Override
    public void setSimPosition(double rotations) {
        encoder.setSimPosRotations(rotations);
    }

    @Override
    public void setSimVelocity(double rps) {
        encoder.setSimVelRotations(rps);
    }

    @Override
    public void simulationInit(double timestamp) {
        encoder.simulationInit(timestamp);
    }

    @Override
    public void simulationPeriodic(double timestamp) {
        encoder.simulationPeriodic(timestamp);
    }

    /** Returns the underlying EncoderFX for direct access when needed. */
    public EncoderFX getEncoder() {
        return encoder;
    }
}
