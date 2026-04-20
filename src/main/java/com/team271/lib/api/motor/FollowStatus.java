package com.team271.lib.api.motor;

/**
 * Status returned by motor follow operations.
 *
 * <p>Replaces {@code ControllerBase.ControllerStatus} with a vendor-neutral type.
 */
public enum FollowStatus {
    OK,
    ERROR,
    ERROR_INVALID_BUS,
    UNKNOWN
}
