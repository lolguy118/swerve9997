package com.team271.lib.hardware.transmissions;

import com.team271.lib.hardware.transmissions.TransmissionBase.ShifterState;

/**
 * Interface for gear shift actuators.
 * <p>
 * Separates the physical actuation mechanism (pneumatics, servo, motor, etc.)
 * from the transmission's gear state and ratio bookkeeping. Implementations
 * handle the hardware-specific details of engaging a gear.
 */
public interface Shifter {
    /**
     * Actuate the shift mechanism to engage the requested gear.
     *
     * @param argShiftTo the target gear
     */
    void actuate(ShifterState argShiftTo);
}
