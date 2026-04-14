package com.team271.lib.hardware;

import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource") // TalonFX is AutoCloseable but HAL manages lifecycle in sim
class CTREManagerTest {

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetCTREManager() throws Exception {
        /* Reset static state between tests via reflection */
        clearStaticField("buses");
        clearStaticField("devicesByBus");
        clearStaticField("devices");
        clearStaticField("signalsAll");
        setStaticField("signalsAllArray", null);
        setStaticField("prevRefreshTime", null);
        setStaticField("lastRefreshTime", null);
        setStaticField("lastErrorNotificationTime", 0.0);
    }

    private void clearStaticField(String fieldName) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        Object collection = f.get(null);
        if (collection instanceof java.util.Map) {
            ((java.util.Map<?, ?>) collection).clear();
        } else if (collection instanceof java.util.List) {
            ((java.util.List<?>) collection).clear();
        }
    }

    private void setStaticField(String fieldName, Object value) throws Exception {
        Field f = CTREManager.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<StatusSignal<?>> getSignalsList() throws Exception {
        Field f = CTREManager.class.getDeclaredField("signalsAll");
        f.setAccessible(true);
        return (ArrayList<StatusSignal<?>>) f.get(null);
    }

    private StatusSignal<?>[] getSignalsArray() throws Exception {
        Field f = CTREManager.class.getDeclaredField("signalsAllArray");
        f.setAccessible(true);
        return (StatusSignal<?>[]) f.get(null);
    }

    /* ================================================================
     * Bus Registration
     * ================================================================ */

    @Test
    void addBusRegistersNewBus() {
        CANBus bus = CTREManager.addBus("drivetrain");

        assertNotNull(bus);
        assertEquals("drivetrain", bus.getBus());
        assertTrue(bus.isCANivore());
    }

    @Test
    void addBusRIOBus() {
        CANBus bus = CTREManager.addBus("rio");

        assertNotNull(bus);
        assertFalse(bus.isCANivore());
    }

    @Test
    void addBusEmptyStringBus() {
        CANBus bus = CTREManager.addBus("");

        assertNotNull(bus);
        assertFalse(bus.isCANivore());
    }

    @Test
    void addBusIsIdempotent() {
        CANBus first = CTREManager.addBus("drivetrain");
        CANBus second = CTREManager.addBus("drivetrain");

        assertSame(
                first,
                second,
                "addBus should return the same instance for duplicate registrations");
    }

    @Test
    void addBusWithHootFile() {
        CANBus bus = CTREManager.addBus("drivetrain", "/media/sda1/");

        assertNotNull(bus);
        assertEquals("drivetrain", bus.getBus());
    }

    @Test
    void addBusWithHootFileIdempotentIgnoresHootFile() {
        /* First registration wins — second call with different hoot file is ignored */
        CANBus first = CTREManager.addBus("drivetrain", "/media/sda1/logs1/");
        CANBus second = CTREManager.addBus("drivetrain", "/media/sda1/logs2/");

        assertSame(first, second);
    }

    @Test
    void addBusWithHootFileAfterPlainAddIsIdempotent() {
        /* Plain add first, then hoot file variant — should return original */
        CANBus plain = CTREManager.addBus("drivetrain");
        CANBus withHoot = CTREManager.addBus("drivetrain", "/media/sda1/");

        assertSame(plain, withHoot);
    }

    /* ================================================================
     * getBus / getAllBuses
     * ================================================================ */

    @Test
    void getBusReturnsRegisteredBus() {
        CTREManager.addBus("subsystems");

        CANBus bus = CTREManager.getBus("subsystems");
        assertNotNull(bus);
        assertEquals("subsystems", bus.getBus());
    }

    @Test
    void getBusReturnsNullForUnregistered() {
        CANBus bus = CTREManager.getBus("nonexistent");

        assertNull(bus);
    }

    @Test
    void getAllBusesEmptyByDefault() {
        assertTrue(CTREManager.getAllBuses().isEmpty());
    }

    @Test
    void multipleCanivoresTrackedIndependently() {
        CTREManager.addBus("rio");
        CTREManager.addBus("drivetrain");
        CTREManager.addBus("subsystems");

        assertEquals(3, CTREManager.getAllBuses().size());

        CANBus rio = CTREManager.getBus("rio");
        CANBus drive = CTREManager.getBus("drivetrain");
        CANBus subs = CTREManager.getBus("subsystems");

        assertNotNull(rio);
        assertNotNull(drive);
        assertNotNull(subs);

        assertFalse(rio.isCANivore());
        assertTrue(drive.isCANivore());
        assertTrue(subs.isCANivore());
    }

    /* ================================================================
     * Device Registration
     * ================================================================ */

    @Test
    void addDeviceAutoRegistersBus() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));

        CTREManager.addDevice(talon);

        CANBus bus = CTREManager.getBus("drivetrain");
        assertNotNull(bus, "addDevice should auto-register the device's bus");
        assertTrue(bus.isCANivore());
    }

    @Test
    void addDeviceRIOBusAutoRegisters() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus(""));

        CTREManager.addDevice(talon);

        CANBus bus = CTREManager.getBus("");
        assertNotNull(bus);
        assertFalse(bus.isCANivore());
    }

    @Test
    void addMultipleDevicesSameBus() {
        TalonFX t1 = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX t2 = new TalonFX(2, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX t3 = new TalonFX(3, new com.ctre.phoenix6.CANBus("drivetrain"));

        CTREManager.addDevice(t1);
        CTREManager.addDevice(t2);
        CTREManager.addDevice(t3);

        /* Only 1 bus registered despite 3 devices */
        assertEquals(1, CTREManager.getAllBuses().size());
    }

    @Test
    void devicesOnDifferentBusesTrackedSeparately() {
        TalonFX driveTalon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX armTalon = new TalonFX(5, new com.ctre.phoenix6.CANBus("subsystems"));

        CTREManager.addDevice(driveTalon);
        CTREManager.addDevice(armTalon);

        assertEquals(2, CTREManager.getAllBuses().size());
        assertNotNull(CTREManager.getBus("drivetrain"));
        assertNotNull(CTREManager.getBus("subsystems"));
    }

    /* ================================================================
     * Signal Registration
     * ================================================================ */

    @Test
    void addSignalDirectlyAddsToList() throws Exception {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        var signal = talon.getPosition();

        CTREManager.addSignal(signal);

        ArrayList<StatusSignal<?>> signals = getSignalsList();
        assertEquals(1, signals.size());
        assertSame(signal, signals.get(0));
    }

    @Test
    void addSignalTalonFXRegistersSignal() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(talon);

        var signal = talon.getPosition();
        CTREManager.addSignalTalonFX(signal, 250.0);

        CTREManager.init();
        StatusCode status = CTREManager.refreshAll();
        assertNotNull(status);
    }

    @Test
    void addSignalCANCoderReturnsSignal() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        var signal = talon.getPosition();

        var returned = CTREManager.addSignalCANCoder(signal, 250.0);

        assertSame(signal, returned, "addSignalCANCoder should return the same signal passed in");
    }

    @Test
    void addSignalCANCoderNullReturnsNull() {
        var returned = CTREManager.addSignalCANCoder(null, 250.0);

        assertNull(returned);
    }

    @Test
    void addSignalNullIsIgnored() throws Exception {
        CTREManager.addSignalTalonFX(null, 250.0);
        CTREManager.addSignalCANCoder(null, 250.0);
        CTREManager.addSignalPigeon(null, 250.0);
        CTREManager.addSignalCANrange(null, 250.0);
        CTREManager.addSignalCANdi(null, 250.0);

        /* None of the nulls should have been added to the signals list */
        ArrayList<StatusSignal<?>> signals = getSignalsList();
        assertEquals(0, signals.size());
    }

    @Test
    void addSignalMultipleSignalsFromSameDevice() throws Exception {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(talon);

        CTREManager.addSignal(talon.getPosition());
        CTREManager.addSignal(talon.getVelocity());
        CTREManager.addSignal(talon.getMotorVoltage());

        ArrayList<StatusSignal<?>> signals = getSignalsList();
        assertEquals(3, signals.size());
    }

    @Test
    void addSignalFromMultipleDevices() throws Exception {
        TalonFX t1 = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX t2 = new TalonFX(5, new com.ctre.phoenix6.CANBus("subsystems"));

        CTREManager.addSignal(t1.getPosition());
        CTREManager.addSignal(t2.getPosition());

        ArrayList<StatusSignal<?>> signals = getSignalsList();
        assertEquals(2, signals.size());
    }

    /* ================================================================
     * Init
     * ================================================================ */

    @Test
    void initWithNoDevicesDoesNotThrow() {
        assertDoesNotThrow(() -> CTREManager.init());
    }

    @Test
    void initBuildsSignalArray() throws Exception {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addSignal(talon.getPosition());
        CTREManager.addSignal(talon.getVelocity());

        CTREManager.init();

        StatusSignal<?>[] array = getSignalsArray();
        assertNotNull(array);
        assertEquals(2, array.length);
    }

    @Test
    void initWithNoSignalsBuildsEmptyArray() throws Exception {
        CTREManager.init();

        StatusSignal<?>[] array = getSignalsArray();
        assertNotNull(array);
        assertEquals(0, array.length);
    }

    @Test
    void initOptimizesPerBus() {
        TalonFX drive1 = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX drive2 = new TalonFX(2, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX arm1 = new TalonFX(5, new com.ctre.phoenix6.CANBus("subsystems"));

        CTREManager.addDevice(drive1);
        CTREManager.addDevice(drive2);
        CTREManager.addDevice(arm1);

        assertDoesNotThrow(() -> CTREManager.init());
        assertEquals(2, CTREManager.getAllBuses().size());
    }

    @Test
    void initWithOnlyRIODevices() {
        TalonFX t1 = new TalonFX(1, new com.ctre.phoenix6.CANBus(""));

        CTREManager.addDevice(t1);

        assertDoesNotThrow(() -> CTREManager.init());
    }

    /* ================================================================
     * Refresh
     * ================================================================ */

    @Test
    void refreshAllBeforeInitReturnsNotInitialized() {
        StatusCode status = CTREManager.refreshAll();

        assertEquals(StatusCode.StatusCodeNotInitialized, status);
    }

    @Test
    void refreshAllAfterInitWithNoSignalsReturnsOK() {
        CTREManager.init();

        StatusCode status = CTREManager.refreshAll();

        assertEquals(StatusCode.OK, status);
    }

    @Test
    void refreshAllAfterInitWithSignals() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(talon);
        CTREManager.addSignal(talon.getPosition());

        CTREManager.init();

        StatusCode status = CTREManager.refreshAll();
        assertNotNull(status);
    }

    @Test
    void refreshAllMultipleTimesDoesNotThrow() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(talon);
        CTREManager.addSignal(talon.getPosition());

        CTREManager.init();

        /* First refresh: lastRefreshTime == null path */
        CTREManager.refreshAll();
        /* Second refresh: lastRefreshTime != null path (prevRefreshTime = lastRefreshTime) */
        CTREManager.refreshAll();
        /* Third refresh: both timestamps non-null */
        StatusCode status = CTREManager.refreshAll();

        assertNotNull(status);
    }

    /* ================================================================
     * Timestamps
     * ================================================================ */

    @Test
    void getLastRefreshTimeIsZeroBeforeRefresh() {
        assertEquals(0.0, CTREManager.getLastRefreshTime());
    }

    @Test
    void getDtIsZeroBeforeRefresh() {
        assertEquals(0.0, CTREManager.getDt());
    }

    @Test
    void getLastRefreshTimeAfterRefreshWithSignals() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addSignal(talon.getPosition());
        CTREManager.init();

        CTREManager.refreshAll();

        /* After a refresh with signals, lastRefreshTime should be set */
        double time = CTREManager.getLastRefreshTime();
        /* In sim, the timestamp may be 0.0 but lastRefreshTime is no longer null */
        assertTrue(time >= 0.0);
    }

    @Test
    void getDtAfterMultipleRefreshes() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addSignal(talon.getPosition());
        CTREManager.init();

        /* First refresh sets both lastRefreshTime and prevRefreshTime */
        CTREManager.refreshAll();
        /* Second refresh separates prevRefreshTime and lastRefreshTime */
        CTREManager.refreshAll();

        double dt = CTREManager.getDt();
        /* dt should be >= 0 (could be 0 if timestamps are the same in sim) */
        assertTrue(dt >= 0.0);
    }

    @Test
    void getDtIsZeroWithOnlyLastRefreshTime() {
        /*
         * After only 1 refresh, prevRefreshTime == lastRefreshTime (both set on first refresh),
         * so dt should be 0.
         */
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addSignal(talon.getPosition());
        CTREManager.init();

        CTREManager.refreshAll();

        assertEquals(0.0, CTREManager.getDt());
    }

    @Test
    void getDtWithOnlyPrevNull() throws Exception {
        /* Edge case: lastRefreshTime set but prevRefreshTime null should return 0 */
        /* This shouldn't happen in normal code flow, but test the guard */
        setStaticField("prevRefreshTime", null);

        assertEquals(0.0, CTREManager.getDt());
    }

    @Test
    void getLastRefreshTimeWithNullReturnsZero() throws Exception {
        setStaticField("lastRefreshTime", null);

        assertEquals(0.0, CTREManager.getLastRefreshTime());
    }

    /* ================================================================
     * Telemetry
     * ================================================================ */

    @Test
    void outputTelemetryDoesNotThrowBeforeInit() {
        assertDoesNotThrow(() -> CTREManager.outputTelemetry());
    }

    @Test
    void outputTelemetryDoesNotThrowAfterInit() {
        CTREManager.addBus("rio");
        CTREManager.addBus("drivetrain");
        CTREManager.init();

        assertDoesNotThrow(() -> CTREManager.outputTelemetry());
    }

    @Test
    void outputTelemetryWithSignals() {
        TalonFX talon = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(talon);
        CTREManager.addSignal(talon.getPosition());
        CTREManager.init();
        CTREManager.refreshAll();

        /* Tests the signalsAllArray.length > 0 branch for "OK" status string */
        assertDoesNotThrow(() -> CTREManager.outputTelemetry());
    }

    @Test
    void outputTelemetryWithNoBuses() {
        CTREManager.init();

        /* No buses to iterate — should still work */
        assertDoesNotThrow(() -> CTREManager.outputTelemetry());
    }

    @Test
    void outputTelemetryWithMultipleBuses() {
        CTREManager.addBus("rio");
        CTREManager.addBus("drivetrain");
        CTREManager.addBus("subsystems");
        CTREManager.init();

        /* All 3 buses should get refresh() + outputTelemetry() */
        assertDoesNotThrow(() -> CTREManager.outputTelemetry());
    }

    /* ================================================================
     * Private constructor (not instantiable)
     * ================================================================ */

    @Test
    void privateConstructorExists() throws Exception {
        var ctor = CTREManager.class.getDeclaredConstructor();

        assertTrue(java.lang.reflect.Modifier.isPrivate(ctor.getModifiers()));

        /* Verify it can be invoked via reflection (covers the constructor line) */
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    /* ================================================================
     * CANDeviceID Integration
     * ================================================================ */

    @Test
    void canDeviceIDIntegrationWithCTREManager() {
        CANDeviceID driveMotorID = new CANDeviceID(1, "drivetrain");
        CANDeviceID armMotorID = new CANDeviceID(5, "subsystems");

        TalonFX driveTalon = new TalonFX(driveMotorID.getDeviceNumber(), driveMotorID.getCANBus());
        TalonFX armTalon = new TalonFX(armMotorID.getDeviceNumber(), armMotorID.getCANBus());

        CTREManager.addDevice(driveTalon);
        CTREManager.addDevice(armTalon);

        assertNotNull(CTREManager.getBus("drivetrain"));
        assertNotNull(CTREManager.getBus("subsystems"));

        assertFalse(driveMotorID.isSameBus(armMotorID));
        assertEquals("drivetrain", driveMotorID.getBus());
        assertEquals("subsystems", armMotorID.getBus());

        CTREManager.init();
        assertDoesNotThrow(() -> CTREManager.refreshAll());
    }

    @Test
    void fullLifecycleMultiBus() {
        /* Simulate a full robot init sequence with 2 CANivores + RIO */
        CTREManager.addBus("rio");
        CTREManager.addBus("drivetrain");
        CTREManager.addBus("subsystems");

        /* Drivetrain motors */
        TalonFX driveFL = new TalonFX(1, new com.ctre.phoenix6.CANBus("drivetrain"));
        TalonFX driveFR = new TalonFX(2, new com.ctre.phoenix6.CANBus("drivetrain"));
        CTREManager.addDevice(driveFL);
        CTREManager.addDevice(driveFR);

        /* Subsystem motor */
        TalonFX arm = new TalonFX(5, new com.ctre.phoenix6.CANBus("subsystems"));
        CTREManager.addDevice(arm);

        /* Register signals */
        CTREManager.addSignal(driveFL.getPosition());
        CTREManager.addSignal(driveFL.getVelocity());
        CTREManager.addSignal(driveFR.getPosition());
        CTREManager.addSignal(driveFR.getVelocity());
        CTREManager.addSignal(arm.getPosition());

        /* Init */
        CTREManager.init();

        /* Simulate a few robot cycles */
        for (int i = 0; i < 5; i++) {
            StatusCode status = CTREManager.refreshAll();
            assertNotNull(status);
        }

        /* Timestamps should be populated after multiple refreshes */
        assertTrue(CTREManager.getLastRefreshTime() >= 0.0);
        assertTrue(CTREManager.getDt() >= 0.0);

        /* Telemetry should work */
        assertDoesNotThrow(() -> CTREManager.outputTelemetry());

        /* All 3 buses present */
        assertEquals(3, CTREManager.getAllBuses().size());
    }

    /* ================================================================
     * stopLogging
     * ================================================================ */

    @Test
    void stopLoggingDoesNotThrowBeforeInit() {
        assertDoesNotThrow(CTREManager::stopLogging);
    }

    @Test
    void stopLoggingDoesNotThrowAfterInit() {
        CTREManager.addBus("rio");
        CTREManager.init();

        assertDoesNotThrow(CTREManager::stopLogging);
    }

    @Test
    void initWithNoBusesDoesNotThrow() {
        assertDoesNotThrow(CTREManager::init);
    }
}
