package com.team271.lib.api.motor;

/**
 * Vendor-neutral neutral mode for motor controllers.
 *
 * <p>Replaces {@code ControllerBase.NeutralState} with a type that has no vendor dependencies.
 */
public enum NeutralMode {
    BRAKE,
    COAST
}
