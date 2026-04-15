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

    public FaultMonitor(TObj parent, String deviceName) {
        this.deviceName = deviceName;
        this.faultTable = parent.getTable();
        this.ntHasAnyFault = new NTEntry(faultTable, "Has Fault", false);
    }

    /**
     * Add a fault signal to monitor.
     *
     * @param name short name for the fault (e.g., "BootDuringEnable", "DeviceTemp")
     * @param signal the CTRE sticky fault StatusSignal
     * @param updateFreqHz signal refresh frequency
     */
    public void addFault(String name, StatusSignal<Boolean> signal, double updateFreqHz) {
        faults.add(new FaultEntry(name, signal, updateFreqHz));
    }

    /** Register all fault signals with CTREManager. Call in robotInit(). */
    public void registerSignals() {
        for (FaultEntry f : faults) {
            CTREManager.addSignal(f.signal, f.updateFreqHz);
        }
    }

    /** Check all fault signals and update alerts. Call in robotPeriodicBefore(). */
    public void refresh() {
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
        final String name;
        final StatusSignal<Boolean> signal;
        final double updateFreqHz;
        final Alert alert;
        final NTEntry ntEntry;
        boolean isActive = false;

        FaultEntry(String name, StatusSignal<Boolean> signal, double updateFreqHz) {
            this.name = name;
            this.signal = signal;
            this.updateFreqHz = updateFreqHz;
            this.alert = new Alert("Faults", deviceName + ": " + name, AlertType.WARNING);
            this.ntEntry = new NTEntry(faultTable, "Fault " + name, false);
        }
    }
}
