package com.team271.lib.hardware;

import com.ctre.phoenix6.StatusSignal;
import com.team271.lib.TObj;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.nt.NTTable;
import com.team271.lib.util.Alert;
import com.team271.lib.util.Alert.AlertType;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitors CTRE sticky fault signals for a device. Creates an Alert for each fault and publishes
 * fault state to NetworkTables.
 *
 * <p>Usage: create in the device's constructor, call {@link #registerSignals} in robotInit, call
 * {@link #refresh} in robotPeriodicBefore, and call {@link #outputTelemetry} in outputTelemetry.
 */
public class FaultMonitor {
    private final String deviceName;
    private final NTTable faultTable;
    private final List<FaultEntry> faults = new ArrayList<>();
    private final NTEntry ntHasAnyFault;

    public FaultMonitor(final TObj argParent, final String argDeviceName) {
        this.deviceName = argDeviceName;
        this.faultTable = argParent.getTable();
        this.ntHasAnyFault = new NTEntry(faultTable, "Has Fault", false);
        // Each instance gets a unique phase offset so fault checks are spread across cycles
        this.phaseOffset = nextPhaseOffset++ % REFRESH_INTERVAL;
        this.refreshCounter = phaseOffset;
    }

    /**
     * Add a fault signal to monitor.
     *
     * @param argName short name for the fault (e.g., "BootDuringEnable", "DeviceTemp")
     * @param argSignal the CTRE sticky fault StatusSignal
     * @param argUpdateFreqHz signal refresh frequency
     */
    public void addFault(
            final String argName,
            final StatusSignal<Boolean> argSignal,
            final double argUpdateFreqHz) {
        faults.add(new FaultEntry(argName, argSignal, argUpdateFreqHz));
    }

    /** Register all fault signals with CTREManager. Call in robotInit(). */
    public void registerSignals() {
        for (FaultEntry f : faults) {
            CTREManager.addSignal(f.signal, f.updateFreqHz);
        }
    }

    // Rate-limit fault checks — faults don't change rapidly, no need to check every cycle.
    // Each FaultMonitor instance gets a unique phase offset so checks are spread evenly
    // across cycles. With 15 motors and interval 10, at most 2 monitors fire per cycle.
    private static final int REFRESH_INTERVAL = 10;
    private static int nextPhaseOffset = 0;
    private final int phaseOffset;
    private int refreshCounter;

    /** Check all fault signals and update alerts. Call in robotPeriodicBefore(). */
    public void refresh() {
        if (++refreshCounter < REFRESH_INTERVAL) {
            return;
        }
        refreshCounter = 0;
        for (FaultEntry f : faults) {
            if (f.signal != null && f.signal.getStatus().isOK()) {
                boolean faulted = f.signal.getValue();
                f.alert.set(faulted);
                f.isActive = faulted;
            }
        }
    }

    /** Returns true if any monitored fault is currently active. */
    public boolean hasAnyFault() {
        for (FaultEntry f : faults) {
            if (f.isActive) {
                return true;
            }
        }
        return false;
    }

    /** Publish fault states to NetworkTables. Call in outputTelemetry(). */
    public void outputTelemetry() {
        ntHasAnyFault.publish(hasAnyFault());
        for (FaultEntry f : faults) {
            f.ntEntry.publish(f.isActive);
        }
    }

    private class FaultEntry {
        final StatusSignal<Boolean> signal;
        final double updateFreqHz;
        final Alert alert;
        final NTEntry ntEntry;
        boolean isActive = false;

        FaultEntry(
                final String argName,
                final StatusSignal<Boolean> argSignal,
                final double argUpdateFreqHz) {
            this.signal = argSignal;
            this.updateFreqHz = argUpdateFreqHz;
            this.alert = new Alert("Faults", deviceName + ": " + argName, AlertType.WARNING);
            this.ntEntry = new NTEntry(faultTable, "Fault " + argName, false);
        }
    }
}
