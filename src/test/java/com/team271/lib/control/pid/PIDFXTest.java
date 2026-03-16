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
}
