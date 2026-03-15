package com.team271.lib.hardware;

import com.ctre.phoenix6.*;
import com.ctre.phoenix6.hardware.ParentDevice;
import com.team271.lib.ConstantsLib;
import java.util.ArrayList;

public class CTREManager {
    /*
     * Singleton
     */
    protected static CTREManager mInstance;

    public static CTREManager getInstance() {
        if (mInstance == null) {
            mInstance = new CTREManager();
        }
        return mInstance;
    }

    // create a CAN bus for the CANivore named drivetrain
    public CANBus canbusDrive = new CANBus(ConstantsLib.CAN_BUS_NAME_DRIVE);
    public CANBus canbusSubsystems = new CANBus(ConstantsLib.CAN_BUS_NAME_SUBSYSTEMS);

    private static final ArrayList<ParentDevice> devices = new ArrayList<>(20);
    private static ParentDevice[] devicesAllArray;

    private static final ArrayList<StatusSignal<?>> signalsAll = new ArrayList<>(20);
    private static final ArrayList<StatusSignal<?>> signalsTalonFX = new ArrayList<>(20);
    private static final ArrayList<StatusSignal<?>> signalsCANCoder = new ArrayList<>(20);
    private static final ArrayList<StatusSignal<?>> signalsPigeon = new ArrayList<>(1);
    private static final ArrayList<StatusSignal<?>> signalsCANrange = new ArrayList<>(20);
    private static final ArrayList<StatusSignal<?>> signalsCANdi = new ArrayList<>(20);

    private static StatusSignal<?>[] signalsAllArray;
    // private StatusSignal<?>[] signalsTalonFXArray;
    // private StatusSignal<?>[] signalsCANCoderArray;
    // private StatusSignal<?>[] signalsPigeonArray;

    private static AllTimestamps prevRefreshTime = null;
    private static AllTimestamps lastRefreshTime = null;

    private CTREManager() {
    }

    public static void addDevice(final ParentDevice argDevice) {
        devices.add(argDevice);
    }

    public static void addSignal(final StatusSignal<?> argSignal) {
        signalsAll.add(argSignal);
    }

    public static void addSignalTalonFX(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);

            signalsTalonFX.add(argSignal);

            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    public static StatusSignal<?> addSignalCANCoder(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);

            signalsCANCoder.add(argSignal);

            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }

        return argSignal;
    }

    public static void addSignalPigeon(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);

            signalsPigeon.add(argSignal);

            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    public static void addSignalCANrange(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);

            signalsCANrange.add(argSignal);

            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    public static void addSignalCANdi(final StatusSignal<?> argSignal, final double argUpdateRate) {
        if ((argSignal != null) && argSignal.getStatus().isOK()) {
            addSignal(argSignal);

            signalsCANdi.add(argSignal);

            argSignal.setUpdateFrequency(argUpdateRate, ConstantsLib.CAN_LONG_TIMEOUT_MS);
        }
    }

    public static void init() {
        signalsAllArray = signalsAll.toArray(new StatusSignal<?>[0]);
        // signalsTalonFXArray = signalsTalonFX.toArray(new StatusSignal<?>[0]);
        // signalsCANCoderArray = signalsCANCoder.toArray(new StatusSignal<?>[0]);
        // signalsPigeonArray = signalsPigeon.toArray(new StatusSignal<?>[0]);

        devicesAllArray = devices.toArray(new ParentDevice[0]);

        /* Disable all status frames we don't need */
        ParentDevice.optimizeBusUtilizationForAll(devicesAllArray);
    }

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

    public static StatusCode refreshAll() {
        StatusCode tmpReturn = StatusCode.OK;

        if (signalsAllArray == null) {
            edu.wpi.first.wpilibj.DriverStation.reportWarning(
                    "CTREManager.refreshAll() called before init() — no signals to refresh", false);
            return tmpReturn;
        }

        if (signalsAllArray.length > 0) {
            tmpReturn = BaseStatusSignal.refreshAll(signalsAllArray);

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
}
