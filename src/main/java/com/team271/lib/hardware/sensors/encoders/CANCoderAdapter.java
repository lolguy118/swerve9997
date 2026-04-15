package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.hardware.transmissions.GearRatio;

/**
 * Adapter wrapping a CANCoder external encoder ({@link EncoderCANCoder}) with gear ratio
 * conversions. Uses the sensor-to-mechanism conversion chain and exposes absolute position.
 */
public class CANCoderAdapter implements EncoderAdapter {
    private final EncoderCANCoder encoder;
    private GearRatio gearRatio;

    public CANCoderAdapter(EncoderCANCoder encoder, GearRatio gearRatio) {
        this.encoder = encoder;
        this.gearRatio = gearRatio;
    }

    @Override
    public void refresh() {
        encoder.refresh();
    }

    @Override
    public double getPosition() {
        return gearRatio.sensorRelToOutput(encoder.getPosRotations());
    }

    @Override
    public double getAbsolutePosition() {
        return gearRatio.sensorAbsToOutput(encoder.getPosAbsRotations());
    }

    @Override
    public double getVelocity() {
        return gearRatio.sensorRelToOutput(encoder.getVelRPS());
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
        return gearRatio.outputToSensorRel(mechanismUnits);
    }

    @Override
    public double mechanismVelocityToNative(double mechanismUnitsPerSec) {
        return gearRatio.outputToSensorRel(mechanismUnitsPerSec);
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

    /** Returns the underlying EncoderCANCoder for direct access when needed. */
    public EncoderCANCoder getEncoder() {
        return encoder;
    }
}
