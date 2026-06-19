package com.team271.lib.hardware.transmissions;

import com.team271.lib.hardware.transmissions.TransmissionBase.ShifterState;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

/** Pneumatic gear shifter using a REV Pneumatics Hub DoubleSolenoid. */
public class ShifterPneumatic implements Shifter {
    private final DoubleSolenoid solenoid;

    /**
     * @param argPhCanId CAN ID of the Pneumatics Hub
     * @param argChGear1 solenoid channel for gear 1 (forward)
     * @param argChGear2 solenoid channel for gear 2 (reverse)
     */
    public ShifterPneumatic(final int argPhCanId, final int argChGear1, final int argChGear2) {
        solenoid =
                new DoubleSolenoid(argPhCanId, PneumaticsModuleType.REVPH, argChGear1, argChGear2);
    }

    @Override
    public void actuate(final ShifterState argShiftTo) {
        if (argShiftTo == ShifterState.GEAR_1) {
            solenoid.set(Value.kForward);
        } else if (argShiftTo == ShifterState.GEAR_2) {
            solenoid.set(Value.kReverse);
        }
    }
}
