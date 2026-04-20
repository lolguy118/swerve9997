package com.team271.lib.api.sensor;

import com.team271.lib.Lifecycle;
import com.team271.lib.Named;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;

/**
 * Vendor-neutral limit switch interface.
 *
 * <p>Reports whether the switch is triggered and supports enable/disable and trigger type
 * configuration (normally open or normally closed).
 */
public interface LimitSwitch extends Lifecycle, Named {

    /** Returns true if the switch is currently triggered. */
    boolean isTriggered();

    /**
     * Enables or disables this limit switch.
     *
     * @param argEnabled true to enable
     */
    void setEnabled(boolean argEnabled);

    /**
     * Sets the trigger type (normally open or normally closed).
     *
     * @param argType the trigger type
     */
    void setTriggerType(SwitchTrigger argType);
}
