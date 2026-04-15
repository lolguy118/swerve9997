package com.team271.lib.hardware.sensors.switches;

import com.team271.lib.TObj;
import edu.wpi.first.wpilibj.DigitalInput;

/**
 * Limit switch connected to a RoboRIO DIO port.
 *
 * <p>Wraps WPILib {@link DigitalInput} for standard limit switches wired to the RoboRIO's digital
 * I/O pins. Supports normally-open (NO) and normally-closed (NC) configurations via {@link
 * SwitchTrigger}.
 *
 * <p>Unlike {@link SwitchFX} (which reads limit switches through the TalonFX), this class reads
 * directly from the RoboRIO — useful for limit switches not connected to a motor controller.
 */
public class SwitchDIO extends SwitchBase {
    private DigitalInput digitalInput;
    private final int dioChannel;
    private boolean enabled = true;
    private SwitchTrigger triggerType = SwitchTrigger.NO;

    public SwitchDIO(final TObj argParent, final String argName, final int argDIOChannel) {
        super(argParent, "(DIO)" + argName, SwitchType.RIO_DIO);

        dioChannel = argDIOChannel;

        create();
    }

    @Override
    protected void create() {
        digitalInput = new DigitalInput(dioChannel);
    }

    /** Passthrough — returns the underlying WPILib DigitalInput. */
    public DigitalInput getDigitalInput() {
        return digitalInput;
    }

    @Override
    public void setEnabled(final boolean argEnabled) {
        enabled = argEnabled;
    }

    @Override
    public void setTriggerType(final SwitchTrigger argTriggerType) {
        triggerType = argTriggerType;
    }

    @Override
    public boolean getTriggered() {
        if (!enabled || digitalInput == null) {
            return false;
        }

        boolean raw = digitalInput.get();

        // NO (normally open): triggered when input is LOW (false)
        // NC (normally closed): triggered when input is HIGH (true)
        if (triggerType == SwitchTrigger.NC) {
            return raw;
        }
        return !raw;
    }
}
