package com.team271.lib.hardware.sensors.switches;

import static org.junit.jupiter.api.Assertions.*;

import com.team271.lib.hardware.CANDeviceID;
import com.team271.lib.hardware.CTREManager;
import com.team271.lib.hardware.controllers.ControllerTalonFX;
import com.team271.lib.hardware.motors.MotorBase;
import com.team271.lib.hardware.sensors.switches.SwitchBase.SwitchTrigger;
import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class SwitchCANCoderTest {

    private static final MotorBase KRAKEN = new MotorBase(MotorBase.MotorType.KRAKENX60);

    @BeforeAll
    static void initHAL() {
        HAL.initialize(500, 0);
    }

    @BeforeEach
    void resetCTREManager() throws Exception {
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

    @Test
    void constructorFwdLimitNO() {
        CANDeviceID id = new CANDeviceID(86);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void constructorRevLimitNC() {
        CANDeviceID id = new CANDeviceID(87);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NC, true, 1.0, 250.0);

        assertNotNull(sw);
    }

    @Test
    void setEnabledFwd() {
        CANDeviceID id = new CANDeviceID(88);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ForwardLimitEnable);
    }

    @Test
    void setEnabledRev() {
        CANDeviceID id = new CANDeviceID(89);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setEnabled(false));
        assertFalse(controller.getConfig().HardwareLimitSwitch.ReverseLimitEnable);
    }

    @Test
    void setTriggerTypeFwdNC() {
        CANDeviceID id = new CANDeviceID(90);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.setTriggerType(SwitchTrigger.NC));
    }

    @Test
    void getTriggeredReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(91);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void getTriggeredRevReturnsFalseBeforeRobotInit() {
        CANDeviceID id = new CANDeviceID(92);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "RevSw", controller, false, SwitchTrigger.NO, false, 0.0, 250.0);

        assertFalse(sw.getTriggered());
    }

    @Test
    void outputTelemetryDoesNotThrow() {
        CANDeviceID id = new CANDeviceID(93);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(sw::outputTelemetry);
    }

    @Test
    void autoSetConfiguredByConstructor() {
        CANDeviceID id = new CANDeviceID(94);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, true, 5.0, 250.0);

        assertTrue(sw.getAutoSet());
        assertEquals(5.0, sw.getAutoSetPos(), 1e-6);
    }

    @Test
    void robotInitRegistersSignals() {
        CANDeviceID id = new CANDeviceID(95);
        ControllerTalonFX controller = new ControllerTalonFX(null, "Motor", id, KRAKEN);
        SwitchCANCoder sw =
                new SwitchCANCoder(
                        null, "FwdSw", controller, true, SwitchTrigger.NO, false, 0.0, 250.0);

        assertDoesNotThrow(() -> sw.robotInit(0.0));
    }
}
