package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.hardware.transmissions.GearRatio;

/**
 * Adapter wrapping a TalonFX internal encoder ({@link EncoderFX}) with gear ratio conversions. Uses
 * the rotor-to-mechanism conversion chain.
 */
public class FXEncoderAdapter implements EncoderAdapter {
    private final EncoderFX encoder;
    private GearRatio gearRatio;

    public FXEncoderAdapter(final EncoderFX argEncoder, final GearRatio argGearRatio) {
        this.encoder = argEncoder;
        this.gearRatio = argGearRatio;
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
    public void setPositionRotations(final double argRotations) {
        encoder.setPosRotations(argRotations);
    }

    @Override
    public void reset() {
        encoder.reset();
    }

    @Override
    public double mechanismToNative(final double argMechanismUnits) {
        return gearRatio.outputToRotor(argMechanismUnits);
    }

    @Override
    public double mechanismVelocityToNative(final double argMechanismUnitsPerSec) {
        return gearRatio.outputToRotor(argMechanismUnitsPerSec);
    }

    @Override
    public GearRatio getGearRatio() {
        return gearRatio;
    }

    @Override
    public void updateGearRatio(final GearRatio argNewRatio) {
        this.gearRatio = argNewRatio;
    }

    @Override
    public void robotInit(final double argTimestamp) {
        encoder.robotInit(argTimestamp);
    }

    @Override
    public void outputTelemetry() {
        encoder.outputTelemetry();
    }

    @Override
    public void setSimPosition(final double argRotations) {
        encoder.setSimPosRotations(argRotations);
    }

    @Override
    public void setSimVelocity(final double argRps) {
        encoder.setSimVelRotations(argRps);
    }

    @Override
    public void simulationInit(final double argTimestamp) {
        encoder.simulationInit(argTimestamp);
    }

    @Override
    public void simulationPeriodic(final double argTimestamp) {
        encoder.simulationPeriodic(argTimestamp);
    }

    /** Returns the underlying EncoderFX for direct access when needed. */
    public EncoderFX getEncoder() {
        return encoder;
    }
}
