package com.team271.lib.hardware;

import com.team271.lib.api.SignalRefreshable;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates per-cycle signal refresh for mixed-vendor hardware setups.
 *
 * <p>CTRE devices are bulk-refreshed by {@link CTREManager#refreshAll()}. Non-CTRE devices (e.g.,
 * WPILib sensors) implement {@link SignalRefreshable} and are refreshed individually after the CTRE
 * bulk read.
 *
 * <p>Call {@link #refreshAll()} in {@code robotPeriodicBefore()} instead of calling {@code
 * CTREManager.refreshAll()} directly. For CTRE-only setups this is functionally equivalent.
 *
 * <pre>{@code
 * // In Robot.robotPeriodic():
 * HardwareManager.refreshAll();  // replaces CTREManager.refreshAll()
 * SubsystemManager.robotPeriodicBefore();
 * ...
 * }</pre>
 */
public final class HardwareManager {

    private static final List<SignalRefreshable> sNonCTREDevices = new ArrayList<>();

    private HardwareManager() {}

    /**
     * Registers a non-CTRE device for per-cycle refresh.
     *
     * <p>CTRE devices should NOT be registered here — they are bulk-refreshed by CTREManager.
     *
     * @param argDevice the device to refresh each cycle
     */
    public static void registerNonCTRE(final SignalRefreshable argDevice) {
        sNonCTREDevices.add(argDevice);
    }

    /**
     * Refreshes all hardware signals. Call once per robot periodic cycle.
     *
     * <p>First performs the CTRE bulk signal refresh (consistent timestamps across all CTRE
     * devices), then refreshes any registered non-CTRE devices individually.
     */
    public static void refreshAll() {
        CTREManager.refreshAll();
        for (final SignalRefreshable device : sNonCTREDevices) {
            device.refresh();
        }
    }

    /** Resets all state. Call in test setup for isolation. */
    public static void resetForTesting() {
        sNonCTREDevices.clear();
        CTREManager.resetForTesting();
    }
}
