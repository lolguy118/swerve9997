package com.team271.lib.hardware;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.SignalLogger;
import com.ctre.phoenix6.hardware.ParentDevice;
import com.team271.lib.ConstantsLib;
import com.team271.lib.nt.NTEntry;
import com.team271.lib.nt.NTTable;
import com.team271.lib.util.Elastic;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Centralized manager for all CTRE Phoenix 6 devices and status signals.
 *
 * <p>Supports any number of CAN buses (RIO + multiple CANivores). Buses are registered via {@link
 * #addBus(String)} or automatically when devices are added. Each bus gets independent utilization
 * tracking and telemetry.
 *
 * <p>Typical multi-CANivore setup (in Robot.robotInit(), before subsystem init):
 *
 * <pre>
 *   CTREManager.addBus("rio");
 *   CTREManager.addBus("drivetrain");
 *   CTREManager.addBus("subsystems");
 * </pre>
 *
 * <p>Lifecycle:
 *
 * <ol>
 *   <li>Register buses (optional — auto-registered when devices are added)
 *   <li>Subsystems create devices and register signals during {@code robotInit()}
 *   <li>Call {@code CTREManager.init()} after all subsystems are initialized
 *   <li>Call {@code CTREManager.refreshAll()} once per cycle in {@code robotPeriodicBefore()}
 * </ol>
 */
public class CTREManager {
    /*
     * CAN Buses — keyed by bus name, preserving insertion order
     */
    private static final LinkedHashMap<String, CANBus> buses = new LinkedHashMap<>();

    /*
     * Per-bus device lists — for per-bus optimization
     */
    private static final LinkedHashMap<String, ArrayList<ParentDevice>> devicesByBus =
            new LinkedHashMap<>();

    /*
     * All devices (for backwards compatibility)
     */
    private static final ArrayList<ParentDevice> devices = new ArrayList<>(20);

    /*
     * Signals
     */
    private static final ArrayList<StatusSignal<?>> signalsAll = new ArrayList<>(20);
    private static StatusSignal<?> @Nullable [] signalsAllArray;
    private static boolean initialized = false;

    /*
     * Timestamps
     */
    @Nullable private static AllTimestamps prevRefreshTime = null;
    @Nullable private static AllTimestamps lastRefreshTime = null;

    /*
     * Telemetry (NT)
     */
    private static final NTTable table = new NTTable("CTREManager");
    private static final NTEntry ntSignalCount = new NTEntry(table, "Signal Count", 0);
    private static final NTEntry ntDeviceCount = new NTEntry(table, "Device Count", 0);
    private static final NTEntry ntBusCount = new NTEntry(table, "Bus Count", 0);
    private static final NTEntry ntDt = new NTEntry(table, "dt", 0.0);
    private static final NTEntry ntRefreshStatus = new NTEntry(table, "Refresh Status", "");

    private static double lastErrorNotificationTime = 0;

    // Pre-allocated notification to avoid GC pressure in refreshAll()
    private static final Elastic.Notification refreshErrorNotification =
            new Elastic.Notification(Elastic.NotificationLevel.ERROR, "CAN Refresh Error", "");

    /** Hoot log path — default is RoboRIO USB drive. Set before calling {@link #init()}. */
    private static String hootLogPath = "/U/logs";

    /** Whether to start CTRE SignalLogger (Hoot). Set before calling {@link #init()}. */
    private static boolean hootLoggingEnabled = true;

    private static boolean hootLoggingStarted = false;

    private CTREManager() {}

    /**
     * Resets all internal state for test isolation. Call in {@code @BeforeEach} to ensure tests
     * start with a clean CTREManager. Replaces the reflection-based cleanup pattern.
     */
    public static void resetForTesting() {
        /*
         * Close every tracked device so the Phoenix 6 sim unregisters its native
         * state (sim motor/rotor/limit handles). Leaking these caused SIGSEGVs
         * in libCTRE_SimProTalonFX when a subsequent test triggered a bus-wide
         * iteration that dereferenced freed sim handles.
         */
        for (ParentDevice device : devices) {
            if (device instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                    /* Test-only cleanup — continue closing the rest. */
                }
            }
        }

        buses.clear();
        devicesByBus.clear();
        devices.clear();
        signalsAll.clear();
        signalsAllArray = null;
        prevRefreshTime = null;
        lastRefreshTime = null;
        lastErrorNotificationTime = 0;
        initialized = false;
        hootLoggingStarted = false;
    }

    /** Sets the hoot log file path. Must be called before {@link #init()}. */
    public static void setHootLogPath(final String argPath) {
        hootLogPath = argPath;
    }

    /** Enable or disable CTRE Hoot logging. Must be called before {@link #init()}. */
    public static void setHootLoggingEnabled(final boolean argEnabled) {
        hootLoggingEnabled = argEnabled;
    }

    /**
     * True if {@link #init()} started the SignalLogger. Used to guard {@link #stopHootLogging()}.
     */
    public static boolean isHootLoggingStarted() {
        return hootLoggingStarted;
    }

    /** Stop CTRE Hoot logging if it was started. Safe to call unconditionally. */
    public static void stopHootLogging() {
        if (hootLoggingStarted) {
            SignalLogger.stop();
            hootLoggingStarted = false;
        }
    }

    /*
     * Bus Registration
     */

    /**
     * Register a CAN bus for tracking. Idempotent — calling with the same name multiple times is
     * safe.
     *
     * @param argBusName bus name ("rio", "", or a CANivore name like "drivetrain")
     * @return the CANBus tracking object
     */
    public static CANBus addBus(final String argBusName) {
        return buses.computeIfAbsent(argBusName, CANBus::new);
    }

    /** Register a CAN bus with hoot file logging. */
    public static CANBus addBus(final String argBusName, final String argHootFile) {
        return buses.computeIfAbsent(argBusName, name -> new CANBus(name, argHootFile));
    }

    /** Get a registered bus by name, or null if not registered. */
    @Nullable
    public static CANBus getBus(final String argBusName) {
        return buses.get(argBusName);
    }

    /** Get all registered buses. */
    public static Map<String, CANBus> getAllBuses() {
        return buses;
    }

    /*
     * Devices
     */

    /**
     * Register a CTRE device. The device's bus is automatically registered if not already tracked.
     */
    public static void addDevice(final ParentDevice argDevice) {
        devices.add(argDevice);

        /* Track device by bus for per-bus optimization */
        String busName = argDevice.getNetwork().getName();
        devicesByBus.computeIfAbsent(busName, k -> new ArrayList<>()).add(argDevice);

        /* Auto-register the bus */
        addBus(busName);
    }

    /*
     * Signals
     */
    public static void addSignal(final StatusSignal<?> argSignal) {
        signalsAll.add(argSignal);
    }

    /**
     * Register a status signal with a specified update frequency. Works for any CTRE device type.
     * Warns if called after {@link #init()}.
     */
    public static void addSignal(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if (initialized) {
            edu.wpi.first.wpilibj.DriverStation.reportWarning(
                    "CTREManager: signal registered after init() — it will not be included"
                            + " in the bulk refresh array. Call addSignal() before init().",
                    false);
        }
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);
            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    /*
     * Init — call after all subsystems have registered their devices and signals
     */
    public static void init() {
        initialized = true;
        signalsAllArray = signalsAll.toArray(new StatusSignal<?>[0]);

        /* Optimize bus utilization per-bus for correct frame scheduling */
        for (var entry : devicesByBus.entrySet()) {
            ParentDevice[] busDevices = entry.getValue().toArray(new ParentDevice[0]);
            if (busDevices.length > 0) {
                ParentDevice.optimizeBusUtilizationForAll(busDevices);
            }
        }

        /* Start CTRE hoot logging if a CANivore bus exists (not supported on RIO) and the flag is set */
        boolean hasCANivore = buses.values().stream().anyMatch(CANBus::isCANivore);
        if (hasCANivore && hootLoggingEnabled) {
            SignalLogger.setPath(hootLogPath);
            SignalLogger.enableAutoLogging(true);
            SignalLogger.start();
            hootLoggingStarted = true;
        }
    }

    /** Stop CTRE hoot file logging. */
    public static void stopLogging() {
        boolean hasCANivore = buses.values().stream().anyMatch(CANBus::isCANivore);
        if (hasCANivore) {
            SignalLogger.stop();
        }
    }

    /*
     * Timestamps
     */
    public static double getLastRefreshTime() {
        if (lastRefreshTime == null) {
            return 0;
        }
        return lastRefreshTime.getBestTimestamp().getTime();
    }

    public static double getDt() {
        if (lastRefreshTime == null || prevRefreshTime == null) {
            return 0;
        }

        return lastRefreshTime.getBestTimestamp().getTime()
                - prevRefreshTime.getBestTimestamp().getTime();
    }

    /*
     * Refresh — call once per cycle in robotPeriodicBefore()
     */
    public static StatusCode refreshAll() {
        StatusCode tmpReturn = StatusCode.OK;

        if (signalsAllArray == null) {
            edu.wpi.first.wpilibj.DriverStation.reportWarning(
                    "CTREManager.refreshAll() called before init() — no signals to refresh", false);
            return StatusCode.StatusCodeNotInitialized;
        }

        if (signalsAllArray.length > 0) {
            tmpReturn = BaseStatusSignal.refreshAll(signalsAllArray);

            if (tmpReturn != StatusCode.OK) {
                double now = edu.wpi.first.wpilibj.Timer.getFPGATimestamp();
                if (now - lastErrorNotificationTime > 2.0) {
                    lastErrorNotificationTime = now;
                    refreshErrorNotification.setDescription(
                            "CTREManager.refreshAll() returned " + tmpReturn.getName());
                    Elastic.sendNotification(refreshErrorNotification);
                }
            }

            if (lastRefreshTime == null) {
                lastRefreshTime = signalsAllArray[0].getAllTimestamps();
                prevRefreshTime = lastRefreshTime;
            } else {
                prevRefreshTime = lastRefreshTime;
                lastRefreshTime = signalsAllArray[0].getAllTimestamps();
            }
        }

        return tmpReturn;
    }

    /*
     * Telemetry
     */
    public static void outputTelemetry() {
        // dt and refresh status matter every cycle for CAN health; counts are static and gated.
        ntDt.publish(getDt());
        ntRefreshStatus.publish(
                signalsAllArray != null && signalsAllArray.length > 0 ? "OK" : "No Signals");

        if (ConstantsLib.isDebugTelemetryEnabled()) {
            ntSignalCount.publish(signalsAllArray != null ? signalsAllArray.length : 0);
            ntDeviceCount.publish(devices.size());
            ntBusCount.publish(buses.size());
        }

        for (CANBus bus : buses.values()) {
            bus.refresh();
            bus.outputTelemetry();
        }
    }
}
