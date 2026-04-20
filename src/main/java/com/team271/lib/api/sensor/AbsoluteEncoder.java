package com.team271.lib.api.sensor;

/**
 * Vendor-neutral absolute encoder interface.
 *
 * <p>Extends {@link Encoder} with absolute position reporting. Absolute encoders retain their
 * position across power cycles using a magnet or other reference.
 */
public interface AbsoluteEncoder extends Encoder {

    /** Returns the absolute position in rotations (retained across power cycles). */
    double getAbsolutePositionRotations();
}
