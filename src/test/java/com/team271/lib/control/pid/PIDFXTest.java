package com.team271.lib.control.pid;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class PIDFXTest {

    private ControllerTalonFX talonFX;
    private PIDFX pid;

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void setup() throws Exception {
        resetCTREManager();

        talonFX =
                new ControllerTalonFX(
                        null,
                        "TestMotor",
                        new CANDeviceID(50),
                        new MotorBase(MotorBase.MotorType.KRAKENX60));
    }

    private void resetCTREManager() throws Exception {
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

    /* --- Constructor --- */

    @Test
    void constructorWithFullArgs() {
        pid = new PIDFX(null, "Full", talonFX, 1.0, 0.5, 0.1, 0.05);

        assertEquals(1.0, pid.getP());
        assertEquals(0.5, pid.getI());
        assertEquals(0.1, pid.getD());
    }

    @Test
    void constructorWithPIDOnly() {
        pid = new PIDFX(null, "PID", talonFX, 2.0, 0.0, 0.5);

        assertEquals(2.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.5, pid.getD());
    }

    @Test
    void constructorMinimal() {
        pid = new PIDFX(null, "Min", talonFX);

        assertEquals(0.0, pid.getP());
        assertEquals(0.0, pid.getI());
        assertEquals(0.0, pid.getD());
    }

    /* --- setGoal --- */

    @Test
    void setGoalDoesNotThrow() {
        pid = new PIDFX(null, "Goal", talonFX, 1.0, 0.0, 0.0);
        assertDoesNotThrow(() -> pid.setGoal(5.0));
    }

    @Test
    void setGoalMultipleTimesDoesNotThrow() {
        pid = new PIDFX(null, "Multi", talonFX);

        assertDoesNotThrow(() -> pid.setGoal(0.0));
        assertDoesNotThrow(() -> pid.setGoal(10.0));
        assertDoesNotThrow(() -> pid.setGoal(-5.0));
    }

    /* --- outputTelemetry --- */

    @Test
    void outputTelemetryDoesNotThrow() {
        pid = new PIDFX(null, "Telem", talonFX, 1.0, 0.0, 0.0);
        assertDoesNotThrow(() -> pid.outputTelemetry());
    }

    /* --- Type --- */

    @Test
    void typeIsTalonFX() {
        pid = new PIDFX(null, "Type", talonFX);
        assertEquals(PIDBase.PIDType.TALONFX, pid.type);
    }

    /* --- Additional coverage --- */

    @Test
    void calcPopulatesFields() {
        pid = new PIDFX(null, "Calc", talonFX, 1.0, 0.0, 0.0);
        pid.setGoal(5.0);

        double output = pid.calc(0.0, 5.0, 0.0);
        // In sim, CL error and output are 0 since no real hardware loop
        assertEquals(0.0, output, 1e-6);
    }

    @Test
    void setGoalWithFeedForward() {
        pid = new PIDFX(null, "FF", talonFX, 1.0, 0.0, 0.0);
        assertDoesNotThrow(() -> pid.setGoal(3.0, 0.5));
    }

    @Test
    void setPSyncsToController() {
        pid = new PIDFX(null, "SyncP", talonFX, 0.0, 0.0, 0.0);
        pid.setP(2.5);

        assertEquals(2.5, pid.getP(), 1e-6);
        assertEquals(2.5, talonFX.getPSlot(0), 1e-6);
    }

    @Test
    void setISyncsToController() {
        pid = new PIDFX(null, "SyncI", talonFX, 0.0, 0.0, 0.0);
        pid.setI(0.3);

        assertEquals(0.3, pid.getI(), 1e-6);
        assertEquals(0.3, talonFX.getISlot(0), 1e-6);
    }

    @Test
    void setDSyncsToController() {
        pid = new PIDFX(null, "SyncD", talonFX, 0.0, 0.0, 0.0);
        pid.setD(0.05);

        assertEquals(0.05, pid.getD(), 1e-6);
        assertEquals(0.05, talonFX.getDSlot(0), 1e-6);
    }
}
