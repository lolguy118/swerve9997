package com.team271.lib.hardware.sensors.encoders;

import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;

public abstract class EncoderBase extends TObj {
    public enum EncoderType {
        INTERNAL_FX,
        INTERNAL_MAX,
        CANCODER
    }

    public enum EncoderDirection {
        CW,
        CCW
    }

    /*
     * Encoder
     */
    protected final EncoderType type;
    protected EncoderDirection direction;

    protected double posRotations = 0.0;
    protected double velRotations = 0.0;

    /*
     * Simulation
     */

    /*
     *
     * Telemetry (NT)
     *
     */
    final NTEntry ntPos = new NTEntry(table, "Pos", 0.0);
    final NTEntry ntVel = new NTEntry(table, "Vel", 0.0);

    /*
     *
     * Constructors
     *
     */
    protected EncoderBase(
            final TObj argParent, final String argName, final EncoderType argEncoderType) {
        super(argParent, "(Encoder)" + argName);

        /*
         * Store Type
         */
        type = argEncoderType;

        /*
         * Set Default Position
         */
        setPosRotations(0);
    }

    /*
     *
     * Encoder
     *
     */
    protected abstract void create();

    public void reset() {
        setPosRotations(0.0);
    }

    /*
     * Position - Rotations
     */
    public double getPosRotations() {
        return posRotations;
    }

    public void setPosRotations(final double argPositionRotations) {
        posRotations = argPositionRotations;
    }

    /*
     * Velocity - Rotations
     */
    public double getVelRPS() {
        return velRotations;
    }

    /*
     *
     * Simulation
     *
     */
    public abstract void setSimVelRotations(final double argVelRotations);

    public abstract void setSimPosRotations(final double argPositionRotations);

    public abstract void simulationInit(final double argTimestamp);

    public abstract void simulationPeriodic(final double argTimestamp);

    /*
     *
     * Telemetry
     *
     */
    public void outputTelemetry() {
        ntPos.publish(getPosRotations());
        ntVel.publish(getVelRPS());
    }
}
