package com.team271.lib.api;

/**
 * Interface for devices that need per-cycle signal refresh.
 *
 * <p>CTRE devices implement this as a no-op because their signals are bulk-refreshed by {@code
 * CTREManager.refreshAll()}. Non-CTRE devices (e.g., WPILib sensors) implement this to read from
 * the HAL on each cycle.
 *
 * <p>Used by {@code HardwareManager} to orchestrate refresh for mixed-vendor setups.
 */
public interface SignalRefreshable {

    /** Update cached values from hardware. Called once per robot periodic cycle. */
    void refresh();
}
