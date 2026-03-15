package com.team271.lib.hardware.transmissions;

import com.team271.lib.ConstantsLib;
import com.team271.lib.hardware.transmissions.TransmissionBase.ShifterState;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

/**
 * Pneumatic gear shifter using a REV Pneumatics Hub DoubleSolenoid.
 */
public class ShifterPneumatic implements Shifter {
    private final DoubleSolenoid solenoid;

    /**
     * @param chGear1 solenoid channel for gear 1 (forward)
     * @param chGear2 solenoid channel for gear 2 (reverse)
     */
    public ShifterPneumatic(final int chGear1, final int chGear2) {
        solenoid = new DoubleSolenoid(ConstantsLib.CAN_ID_PH, PneumaticsModuleType.REVPH, chGear1, chGear2);
    }

    /**
     * @param phCanId CAN ID of the Pneumatics Hub
     * @param chGear1 solenoid channel for gear 1 (forward)
     * @param chGear2 solenoid channel for gear 2 (reverse)
     */
    public ShifterPneumatic(final int phCanId, final int chGear1, final int chGear2) {
        solenoid = new DoubleSolenoid(phCanId, PneumaticsModuleType.REVPH, chGear1, chGear2);
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
